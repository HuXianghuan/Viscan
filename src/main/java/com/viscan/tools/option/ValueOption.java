package com.viscan.tools.option;

import java.util.Optional;

public class ValueOption implements ToolOption{

    private final String key;
    private final String value;

    public ValueOption(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public Optional<String> asArgument() {
        if (value == null || value.isBlank()) return Optional.empty();
        return Optional.of(key + " " +  value);

    }
}
