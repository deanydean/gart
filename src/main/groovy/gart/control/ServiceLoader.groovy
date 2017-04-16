/*
 * Copyright 2017 Matt Dean
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
    private classloader = new GroovyClassLoader()
    private services = []

    public ServiceLoader(){
        super("loader", true)
    }
    
    public void onStart(){
        // Find all service script dirs
        def servicesDirs = []
        for(scriptsDir in config.scriptsDirs){
            servicesDirs << new File("${Gart.GART_HOME}/$scriptsDir")
        }
        Gart.LOG.debug "Checking GART_PATH ${Gart.PATH} for services"
        Gart.PATH.each {
            def servicesDir = new File(it+"/services")
            Gart.LOG.debug "Services in ${it}? ${servicesDir.exists()}"
            if(servicesDir.exists()) servicesDirs << servicesDir
        }
        
        Gart.LOG.debug("Loading service scripts from ${servicesDirs}")

        // Add all scripts from each services dir
        def scripts = []
        servicesDirs.each { dir ->
            dir.list([accept:{d, f-> f ==~ /.*?\.groovy*/ }] as FilenameFilter)
                .each { f ->
                    Gart.LOG.debug("Got service $f")
                    scripts << "${dir}/$f"
                }
        }

        // Load each service script
        scripts.sort().each { script ->
            try{
                // Create the class for the script
                def serviceClass = classloader.parseClass(new File(script));

                // Make sure we don't already have this service loaded
                if(services.find { it.class == serviceClass }){
                    Gart.LOG.debug "Skipping loaded service ${serviceClass}"
                    return
                }
                
                // Create an instance
                def service = serviceClass.newInstance()
                if(service.respondsTo("init"))
                    service.init()
                else
                    Gart.LOG.debug "Uninited service ${service.name} loaded"
                    
                this.services << service
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
        Gart.LOG.error "Cannot stop service loader"
    }
}
