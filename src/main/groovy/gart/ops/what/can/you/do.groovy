#!/usr/bin/groovy
// Script that lists what Gart can do
package gart.ops.what.can.you

import groovy.io.FileType

// Find all the ops
def ops = []
if ( GART.GART_PATH )
{
    GART.GART_PATH.tokenize(":").each { path ->
        def opsDir = new File(path+"/ops")

        if ( opsDir.exists() )
        {
            opsDir.eachFileRecurse ( FileType.FILES ) {
                ops << "$it".minus("$opsDir/")
                            .minus(".groovy")
                            .replaceAll("/", " ")
            }
        }

        def scriptsDir = new File(path+"/scripts")

        if ( scriptsDir.exists() )
        {
            scriptsDir.eachFileRecurse ( FileType.FILES ) {
                ops << "$it".minus("$scriptsDir/")
                            .minus(".groovy")
                            .minus(".sh")
                            .replaceAll("-", " ")
                            .replaceAll("/", " ")
            }
        }
    }
}

// Sort and display
LOG.info "Available ops:"
ops.sort().unique().each { LOG.info "    $it" }

