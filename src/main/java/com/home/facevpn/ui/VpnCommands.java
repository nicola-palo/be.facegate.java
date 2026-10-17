package com.home.facevpn.ui;

import org.springframework.shell.core.command.annotation.Argument;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.stereotype.Component;


@Component
public class VpnCommands {

	private volatile boolean active = false;

	@Command(name = "vpn", description = "Active/Deactive VPN with FaceID")
	public String vpn(
			@Argument(index = 0, description = "on | off | toggle", defaultValue = "toggle") String action) {
		return switch (action.toLowerCase()) {
			case "on" -> turnOn();
			case "off" -> turnOff();
			case "toggle" -> active ? turnOff() : turnOn();
            default -> "Action don't valid: '" + action + "' (use on, off or toggle)";
		};
	}

	private String turnOn() {
		if (active) {
			return "VPN is already active";
		}
		active = true;
		return "VPN is active";
	}

	private String turnOff() {
		if (!active) {
			return "VPN is already deactivate";
		}
		active = false;
		return "VPN is deactivate";
	}
}
