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

import com.hazelcast.config.Config
import com.hazelcast.core.Hazelcast

/**
 * An interface onto the bot swarm
 * @author deanydean
 */
class Swarm {

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
   
    public Swarm(String name){
        this.ident = name
        this.hzInstance = Hazelcast.getHazelcastInstanceByName(name)

        if(!this.hzInstance){
            this.config = new Config()
            this.config.setInstanceName(name)
            this.hzInstance = Hazelcast.newHazelcastInstance(this.config)
        }
    }

    public Swarm(Object swarmConfig){
        this.ident = swarmConfig.id
        this.config = new Config()
        this.config.setInstanceName(this.ident)

        def network = this.config.getNetworkConfig()

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
}
