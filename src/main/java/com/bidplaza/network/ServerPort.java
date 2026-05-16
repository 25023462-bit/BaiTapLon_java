package com.bidplaza.network;

public final class ServerPort {
    public static final int DEFAULT = 8080;

    private ServerPort() {}

    public static int get() {
        String portStr = System.getProperty("bidplaza.port");
        if (portStr != null) {
            try {
                return Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        return DEFAULT;
    }
}
