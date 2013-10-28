package bot.util

import bot.comm.Communicator

import groovy.transform.AutoClone

/**
 * An object that can be manipulated using the commex.
 */
@AutoClone
class SmartObject extends Communicator {
   
    public static final ON_ARGS = "on.arguments"
    public static final ON_COMPLETE = "on.complete"

    def prefix
    def ident
    def properties = [:]

    public SmartObject(id, commPrefix="obj"){
        super({ data ->
            data[0].on(data[1])
        })
        prefix = commPrefix
        ident = id
        subscribeTo("${commPrefix}.${id}")
    }

    public final void on(comm){
        LOG.debug "$this $comm"

        // Work out what to call and what to pass in as an arg
        def names = comm.id.tokenize(".")
        def callName = "on${names[0][0].toUpperCase()}${names[0].substring(1)}"
        def args = comm.get(ON_ARGS)
        if(names.size() > 1) args << names

        // Call the method
        def res = this."$callName"(*args)

        // Call an oncomplete callback if there is one
        if(comm.get(ON_COMPLETE))
            comm.get(ON_COMPLETE)(res, this)
    }

    def onRead(propertiesNames){
        def res = [:]
        propertiesNames.each { 
            res << [ "$it": this.properties[it] ]
        }
        return res
    }

    def onUpdate(newProperties){
        this.properties << newProperties 
    }
        
    def onClone(newName){
        def clone = clone()
        clone.prefix = prefix
        clone.ident = newName
        clone.subscribeTo("${prefix}.${newName}")
        return clone
    }
}
