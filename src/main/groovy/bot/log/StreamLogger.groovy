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

import java.util.logging.Level
/**
 *
 * @author deanydean
 */
class StreamLogger implements Runnable {
    private InputStream stream
    private Level level
    private Log logger

    public StreamLogger(InputStream stream, Log logger, Level level){
        this.stream = stream
        this.level = level
        this.logger = logger
    }

    public void run(){
        this.stream.eachLine { line ->
            this.logger.log(this.level, line)
        }
    }
}

