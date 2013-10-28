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
package bot.util

import java.util.concurrent.*

/**
 * A tool that can be use to do things at certain times
 * @author deanydean
 */
class TimeTool {

    def threads
    def scheduledPool

    public TimeTool(poolSize=1){
        threads = poolSize
        initThreadPool()
    }

    public interval(callback, interval, unit=TimeUnit.SECONDS){
        scheduledPool.scheduleWithFixedDelay(callback, 0, interval, unit)
    }

    public delay(callback, delay, unit=TimeUnit.SECONDS){
        scheduledPool.schedule(callback, delay, unit)
    }
    
    public cancelAll(){
        scheduledPool.shutdownNow()
        initThreadPool()
    }

    private initThreadPool(){
        scheduledPool = Executors.newScheduledThreadPool(threads);
    }    
}

