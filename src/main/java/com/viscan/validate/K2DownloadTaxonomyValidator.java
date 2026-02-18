package com.viscan.validate;

public class K2DownloadTaxonomyValidator {
    public static ValidationResult validate(
            String databaseDir
    ) {
        if (databaseDir == null || databaseDir.isBlank()) {
            return ValidationResult.error("Target database directory must not be empty.");
        }


        return ValidationResult.ok();
    }
}
