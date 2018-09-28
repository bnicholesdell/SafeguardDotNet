package com.oneidentity.safeguard.safeguardclient.restclient;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

public class SimpleLoggingFilter implements ClientRequestFilter {
    private static final Logger LOG = Logger.getLogger(SimpleLoggingFilter.class.getName());

    @Override
    public void filter(ClientRequestContext crc) throws IOException {
        LOG.log(Level.INFO, crc.getEntity().toString()); // you can configure logging level here
    }
}
