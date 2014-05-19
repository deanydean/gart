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
package bot.control

import groovy.lang.Binding
import groovy.util.GroovyScriptEngine
import groovyx.gpars.dataflow.DataflowQueue
import static groovyx.gpars.dataflow.Dataflow.task

import java.util.concurrent.*

import bot.Bot
import bot.comm.*
import bot.log.Log

/**
 * Loads and runs operations for the bot
 * @author deanydean
 */
class OpRunner extends Service {
    private static final Log LOG = new Log(OpRunner.class)
    
    private config = Bot.CONFIG.ops
    private opComm
    private scriptEngine
    private bot
    private running = false
    private executor = Executors.newFixedThreadPool(10, { r ->
        def t = new Thread(r, "oprunner-executor")
        t.setDaemon(true)
        return t
    } as ThreadFactory)

    public OpRunner(){
        super("ops", true)
        
        // Create the roots....
        def roots = []
       
        // Add scriptsdirs from config
        def scriptDirs = config.scriptsDirs
        for(scriptDir in scriptDirs)
            roots << new File(Bot.BOT_HOME+"/"+scriptDir).toURL()
            
        // Load the script engine
        this.scriptEngine = new GroovyScriptEngine(roots as URL[]);
    }

    public adopt(bot){
        this.bot = bot
        return this
    }
    
    public void onStart(){
        if(this.running) return

        if(!this.opComm){
            // Create the op subscriber
            this.opComm = new Communicator({ commData ->
                // Get the op info
                def service = commData[0]
                def comm = commData[1]
                def opComm = new Comm(comm.id, comm)
                comm.reply = null
               
                this.submitOp(opComm)
            })
        }

        this.opComm.subscribeTo("op")
    }

    public submitOp(comm){
        // Submit the ops
        this.executor.submit({ 
            LOG.debug("Received op comm ${comm}")
 
            // Get the args for script
            def args = comm.get("args")
            if(!args){
                if(comm.id.count(".") > 0){
                    // Split the comm name by "."
                    args = comm.id.tokenize(".")
                }else{
                    args = [comm.id]
                }
            }
            else if(args instanceof String) args = [ args ]
        
            LOG.debug("Im going to ${args}")
            def result = perform(args)
            comm.set("result", result)

            this.executor.submit(comm.reply as Runnable)
        } as Runnable)
    }
    
    public void onStop(){
        if(!this.running) return
        this.opComm.unsubscribeFrom("op")
        this.running = false
        LOG.debug "OpManager not listening for op comms"
    }
    
    public perform(args){
        // Split multiple ops
        def ops = args.join(" ").split("and")
        def results = []
        def opsThreads = []
        ops.each { op -> 
            opsThreads << Thread.start {
                LOG.debug "Performing op $op"
                results << performOp(op.trim().split(" ") as List)
            }
        }

        // Wait for all threads to finish
        opsThreads.each { it.join() }
        return (results.size() == 1) ? results[0] : null
    }

    private performOp(args){    
        // Work out if we have a script for the command
        def scriptName = null
        def op = []
        args.find { arg ->
            op << arg
            def name = op.join("/")+".groovy"
           
            LOG.debug "Loading script $name"
            try{
                this.scriptEngine.loadScriptByName(name)
                scriptName = name
                return true
            }catch(ScriptException se){
                LOG.error("ScriptException for {0} : {1}", name, se)
            }catch(ResourceException re){
                LOG.debug("ResourceException for {0} : {1}", name, re)
            }
            
            return false
        }

        if(scriptName){
            return runOp(scriptName, args-op)
        }else{
            LOG.error("I dont know how to {0}", args.join(" "))
            return null
        }
    }
        
    public runOp(name, args){
        LOG.debug "Running script $name $args"

        // Run the op script
        def binding = new Binding()
        binding.setVariable("args", args)
        binding.setVariable("BOT", this.bot)
        binding.setVariable("LOG", this.bot.LOG)
        binding.setVariable("CONFIG", this.bot.CONFIG)
        this.scriptEngine.run(name, binding)
        
        if(binding.hasVariable("result"))
            return binding.getVariable("result")
            
        return null
    }
}

