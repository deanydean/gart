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
package gart.comm

/**
 * A communication
 * If you change the comm API you should rev the version uid also.
 * @author deanydean
 */
class Comm implements Serializable {

    private static final long serialVersionUID = 1l

    public id
    public data = [:]
    public reply
    public result

    public Comm(id){
        this.id = id
    }

    public Comm(id, comm){
        this.id = comm.id
        this.data << comm.data
        this.reply = comm.reply
    }
    
    public Comm copyAndConsume(id){
        String consumed = this.id.replace(id, "")
        if(consumed.startsWith(".")){
            consumed = consumed.substring(1)
        }
        
        // Copy
        def copy = new Comm(consumed)
        copy.data << this.data
        if(this.reply)
            copy.reply = this.reply
       
        // Make sure I don't call reply (as I've passed it on)
        this.reply = null
        
        return copy
    }
    
    public boolean isSet(id){
        return (data[id] == true)
    }
    
    public get(id){
        return data[id]
    }
    
    public set(id, value){
        data[id] = value
        return this
    }

    public setAll(all){
        all.each { k, v -> data[k] = v }
        return this
    }
    
    public publish(reply=null){
        this.reply = reply
        CommExchange.publish(this)
        return this
    }
    
    public String toString(){
        def str = new StringBuffer("COMM[ id:${id} ")
        data.each { k, v -> str << "$k:${v.toString()} " }
        str << "]"
        return str.toString()
    }
}

