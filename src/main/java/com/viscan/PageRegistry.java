package com.viscan;

import java.util.HashMap;
import java.util.Map;

public class PageRegistry {
    private static final Map<String, CommandPage> pageMap = new HashMap<>();

    static {
        pageMap.put("config", new CommandPage("config.fxml", "config"));
        pageMap.put("status", new CommandPage("status.fxml", "status"));
        pageMap.put("install", new CommandPage("install.fxml", "install"));

        pageMap.put("fastp", new CommandPage("fastp.fxml", "fastp"));
        pageMap.put("bowtie2", new CommandPage("bowtie2.fxml", "bowtie2"));
        pageMap.put("k2-classify", new CommandPage("k2-classify.fxml", "k2-classify"));
        pageMap.put("rcf", new CommandPage("rcf.fxml", "rcf"));
        pageMap.put("k2-download-library", new CommandPage("k2-download-library.fxml", "k2-download-library"));
        pageMap.put("k2-download-taxonomy", new CommandPage("k2-download-taxonomy.fxml", "k2-download-taxonomy"));
        pageMap.put("k2-build", new CommandPage("k2-build.fxml", "k2-build"));

        pageMap.put("flow1", new CommandPage("flow1.fxml", "flow1"));

    }

    public static CommandPage getPageById(String id) {
        return pageMap.get(id);
    }
}
