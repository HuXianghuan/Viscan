package com.viscan.validate;

import java.util.List;


public class RcfValidator {

    public static ValidationResult validate(
            List<String> caseInput,
            String taxdumpDir,
            String outputDir,
            String outputPrefix,
            String scoreMethod,
            String minScore,
            String controlMinScore
    ) {
        if (taxdumpDir == null || taxdumpDir.isBlank()) {
            return ValidationResult.error("Nodes information directory must not be empty.");
        }

        if (caseInput.isEmpty()) {
            return ValidationResult.error("At least one case input file is required.");
        }

        if (outputDir == null || outputDir.isBlank()) {
            return ValidationResult.error(ValidationMessages.OUTPUT_DIR_REQUIRED);
        }

        if (outputPrefix == null || outputPrefix.isBlank()) {
            return ValidationResult.error("Output prefix must not be empty.");
        }

        boolean hasScore = !isBlank(minScore) || !isBlank(controlMinScore);

        if (hasScore && isBlank(scoreMethod)) {
            return ValidationResult.error(
                    "Recentrifuge score method must be specified when score is provided."
            );
        }

        if (!isBlank(minScore) && !isNumeric(minScore)) {
            return ValidationResult.error(
                    "Recentrifuge min score must be numeric."
            );
        }

        if (!isBlank(controlMinScore) && !isNumeric(controlMinScore)) {
            return ValidationResult.error(
                    "Recentrifuge control min score must be numeric."
            );
        }



        return ValidationResult.ok();
    }


    private static boolean isNumeric(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
