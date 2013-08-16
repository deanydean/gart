package bot.control

import bot.Bot
import bot.comm.Comm

import jline.Completor
import org.codehaus.groovy.tools.shell.*

public class BotshOpCommand extends CommandSupport {

    public static final OP_COMMAND_NAME = "op"

    public BotshOpCommand(Shell shell){
        super(shell, OP_COMMAND_NAME, ":")
    }

    public Object execute(List args){
        new Comm("op").set("args", args).publish()
    }

}
