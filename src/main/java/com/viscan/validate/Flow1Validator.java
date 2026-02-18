package com.viscan.validate;

import java.util.List;

public class Flow1Validator {

    public static ValidationResult validate(
            boolean isPaired,
            List<List<String>> caseFiles,
            List<List<String>> controlFiles,

            String projectName,
            String projectDir,
            String threads,

            String krakenHostDb,
            String krakenHostConfidence,
            String krakenPathogenDb,
            String krakenPathogenConfidence,

            String rcfTaxdumpDir,
            String rcfScoreMethod,
            String rcfMinScore,
            String rcfControlMinScore
    ) {

        if (projectName.isBlank()) {
            return ValidationResult.error("Project name cannot be empty.");
        }

        if (projectDir.isBlank()) {
            return ValidationResult.error("Project directory cannot be empty.");
        }

        if (caseFiles == null || caseFiles.isEmpty()) {
            return ValidationResult.error("Case files are required.");
        }


        if (!validateFileGroups(caseFiles, isPaired, "Case")) {
            return ValidationResult.error("Invalid case files for paired/single mode.");
        }

        if (!validateFileGroups(controlFiles, isPaired, "Control")) {
            return ValidationResult.error("Invalid control files for paired/single mode.");
        }

        try {
            int t = Integer.parseInt(threads);
            if (t <= 0) {
                return ValidationResult.error("Threads must be a positive integer.");
            }
        } catch (NumberFormatException e) {
            return ValidationResult.error("Threads must be an integer.");
        }


        //kraken

        if (krakenHostDb == null || krakenHostDb.isBlank()) {
            return ValidationResult.error("Kraken host database cannot be empty.");
        }

        if (krakenPathogenDb == null || krakenPathogenDb.isBlank()) {
            return ValidationResult.error("Kraken pathogen database cannot be empty.");
        }

        if (!isValidConfidence(krakenHostConfidence)) {
            return ValidationResult.error("Kraken host confidence must be a number between 0 and 1.");
        }

        if (!isValidConfidence(krakenPathogenConfidence)) {
            return ValidationResult.error("Kraken pathogen confidence must be a number between 0 and 1.");
        }

        //recentrifuge

        if (rcfTaxdumpDir == null || rcfTaxdumpDir.isBlank()) {
            return ValidationResult.error("Recentrifuge taxdump directory cannot be empty.");
        }

        boolean hasScore = !isBlank(rcfMinScore) || !isBlank(rcfControlMinScore);

        if (hasScore && isBlank(rcfScoreMethod)) {
            return ValidationResult.error(
                    "Recentrifuge score method must be specified when score is provided."
            );
        }

        if (!isBlank(rcfMinScore) && !isNumeric(rcfMinScore)) {
            return ValidationResult.error(
                    "Recentrifuge min score must be numeric."
            );
        }

        if (!isBlank(rcfControlMinScore) && !isNumeric(rcfControlMinScore)) {
            return ValidationResult.error(
                    "Recentrifuge control min score must be numeric."
            );
        }


        return ValidationResult.ok();






    }




    private static boolean validateFileGroups(
            List<List<String>> groups,
            boolean isPaired,
            String label
    ) {
        for (List<String> group : groups) {
            if (group == null || group.isEmpty()) {
                return false;
            }

            if (isPaired) {
                if (group.size() < 2) {
                    return false;
                }
                if (isBlank(group.get(0)) || isBlank(group.get(1))) {
                    return false;
                }
            } else {
                if (isBlank(group.get(0))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isValidConfidence(String value) {
        try {
            double d = Double.parseDouble(value);
            return d >= 0.0 && d <= 1.0;
        } catch (NumberFormatException e) {
            return false;
        }
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
