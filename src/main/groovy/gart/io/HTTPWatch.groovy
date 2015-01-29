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
package gart.io

import gart.comm.*
import gart.util.TimeTool
import gart.Gart

@Grab("org.codehaus.groovy.modules.http-builder:http-builder:0.7")
import groovyx.net.http.*

/**
 * 
 */
class HTTPWatch {

    public static final HTTP_CHANGED = "changed"

    public static final URL_BASE = "url.base"
    public static final URL_PATH = "url.path"
    public static final HTTP_STATUS = "http.status"
    public static final HTTP_HEADERS = "http.headers"
    public static final HTTP_DATA = "http.data"

    def timer
    def http
    def base
    def lastChecked = [:]

    def commId = { it.bytes.encodeBase64().toString() }

    def check = { url ->
        http.request(Method.GET) { req ->
            uri.path = url
            response.success = { resp, reader ->
                lastChecked[url] = resp.headers."Last-Modified"

                new Comm("http.${commId(this.base)}.${HTTP_CHANGED}")
                    .set(URL_BASE, this.base)
                    .set(URL_PATH, url)
                    .set(HTTP_STATUS, resp.statusLine)
                    .set(HTTP_HEADERS, resp.headers)
                    .set(HTTP_DATA, reader.text)
                    .publish()
            }
            response."304" = {
                Gart.LOG.debug "${base}${url} hasn't changed"
            }
            response.failure = {
                Gart.LOG.error "Check of ${base}${url} failed: $it.statusLine"
            }
        
            if(lastChecked[url])
                headers."If-Modified-Since" = lastChecked[url]
        }
    }

    public HTTPWatch(base, urls, interval=60){
        this.base = base
        this.http = new HTTPBuilder(this.base)
        this.timer = new TimeTool("HTTPWatch for $base")

        urls.each { url ->
            timer.interval({ 
                try{
                    Gart.LOG.info "Checking ${base}${url}"
                    check(url)
                }catch(e){
                    Gart.LOG.error "Check of ${base}${url} failed $e" 
                }
            }, interval) 
        }
    }

    public void addSubscriber(communicator){
        communicator.subscribeTo("http.${commId(this.base)}")
    }
}
