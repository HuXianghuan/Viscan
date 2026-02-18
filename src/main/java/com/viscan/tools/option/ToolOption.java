package com.viscan.tools.option;

import java.util.Optional;

public interface ToolOption {
    Optional<String> asArgument();


    default int order() {
        return -1; //ordinary option lie first in default
    }
}
