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
package bot.services

import bot.Bot
import bot.control.Service

@Grab("org.glassfish.grizzly:grizzly-http-server:2.3.7")
@Grab("org.glassfish.grizzly:grizzly-http-server-jaxws:2.3.7")
import org.glassfish.grizzly.http.server.*
import org.glassfish.grizzly.jaxws.JaxwsHandler
    
/**
 * A www server service.
 */
class WWWService extends Service {
    
    def config = Bot.CONFIG.www
    def http
    
    public WWWService(){
        super("www", true, 9)

        if(!this.config.docPath){
            LOG.error "No www.docPath config for www service"
            return
        }

        this.http = new HttpServer();
        def networkListener = new NetworkListener("bot-www",
                "0.0.0.0", this.config.port);

        // Add static http handler
        def docPath = "${Bot.BOT_HOME}/${this.config.docPath}"
        def staticHandler = new StaticHttpHandler(docPath)
        this.http.getServerConfiguration().addHttpHandler(staticHandler, "/")
//        this.http = HttpServer.createSimpleServer(docPath, this.config.port)
        
        // Add the datastore handler
        def datastore = new DatastoreService()
        def store = new JaxwsHandler(datastore)
        this.http.getServerConfiguration().addHttpHandler(store, "/store")
    }
    
    @Override
    public void onStart(){
        if(this.http)
            this.http.start()
        else
            LOG.error "Can't start www service as there's no http server"
    }
    
    @Override
    public void onStop(){
        if(this.http)
            this.http.stop()
        else
            LOG.error "Can't stop www service as there's no http server"
    }
}
