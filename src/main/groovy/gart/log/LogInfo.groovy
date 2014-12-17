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

import java.util.logging.Level
import gart.comm.Comm
/**
 *
 * @author deanydean
 */
class LogInfo {
    public static final String LEVEL = "log.level";
    public static final String CLASSNAME = "log.classname";
    public static final String METHODNAME = "log.methodname";
    public static final String MESSAGE = "log.message";
    public static final String ARGS = "log.args";

    private Comm comm;

    public LogInfo(Comm comm){
        this.comm = comm;
    }

    public Level getLevel(){
        return (Level) this.comm.get(LEVEL);
    }

    public String getClassName(){
        return (String) this.comm.get(CLASSNAME);
    }

    public String getMethodName(){
        return (String) this.comm.get(METHODNAME);
    }

    public String getMessage(){
        return (String) this.comm.get(MESSAGE);
    }

    public Object[] getArgs(){
        return (Object[]) this.comm.get(ARGS);
    }
}

