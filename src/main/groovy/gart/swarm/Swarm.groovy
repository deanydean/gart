/*
 * Copyright 2015 Matt Dean
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
package gart.swarm

import gart.Gart
import gart.comm.*
    
@Grab("com.hazelcast:hazelcast:3.1.4")
import com.hazelcast.config.*
import com.hazelcast.core.*

/**
 * An interface onto the gart swarm
 * @author deanydean
 */
class Swarm extends Communicator implements MessageListener<Comm> {

    // The default swarm
    //public static final GART = (Gart.CONFIG.swarm) ? 
        //  new Swarm(Gart.CONFIG.swarm) : null

    // Scopes
    public static final SCOPE_SWARM = "swarm"
    public static final SCOPE_RT = "gart"

    // Resources
    public static final RES_HEAP = "heap"

    // Config
    public static final FILE_STORE = "gart.swarm.FileMapStore"
    
    def ident
    def config

    // Hazelcast components
    def hzConfig
    def hzInstance

    private static ON_COMM = { 
        try{ it[0].publishComm(it[1]) }
        catch(e){ Gart.LOG.error "Swarm.. comm.. ARGH! $e" }
    }

    public Swarm(){
        this(Gart.CONFIG.swarm)
    }

    public Swarm(String name){
        super(ON_COMM)

        this.ident = name
        this.hzInstance = Hazelcast.getHazelcastInstanceByName(name)

        if(!this.hzInstance){
            this.hzConfig = new Config()
            this.hzConfig.setInstanceName(name)
            this.hzInstance = Hazelcast.newHazelcastInstance(this.hzConfig)
            this.init()
        }
    }

    public Swarm(Object swarmConfig){
        super(ON_COMM)

        this.ident = (swarmConfig.id) ? swarmConfig.id : Gart.CONFIG.id
        this.config = swarmConfig
        this.hzConfig = new Config()
        this.hzConfig.setInstanceName(this.ident)

        // Load all the named stores
        def mapConfigs = [:]
        this.config.stores.each { k, v ->
            mapConfigs[k] = new MapConfig(k)
            def storeConfig = new MapStoreConfig()
            storeConfig.setEnabled(v.enabled)
                .setImplementation(loadMapStore(v.mapStore, k))
                .setWriteDelaySeconds(v.writeDelay)
            mapConfigs[k].setMapStoreConfig(storeConfig)
        }
        this.hzConfig.setMapConfigs(mapConfigs)

        def network = this.hzConfig.getNetworkConfig()

        // Enable ssl if required
        if(this.config.ssl && this.config.ssl.enabled){
            def sslConfig = new SSLConfig()
            sslConfig.setEnabled(true)
            sslConfig.setFactoryClassName(
                swarmConfig.ssl.factoryClass)
            swarmConfig.ssl.properties.each { k, v ->
                sslConfig.setProperty(k, v)
            }
            network.setSSLConfig(sslConfig)
        }

        if(this.config.port){
            // Set the swarm ports
            network.setPort(this.config.port)        
            network.setPortAutoIncrement(true)
        }

        // Configure joining the swarm
        def join = network.getJoin()

        if(this.config.enableMulticase){
            // Setup local multicast comms
            join.getMulticastConfig()
                .setEnabled(this.config.enableMulticast)
                .setMulticastPort(this.config.mcPort)
        }

        if(this.config.tcpMembers){
            // Add tcp comms for all members
            for(def member in this.config.tcpMembers)
                join.getTcpIpConfig().addMember(member)
            
            join.getTcpIpConfig().setEnabled(true)
        }

        if(this.config.interfaces){
            // Configure the interfaces to use
            for(def iface in this.config.interfaces)
                network.getInterfaces().addInterface(iface)
        
            network.getInterfaces().setEnabled(true)
        }

        this.hzInstance = Hazelcast.newHazelcastInstance(this.hzConfig)
        this.init()
    }

    private init(){
        if(this.hzInstance){
            // Plumb swarm events into gart comms
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
        return this.hzInstance.getMap("${SCOPE_RT}.${this.ident}.${RES_HEAP}")
    }

    public Map<String,Object> rtHeap(ident){
        return this.hzInstance.getMap("${SCOPE_RT}.${ident}.${RES_HEAP}")
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

    private loadMapStore(impl, name){
        return this.class.classLoader.loadClass(
            "gart.swarm.${impl}MapStore", true, false)?.newInstance(name)
    }
}
