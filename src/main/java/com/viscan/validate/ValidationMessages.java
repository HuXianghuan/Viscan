package com.viscan.validate;

public final class ValidationMessages {

    private ValidationMessages() {}

    public static final String INPUT_REQUIRED =
            "At least one input file is required.";

    public static final String PAIRED_INPUT_REQUIRED =
            "Paired-end mode requires two input files.";

    public static final String OUTPUT_DIR_REQUIRED =
            "Output directory must not be empty.";

    public static final String THREAD_NOT_INTEGER =
            "Thread count must be an integer.";

    public static final String THREAD_INVALID =
            "Thread count must be greater than 0.";
}
