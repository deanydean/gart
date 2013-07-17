package bot.io

import bot.log.*

import java.nio.*

/**
 * Receive input from a device
 * @author deanydean
 */
class DevInput {
    
    private static final Log LOG = new Log(DevInput.class)
    
    def name
    def blockSize
    def reader
    def running = false
    
    public DevInput(String name, int blockSize){
        this.name = name
        this.blockSize = blockSize
    }
    
    public void start(Closure onReadBlock){
        this.running = true;
        this.reader = Thread.start {
            new File(this.name).eachByte(this.blockSize) { bytes, read ->
                def buffer = ByteBuffer.wrap(bytes as byte[])
                buffer.order(ByteOrder.LITTLE_ENDIAN)
                onReadBlock(buffer, read)
            }
        }
    }
    
    public void stop(){
        this.running = false;
        this.reader.interrupt();
    }
}
