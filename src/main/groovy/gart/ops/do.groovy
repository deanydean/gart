#!/usr/bin/groovy
// Run a command from the path
package gart.ops

import gart.util.CommandExecutor

def cmd = new CommandExecutor("Doing $args", args)
cmd.inheritEnv()

LOG.info "Doing ${args}... "
cmd.exec()
