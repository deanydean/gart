package bot.ops

import bot.Bot
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
