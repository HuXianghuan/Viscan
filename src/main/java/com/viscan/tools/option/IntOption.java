package com.viscan.tools.option;

import java.util.Optional;

public class IntOption implements ToolOption{
    private final String key;
    private final int value;

    public IntOption(String key, int value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public Optional<String> asArgument() {
        return Optional.of(key + " " + value);
    }
}
