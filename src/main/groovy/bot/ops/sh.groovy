//
// A shell for the bot...
import bot.Bot
import bot.util.Botsh

def shBindings = [
    "args": args,
    "BOT": BOT
]

// Start the shell
new Botsh(shBindings).run()
