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
package bot.services

import bot.*
import bot.control.*

@Grab("org.neo4j:neo4j:2.1.2")
import org.neo4j.graphdb.*
import org.neo4j.graphdb.factory.*
import org.neo4j.tooling.*

/**
 * A graph database service backed by an embedded Neo4j graph db
 * @author matt
 */
class GraphService extends Service {
    
    public static final NODE_LABEL = "node.label"
    public static final NODE_PROPS = "node.props"
    
    public static final OP_RESULT = "graph.op.result"

    def config
    def database
    
    def static graphComm = { comm, service ->
        if(!service.database){
            service.LOG.error "No database for ${comm}"
            return
        }

        def tx = service.database.beginTx()

        try{
            def result
            switch(comm.id){
                case "create": result = service.create(tx, comm); break
                case "read": result = service.read(tx, comm); break;
                case "update": result = service.update(tx, comm); break;
                case "delete": result = services.delete(tx, comm); break;
                default: 
                    throw new Exception("Unknown comm ${comm}")
            }
            tx.success()

            // Set the result data
            comm.set(OP_RESULT, result)
        }catch(e){
            service.LOG.error "Failed to process ${comm} : ${e}"
            comm.set(OP_RESULT, e)
            tx.failure()
        }finally{
            tx.close()
        }
    }
    
    public GraphService(){
        super("graph", true, 9, graphComm)
        this.config = Bot.CONFIG.graph
    }
    
    public void onStart(){
        LOG.debug "Starting graph db service, loading ${this.config}"
        this.database = 
            new GraphDatabaseFactory().newEmbeddedDatabase(this.config.path)
    }
    
    public void onStop(){
        this.database.shutdown()
    }
    
    public create(comm){
        LOG.debug "Creating node for ${comm}"
        
        def label = DynamicLabel.label(comm.get(NODE_LABEL))
        def node = this.database.createNode(this.label)
        comm.get(NODE_PROPS).each { k, v -> node.setProperty(k, v) } 
        return node 
    }
    
    public read(comm){
        LOG.debug "Reading nodes for ${comm}"

        def result = []
        def label = DynamicLabel.label(comm.get(NODE_LABEL))
        comm.get(NODE_PROPS).each { k, v ->
            result << this.database.findNodesByLabelAndProperty(
                this.label, k, v)
        }
        return result
    }
     
    public update(comm){
        LOG.debug "Updating nodes for ${comm}"
        
        // Get nodes
        def nodes = read(comm)
        nodes.each { node ->
            comm.get(NODE_PROPS).each { k, v ->
                node.setProperty(k, v)
            }
        }
        return nodes
    }
    
    public delete(comm){
        LOG.debug "Deleting nodes for ${comm}"
        read(comm).each { it.delete() }
    }
}
