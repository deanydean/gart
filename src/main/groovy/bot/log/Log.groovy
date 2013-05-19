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
package bot.log

import bot.Bot
import bot.comm.Comm

import java.util.logging.*

/**
 * A log implementation
 * @author deanydean
 */
class Log {
    
    public static final String LOG_COMMS_PREFIX = "log."
    
    public static final Level ERROR = Level.WARNING
    public static final Level INFO = Level.INFO
    public static final Level DEBUG = Level.FINE
    
    private long lastLogTime = 0l
    private boolean repeatLock = false
    private long repeatInterval = 1000l
    private boolean async = false
    private String name
    
    def level = Bot.CONFIG.log.level

    @SuppressWarnings("NonConstantLogger")
    private Logger logger
    
    public Log(String name){
        this.name = name
        init()
    }
    
    public Log(Class clazz){
        this.name = clazz.getName()
        init()
    }
    
    public Log(Object obj){
        this.name = obj.class.getName()
        init()
    }

    protected final void init(){
        this.logger = Logger.getLogger(this.name)
        
        // Override all logging handlers
        Handler[] handlers = this.logger.getHandlers()
        for(Handler h : handlers){
            this.logger.removeHandler(h)
        }
        def handler = new LogHandler();
        this.logger.addHandler(handler)
        this.logger.setUseParentHandlers(false)
        
        // Set levels
        if(this.level){
            this.logger.setLevel(Level.parse(level))
            handler.setLevel(Level.parse(level))
        }
        
        if(this.async){
            if(this.logger != getLogger(CommsExchange.class)){
                subscribe(LOG_COMMS_PREFIX+this.logger.getName())
            }else{
                this.async = false
            }
        }
    }
    
    public void setRepeatLock(boolean lock){
        this.repeatLock = lock;
    }
    
    public void setAsync(boolean async){
        if(async && !this.async){
            subscribe(LOG_COMMS_PREFIX+this.logger.getName())
        }else if(!async && this.async){
            unsubscribe(LOG_COMMS_PREFIX+this.logger.getName())
        }
        
        this.async = async
    }
    
    public static Logger getLogger(Class clazz){
        return Logger.getLogger(clazz.getName())
    }
    
    public static Logger getLogger(Object object){
        return Logger.getLogger(object.getClass().getName())
    }
    
    private void updateLastLogTime(){
        lastLogTime = System.currentTimeMillis()
    }

    private boolean isRepeatLocked(){
        return this.repeatLock && 
            (lastLogTime+this.repeatInterval >= System.currentTimeMillis())
    }
    
    private String[] getCallerInfo(){
        StackTraceElement[] stack = Thread.currentThread().getStackTrace()
        return [ stack[3].getClassName(), stack[3].getMethodName() ]
    }
    
    public void info(String message, Object... args){
        if(!this.isRepeatLocked() && logger.isLoggable(Level.INFO)){
            String[] caller = getCallerInfo();
            logp(Level.INFO, caller[0], caller[1], message, args);
            updateLastLogTime();
        }
    }

    public void debug(String message, Object... args){
        if(!this.isRepeatLocked() && logger.isLoggable(Level.FINE)){
            String[] caller = getCallerInfo();
            logp(Level.FINE, caller[0], caller[1], message, args);
            updateLastLogTime();
        }
    }
    
    public void error(String message, Object... args){
        if(!this.isRepeatLocked() && logger.isLoggable(Level.WARNING)){
            String[] caller = getCallerInfo();
            logp(Level.WARNING, caller[0], caller[1], message, args);
            updateLastLogTime();
        }
    }
    
    public void log(Level level, String message, Object... args){
        if(!this.isRepeatLocked() && logger.isLoggable(level)){
            String[] caller = getCallerInfo();
            logp(level, caller[0], caller[1], message, args);
            updateLastLogTime();
        }
    }
    
    private void logp(Level level, String className, String methodName, 
            String message, Object[] args){
        if(this.async){
            Comm log = new Comm(LOG_COMMS_PREFIX+this.logger.getName());
            log.set(LogInfo.LEVEL, level);
            log.set(LogInfo.CLASSNAME, className);
            log.set(LogInfo.METHODNAME, methodName);
            log.set(LogInfo.MESSAGE, message);
            log.set(LogInfo.ARGS, args);
            publish(log);
        }else{
            logger.logp(level, className, methodName, message, args);
        }
    }

    public void logFromStream(InputStream stream, Level level){
        Thread loggerThread = new Thread(new StreamLogger(stream, this, level));
        loggerThread.setDaemon(true);
        loggerThread.start();
    }

    @Override
    protected void onComm(Comm comm){
        LogInfo info = new LogInfo(comm);
        logger.logp(info.getLevel(), info.getClassName(), info.getMethodName(),
            info.getMessage(), info.getArgs());
    }
}

