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

import groovyx.gpars.actor.DefaultActor
import bot.Bot
import bot.log.Log

/**
 * A communicator.
 * Does something when a communication is received.
 * @author deanydean
 */
class Communicator extends DefaultActor {
    
    def Log LOG = new Log(Communicator.class)
    
    def onCommAction
    
    public Communicator(onComm){
        this.onCommAction = onComm
        
        // Start the actor
        this.start()
    }
    
    public void subscribeTo(String id){
        new Comm(CommExchange.SUBSCRIBE)
            .set(CommExchange.COMM_NAME, id)
            .set(CommExchange.COMMUNICATOR, this)
            .publish()
    }
    
    public void unsubscribeFrom(String id){
        new Comm(CommExchange.UNSUBSCRIBE)
            .set(CommExchange.COMM_NAME, id)
            .set(CommExchange.COMMUNICATOR, this)
            .publish()
    }
    
    protected final void onComm(Comm comm){
        this.onCommAction([ this, comm ])
    }
    
    void act(){
        loop {
            react { Comm comm ->
                try{
                    this.onComm(comm)
                }catch(err){
                    LOG.info("Failed to handle comm ${comm} : ${err}")
                }
            }
        }
    }
}
