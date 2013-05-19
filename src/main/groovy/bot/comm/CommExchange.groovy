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
package bot.comm

import groovyx.gpars.actor.*
import java.util.concurrent.*

import bot.Bot
import bot.log.*

/**
 * A communication exchange mechanism
 * @author deanydean
 */
class CommExchange {
    
    private static Log LOG = new Log(CommExchange.class);
    
    private static CommExchange instance = new CommExchange();
    
    private ConcurrentHashMap<String,List<Communicator>> register = 
        new ConcurrentHashMap<String,List<Communicator>>();
        
    def handlerCount = Bot.CONFIG.core.commThreads
        
    private CommExchange(){
        Actors.defaultActorPGroup.resize handlerCount
    }
        
    static void subscribe(String name, Communicator communicator){
        if(instance.register[name]){
            instance.register[name] << communicator
        }else{
            instance.register[name] = [ communicator ]
        }
    }
    
    static void unsubscribe(String name, Communicator communicator){
        if(instance.register[name]){
            def registered = instance.register[name]
            for(def i=0; i<registered.size(); i++){
                if(registered[i] == communicator)
                    instance.register[name].remove(communicator)
            }
        }
    }
    
    static void publish(Comm comm){
        def components = comm.id.tokenize(".")
        
        boolean received = false
        def name = ""
        
        for(String bit in components){
            name+=bit
            try{
                def communicators = instance.register[name]
                if(communicators && communicators.size() > 0){
                    for(def communicator in communicators){
                        Comm toPublish = comm.copyAndConsume(name)
                        communicator.send(toPublish)
                        received = true
                    }
                }
            }catch(err){
                LOG.error "Publish of $name failed : $err"
            }
            name+="."
        }
        
        if(!received){
            LOG.debug "Comm unreceived ${comm.id}"
        }
    }   
}
