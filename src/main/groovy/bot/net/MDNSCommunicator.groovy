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
package bot.net

import bot.Bot
import bot.comm.Communicator
import bot.comm.Comm

import javax.jmdns.*

/**
 * A communicator that detects/publishes services using mDNS.
 * @author deanydean
 */
class MDNSCommunicator extends Communicator {

    public static final String MDNS_COMM = "mdns"
    public static final String MDNS_NEW = "mdns.new"
    public static final String MDNS_RM = "mdns.rm"
    public static final String MDNS_SERVICE_TYPE = "service-type"
    public static final String MDNS_SERVICE = "service"
    
    public static final String SERVICE_NAME = "service-name"
    public static final String SERVICE_TYPE = "service-type"
    public static final String SERVICE_INFO = "service-info"
    public static final String SERVICE_EVENT = "service-event"
    public static final String SERVICE_PORT = "service-port"
    public static final String SERVICE_TEXT = "service-text"
    public static final String SERVICE_PROPS = "service-props"
    
    
    def config = Bot.CONFIG.mdns
    
    //def jmdns = JmDNS.create(null, "MDNSCommunicator")
    def jmdns = null
    //def jmdns = JmmDNS.Factory.getInstance();
    
    public MDNSCommunicator(){
        super({
            def comm = it[1]
            Bot.LOG.debug("MDNSComm recevied: $comm")
            
            if(comm.id.equals(MDNS_SERVICE_TYPE)){
                def type = comm.get(SERVICE_TYPE)
                Bot.LOG.debug("Registering new service type: $type")
                it[0].jmdns.registerServiceType(type)
            }else if(comm.id.equals(MDNS_SERVICE)){
                try{
                    Bot.LOG.debug("Creating service info for $comm")
                    def serviceInfo = ServiceInfo.create(comm.get(SERVICE_TYPE),
                        comm.get(SERVICE_NAME), comm.get(SERVICE_PORT),
                        100, 1, comm.get(SERVICE_PROPS))
                    
                    Bot.LOG.debug("Registering new service: $serviceInfo")
                    it[0].jmdns.registerService(serviceInfo)
                }catch(e){
                    Bot.LOG.error("Failed to register $comm : $e")
                }
            }
        })
    
        def host = InetAddress.getLocalHost();
        Bot.LOG.debug("Listening for mDNS on $host")
        this.jmdns = JmDNS.create(host, "mdns-communicator")
    }
    
    public MDNSCommunicator startService(){
        jmdns.addServiceTypeListener(
            new ServiceTypeListener(){
                public void serviceTypeAdded(ServiceEvent e){
                    Bot.LOG.debug("STYPE ADD: ${e.getType()}")
                    addService(e.getType(), e.getName())
                }
                
                public void subTypeForServiceTypeAdded(ServiceEvent e){
                    Bot.LOG.debug("SUB ADD: ${e.getType()}")
                }
            }
        )
        Bot.LOG.debug("MDNS communicator listening for MDNS stuff")
        
        this.subscribeTo(MDNS_NEW)
        Bot.LOG.debug("MDNS communicator listening for new mdns stuff")
        
        return this
    }
    
    private void addService(type, name){
        jmdns.addServiceListener(type, 
            new ServiceListener(){
                public void serviceResolved(ServiceEvent e){
                    Bot.LOG.debug("SRV RESOLV: ${e.getName()} ${e.getType()} "+
                        "${e.getInfo()}")
                    
                    // Publish a comm
                    def comm = new Comm(MDNS_COMM+"."+getCommName(e.getType()))
                    comm.set(SERVICE_NAME, e.getName())
                    comm.set(SERVICE_INFO, e.getInfo())
                    comm.set(SERVICE_TYPE, e.getType())
                    comm.set(SERVICE_EVENT, e)
                    comm.publish()
                }
                
                public void serviceRemoved(ServiceEvent e){
                    Bot.LOG.debug("SRV RM: ${e.getName()} ${e.getType()}}")
                }
                
                public void serviceAdded(ServiceEvent e){
                    Bot.LOG.debug("SRV ADD: ${e.getName()} ${e.getType()}")
                    jmdns.requestServiceInfo(e.getType(), e.getName(), true)
                }
            }
        )
    }
    
    public void stopService(){
        this.jmdns.close()
    }
    
    private String getCommName(String type){
        def bits = type.tokenize(".")
        def name = new StringBuilder()
        
        bits.reverseEach {
            name << it << "."
        }
        
        return name.substring(0, name.length()-1)
    }    
}

