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
package bot.comm

/**
 * A communication
 * @author deanydean
 */
class Comm implements Serializable {

    public id;
    public data = [:];
    
    public Comm(id){
        this.id = id;
    }
    
    public Comm copyAndConsume(id){
        String consumed = this.id.replace(id, "");
        if(consumed.startsWith(".")){
            consumed = consumed.substring(1);
        }
        
        // Copy
        def copy = new Comm(consumed);
        copy.data << this.data;
        return copy;
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
    
    public publish(){
        CommExchange.publish(this);
        return this
    }
    
    public String toString(){
        def str = new StringBuffer("COMM[ id:${id} ")
        data.each { k, v -> str << "$k:${v.toString()} " }
        str << "]"
        return str.toString()
    }
}

