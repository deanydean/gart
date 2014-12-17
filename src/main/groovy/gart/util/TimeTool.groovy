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
package gart.util

import java.util.concurrent.*
import java.util.concurrent.atomic.*

/**
 * A tool that can be used to do things at certain times
 * @author deanydean
 */
class TimeTool {

    def name
    def threads
    def scheduledPool
    def threadFactory

    public TimeTool(poolName="tt-${System.currentTimeMillis()}", poolSize=1){
        threads = poolSize
        name = poolName
        threadFactory = new TimerThreadFactory(poolName)
        initThreadPool()
    }

    public interval(callback, interval, unit=TimeUnit.SECONDS){
        scheduledPool.scheduleWithFixedDelay(callback, 0, interval, unit)
    }

    public delay(callback, delay, unit=TimeUnit.SECONDS){
        scheduledPool.schedule(callback, delay, unit)
    }

    public schedule(callback, timestamp){
        scheduledPool.schedule(callback, 
            [0,timestamp-System.currentTimeMillis()].max(), 
            TimeUnit.MILLISECONDS)
    }
    
    public cancelAll(){
        scheduledPool.shutdownNow()
        initThreadPool()
    }

    public cancelAndWait(maxWait=60, unit=TimeUnit.SECONDS){
        scheduledPool.shutdown()
        if(!scheduledPool.awaitTermination(maxWait, unit)){
            sheduledPool.shutdownNow()
        }
    }

    private initThreadPool(){
        scheduledPool = Executors.newScheduledThreadPool(threads, threadFactory);
    }

    class TimerThreadFactory extends ThreadFactory {
        
        def counter = new AtomicInteger(0)
        def name

        TimerThreadFactory(name){
            this.name = name
        }

        @Override
        public Thread newThread(Runnable runnable){
            def thread = new Thread(runnable,
                "${name}-${counter.getAndIncrement()}")
            thread.setDaemon(true)
            return thread
        }

    }
}

