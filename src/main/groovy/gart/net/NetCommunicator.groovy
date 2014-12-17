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
package gart.net

import gart.Gart
import gart.comm.*
import gart.log.*

import java.nio.ByteBuffer
import java.nio.channels.*

/**
 * A communicator that comms over a network.
 * @author deanydean
 */
class NetCommunicator extends Communicator implements Runnable {
    
    private static final Log LOG = new Log(NetCommunicator.class)
    
    public static final String NET_TARGET = "target"
    public static final String NET_PAYLOAD = "payload"
    
    def port = Gart.CONFIG.net.commsPort
    def mode = Gart.CONFIG.net.mode
    
    private SelectableChannel dataChannel
    private Selector selector
    
    private Thread channelHandler
    private boolean running = false
    private boolean listening = false
    
    // Buffers
    private ByteBuffer pendingBytes = null
    
    // The channel factory
    def channelCreator = [
        "udp": { bindPort ->
            this.dataChannel = DatagramChannel.open()
            LOG.debug "My UDP bind port is $bindPort"
            try{
                this.dataChannel.socket().bind(new InetSocketAddress(bindPort))
                this.listening = true
            }catch(BindException be){
                LOG.error "I cannot listen for UDP, something else is doing it"
            }
        } 
    ]
    
    def commSender = [
        "udp": { buffer, endpoint ->
            this.dataChannel.send(buffer, endpoint)
        }
    ]
    
    def commReceiver = [
        "udp": { 
            def buffer = ByteBuffer.allocate(2048)
            SocketAddress from = this.dataChannel.receive(buffer)
            
            LOG.debug "Received datagram from $from"
            return buffer 
        }
    ]
    
    public NetCommunicator(){
        super({ communicator, comm ->
            communicator.handleComm(comm)
        })
    }
    
    private void init(){        
        // Use the factory to create the channel
        channelCreator[mode](this.port)
        
        if(this.listening){
            // Always non-blocking
            this.dataChannel.configureBlocking(false)
        
            // Only select on reads
            this.selector = Selector.open()
            this.dataChannel.register(selector, SelectionKey.OP_READ)
            this.channelHandler = new Thread(this, "NetComms on port "+this.port)
        }
        
        // Subscribe for outgoing packets
        subscribeTo("net")
    }
    
    public static final start = { port ->
        // Creat ta communicator
        def communicator = new NetCommunicator()
        
        // Init the communicator
        if(port) communicator.port = port  
        communicator.init()
       
        if(communicator.listening){
            // Start the channel handler
            communicator.channelHandler.start()
        }
        
        return communicator
    }
    
    public stopService(){
        if(!this.running){
            return
        }
        
        this.running = false
        
        try{
            this.selector.close()
            this.dataChannel.close()
        }catch(IOException ioe){
            LOG.error "Failed to disconnect $this: $ioe"
        }
    }
    
    final handleComm = { comm ->
        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream()
            ObjectOutputStream oos = new ObjectOutputStream(baos)
            oos.writeObject(comm)
            byte[] data = baos.toByteArray()
            
            ByteBuffer buffer = ByteBuffer.allocate(data.length+4)
            buffer.putInt(data.length)
            buffer.put(data)
            buffer.flip()
            
            String endpoint = (String) comm.get(NET_TARGET)
            if(endpoint != null){
                InetSocketAddress endpointAddress =
                    new InetSocketAddress(endpoint, port)
                this.commSender[mode](buffer, endpointAddress)
            }else{
                LOG.error "Unknown destination for net comm"
            }
        }catch(IOException ioe){
            LOG.error "Failed to handle net comm : $ioe"
        }
    }

    @Override
    public void run() {
        this.running = true
        
        while(this.running){
            try{
                int selected = selector.select(5000)
                if(selected <= 0){
                    continue
                }
                
                Set<SelectionKey> readyKeys = selector.selectedKeys()
                Iterator<SelectionKey> iter = readyKeys.iterator()
                
                while(iter.hasNext()){
                    SelectionKey key = iter.next()
                    
                    if(key.isReadable()){
                        ByteBuffer buffer = this.commReceiver[mode]()
                        onData(buffer)
                    }else{
                        LOG.error "Unhandle select key: $key"
                    }
                    
                    iter.remove()
                }
            }catch(IOException ioe){
                LOG.error "$this handler failed: $ioe"
            }
        }

        LOG.debug "$this ended"
    }
    
    private void onData(ByteBuffer data) throws IOException {
        // Reset the data buffer
        data.flip()
        
        // Work out if we've got all the data we need.
        if(pendingBytes == null){
            // Read length
            int length = data.getInt()
            this.pendingBytes = ByteBuffer.allocate(length)
        }
        
        if(pendingBytes.remaining() > data.remaining()){
            // Read and return
            this.pendingBytes.put(data)
            return
        }else{
            // Read and continue
            this.pendingBytes.put(data)
        }

        //  Read the data as a comm
        ByteArrayInputStream bias = 
            new ByteArrayInputStream(pendingBytes.array())
        ObjectInputStream ois = new ObjectInputStream(bias)
        
        Comm comm = null
        try{
            Object readObj = ois.readObject()
            if(!(readObj instanceof Comm)){
                LOG.error "Non-Comm object received : $readObj"
                return;
            }
            comm = (Comm) readObj
        }catch(ClassNotFoundException cnfe){
            LOG.error "Cannot find class : $cnfe"
        }
        
        this.pendingBytes.clear()
        this.pendingBytes = null
        
        if(comm != null){
            // Publish Comm
            Comm payload = (Comm) comm.get(NET_PAYLOAD)
            if(payload)
                payload.publish()
            else    
                comm.publish()
        }
    }
    
    @Override
    public String toString(){
        return "NetComms[mode=${this.mode} port=${this.port}]"
    }
}

