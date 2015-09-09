#!/usr/bin/groovy
// Start the GART www service

LOG.debug "CONFIG: ${GART.CONFIG}"

LOG.info "Starting shells www service..."
GART.comm("srv.loader.start", {
    LOG.info "Services loaded"

    // Start www service
    GART.comm("srv.www.start", {
        LOG.info "www service started"
    })
})

// Run forever
GART.join()
