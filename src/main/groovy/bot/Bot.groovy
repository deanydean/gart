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
package bot

import bot.control.*
import bot.log.*

/**
 * The main bot instance
 * @author deanydean
 */
class Bot {
    
    // Setup the static members
    def static final ENV = System.getenv()
    def static final BOT_HOME = ENV['BOT_HOME']
    def static final CONFIG = new ConfigSlurper().parse(
        new File("$BOT_HOME/bot.conf").toURL())
    def static final LOG = new Log(Bot.class)
    def static IS_DAEMON = false
    def static BOT = null
    
    def options
    def opManager
    def op
    def args
    
    Bot(options){
        this.options = options
        this.args = new ArrayList(this.options.arguments())
          
        this.opManager = new OpManager()
        this.opManager.adopt(this)
        
        // Parse Ds
        def params = this.options.Ds
        if(params){
            for(def i=0; i<params.size(); i+=2){
                CONFIG[params[i]] = params[i+1]
            }
        }
    }
    
    String toString(){
        return "BOT: ${this.options.toString()}"
    }
    
    void run(){
        // Perform the required action
        this.args.isEmpty() ? start() : op()
    }
    
    void start(){
        IS_DAEMON = true
        LOG.info "Bot daemon started"
        new Daemon().start().join()
        LOG.debug "Daemon has ended"
    }
    
    void op(){
        try{
            // Do the op
            def result = this.opManager.perform(this.args)
            
            if(!result){
                // Do nothing
            }else if(result.success){
                LOG.info result.message
            }else{
                throw result.message
            }
        }catch(e){
            LOG.error "Oh dear! ${e.getMessage()}"
            e.printStackTrace()
        }
    }
}
