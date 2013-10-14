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

import bot.comm.*
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

    def options
    def opManager
    def daemon
    def op
    def args
    def botsh
    
    Bot(options){
        this.options = options
        this.args = new ArrayList(this.options.arguments())
          
        this.opManager = new OpManager()
        this.opManager.adopt(this).start()
        
        // Parse Ds
        def params = this.options.Ds
        if(params){
            for(def i=0; i<params.size(); i+=2){
                CONFIG[params[i]] = params[i+1]
            }
        }

        // Check if we're running in daemon mode.....
        if(this.options.daemon){
            this.daemon = new Daemon()
        }

        // Subscribe to bot comms
        new Communicator({ commData ->
            def comm = commData[1]
            LOG.info("Bot received comm ${comm}")

            if(comm.id == "restart"){
                this.restart()
            }
        }).subscribeTo("bot");
    }
    
    String toString(){
        return "BOT: ${this.options.toString()}"
    }
    
    void run(){
        if(this.args.isEmpty())
            this.daemon ? start() : shell()
        else
            op()
    }
    
    void start(){
        IS_DAEMON = true
        LOG.info "Bot daemon started"
        this.daemon.start().join()
        LOG.debug "Daemon has ended"
    }

    void restart(){
        // Create restart file.....
        new File("${BOT_HOME}/.restart").createNewFile()
        
        if(IS_DAEMON){
            this.daemon.stop()
        }else{
            System.exit(0)
        }
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

    private void shell(){
        // Start the bot shell
        this.botsh = new Botsh([
            "BOT": this
        ])

        LOG.info "Hello, Bot at your service. How can I help?"
        this.botsh.run()
    }
}
