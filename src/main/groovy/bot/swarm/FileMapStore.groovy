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
package bot.swarm

import bot.Bot

@Grab("com.hazelcast:hazelcast:3.1.4")
import com.hazelcast.core.*

class FileMapStore implements MapStore<Object,Object> {

    private static LOG = Bot.LOG

    def config
    def path
    def enabled

    public FileMapStore(){
        this.config = Bot.CONFIG.swarm
        if(this.config)
            this.path = this.config.fileStorePath
        this.enabled = this.path && config.fileStore
    }

    public void delete(Object key){
        LOG.debug "Deleting swarm entry $key"
    }

    public void deleteAll(Collection keys){
        LOG.debug "Deleting swarm entries ${keys}"
    }

    public void store(Object key, Object value){
        LOG.debug "Storing swarm entry $key->$value"
    }

    public void storeAll(Map map){
        LOG.debug "Storing swarm entries ${map}"
    }

    public Object load(Object key){ 
        LOG.debug "Loading swarm entry $key"
        return null 
    }

    public Map loadAll(Collection keys){ 
        LOG.debug "Loading swarm entries ${keys}"
        return null 
    }
    
    public Set loadAllKeys(){ 
        LOG.debug "Loading all swarm keys"
        return null 
    }

}
