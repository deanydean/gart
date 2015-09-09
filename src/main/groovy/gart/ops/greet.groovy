#!/usr/bin/groovy
// Greet a user

def rng = new Random()
def greets = [
    "What?",
    "What do you want?",
    "Hi! I am ${GART.CONFIG.id.capitalize()}. How can I help you?", 
    "Whhhaasssaaaappppp?!",
    "Hey hey hey",
    "Ey up",
    "Now then",
    "Alright?",
    "What's going down?",
    "To what do I owe this pleasure?",
    "What did you do?",
    "What is it now?",
    "What!? What do you want?!",
    "Que?"
]

LOG.info "${greets[rng.nextInt(greets.size())]}"
