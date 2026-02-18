package com.viscan.tools;

import com.viscan.ConfigManager;
import com.viscan.tools.option.ToolOption;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Bowtie2Tool implements ExternalTool{
    private final List<ToolOption> options = new ArrayList<>();

    public Bowtie2Tool addOption(ToolOption option) {
        options.add(option);
        return this;
    }

    @Override
    public String buildCommand() {
        return ConfigManager.getConfig().getBowtie2ExecutableDir() + " " +
                options.stream()
                        .map(ToolOption::asArgument)
                        .flatMap(Optional::stream)
                        .collect(Collectors.joining(" "));
    }

    public String buildCommandPretty() {
        return ConfigManager.getConfig().getBowtie2ExecutableDir() + " \\\n" +
                options.stream()
                        .map(ToolOption::asArgument)
                        .flatMap(Optional::stream)
                        .collect(Collectors.joining(" \\\n"));
    }
}
