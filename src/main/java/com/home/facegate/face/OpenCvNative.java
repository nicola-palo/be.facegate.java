package com.home.facegate.face;

public final class OpenCvNative {

    private static boolean loaded = false;

    private OpenCvNative() {
    }

    public static synchronized void load() {
        if (!loaded) {
            nu.pattern.OpenCV.loadLocally();
            loaded = true;
        }
    }
}
