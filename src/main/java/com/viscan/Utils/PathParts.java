package com.viscan.Utils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PathParts {

    private Path parent;
    private List<String> nameParts;
    private String suffix;


    public PathParts(Path parent, List<String> nameParts, String suffix) {
        this.parent = parent;
        this.nameParts = nameParts;
        this.suffix = suffix;
    }

    public static PathParts parse(String str) {
        Path path = Path.of(str);

        String fileName = path.getFileName().toString();
        int lastDot = fileName.lastIndexOf('.');

        if (lastDot <= 0 || lastDot == fileName.length() - 1) {
            throw new IllegalArgumentException("Invalid file name: " + fileName);
        }

        String base = fileName.substring(0, lastDot);
        String suffix = fileName.substring(lastDot + 1);

        List<String> parts = new ArrayList<>(
                Arrays.asList(base.split("\\."))
        );

        return new PathParts(path.getParent(), parts, suffix);
    }

    public Path getParent() {
        return parent;
    }

    public String getLinuxParent() {
        return WslPathConverter.toLinuxString(parent);
    }
    public String getLinuxPath() {
        return WslPathConverter.toLinuxString(parent.resolve(getFileName()));
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

    public void setParent(Path parent) {
        this.parent = parent;
    }

    public void setNameParts(List<String> nameParts) {
        this.nameParts = nameParts;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
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

