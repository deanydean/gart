#!/opt/groovy/bin/groovy
// Get something from a URL
package gart.ops

@Grab("org.codehaus.groovy.modules.http-builder:http-builder:0.7")
import groovyx.net.http.HTTPBuilder

def url = "${args[0]}".toURL()
def outfile = (args.size() > 1) ? args[1] : ".${url.file}"

if(!outfile.isEmpty() && outfile != "-"){
    // Write the url to the file
    def file = new File(outfile).newOutputStream()
    file << url.openStream()
    file.close()
}else{
    // Return the stream
    return url.openStream()
}
