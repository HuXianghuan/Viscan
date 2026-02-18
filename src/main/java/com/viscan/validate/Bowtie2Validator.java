package com.viscan.validate;

import java.util.List;

public class Bowtie2Validator {
      public static ValidationResult validate(
              boolean isPaired,
              List<String> inputs,
              String indexDir,
              String indexPrefix,
              String outputDir,

              boolean demandSam,
              boolean demandUnalign,
              boolean demandAlign,

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

          if (indexDir == null || indexDir.isBlank()) {
              return ValidationResult.error("Index directory must not be empty.");
          }

          if (indexPrefix == null || indexPrefix.isBlank()) {
              return ValidationResult.error("Index prefix must not be empty.");
          }

          try {
              int t = Integer.parseInt(threadText);
              if (t <= 0) {
                  return ValidationResult.error(ValidationMessages.THREAD_INVALID);
              }
          } catch (NumberFormatException e) {
              return ValidationResult.error(ValidationMessages.THREAD_NOT_INTEGER);
          }

          if (!demandSam && !demandUnalign && !demandAlign) {
              return ValidationResult.error("At least one output file option must be selected.");
          }

//          if (optionA && optionB) {
//              return ValidationResult.error("Option A and B cannot be selected together");
//          }

          return ValidationResult.ok();
      }

}
