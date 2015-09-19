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

import gart.comm.Communicator
import gart.log.Log

/**
 * A proxy communicator that provides access to an object via the commex
 * mechanism.
 */
class MetaProxy extends Communicator {

    static final Log LOG = new Log(MetaProxy.class)

    def obj

    def invoke = { name, comm ->
        LOG.debug "[META] Invoking $name(${comm.get("args")} on $obj"       
        return obj."$name"(*comm.get("args")) 
    }

    def get = { name, comm ->
        LOG.debug "[META] Getting $name from $obj"
        return obj."$name"
    }

    def set = { name, comm ->
        LOG.debug "[META] Setting $name of $obj to ${comm.get("value")}"
        obj."$name" = comm.get("value")
    }

    MetaProxy(obj){
        super({ service, comm ->
            def (proc, name) = comm.id.tokenize(".")
            
            try{
                return service["$proc"](name, comm)
            }catch(t){
                LOG.error "[META] Failed $proc on $obj for $name : $t"
                return null
            }
        })

        this.obj = obj
    }
}
