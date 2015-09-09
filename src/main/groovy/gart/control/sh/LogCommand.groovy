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
package gart.control.sh

import gart.Gart
import gart.comm.Comm

import org.codehaus.groovy.tools.shell.*

public class LogCommand extends CommandSupport {

    public static final LOG_COMMAND_NAME = "log"

    public LogCommand(Shell shell){
        super(shell, LOG_COMMAND_NAME, "#")
    }

    public Object execute(List args){
        Gart.LOG.setLevel(args[0])
    }

}
