package com.viscan.validate;

public class K2BuildValidator {
    public static ValidationResult validate(
            String databaseDir,
            String threadText
    ) {
        if (databaseDir == null || databaseDir.isBlank()) {
            return ValidationResult.error("Target database directory must not be empty.");
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
