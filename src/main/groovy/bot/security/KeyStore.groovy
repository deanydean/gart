package bot.security

import bot.Bot

import java.security.*

class KeyStore {

    def keyFile
    def keyStore
    def storePass

    public KeyStore(file, password){
        this.keyFile = new File(file)
        this.storePass = password
        this.keyStore = java.security.KeyStore.getInstance(
            java.security.KeyStore.getDefaultType())
        
        // Load the keystore
        def fileInput = this.keyFile.exists() ? 
            new FileInputStream(this.keyFile) : null

        try{
            this.keyStore.load(fileInput, this.storePass)
        }finally{
            if(fileInput) fileInput.close()
        }

        Bot.LOG.info "Loaded keystore from file ${this.keyFile}"
    }

    /**
     * Save the keystore to the file.
     * This should be run after all operations that change the file
     */
    private save(){
        def fileOutput = new FileOutputStream(this.keyFile)
        try{
            this.keyStore.store(fileOutput, this.storePass)
        }finally{
            fileOutput.close()
        }
    }

}
