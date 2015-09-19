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
package gart.meta

import gart.Gart
import gart.comm.*

/**
 * A Meta provides asynchronous access to a backend type. 
 * This is an abstraction layer that is built on top of the commex mechanism. 
 * It should be used to disconnect calls or access from an actual instance 
 * or provider.
 */
public class Meta {

    def metaId

    public Meta(ident){
        this.metaId = ident
    }

    def methodMissing(String name, args){
        def actor = new MetaActor()

        // Invoke
        new Comm("meta.${metaId}.invoke.${name}")
            .set("args", args)
            .publish({ actor.proceed(it) })
        actor.pause()

        return actor.metaReply.result
    }

    def propertyMissing(String name){
        def actor = new MetaActor()

        // Get the info
        new Comm("meta.${metaId}.get.${name}")
            .publish({ actor.proceed(it) })
        actor.pause()

        return actor.metaReply.result
    }

    def void propertyMissing(String name, value){
        def actor = new MetaActor()

        // Set the info
        new Comm("meta.${metaId}.set.${name}")
            .set("value", value)
            .publish({ actor.proceed() })
        actor.pause()
    }
    
    static Meta asMeta(obj){
        def id = "${obj.hashCode()}"
        
        // Create the meta proxy
        new MetaProxy(obj).subscribeTo("meta.$id")
        
        // Return a new meta
        return new Meta("$id")
    }
}
