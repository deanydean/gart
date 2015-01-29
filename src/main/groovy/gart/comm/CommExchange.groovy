/*
 * Copyright 2014 Matt Dean
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
package gart.comm

import java.util.concurrent.*

import groovyx.gpars.actor.Actors

import gart.Gart
import gart.log.*

/**
 * A communication exchange mechanism
 * @author deanydean
 */
class CommExchange extends Communicator {

    public static final COMMEX = "commex"
    public static final SUBSCRIBE = "commex.subscribe"
    public static final UNSUBSCRIBE = "commex.unsubscribe"
    public static final COMMUNICATOR = "communicator"
    public static final COMM_NAME = "comm.name"
    public static final COMM_SOURCE = "comm.source"
    
    private static Log LOG = new Log(CommExchange.class);
    
    private static CommExchange instance = new CommExchange();
    
    private ConcurrentHashMap<String,List<Communicator>> register = 
        new ConcurrentHashMap<String,List<Communicator>>();
        
    def handlerCount = Gart.CONFIG.threads

    private CommExchange(){
        super({ commInfo ->
            def commex = commInfo[0]
            def comm = commInfo[1]

            def components = comm.id.tokenize(".")

            if(components[0] == COMMEX){
                switch(comm.id){
                    case SUBSCRIBE:
                        commex.subscribe(comm.get(COMM_NAME), 
                            comm.get(COMMUNICATOR))
                        break
                    case UNSUBSCRIBE:
                        commex.unsubscribe(comm.get(COMM_NAME), 
                            comm.get(COMMUNICATOR))
                        break
                    default:
                        LOG.error "Unknown comm ${comm.id} for ${commex}"
                }

                LOG.debug "Commex ${commex} handled comm ${comm}"
                return
            }

            
        
            boolean received = false
            def name = ""
        
            for(String bit in components){
                name+=bit
                try{
                    def communicators = commex.register[name]
                    if(communicators && communicators.size() > 0){
                        for(def communicator in communicators){
                            Comm toPublish = comm.copyAndConsume(name)
                            communicator << toPublish
                            received = true
                        }
                    }
                }catch(err){
                    LOG.error "Publish of $name failed : $err"
                }
                name+="."
            }
        
            if(!received){
                LOG.debug "Comm was undelivered ${comm.id}"
            }
        })
        Actors.defaultActorPGroup.resize handlerCount
        LOG.debug "New commex created: $this"
    }
    
    void subscribe(String name, Communicator communicator){
        if(instance.register[name]){
            instance.register[name] << communicator
        }else{
            instance.register[name] = [ communicator ]
        }
    }
    
    void unsubscribe(String name, Communicator communicator){
        if(instance.register[name]){
            def registered = instance.register[name]
            for(def i=0; i<registered.size(); i++){
                if(registered[i] == communicator)
                    instance.register[name].remove(communicator)
            }
        }
    }
    
    static void publish(Comm comm){
        instance.send(comm)
    }
}
