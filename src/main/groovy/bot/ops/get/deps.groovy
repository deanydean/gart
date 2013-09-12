/*
 * Get all bot dependencies.....
 */
package bot.ops.build;

import bot.Bot
import bot.log.Log
import bot.util.CommandExecutor

def cmd = new CommandExecutor("Get bot dependencies", [ "gradle", "getdeps" ])

// Work out the working dir for the source tree....
if(!new File("./build.gradle").exists()){
    cmd.setWorkingDir(Bot.BOT_HOME)
}

Bot.LOG.info "Getting bot dependencies... "
cmd.exec()
