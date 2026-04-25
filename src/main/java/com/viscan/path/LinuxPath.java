package com.viscan.path;

import java.util.regex.Pattern;

public record LinuxPath(String value) {

    private static final Pattern LINUX_PATH = Pattern.compile("^/.*");

    public LinuxPath {
        if (value == null) {
            throw new IllegalArgumentException("LinuxPath value cannot be null");
        }
        value = value.replace('\\', '/');
        if (!LINUX_PATH.matcher(value).matches()) {
            throw new IllegalArgumentException("Not a valid Linux path: " + value);
        }
    }

    public static LinuxPath fromString(String str) {
        return new LinuxPath(str);
    }

    public static LinuxPath fromWindows(WindowsPath winPath) {
        return winPath.toMnt();
    }

    public LinuxPath resolve(String child) {
        String sep = value.endsWith("/") ? "" : "/";
        return new LinuxPath(value + sep + child.replace('\\', '/'));
    }

    public String parent() {
        int lastSep = value.lastIndexOf('/');
        if (lastSep <= 0) return "/";
        String p = value.substring(0, lastSep);
        return p.isEmpty() ? "/" : p;
    }

    public String fileName() {
        int lastSep = value.lastIndexOf('/');
        if (lastSep < 0) return value;
        return value.substring(lastSep + 1);
    }

    @Override
    public String toString() {
        return value;
    }
}
