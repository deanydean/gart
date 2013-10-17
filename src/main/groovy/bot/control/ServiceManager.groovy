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
    private level = 0
    private services = [:]
    private serviceIndex = [:]
    private autoStart = false
    private scriptEngine

    public ServiceManager(){
        def roots = []

        if(config.defaultLevel) this.level = config.defaultLevel
        
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
                def serviceClass = this.scriptEngine.loadScriptByName(script);

                // Create an instance
                def service = serviceClass.newInstance()
                if(service.respondsTo("init"))
                    service.init()

                if(service.hasProperty("name")){
                    Bot.LOG.debug "Adding service ${service.name}"

                    if(service.hasProperty("level")){
                        if(!services[service.level])
                            services[service.level] = [:]

                        services[service.level][service.name] = service
                    }else{
                        if(!services[9]) services[9] = [:]
                        services[9][service.name] = service
                    }

                    // Add it to the service index
                    serviceIndex[service.name] = service
                }else{
                    Bot.LOG.error "Cannot load $service as it has no name"
                }
            }catch(ScriptException se){
                Bot.LOG.error("ScriptException for {0} : {1}", f, se);
            }catch(ResourceException re){
                Bot.LOG.error("ResourceException for {0} : {1}", f, re);
            }catch(e){
                Bot.LOG.error("Failed to load script {0} : {1}", script, e);
            }
        }
    }

    public void setLevel(newLevel){
        if(newLevel != level){
            Bot.LOG.info "Changing service level from $level to $newLevel"
            if(newLevel < level){
                // Stop all services of a higher runlevel
                (level..newLevel).each { l ->
                    if(services[l])
                        services[l].each { k,v -> stop(k, l) }
                }
            }

            if(newLevel > level && autoStart){
                // Make sure all services of new level and lower are running
                (level..newLevel).each { l ->
                    if(services[l])
                        services[l].each { k,v -> start(k, l) }
                }
            }

            // Switch runlevel
            level = newLevel
        }
    }

    public void start(name, serviceLevel=null){
        def l = (serviceLevel) ? serviceLevel : level
        if(services[l] && services[l].containsKey(name)){
            try{
                Bot.LOG.info "    * Starting ${name} service...."
                services[l][name].startService()
            }catch(e){
                Bot.LOG.error("Could not start service {0} : {1}", name, e)
            }
        }else
            Bot.LOG.error "Unknown level ${l} service: ${name}" 
    }

    public void stop(name, serviceLevel=null){
        def l = (serviceLevel) ? serviceLevel : level
        if(services[l] && services[l].containsKey(name)){
            try{
                Bot.LOG.info "    * Stopping ${name} service...."
                services[l][name].stopService()
            }catch(e){
                Bot.LOG.error "Could not stop service ${name} : ${e}"
            }
        }else
            Bot.LOG.error "Unknown level ${l} service: ${name}"
    }

    public void startServices(){
        (0..this.level).each { level ->
            Bot.LOG.info "Starting level $level services...."
            services[level].each { name, service ->
                if(service.enabled) start(name, level)
            }
        }
        Bot.LOG.info "Level 0->$level services started"
        autoStart = true
    }

    public void stopServices(){
        (this.level..0).each { level ->
            Bot.LOG.info "Stopping level $level services...."
            services[level].each { name, service ->
                if(service.enabled) stop(name, level)
            }
        }
        Bot.LOG.info "Services stopped"
        autoStart = false
    }

    public get(name){
        return serviceIndex[name]
    }
}
