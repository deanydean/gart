#!/usr/bin/groovy
// Greet a user

def rng = new Random()
def greets = [
    "What?",
    "What do you want?",
    "Hi! Im ${GART.CONFIG.id.capitalize()}. How can I help you?", 
    "Whhhaasssaaaappppp?!",
    "Hey hey hey"
]

LOG.info "${greets[rng.nextInt(greets.size())]}"
