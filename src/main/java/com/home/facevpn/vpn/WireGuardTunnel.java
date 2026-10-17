package com.home.facevpn.vpn;

import java.io.IOException;
import java.util.StringJoiner;

public class WireGuardTunnel implements VpnTunnel{

    private final String interfaceName;

    public WireGuardTunnel(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    @Override
    public void up() throws IOException, InterruptedException{
        run("wg-quick", "down", interfaceName);
    }

    @Override
    public void down() throws IOException, InterruptedException {
        run("wg-quick", "down", interfaceName);
    }

    @Override
    public boolean isActive() {
        try {
            Process p = new ProcessBuilder("ip", "link", "show", interfaceName)
                    .redirectErrorStream(true)
                    .start();
            return p.waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    private static void run(String... cmd) throws IOException, InterruptedException {
        Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
        String output = new String(p.getInputStream().readAllBytes());
        if (p.waitFor() != 0) {
            StringJoiner joiner = new StringJoiner(" ");
            for (String c : cmd) joiner.add(c);
            throw new IOException("Command fail: " + joiner + "\n" + output);
        }
    }
}
