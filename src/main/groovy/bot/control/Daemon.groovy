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
package bot.control

import bot.Bot

/**
 * The main bot daemon
 * @author deanydean
 */
class Daemon {
    
    private running = false
    private daemon = null
    
    def serviceManager = new ServiceManager()

    public Daemon(){
        this.serviceManager.level = 9
    }
    
    public Daemon start(){
        this.daemon = Thread.start("bot-daemon", {
            this.running = true
            this.serviceManager.startServices()
            
            while(this.running){
                try{
                    synchronized(this.daemon){
                        this.daemon.wait()
                    }
                }catch(err){
                    Bot.LOG.error "Daemon was interrupted : $err"
                    this.daemon.interrupted()
                }
            }
            
            this.serviceManager.stopServices()
                
        })
    
        return this
    }
    
    public void join(){
        this.daemon.join()
    }
    
    public void stop(){
        this.running = false;
        this.daemon.interrupt()
    }
}
