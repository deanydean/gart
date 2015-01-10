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
package gart.control

import gart.Gart
import gart.comm.Communicator

public class ServiceLoader extends Service {

    private config = Gart.CONFIG.services

    public ServiceLoader(){
        super("loader", true)
        
        // Load services from service dirs
        def scriptsDirs = config.scriptsDirs
        Gart.LOG.debug("Loading service scripts from ${scriptsDirs}")
    }
    
    public void onStart(){
        // Detect all services
        def scripts = []
        for(scriptsDir in config.scriptsDirs){
            def dir = new File("${Gart.GART_HOME}/$scriptsDir").list(
                [accept:{d, f-> f ==~ /.*?\.groovy*/ }] as FilenameFilter)
                    .each { f ->
                        Gart.LOG.debug("Got service $f")
                        scripts << "${Gart.GART_HOME}/${scriptsDir}/$f"
                    }
        }

        def classLoader = new GroovyClassLoader()
        scripts.sort().each { script ->
            try{
                // Create the class for the script
                def serviceClass = classLoader.parseClass(new File(script));

                // Create an instance
                def service = serviceClass.newInstance()
                if(service.respondsTo("init"))
                    service.init()
                else
                    Gart.LOG.debug "Uninited service ${service.name} loaded"
            }catch(ScriptException se){
                Gart.LOG.error("ScriptException for {0} : {1}", f, se)
            }catch(ResourceException re){
                Gart.LOG.error("ResourceException for {0} : {1}", f, re)
            }catch(e){
                Gart.LOG.error("Failed to load script {0} : {1}", script, e)
            }catch(Error e){
                Gart.LOG.error("Error loading script {0} : {1}", script, e)
            }
        }
    }

    public void onStop(){
    }
}
