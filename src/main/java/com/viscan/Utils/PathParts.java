package com.viscan.Utils;

import com.viscan.path.LinuxPath;
import com.viscan.path.WindowsPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PathParts {

    private final LinuxPath parent;
    private final List<String> nameParts;
    private final String suffix;

    public PathParts(LinuxPath parent, List<String> nameParts, String suffix) {
        this.parent = parent;
        this.nameParts = nameParts;
        this.suffix = suffix;
    }

    public static PathParts fromWindowsPath(WindowsPath winPath) {
        LinuxPath linuxPath = winPath.toMnt();
        return fromLinuxPath(linuxPath);
    }

    public static PathParts fromLinuxPath(LinuxPath linuxPath) {
        String value = linuxPath.value();
        String fileName = linuxPath.fileName();
        int lastDot = fileName.lastIndexOf('.');

        if (lastDot <= 0 || lastDot == fileName.length() - 1) {
            throw new IllegalArgumentException("Invalid file name: " + fileName);
        }

        String base = fileName.substring(0, lastDot);
        String suffix = fileName.substring(lastDot + 1);

        List<String> parts = new ArrayList<>(
                Arrays.asList(base.split("\\."))
        );

        LinuxPath parentPath = new LinuxPath(linuxPath.parent());
        return new PathParts(parentPath, parts, suffix);
    }

    public static PathParts parse(String str) {
        if (WslPathConverter.isWindowsPath(str)) {
            return fromWindowsPath(new WindowsPath(str));
        }
        return fromLinuxPath(new LinuxPath(str));
    }

    public LinuxPath getParent() {
        return parent;
    }

    public String getLinuxPath() {
        return parent.resolve(getFileName()).toString();
    }

    public List<String> getNameParts() {
        return nameParts;
    }

    public List<String> getTagParts() {
        List<String> tags = new ArrayList<>(nameParts);
        tags.remove(0);
        return tags;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getFileName() {
        return String.join(".", nameParts) + "." + suffix;
    }

    public PathParts withParent(LinuxPath newParent) {
        return new PathParts(newParent, new ArrayList<>(nameParts), suffix);
    }

    public PathParts copy() {
        return new PathParts(parent, new ArrayList<>(nameParts), suffix);
    }

    public static List<String> addTagToParts(List<String> parts, String newTag) {
        List<String> newParts = new ArrayList<>(parts);
        newParts.add(newTag);
        return newParts;
    }

}
