package com.viscan.path;

import java.util.Locale;
import java.util.regex.Pattern;

public record WindowsPath(String value) {

    private static final Pattern WINDOWS_PATH = Pattern.compile("^[a-zA-Z]:\\\\.*");

    public WindowsPath {
        if (value == null) {
            throw new IllegalArgumentException("WindowsPath value cannot be null");
        }
        value = value.replace('/', '\\');
        if (!WINDOWS_PATH.matcher(value).matches()) {
            throw new IllegalArgumentException("Not a valid Windows path: " + value);
        }
    }

    public static WindowsPath fromString(String str) {
        return new WindowsPath(str);
    }

    public LinuxPath toMnt() {
        String p = value;
        String drive = p.substring(0, 1).toLowerCase(Locale.ROOT);
        String rest = p.substring(2).replace('\\', '/');
        String linux = "/mnt/" + drive + "/" + (rest.startsWith("/") ? rest.substring(1) : rest);
        return new LinuxPath(linux);
    }

    public WindowsPath resolve(String child) {
        String sep = value.endsWith("\\") ? "" : "\\";
        return new WindowsPath(value + sep + child.replace('/', '\\'));
    }

    public String parent() {
        int lastSep = value.lastIndexOf('\\');
        if (lastSep <= 2) return value;
        return value.substring(0, lastSep);
    }

    public String fileName() {
        int lastSep = value.lastIndexOf('\\');
        if (lastSep < 0) return value;
        return value.substring(lastSep + 1);
    }

    @Override
    public String toString() {
        return value;
    }
}
