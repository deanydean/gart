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
package gart

import gart.comm.*
import gart.control.*
import gart.control.sh.Garsh
import gart.log.*

/**
 * The main gart instance
 * @author deanydean
 */
class Gart extends Communicator {
    
    // Setup the static members
    def static final ENV = System.getenv()
    def static final GART_HOME = ENV['GART_HOME']
    def static final CONFIG = getConfigFile()
    def static final LOG = new Log(Gart.class)

    // Static state that will change
    def static STORE = [:]

    def options
    def args
    def garsh

    def shell = {
        if(!this.garsh){
            comm("op.greet", {
                LOG.debug "Starting garsh"
                try{
                    this.garsh = new Garsh([
                        "GART": this,
                        "LOG": this.LOG
                    ])
                    LOG.debug "Running garsh $garsh"
                    this.garsh.run()
                }catch(Throwable t){
                    LOG.debug "Failed to start garsh $t"
                    t.printStackTrace()
                }
                comm("gart.stop")
            })
        }
    }
  
    def static handleComm = { commData -> 
        def gart = commData[0]
        def comm = commData[1]
        LOG.info("I've been told to ${comm.id}")

        try{
            if(comm.id == "restart")
                gart.restart()
            else(comm.id == "stop")
                gart.stopNow()
        }catch(Throwable t){
            LOG.error "Failed to ${comm.id} due to $t"
        }
    }

    Gart(options){
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

        // Subscribe to gart comms
        this.subscribeTo("gart")
    }
    
    String toString(){
        return "GART"
    }

    void run(){
        LOG.debug "Running gart daemon?${this.options.daemon} op=${this.args}"

        if(!this.args.isEmpty())
            comm("op", { this.terminate() }, this.args)
        else {
            comm("srv.loader.start", { 
                if(!this.options.daemon)
                    shell()
                else
                    LOG.info "Gart daemon started" 
            })
        }

        this.join()
    }
    
    void restart(){
        // Create restart file.....
        new File("${GART_HOME}/.restart").createNewFile()
        comm("gart.stop")
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

        // Load all config files from gart.conf.d
        def configDir = new File("$GART_HOME/etc/gart.conf.d")
        configDir.listFiles([accept:{ f -> f ==~ /.*?\.conf/ }] as FileFilter)
            .toList().each {
                config.putAll(new ConfigSlurper().parse(it.toURL()))
            }
        

        // Load the master config
        def cfgFile = new File("$GART_HOME/etc/gart.conf")
        if(cfgFile.exists())
            config.merge(new ConfigSlurper().parse(cfgFile.toURL()))

        return config
    }
}
