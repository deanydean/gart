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
package gart.swarm

import gart.Gart

@Grab("com.hazelcast:hazelcast:3.1.4")
import com.hazelcast.core.*

@Grab("org.neo4j:neo4j:2.1.2")
import org.neo4j.graphdb.*
import org.neo4j.graphdb.factory.*
import org.neo4j.tooling.*

/**
 * A Hazelcast MapStore impl backed by an embedded neo4j graph
 */
class Neo4jMapStore implements MapStore<String,Object> {

    public static final PROP_ID = "id"
    public static final PROP_DATA = "data"

    private static LOG = Gart.LOG

    private static final CONFIG = Gart.CONFIG.swarm.store
    private static DB // do not use directly, use database() instead

    def name
    def config
    
    def database
    def label

    public Neo4jMapStore(name){
        this.name = name
        this.config = Gart.CONFIG.swarm.stores[name]
   
        // Set the label for this store
        this.label = DynamicLabel.label(name)

        // Create index so we can search on ids
        def tx = getNewTransaction()
        try{
            database().schema().indexFor(label).on(PROP_ID).create()
            tx.success()
        }catch(e){
            LOG.error "Failed to create index for $label on $PROP_ID : $e"
            tx.failure()
        }finally{
            tx.close()
        }
    }

    public void delete(String key){
        LOG.debug "Deleting swarm entry $key"
        def tx = getNewTransaction()
        try{
            safeDelete(key)
            tx.success()
        }catch(e){
            LOG.error "Failed to delete $key : $e"
            tx.failure()
        }finally{
            tx.close()
        }
    }

    public void deleteAll(Collection keys){
        LOG.debug "Deleting swarm entries ${keys}"
        def tx = getNewTransaction()
        try{
            keys.each { safeDelete(it) }
            tx.success() 
        }catch(e){
            LOG.error "Failed to delete ${keys}"
            tx.failure()
        }finally{
            tx.close()
        }
    }

    private void safeDelete(key){
        def nodes = database().findNodesByLabelAndProperty(this.label,
            PROP_ID, key)
        nodes.each { node.delete() }
    }

    public void store(String key, Object value){
        LOG.debug "Storing swarm entry $key->$value"
        def tx = getNewTransaction()
        try{
            safeStore(key, value)            
            tx.success()
        }catch(e){
            LOG.error "Failed to store ${key}-${value}: $e"
            tx.failure()
        }finally{
            tx.close()
        }
    }

    public void storeAll(Map map){
        LOG.debug "Storing swarm entries ${map}"
        def tx = getNewTransaction()
        try{
            map.each { k, v -> safeStore(k, v) }
            tx.success()
        }catch(e){
            LOG.error "Failed to store ${map} : $e"
            tx.failure()
        }finally{
            tx.close() 
        }
    }

    private void safeStore(key, value){
        def node = database().createNode(this.label)
        node.setProperty(PROP_ID, key)
        node.setProperty(PROP_DATA, getBytes(value))
    }

    public Object load(Object key){ 
        LOG.debug "Loading swarm entry $key"
        def tx = getNewTransaction()
        try{
            return safeLoad(key)
        }catch(e){
            LOG.error "Failed to load $key : $e"
            tx.failure()
            return null
        }finally{
            tx.close() 
        }
    }

    public Map loadAll(Collection keys){ 
        LOG.debug "Loading swarm entries ${keys}"
        def tx = getNewTransaction()
        def all = [:]

        try{
            keys.each { all["$it"] = safeLoad(it) }
            tx.success()
            return all
        }catch(e){
            LOG.error "Failed to load $keys : $e"
            tx.failure()
            return null
        }finally{
            tx.close()
        }
    }

    private Object safeLoad(key){
        def nodes = database().findNodesByLabelAndProperty(this.label,
            PROP_ID, key)
        for(node in nodes)
            return getObject(node.getProperty(PROP_DATA))
    }
    
    public Set loadAllKeys(){ 
        LOG.debug "Loading all swarm keys"
        def tx = getNewTransaction()
        def keys = []
    
        try{
            // Get all
            def nodes = 
                GlobalGraphOperations.at(database()).getAllNodesWithLabel(
                    this.label)
            nodes.each { node ->
                keys << node.getProperty(PROP_ID)
            }
            tx.success()
        }catch(e){
            LOG.error "Failed to load keys: $e"
            tx.failure()
        }finally{
            tx.close()
        }

        LOG.debug "Found keys $keys"
        return keys as Set
    }

    Transaction getNewTransaction(){
        return database().beginTx()
    }

    static byte[] getBytes(Object object){
        def baos = new ByteArrayOutputStream()
        try{
            def oos = new ObjectOutputStream(baos)
            try{
                oos.writeObject(object)
                return baos.toByteArray()
            }finally{
                oos.close()
            }
        }finally{
            baos.close()
        }
    }

    static Object getObject(byte[] bytes){
        def bais = new ByteArrayInputStream(bytes)
        try{
            def ois = new ObjectInputStream(bais)
            try{
                return ois.readObject()
            }finally{
                ois.close()
            }
        }finally{
            bais.close()
        }
    }

    synchronized static database(){
        if(!DB)
            DB = new GraphDatabaseFactory().newEmbeddedDatabase(CONFIG.path)
        return DB
    }
}
