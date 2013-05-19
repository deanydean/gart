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

import java.util.logging.*
import java.text.MessageFormat

/**
 * A log formatter
 * @author deanydean
 */
class LogFormatter extends Formatter {    
    
    @Override
    public String format(LogRecord record) {
        if(record.getLevel() == Level.INFO){
            return this.info(record.getMessage(), record.getParameters())
        }

        if(record.getLevel() == Level.WARNING ||
                record.getLevel() == Level.SEVERE){
            return this.error(record)
        }

        return debug(record)
    }

    public String info(String message, Object[] params){
        return "BOT> ${MessageFormat.format(message, params)}\n"
    }

    public String error(LogRecord record){
        return "ERROR: ${MessageFormat.format(record.getMessage(),record.getParameters())}\n";
    }

    public String debug(LogRecord record){
        return "DEBUG: ${MessageFormat.format(record.getMessage(),record.getParameters())}\n";
    }
}
