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

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;

import bot.Bot
import bot.comm.Communicator
import bot.log.Log

/**
 * Runs operations for the bot
 * @author deanydean
 */
class OpManager {
    def static final Log LOG = new Log(OpManager.class)
    
    private config = Bot.CONFIG.ops
    private opComm
    private scriptEngine
    private bot
    
    public OpManager(){
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
    }
    
    public start(){
        // Create the op subscriber
        this.opComm = new Communicator({ commData ->
            // Get the op info
            def comm = commData[1]
            LOG.info("Received op comm ${comm}")
                
            // Get the args for script
            def args = comm.get("arg")
            if(!args) args = []
            else if(args instanceof String) args = [ args ]
                
            perform(args)
            
            // TODO: Async return result
        })
        this.opComm.subscribeTo("op")
        return this
    }
    
    public stopService(){
        this.opComm.unsubscribeFrom("op")
    }
    
    public perform(args){
        // Work out if we have a script for the command
        def scriptName = null
        def op = []
        args.find { arg ->
            op << arg
            def name = op.join("/")+".groovy"
            
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
            LOG.error("I dont know {0}", args)
            return null
        }
    }
        
    public runOp(name, args){
        // Run the op script
        def binding = new Binding()
        binding.setVariable("args", args)
        binding.setVariable("BOT", this.bot)
        this.scriptEngine.run(name, binding)
        
        if(binding.hasVariable("result"))
            return binding.getVariable("result")
            
        return null
    }
}

