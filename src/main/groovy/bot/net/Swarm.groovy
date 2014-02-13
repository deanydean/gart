/*
 * Copyright 2013 Matt Dean
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package bot.net

import bot.Bot
import bot.comm.*
    
@Grab("com.hazelcast:hazelcast:3.1.5")
import com.hazelcast.config.*
import com.hazelcast.core.*

/**
 * An interface onto the bot swarm
 * @author deanydean
 */
class Swarm extends Communicator implements MessageListener<Comm> {

    // This Bot's default swarm
    public static final BOT = (Bot.CONFIG.swarm) ? 
        new Swarm(Bot.CONFIG.swarm) : null

    // Scopes
    public static final SCOPE_SWARM = "swarm"
    public static final SCOPE_BOT = "bot"

    // Resources
    public static final RES_HEAP = "heap"
    
    def ident
    def config
    def hzInstance

    private static ON_COMM = { 
        try{ it[0].publishComm(it[1]) }
        catch(e){ Bot.LOG.error "Swarm.. comm.. ARGH! $e" }
    }
 
    public Swarm(String name){
        super(ON_COMM)

        this.ident = name
        this.hzInstance = Hazelcast.getHazelcastInstanceByName(name)

        if(!this.hzInstance){
            this.config = new Config()
            this.config.setInstanceName(name)
            this.hzInstance = Hazelcast.newHazelcastInstance(this.config)
            this.init()
        }
    }

    public Swarm(Object swarmConfig){
        super(ON_COMM)

        this.ident = swarmConfig.id
        this.config = new Config()
        this.config.setInstanceName(this.ident)

        def network = this.config.getNetworkConfig()

        // Enable ssl if required
        if(swarmConfig.ssl && swarmConfig.ssl.enabled){
            def sslConfig = new SSLConfig()
            sslConfig.setEnabled(true)
            sslConfig.setFactoryClassName(
                swarmConfig.ssl.factoryClass)
            swarmConfig.ssl.properties.each { k, v ->
                sslConfig.setProperty(k, v)
            }
            network.setSSLConfig(sslConfig)
        }

        // Set the swarm ports
        network.setPort(swarmConfig.port)        
        network.setPortAutoIncrement(true)
        
        // Configure joining the swarm
        def join = network.getJoin()

        // Setup local multicast comms
        join.getMulticastConfig()
            .setEnabled(swarmConfig.enableMulticast)
            .setMulticastPort(swarmConfig.mcPort)
        
        // Add tcp comms for all members
        for(def member in swarmConfig.tcpmembers)
            join.getTcpIpConfig().addMember(member)

        // Configure the interfaces to use
        for(def iface in swarmConfig.interfaces)
            network.getInterfaces().addInterface(iface)
        
        join.getTcpIpConfig().setEnabled(true)
        network.getInterfaces().setEnabled(true)
            
        this.hzInstance = Hazelcast.newHazelcastInstance(this.config)
        this.init()
    }

    private init(){
        if(this.hzInstance){
            // Plumb swarm events into bot comms
            this.hzInstance.getTopic(SCOPE_SWARM).addMessageListener(this)
            this.hzInstance.getTopic("${SCOPE_SWARM}.${this.ident}")
                .addMessageListener(this)

            // Register for swarm comms
            this.subscribeTo(SCOPE_SWARM)
        }
    }
    
    public Map<String,Object> theHeap(){
        return this.hzInstance.getMap("${SCOPE_SWARM}.${RES_HEAP}")
    }
    
    public Map<String,Object> myHeap(){
        return this.hzInstance.getMap("${SCOPE_BOT}.${this.ident}.${RES_HEAP}")
    }

    public Map<String,Object> botHeap(ident){
        return this.hzInstance.getMap("${SCOPE_BOT}.${ident}.${RES_HEAP}")
    }

    public Map<String,Object> namedMap(name){
        return this.hzInstance.getMap(name)
    }

    public List<Object> namedList(name){
        return this.hzInstance.getList(name)
    }

    public publishComm(comm){
        def topic = this.hzInstance.getTopic("swarm")
        topic.publish(comm)
    }

    void onMessage(Message<Comm> message){
        // Send the comm
        message.messageObject.set("from", message.publishingMember.uuid)
            .set("time", message.publishTime)
            .publish()
    }
}
