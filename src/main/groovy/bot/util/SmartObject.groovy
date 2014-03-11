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
package bot.util

import bot.comm.*

import groovy.transform.AutoClone

/**
 * An object that can be manipulated using the commex.
 */
@AutoClone
class SmartObject extends Communicator {
   
    public static final ON_ARGS = "on.arguments"
    public static final ON_COMPLETE = "on.complete"

    def prefix
    def ident
    def properties = [:]

    public SmartObject(id, commPrefix="obj"){
        super({ data ->
            data[0].on(data[1])
        })
        prefix = commPrefix
        ident = id
        subscribeTo("${commPrefix}.${id}")
    }

    public final void on(comm){
        LOG.debug "$this $comm"

        // Work out what to call and what to pass in as an arg
        def names = comm.id.tokenize(".")
        def args = comm.get(ON_ARGS)
        if(names.size() > 1) args << names

        // Call the method
        def res
        try{
            res = this."${names[0]}"(*args)
        }catch(e){
            LOG.error "Failed to call $callName on $name : $e"
            res = e
        }

        // Call an oncomplete callback if there is one
        if(comm.get(ON_COMPLETE))
            comm.get(ON_COMPLETE)(res, this)
    }

    public final void ado(op, args=[], onComplete=null){
        def doComm = new Comm("${this.name}.$op").set(ON_ARGS, args)
        if(onComplete) doComm.set(ON_COMPLETE, onComplete)
        doComm.publish()
    }

    public getName(){
        return "${prefix}.${ident}"
    }

    def read(propertyNames){
        def res = [:]
        if(!propertyNames)
            res << properties
        else
            propertyNames.each { res << [ "$it": properties[it] ] }
        return res
    }

    def update(newProperties){
        this.properties << newProperties 
    }
        
    def copy(newName){
        def clone = clone()
        clone.prefix = prefix
        clone.ident = newName
        clone.subscribeTo(clone.name)
        return clone
    }
}
