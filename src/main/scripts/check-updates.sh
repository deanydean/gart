#!/bin/bash
# Fetch all remote changes and check if there's anything new

# First fetch
git fetch

# Now check for updates
git log ..@{u} | grep "commit "

# Return code
exit $?
