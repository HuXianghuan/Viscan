package com.viscan.tools;

import java.util.ArrayList;
import java.util.List;

public class CommandPipeline {
    private final List<String> commands = new ArrayList<>();

    public CommandPipeline add(String command) {
        commands.add(command);
        return this;
    }

    public String build() {
        return String.join(" \\\n&& \\\n", commands);
    }
}
