package bot.ops

import bot.Bot

@Grab(group='org.twitter4j', module='twitter4j-core', version='3.+')
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
    Bot.LOG.error "No auth found. Cannot tweet!"
}else{
    Bot.LOG.info "Tweeting $tweet"        
    twitter.updateStatus(tweet.toString())
}
