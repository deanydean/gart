/*
 * Copyright 2015 Matt Dean
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
package gart.log

import gart.Gart
import gart.comm.*

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
    private String name
    
    def level = Gart.CONFIG.log.level
    
    @SuppressWarnings("NonConstantLogger")
    private Logger logger
    private LogHandler handler
    
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
        this.handler = new LogHandler();
        this.logger.addHandler(this.handler)
        this.logger.setUseParentHandlers(false)
        
        // Set levels
        if(this.level)
            this.setLevel(this.level)
    }

    public void setLevel(level){
        def logLevel = Level.parse(level)
        this.logger.setLevel(logLevel)
        this.handler.setLevel(logLevel)
        this.level = level
    }
    
    public void setRepeatLock(boolean lock){
        this.repeatLock = lock;
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
            logger.logp(Level.INFO, caller[0], caller[1], message, args);
            updateLastLogTime();
        }
    }

    public void debug(String message, Object... args){
        if(!this.isRepeatLocked() && logger.isLoggable(Level.FINE)){
            String[] caller = getCallerInfo();
            logger.logp(Level.FINE, caller[0], caller[1], message, args);
            updateLastLogTime();
        }
    }
    
    public void error(String message, Object... args){
        if(!this.isRepeatLocked() && logger.isLoggable(Level.WARNING)){
            String[] caller = getCallerInfo();
            logger.logp(Level.WARNING, caller[0], caller[1], message, args);
            updateLastLogTime();
        }
    }
    
    public void log(Level level, String message, Object... args){
        if(!this.isRepeatLocked() && logger.isLoggable(level)){
            String[] caller = getCallerInfo();
            logger.logp(level, caller[0], caller[1], message, args);
            updateLastLogTime();
        }
    }
    
    public void logFromStream(InputStream stream, Level level){
        Thread loggerThread = new Thread(new StreamLogger(stream, this, level));
        loggerThread.setDaemon(false);
        loggerThread.start();
    }
}
