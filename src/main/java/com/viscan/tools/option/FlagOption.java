package com.viscan.tools.option;

import javax.swing.text.html.Option;
import java.util.Optional;

public class FlagOption implements ToolOption {

    private final String key;
    private final boolean enabled;

    public FlagOption(String key, boolean enabled) {
        this.key = key;
        this.enabled = enabled;
    }

    @Override
    public Optional<String> asArgument() {
        return enabled ? Optional.of(key) : Optional.empty();
    }
}
