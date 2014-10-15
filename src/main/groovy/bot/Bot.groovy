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
package bot

import bot.comm.*
import bot.control.*
import bot.control.sh.Botsh
import bot.log.*

/**
 * The main bot instance
 * @author deanydean
 */
class Bot extends Communicator {
    
    // Setup the static members
    def static final ENV = System.getenv()
    def static final BOT_HOME = ENV['BOT_HOME']
    def static final CONFIG = getConfigFile()
    def static final LOG = new Log(Bot.class)

    // Static state that will change
    def static STORE = [:]

    def options
    def args
    def botsh

    def shell = {
        if(!this.botsh){
            comm("op.greet", {
                LOG.debug "Starting botsh"
                try{
                    this.botsh = new Botsh([
                        "BOT": this,
                        "LOG": this.LOG
                    ])
                    LOG.debug "Running botsh $botsh"
                    this.botsh.run()
                }catch(Throwable t){
                    LOG.debug "Failed to start botsh $t"
                    t.printStackTrace()
                }
                comm("bot.stop")
            })
        }
    }
  
    def static handleComm = { commData -> 
        def bot = commData[0]
        def comm = commData[1]
        LOG.info("I've been told to ${comm.id}")

        try{
            if(comm.id == "restart")
                bot.restart()
            else(comm.id == "stop")
                bot.stopNow()
        }catch(Throwable t){
            LOG.error "Failed to ${comm.id} due to $t"
        }
    }

    Bot(options){
        super(handleComm)

        this.options = options
        this.args = new ArrayList(this.options.arguments())
          
        // Parse Ds
        def params = this.options.Ds
        if(params){
            for(def i=0; i<params.size(); i+=2)
                CONFIG[params[i]] = params[i+1]
        }
        
        // Init service loader
        new ServiceLoader()

        // Init the oploader
        new OpRunner().adopt(this).onStart()

        // Set system properties
        if(CONFIG.net.proxy.host){
            System.properties << [ 
                "http.proxyHost": CONFIG.net.proxy.host,
                "http.proxyPort": CONFIG.net.proxy.port as String,
                "http.nonProxyHosts": CONFIG.net.proxy.nonProxyHosts
            ]
        }
        System.properties << [ 
            "groovy.grape.report.downloads": "true"
        ]

        // Subscribe to bot comms
        this.subscribeTo("bot")
    }
    
    String toString(){
        return "BOT"
    }

    void run(){
        LOG.debug "Running bot daemon?${this.options.daemon} op=${this.args}"

        if(this.options.daemon)
            comm("srv.loader.start", { LOG.info "Bot daemon started" })
        else if(!this.args.isEmpty())
            comm("op", { this.terminate() }, this.args)
        else            
            shell()

        this.join()
    }
    
    void restart(){
        // Create restart file.....
        new File("${BOT_HOME}/.restart").createNewFile()
        comm("bot.stop")
    }

    def stopNow(){ 
        // Stop all running services
        comm("srv.loader.stop", {
            // Now stop all other things that need to be stopped
            comm("stop", {
                LOG.info "Goodbye!"
                this.terminate() 
            })
        })
    }

    public void on(id, task){
        new Communicator({ task(it[1].get("args")) }).subscribeTo(id)
    }

    public void comm(String id, complete=null, args=null, params=[:]){
        new Comm(id).set("args", args).setAll(params).publish(complete)
    }

    private static getConfigFile(){
        def config = new ConfigObject()

        // Load all config files from bot.conf.d
        def configDir = new File("$BOT_HOME/etc/bot.conf.d")
        configDir.listFiles([accept:{ f -> f ==~ /.*?\.conf/ }] as FileFilter)
            .toList().each {
                config.putAll(new ConfigSlurper().parse(it.toURL()))
            }
        

        // Load the master config
        def cfgFile = new File("$BOT_HOME/etc/bot.conf")
        if(cfgFile.exists())
            config.merge(new ConfigSlurper().parse(cfgFile.toURL()))

        return config
    }
}
