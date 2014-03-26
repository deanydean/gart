/**
 * Service that updates the bot
 */
package bot.ext.services

import bot.Bot
import bot.comm.*
import bot.control.Service
import bot.util.*

class UpdateService extends Service {

    public static final String[] CHECK_UPDATE = ["check-updates.sh"]
    public static final String[] DO_UPDATE = ["git", "pull"]
    
    def config = Bot.CONFIG.update
    def timer = new TimeTool()

    public UpdateService(){
        super("update", true)
    }
    
    def checkForUpdates = {
        def checkForUpdate = new CommandExecutor("$name check", CHECK_UPDATE)
        checkForUpdate.setWorkingDir(Bot.BOT_HOME)
        checkForUpdate.suppressOutput = true

        Bot.LOG.info "Checking for updates @ ${new Date()}..."
        if(checkForUpdate.exec() == 0){
            Bot.LOG.info "Updates available"
            if(config && config.auto)
                update()
        }else{
            Bot.LOG.info "No updates are available"
        }
    }

    def update = {
        def merge = 
            new CommandExecutor("perform $name", DO_UPDATE)
        merge.setWorkingDir(Bot.BOT_HOME)
        Bot.LOG.info "Updating..."
            
        if(merge.exec() == 0){
            Bot.LOG.info "Bot updated"
            if(config && config.restartOnUpdate)
                Bot.restart()
        }else{
            Bot.LOG.error "Failed to update bot!"
        }
    }
    
    public void onStart(){
        // Start checking for updates
        def interval = config.interval ? config.interval : 60
        timer.interval(checkForUpdates, (interval*60))
    }
    
    public void onStop(){
        timer.cancelAll()
    }
}
