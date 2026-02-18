package com.viscan;

public class BioConfig {
    private String wslExecutable = "wsl";
    private String fastpExecutableDir = "";

    private String bowtie2ExecutableDir = "";

    private String kraken2ExcutableDir = "";

    private String recentrifugeExecutableDir = "";

    private String workDir = System.getProperty("user.home");

    private String threadNumber = String.valueOf(Runtime.getRuntime().availableProcessors()); //get max threads count

    private String fastpCleanTag = "clean";

    private String bowtie2UnalignTag = "unalign";
    private String bowtie2AlignTag = "align";

    private String kraken2ClassifiedTag = "klassify";
    private String kraken2UnclassifiedTag = "unklassify";
    private String kraken2ReportTag = "kreport";

    private String wslDistro; //todo

    public String getWslExecutable() {
        return wslExecutable;
    }

    public void setWslExecutable(String wslExecutable) {
        this.wslExecutable = wslExecutable;
    }

    public String getFastpExecutableDir() {
        return fastpExecutableDir;
    }

    public void setFastpExecutableDir(String fastpExecutable) {
        this.fastpExecutableDir = fastpExecutable;
    }

    public String getWorkDir() {
        return workDir;
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    public String getThreadNumber() {
        return threadNumber;
    }

    public void setThreadNumber(String threadNumber) {
        this.threadNumber = threadNumber;
    }


    public String getFastpCleanTag() {
        return fastpCleanTag;
    }

    public void setFastpCleanTag(String fastpCleanTag) {
        this.fastpCleanTag = fastpCleanTag;
    }

    public String getBowtie2UnalignTag() {
        return bowtie2UnalignTag;
    }

    public void setBowtie2UnalignTag(String bowtie2UnalignTag) {
        this.bowtie2UnalignTag = bowtie2UnalignTag;
    }

    public String getBowtie2AlignTag() {
        return bowtie2AlignTag;
    }

    public void setBowtie2AlignTag(String bowtie2AlignTag) {
        this.bowtie2AlignTag = bowtie2AlignTag;
    }

    public String getWslDistro() {
        if (wslDistro == null || wslDistro.isBlank()) {
            wslDistro = detectDefaultWslDistro();
        }
        return wslDistro;
    }

    public void setWslDistro(String wslDistro) {
        this.wslDistro = wslDistro;
    }

    public String getBowtie2ExecutableDir() {
        return bowtie2ExecutableDir;
    }

    public void setBowtie2ExecutableDir(String bowtie2ExecutableDir) {
        this.bowtie2ExecutableDir = bowtie2ExecutableDir;
    }

    public String getKraken2ExcutableDir() {
        return kraken2ExcutableDir;
    }

    public void setKraken2ExcutableDir(String kraken2ExcutableDir) {
        this.kraken2ExcutableDir = kraken2ExcutableDir;
    }

    public String getKraken2ClassifiedTag() {
        return kraken2ClassifiedTag;
    }

    public void setKraken2ClassifiedTag(String kraken2ClassifiedTag) {
        this.kraken2ClassifiedTag = kraken2ClassifiedTag;
    }

    public String getKraken2UnclassifiedTag() {
        return kraken2UnclassifiedTag;
    }

    public void setKraken2UnclassifiedTag(String kraken2UnclassifiedTag) {
        this.kraken2UnclassifiedTag = kraken2UnclassifiedTag;
    }

    public String getKraken2ReportTag() {
        return kraken2ReportTag;
    }

    public void setKraken2ReportTag(String kraken2ReportTag) {
        this.kraken2ReportTag = kraken2ReportTag;
    }


    public String getRecentrifugeExecutableDir() {
        return recentrifugeExecutableDir;
    }

    public void setRecentrifugeExecutableDir(String recentrifugeExecutableDir) {
        this.recentrifugeExecutableDir = recentrifugeExecutableDir;
    }



    private String detectDefaultWslDistro() {
        try {
            Process process = new ProcessBuilder(
                    wslExecutable, "-l", "-q"
            ).redirectErrorStream(true).start();

            try (var reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream())
            )) {
                return reader.lines()
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .findFirst()
                        .orElse(null);
            }
        } catch (Exception e) {
            return null;
        }
    }


}
