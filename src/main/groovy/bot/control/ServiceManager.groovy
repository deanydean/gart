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

import bot.Bot

public class ServiceManager {

    private config = Bot.CONFIG.services
    private services = [:]
    private scriptEngine

    public ServiceManager(){
        def roots = []
        
        // Load services from service dirs
        def scriptsDirs = config.scriptsDirs
        for(scriptsDir in scriptsDirs)
            roots << new File(Bot.BOT_HOME+"/"+scriptsDir).toURL()
       
        Bot.LOG.debug("Loading service scripts from ${roots}")
        this.scriptEngine = new GroovyScriptEngine(roots as URL[])

        // Detect all services
        def scripts = []
        for(scriptsDir in config.scriptsDirs){
            def dir = new File(Bot.BOT_HOME+"/"+scriptsDir).list(
                [accept:{d, f-> f ==~ /.*?\.groovy*/ }] as FilenameFilter)
                    .each { f ->
                        Bot.LOG.debug("Got service $f")
                        scripts << f
                    }
        }
        
        scripts.sort().each { script ->
            try{
                // Create the class for the script
                def serviceClass = this.scriptEngine.loadScriptByName(script)

                // Create an instance
                def service = serviceClass.newInstance()
                Bot.LOG.debug "Adding service ${service.name}"
                services[service.name] = service
            }catch(ScriptException se){
                Bot.LOG.error("ScriptException for {0} : {1}", f, se)
            }catch(ResourceException re){
                Bot.LOG.error("ResourceException for {0} : {1}", f, re)
            }
        }
    }

    public Object getService(name){
        return services[name]
    }

    public void start(name){
        if(services.containsKey(name)){
            Bot.LOG.info "Starting ${name} service...."
            services[name].start()
        }else
            Bot.LOG.error "Unknown service: ${name}" 
    }

    public void stop(name){
        if(services.containsKey(name)){
            Bot.LOG.info "Stopping ${name} service...."
            services[name].stop()
        }else
            Bot.LOG.error "Unknown service: ${name}"
    }

    public void startServices(){
        Bot.LOG.info "Starting services...."
        services.each { name, service ->
            if(service.enabled){
                Bot.LOG.info "    * Starting ${name}...."
                service.start()
            }
        }
        Bot.LOG.info "Services started"
    }

    public void stopServices(){
        Bot.LOG.info "Stopping services...."
        services.each { name, service ->
            if(service.enabled){
                Bot.LOG.info "    * Stopping ${name}...."
                service.stop()
            }
        }
        Bot.LOG.info "Services stopped"
    }
}
