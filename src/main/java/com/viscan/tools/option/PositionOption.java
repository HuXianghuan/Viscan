package com.viscan.tools.option;

import java.util.Optional;

public class PositionOption implements ToolOption{

    private final int index;
    private final String value;


    public PositionOption(int index, String value) {
        this.index = index;
        this.value = value;
    }

    @Override
    public Optional<String> asArgument() {
        if (value == null || value.isBlank()) return Optional.empty();
        return Optional.of(value);
    }

    @Override
    public int order() {
        return index;
    }
}
