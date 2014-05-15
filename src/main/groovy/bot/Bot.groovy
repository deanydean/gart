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
class Bot {
    
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
        // Start the bot shell
        def botsh = new Botsh([
            "BOT": this,
            "LOG": this.LOG
        ])

        LOG.info "What?"
        botsh.run()
    }
   
    Bot(options){
        this.options = options
        this.args = new ArrayList(this.options.arguments())
          
        // Parse Ds
        def params = this.options.Ds
        if(params){
            for(def i=0; i<params.size(); i+=2){
                CONFIG[params[i]] = params[i+1]
            }
        }

        // Subscribe to bot comms
        new Communicator({ commData ->
            def comm = commData[1]
            LOG.info("Bot received comm ${comm}")

            if(comm.id == "restart"){
                this.restart()
            }else(comm.id == "stop"){
                this.stop()
            }
        }).subscribeTo("bot");

        // Init service loader and op loader
        new ServiceLoader()
        new OpLoader()

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
    }
    
    String toString(){
        return "BOT: ${this.options.toString()}"
    }

    void run(){
        if(this.options.daemon){
            new Comm("srv.loader.start").publish(shell)
        }else if(!this.args.isEmpty()){
            new Comm("op").set("args", this.args).publish(shell)
        }else{
            shell()
        }
    }
    
    void restart(){
        // Create restart file.....
        new File("${BOT_HOME}/.restart").createNewFile()
        stop()
    }

    void stop(){       
        // Stop all running services
        new Comm("srv.loader.stop").publish({
            LOG.info "Goodbye!"
            System.exit(0)
        })
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
            config.putAll(new ConfigSlurper().parse(cfgFile.toURL()))

        return config
    }
}
