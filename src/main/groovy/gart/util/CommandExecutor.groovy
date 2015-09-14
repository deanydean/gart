/*
 * Copyright 2015 Matt Dean
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
package gart.util

import gart.Gart
import gart.log.Log

/**
 * Run commands
 * @author deanydean
 */
class CommandExecutor {
    
    def ident
    def command
    def suppressOutput = false
    def processBuilder
    
    public CommandExecutor(ident, command){
        this.ident = ident
        this.command = command
        this.processBuilder = new ProcessBuilder()
    }
    
    public setWorkingDir(String workingDir){
        this.processBuilder.directory(new File(workingDir))
    }

    public addEnvVar(key, value){
        this.processBuilder.environment().put(key, value)
    }

    public inheritEnv(excludes=[]){
        def env = System.getenv()
        this.processBuilder.environment().clear();
        env.each { k, v -> 
            if(!excludes.contains(k))
                this.addEnvVar(k, v)
        }
    }

    public leftShift(commandArgs){
        this.command << commandArgs
    }

    public exec(){
        def start = System.currentTimeMillis()
      
        this.processBuilder.command(this.command as String[]);
        def proc = this.processBuilder.start();
        Gart.LOG.logFromStream(proc.err, Log.ERROR, false)
        Gart.LOG.logFromStream(proc.in, Log.INFO, false)

        proc.waitFor()
        def took = System.currentTimeMillis()-start

        def result = proc.exitValue()
        if(!this.suppressOutput){
            if(result == 0){
                def timeInfo = "${new Date()} (took ${took}ms)"
                Gart.LOG.info "${this.ident} completed @ $timeInfo"
            }else{
                Gart.LOG.error "FAILED ${this.ident}!"
                Gart.LOG.error "return code: ${proc.exitValue()}"
            }
        }
        return result
    }
}
