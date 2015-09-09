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
    def workingDir = null
    def envVars = null
    def suppressOutput = false
    
    public CommandExecutor(ident, command){
        this.ident = ident
        this.command = command
    }
    
    public setWorkingDir(String workingDir){
        this.workingDir = new File(workingDir)
    }

    public addEnvVar(key, value){
        if(!this.envVars) this.envVars = []
        this.envVars << "$key=$value"
    }

    public inheritEnv(excludes=[]){
        def env = System.getenv()
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
      
        def proc = this.command.execute(this.envVars, this.workingDir)
        Gart.LOG.logFromStream(proc.err, Log.ERROR)
        Gart.LOG.logFromStream(proc.in, Log.INFO)

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

