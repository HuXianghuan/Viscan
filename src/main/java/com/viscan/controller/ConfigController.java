package com.viscan.controller;

import com.viscan.BioConfig;
import com.viscan.ConfigManager;
import com.viscan.alert.AppAlert;
import com.viscan.validate.ConfigValidator;
import com.viscan.validate.ValidationResult;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class ConfigController {

    @FXML
    private TextField wslExexutableField;
    @FXML
    private TextField fastpExecutableDirField;
    @FXML
    private TextField bowtie2ExecutableDirField;
    @FXML
    private TextField kraken2ExcutableDirField;
    @FXML
    private TextField recentrifugeExecutableDirField;
    @FXML
    private TextField workDirField;
    @FXML
    private TextField threadNumberField;

    @FXML
    private TextField fastpCleanTagField;
    @FXML
    private TextField bowtie2UnalignTagField;
    @FXML
    private TextField bowtie2AlignTagField;
    @FXML
    private TextField kraken2ClassifiedTagField;
    @FXML
    private TextField kraken2UnclassifiedTagField;
    @FXML
    private TextField kraken2ReportTagField;

    @FXML
    private TextField wslDistroField;

    @FXML
    public void initialize() {
        BioConfig config = ConfigManager.getConfig();
        loadConfigToUI(config);
    }


    @FXML
    public void saveButtonOnAction() {
        BioConfig config = ConfigManager.getConfig();

        ValidationResult result = ConfigValidator.validate(
                wslExexutableField.getText(),
                fastpExecutableDirField.getText(),
                bowtie2ExecutableDirField.getText(),
                kraken2ExcutableDirField.getText(),
                recentrifugeExecutableDirField.getText(),
                workDirField.getText(),
                threadNumberField.getText(),
                fastpCleanTagField.getText(),
                bowtie2UnalignTagField.getText(),
                bowtie2AlignTagField.getText(),
                kraken2ReportTagField.getText(),
                kraken2UnclassifiedTagField.getText(),
                kraken2UnclassifiedTagField.getText(),
                wslDistroField.getText()
        );

        if (!result.isValid()) {
            AppAlert.error("Invalide parameters", result.getMessage());
            return;
        }



        boolean flag = AppAlert.confirm("Confirm Save", "Are you sure you want to save the changes to the configuration?");
        if (flag) {
            config.setWslExecutable(wslExexutableField.getText());
            config.setFastpExecutableDir(fastpExecutableDirField.getText());
            config.setBowtie2ExecutableDir(bowtie2ExecutableDirField.getText());
            config.setKraken2ExcutableDir(kraken2ExcutableDirField.getText());
            config.setRecentrifugeExecutableDir(recentrifugeExecutableDirField.getText());
            config.setWorkDir(workDirField.getText());
            config.setThreadNumber(threadNumberField.getText());


            config.setFastpCleanTag(fastpCleanTagField.getText());
            config.setBowtie2UnalignTag(bowtie2UnalignTagField.getText());
            config.setBowtie2AlignTag(bowtie2AlignTagField.getText());
            config.setKraken2ClassifiedTag(kraken2ClassifiedTagField.getText());
            config.setKraken2UnclassifiedTag(kraken2UnclassifiedTagField.getText());
            config.setKraken2ReportTag(kraken2ReportTagField.getText());

            config.setWslDistro(wslDistroField.getText());

            ConfigManager.saveConfig();
        }

    }

    @FXML
    private void resetButtonOnAction() {
        boolean confirm = AppAlert.confirm(
                "Reset Configuration",
                "Discard all unsaved changes and restore last saved configuration?"
        );
        if (!confirm) return;

        loadConfigToUI(ConfigManager.getConfig());
    }

    private void loadConfigToUI(BioConfig config) {
        wslExexutableField.setText(config.getWslExecutable());
        fastpExecutableDirField.setText(config.getFastpExecutableDir());
        bowtie2ExecutableDirField.setText(config.getBowtie2ExecutableDir());
        kraken2ExcutableDirField.setText(config.getKraken2ExcutableDir());
        recentrifugeExecutableDirField.setText(config.getRecentrifugeExecutableDir());

        workDirField.setText(config.getWorkDir());
        threadNumberField.setText(config.getThreadNumber());

        fastpCleanTagField.setText(config.getFastpCleanTag());
        bowtie2UnalignTagField.setText(config.getBowtie2UnalignTag());
        bowtie2AlignTagField.setText(config.getBowtie2AlignTag());
        kraken2ClassifiedTagField.setText(config.getKraken2ClassifiedTag());
        kraken2UnclassifiedTagField.setText(config.getKraken2UnclassifiedTag());
        kraken2ReportTagField.setText(config.getKraken2ReportTag());

        wslDistroField.setText(config.getWslDistro());
    }



}
