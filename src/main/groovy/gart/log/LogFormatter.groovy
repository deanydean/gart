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
package gart.log

import gart.Gart

import java.util.logging.*
import java.text.MessageFormat

/**
 * A log formatter
 * @author deanydean
 */
class LogFormatter extends Formatter {    

    public static final COLOR_RED    = "\u001b[31m"
    public static final COLOR_GREEN  = "\u001b[32m"
    public static final COLOR_YELLOW = "\u001b[33m" 
    public static final COLOR_CYAN   = "\u001b[36m"
    public static final COLOR_WHITE  = "\u001b[37m"
   
    public static final DEFAULT_COLOR = COLOR_CYAN;

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
        def prefix = "${COLOR_GREEN}${Gart.CONFIG.id}>  "

        def log
        try{
            log = "${MessageFormat.format(message, params)}"
        }catch(e){
            log = "$message $params"
        }

        return "$prefix $log $DEFAULT_COLOR\n" 
    }

    public String error(LogRecord record){
        def prefix = "${COLOR_RED}${Gart.CONFIG.id}> X"
       
        def log
        try{
            log = "${MessageFormat.format(record.getMessage(),record.getParameters())}"
        }catch(e){
            log = "${record.getMessage()} ${record.getParameters()}"
        }

        return "$prefix $log $DEFAULT_COLOR\n"

    }

    public String debug(LogRecord record){
        def prefix = "${COLOR_YELLOW}${Gart.CONFIG.id}>>>"

        def log
        try{
            log = "${MessageFormat.format(record.getMessage(),record.getParameters())}"
        }catch(e){
            log = "${prefix} ${record.getMessage()} ${record.getParameters()}"
        }

        return "$prefix $log $DEFAULT_COLOR\n"
    }
}
