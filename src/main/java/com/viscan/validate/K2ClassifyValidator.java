package com.viscan.validate;

import java.util.List;

public class K2ClassifyValidator {
    public static ValidationResult validate(
            boolean isPaired,
            List<String> inputs,
            String databaseDir,
            String outputDir,

//            boolean demandClassification,
//            boolean demandReport,

            String threadText

//            boolean memoryMappingMode,
//            boolean quickMode,
//            boolean useNamesMode

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

        if (databaseDir == null || databaseDir.isBlank()) {
            return ValidationResult.error("Database directory must not be empty.");
        }

        try {
            int t = Integer.parseInt(threadText);
            if (t <= 0) {
                return ValidationResult.error(ValidationMessages.THREAD_INVALID);
            }
        } catch (NumberFormatException e) {
            return ValidationResult.error(ValidationMessages.THREAD_NOT_INTEGER);
        }

//        if (!demandClassification && !demandReport) {
//            return ValidationResult.error("At least one output file option must be selected.");
//        }

        return ValidationResult.ok();
    }
}
