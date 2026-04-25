package com.viscan;

import com.viscan.Utils.PathUtils;
import com.viscan.Utils.WslPathConverter;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

public class FileItem {
    private String winPath;
    private String linuxPath;
    private boolean isDir;

    private String name;

    private boolean isHeader;

    private boolean collapsed;

    private long fileSize;
    private long lastModified;


    public FileItem(String winPath, String linuxPath, boolean isDir, String name, long fileSize, long lastModified, boolean isHeader, boolean collapsed) {
        this.winPath = winPath;
        this.linuxPath = linuxPath;
        this.isDir = isDir;
        this.name = name;
        this.fileSize = fileSize;
        this.lastModified = lastModified;
        this.isHeader = isHeader;
        this.collapsed = collapsed;
    }


    public FileItem() {
    }

    public String getWinPath() {
        return winPath;
    }

    public void setWinPath(String winPath) {
        this.winPath = winPath;
    }

    public String getLinuxPath() {
        if (linuxPath == null && winPath != null) {
            linuxPath = WslPathConverter.autoToLinuxString(winPath);
        }
        return linuxPath;
    }

    public void setLinuxPath(String linuxPath) {
        this.linuxPath = linuxPath;
    }

    public boolean isDir() {
        return isDir;
    }

    public void setDir(boolean dir) {
        isDir = dir;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public boolean isHeader() {
        return isHeader;
    }

    public void setHeader(boolean header) {
        isHeader = header;
    }


    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public static FileItem fromWinPath(File winFile) {
        Objects.requireNonNull(winFile, "file cannot be null");

        Path winPathPath = winFile.toPath();



        String winPath = WslPathConverter.toWindowsString(winPathPath);
        String linuxPath = WslPathConverter.autoToLinuxString(winPath);

        long fileSize = winFile.length();
        long lastModified = winFile.lastModified();

        return new FileItem(winPath, linuxPath, PathUtils.isDir(winPathPath), PathUtils.getName(winPathPath), fileSize, lastModified, false, false);
    }




    public static FileItem header(String title) {
        FileItem f = new FileItem(null, null, false, title, 0, 0,true, false);
        return f;
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }

    @Override
    public String toString() {
        return name;
    }






}
