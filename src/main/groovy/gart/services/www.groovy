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
package gart.services

import gart.Gart
import gart.control.Service

@Grab("com.sparkjava:spark-core:2.0.0")
import spark.Spark

/**
 * A www server service.
 */
class WWWService extends Service {
    
    def config = Gart.CONFIG.www
    def http

    public WWWService(){
        super("www", true, 9)

        if(!this.config.docPath){
            LOG.error "No www.docPath config for www service"
            return
        }
    }


    @Override
    public void onStart(){
        Spark.externalStaticFileLocation(this.config.docPath)
        Spark.setPort(this.config.port)

        Spark.get("/ping", { req, res ->
            return "pong"
        })
    }
    
    @Override
    public void onStop(){
        Spark.stop()
    }
}
