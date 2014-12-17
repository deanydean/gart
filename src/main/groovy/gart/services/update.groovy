/**
 * Service that updates gart
 */
package gart.services

import gart.Gart
import gart.comm.*
import gart.control.Service
import gart.util.*

class UpdateService extends Service {

    public static final String[] CHECK_UPDATE = ["check-updates.sh"]
    public static final String[] DO_UPDATE = ["git", "pull"]
    
    def config = Gart.CONFIG.update
    def timer = new TimeTool()

    public UpdateService(){
        super("update", true)
    }
    
    def checkForUpdates = {
        def checkForUpdate = new CommandExecutor("$name check", CHECK_UPDATE)
        checkForUpdate.setWorkingDir(Gart.GART_HOME)
        checkForUpdate.suppressOutput = true

        Gart.LOG.info "Checking for updates @ ${new Date()}..."
        if(checkForUpdate.exec() == 0){
            Gart.LOG.info "Updates available"
            if(config && config.auto)
                update()
        }else{
            Gart.LOG.info "No updates are available"
        }
    }

    def update = {
        def merge = 
            new CommandExecutor("perform $name", DO_UPDATE)
        merge.setWorkingDir(Gart.GART_HOME)
        Gart.LOG.info "Updating..."
            
        if(merge.exec() == 0){
            Gart.LOG.info "Gart updated"
            if(config && config.restartOnUpdate)
                Gart.restart()
        }else{
            Gart.LOG.error "Failed to update gart!"
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
