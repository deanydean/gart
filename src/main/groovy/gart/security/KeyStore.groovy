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
package gart.security

import gart.Gart

import java.security.*

class KeyStore {

    def keyGen = KeyPairGenerator.getInstance("RSA", "SUN")
    def rng = SecureRandom.getInstance("SHA1PRNG", "SUN")

    def keyFile
    def keyStore
    def storePass

    public KeyStore(file, password){
        this.keyFile = new File(file)
        this.storePass = password
        this.keyStore = java.security.KeyStore.getInstance(
            java.security.KeyStore.getDefaultType())
       
        this.keyGen.initialize(2048, rng)

        // Load the keystore
        def fileIynput = this.keyFile.exists() ? 
            new FileInputStream(this.keyFile) : null

        try{
            this.keyStore.load(fileInput, this.storePass)
        }finally{
            if(fileInput) fileInput.close()
        }

        Gart.LOG.info "Loaded keystore from file ${this.keyFile}"
    }

    /**
     * Load the main key
     */
    public loadKey(){
        
    }

    public trustCertificate(){
    }

    public untrustCertificate(name){
    }

    /**
     * Save the keystore to the file.
     * This should be run after all operations that change the file
     */
    private save(){
        def fileOutput = new FileOutputStream(this.keyFile)
        try{
            this.keyStore.store(fileOutput, this.storePass)
            Gart.LOG.info "Saved keystore to file ${this.keyFile}"
        }finally{
            fileOutput.close()
        }
    }
}
