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
package gart.meta

/**
 * Allows an async meta call to wait for the comm reply.
 * This is the actor implementation used by gart.meta.Meta
 */
class MetaActor {
   
    /**
     * The meta reply (if there is one)
     */
    def metaReply
    
    /**
     * Pause until the meta call has completed.
     */
    synchronized void pause() { wait() }
    
    /**
     * Notify the paused actor that the meta call has completed with no reply.
     */
    synchronized void proceed() { notify() }
    
    /**
     * Notify the paused actor that the meta call has completed with a reply.
     */
    synchronized void proceed(reply) { 
        this.metaReply = reply
        notify() 
    } 
}
