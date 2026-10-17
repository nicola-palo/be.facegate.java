package com.home.facevpn.ui;

import com.home.facevpn.vpn.VpnTunnel;
import org.springframework.shell.core.command.annotation.Argument;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class VpnCommands {

	private final VpnTunnel tunnel;

	public VpnCommands(VpnTunnel tunnel) {
		this.tunnel = tunnel;
	}

	@Command(name = "vpn", description = "Active/Deactive VPN with FaceID")
	public String vpn(
			@Argument(index = 0, description = "on | off | toggle", defaultValue = "toggle") String action) {
		return switch (action.toLowerCase()) {
			case "on" -> turnOn();
			case "off" -> turnOff();
			case "toggle" -> tunnel.isActive() ? turnOff() : turnOn();
            default -> "Action don't valid: '" + action + "' (use on, off or toggle)";
		};
	}

	private String turnOn() {
		if (tunnel.isActive()) {
			return "VPN is already active";
		}
		try {
			tunnel.up();
			return "VPN is active";
		} catch (IOException | InterruptedException e) {
			return "Error with activation VPN: " + e.getMessage();
		}
	}

	private String turnOff() {
		if (!tunnel.isActive()) {
			return "VPN is already inactive";
		}
		try {
			tunnel.down();
			return "VPN is inactive";
		} catch (IOException | InterruptedException e) {
			return "Error with inactive VPN: " + e.getMessage();
		}
	}
}
