package com.viscan.validate;

import java.util.regex.Pattern;

public class ConfigValidator {

    public static ValidationResult validate(
            String wslExecutable,
            String fastpExecutable,
            String bowtie2Dir,
            String kraken2Dir,
            String recentrifugeDir,
            String workDir,
            String defaultThreads,
            String tagFastpClean,
            String tagBowtie2Unalign,
            String tagBowtie2Align,
            String tagKraken2Report,
            String tagKraken2Classified,
            String tagKraken2Unclassified,
            String wslDistro
    ) {
        //WSL executable
        if (wslExecutable == null || wslExecutable.isBlank()) {
            return ValidationResult.error("WSL executable cannot be empty.");
        }

        if (!isValidWindowsPath(wslExecutable) && !wslExecutable.equals("wsl")) {
            return ValidationResult.error("Invalid Windows path: " + wslExecutable);
        }

        //WSL distro
        if (wslDistro == null || wslDistro.isBlank()) {
            return ValidationResult.error("WSL distro cannot be empty.");
        }

        //Linux executables / directories
        String[] linuxPaths = {fastpExecutable, bowtie2Dir, kraken2Dir, recentrifugeDir};
        String[] linuxNames = {"fastp", "Bowtie2", "Kraken2", "Recentrifuge"};
        for (int i = 0; i < linuxPaths.length; i++) {
            if (linuxPaths[i] == null || linuxPaths[i].isBlank()) {
                return ValidationResult.error(linuxNames[i] + " path cannot be empty.");
            }
            if (!isValidLinuxPath(linuxPaths[i])) {
                return ValidationResult.error("Invalid Linux path for " + linuxNames[i] + ": " + linuxPaths[i]);
            }
        }

        //Work directory (Windows path)
        if (workDir == null || workDir.isBlank()) {
            return ValidationResult.error("Work directory cannot be empty.");
        }
        if (!isValidWindowsPath(workDir)) {
            return ValidationResult.error("Invalid Windows path: " + workDir);
        }

        //Default threads
        try {
            int t = Integer.parseInt(defaultThreads);
            if (t <= 0) {
                return ValidationResult.error("Default threads must be a positive integer.");
            }
        } catch (NumberFormatException e) {
            return ValidationResult.error("Default threads must be an integer.");
        }

        //Tags
        String[] tags = {tagFastpClean, tagBowtie2Unalign, tagBowtie2Align,
                tagKraken2Report, tagKraken2Classified, tagKraken2Unclassified};
        String[] tagNames = {"Fastp clean tag", "Bowtie2 unalign tag", "Bowtie2 align tag",
                "Kraken2 report tag", "Kraken2 classified tag", "Kraken2 unclassified tag"};
        for (int i = 0; i < tags.length; i++) {
            if (tags[i] == null || tags[i].isBlank()) {
                return ValidationResult.error(tagNames[i] + " cannot be empty.");
            }
            if (!tags[i].matches("[A-Za-z0-9._-]+")) {
                return ValidationResult.error(tagNames[i] + " contains illegal characters: " + tags[i]);
            }
        }

        return ValidationResult.ok();
    }








    private static final Pattern WIN_DRIVE =
            Pattern.compile("^[a-zA-Z]:[\\\\/].+");

    // \\server\share\xxx
    private static final Pattern WIN_UNC =
            Pattern.compile("^\\\\\\\\[^\\\\/]+[\\\\/][^\\\\/]+[\\\\/]?.*");

    public static boolean isValidWindowsPath(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        return WIN_DRIVE.matcher(path).matches()
                || WIN_UNC.matcher(path).matches();
    }

    private static final Pattern LINUX_ROOT =
            Pattern.compile("^/[^\\s]*");

    // /mnt/c/xxx
    private static final Pattern WSL_MNT =
            Pattern.compile("^/mnt/[a-zA-Z]/[^\\s]*");

    public static boolean isValidLinuxPath(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        return LINUX_ROOT.matcher(path).matches()
                || WSL_MNT.matcher(path).matches();
    }
}
