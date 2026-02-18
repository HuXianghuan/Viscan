package com.viscan.validate;

import java.util.List;

public class FastpValidator {

    public static ValidationResult validate(
            boolean isPaired,
            List<String> inputs,
            String outputDir,
            String threadText
    ) {
        if (inputs.isEmpty()) {
            return ValidationResult.error(ValidationMessages.INPUT_REQUIRED);
        }

        if (isPaired && inputs.size() < 2) {
            return ValidationResult.error(ValidationMessages.PAIRED_INPUT_REQUIRED);
        }

        if (outputDir == null || outputDir.isBlank()) {
            return ValidationResult.error(ValidationMessages.OUTPUT_DIR_REQUIRED);
        }

        try {
            int t = Integer.parseInt(threadText);
            if (t <= 0) {
                return ValidationResult.error(ValidationMessages.THREAD_INVALID);
            }
        } catch (NumberFormatException e) {
            return ValidationResult.error(ValidationMessages.THREAD_NOT_INTEGER);
        }

        return ValidationResult.ok();
    }
}
