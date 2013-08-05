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

import bot.Bot

import org.codehaus.groovy.tools.shell.*
import org.codehaus.groovy.tools.shell.util.*

/**
 * A basic groovy shell for the bot
 * @author deanydean
 */
public class Botsh extends Groovysh {

    def lastResult = null

    private static Closure createDefaultRegistrar(){
        return { shell ->
            def r = new XmlCommandRegistrar(shell, classLoader)
            r.register(Groovysh.class.getResource("commands.xml"))
        }
    }

    public Botsh(){
        super(Thread.currentThread().contextClassLoader,
            new Binding(), new IO(System.in, System.out, System.err),
            createDefaultRegistrar())
    }

    int run(final String[] args){
        io.setVerbosity(IO.Verbosity.QUIET)
        try{
            return super.run(args)
        }catch(e){
            Bot.LOG.error "${e.getMessage()}"
            Bot.LOG.debug "$e"
        }
    }

    public String renderPrompt(){
        return "BOT << "
    }

    public void displayError(e){
        Bot.LOG.error "$e"
    }

}
