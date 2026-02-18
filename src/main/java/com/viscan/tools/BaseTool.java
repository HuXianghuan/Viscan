package com.viscan.tools;

import com.viscan.ConfigManager;
import com.viscan.tools.option.ToolOption;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BaseTool {


    private final List<ToolOption> options = new ArrayList<>();

    public BaseTool addOption(ToolOption option) {
        options.add(option);
        return this;
    }


    public String buildCommandPretty(String toolExecutable) {
        return toolExecutable + " \\\n" +
                options.stream()
                        .sorted(Comparator.comparingInt(ToolOption::order))
                        .map(ToolOption::asArgument)
                        .flatMap(Optional::stream)
                        .collect(Collectors.joining(" \\\n"));
    }


    public String buildCommand(String toolExecutable) {
        return toolExecutable + " " +
                options.stream()
                        .sorted(Comparator.comparingInt(ToolOption::order))
                        .map(ToolOption::asArgument)
                        .flatMap(Optional::stream)
                        .collect(Collectors.joining(" "));
    }


}
