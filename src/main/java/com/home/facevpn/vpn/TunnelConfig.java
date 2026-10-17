package com.home.facevpn.vpn;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TunnelConfig {

    @Bean
    public VpnTunnel vpnTunnel(
            @Value("${vpn.tunnel.mode:fake}") String mode,
            @Value("${vpn.tunnel.interface:wg0}") String interfaceName) {
        return switch (mode.toLowerCase()) {
            case "fake" -> new FakeTunnel();
            case "wireguard" -> new WireGuardTunnel(interfaceName);
            default -> throw new IllegalStateException(
                    "Unknown tunnel mode: " + mode + " (use fake or wireguard)");
        };
    }
}
