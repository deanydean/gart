package bot.meta

/**
 * Allows an async meta call to wait for the comm reply.
 * This is the actor implementation used by bot.meta.Meta
 */
class MetaActor {
   
    /**
     * The meta reply (if there is one)
     */
    def metaReply
    
    /**
     * Pause until the meta call has completed.
     */
    synchronized void pause() { wait() }
    
    /**
     * Notify the paused actor that the meta call has completed with no reply.
     */
    synchronized void proceed() { notify() }
    
    /**
     * Notify the paused actor that the meta call has completed with a reply.
     */
    synchronized void proceed(reply) { 
        this.metaReply = reply
        notify() 
    } 
}
