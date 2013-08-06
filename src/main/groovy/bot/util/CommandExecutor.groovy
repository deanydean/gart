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
package bot.util

import bot.Bot
import bot.log.Log

/**
 * Run commands
 * @author deanydean
 */
class CommandExecutor {
    
    def ident
    def command
    def workingDir
    def suppressOutput = false
    
    public CommandExecutor(ident, command){
        this.ident = ident
        this.command = command
    }
    
    public setWorkingDir(String workingDir){
        this.workingDir = workingDir
    }

    public exec(){
        def start = System.currentTimeMillis()
       
        def proc = (this.workingDir) ? 
            this.command.execute(null, new File(this.workingDir)) :
            this.command.execute()

        Bot.LOG.logFromStream(proc.err, Log.ERROR)
        Bot.LOG.logFromStream(proc.in, Log.DEBUG)

        proc.waitFor()
        def took = System.currentTimeMillis()-start

        def result = proc.exitValue()
        if(!this.suppressOutput){
            if(result == 0){
                def timeInfo = "${new Date()} (took ${took}ms)"
                Bot.LOG.info "${this.ident} completed @ $timeInfo"
            }else{
                Bot.LOG.error "FAILED ${this.ident}!"
                Bot.LOG.error "return code: ${proc.exitValue()}"
            }
        }
        return result
    }
    
    
    
}

