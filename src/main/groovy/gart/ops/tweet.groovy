package gart.ops

import gart.Gart

@Grab("org.twitter4j:twitter4j-core:3.0.5")
import twitter4j.*

/**
 * Tweet.....
 * @author deanydean
 */
// Build the status update...
def tweet = new StringBuilder();
args.each { it ->
    tweet << it << " "
}

// Set up twitter
def twitter = TwitterFactory.getSingleton()
if(!twitter.getAuthorization().isEnabled()){
    Gart.LOG.error "No auth found. Cannot tweet!"
}else{
    Gart.LOG.info "Tweeting $tweet"        
    twitter.updateStatus(tweet.toString())
}
