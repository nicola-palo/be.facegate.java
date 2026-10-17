package com.home.facevpn.vpn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FakeTunnel implements VpnTunnel {

    private static final Logger log = LoggerFactory.getLogger(FakeTunnel.class);

    private boolean active;

    @Override
    public void up() {
        active = true;
        log.info("[FAKE TUNNEL] up");
    }

    @Override
    public void down() {
        active = false;
        log.info("[FAKE TUNNEL] down");
    }

    @Override
    public boolean isActive() {
        return active;
    }
}
