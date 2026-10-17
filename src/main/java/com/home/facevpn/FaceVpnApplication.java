package com.home.facevpn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.core.command.annotation.EnableCommand;
import com.home.facevpn.ui.VpnCommands;

@SpringBootApplication
@EnableCommand(VpnCommands.class)
public class FaceVpnApplication {

	public static void main(String[] args) {
		SpringApplication.run(FaceVpnApplication.class, args);
	}
	
}
