package com.home.facevpn.vpn;

import java.io.IOException;

public interface VpnTunnel {
    void up() throws IOException, InterruptedException;
    void down() throws IOException, InterruptedException;

    boolean isActive();
}
