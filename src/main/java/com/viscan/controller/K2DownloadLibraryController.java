package com.viscan.controller;

import com.viscan.ConfigManager;
import com.viscan.DragAcceptorsManual;
import com.viscan.Utils.PathUtils;
import com.viscan.alert.AppAlert;
import com.viscan.tools.BaseTool;
import com.viscan.tools.option.FlagOption;
import com.viscan.tools.option.IntOption;
import com.viscan.tools.option.ValueOption;
import com.viscan.validate.K2DownloadLibraryValidator;
import com.viscan.validate.ValidationResult;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Map;

public class K2DownloadLibraryController extends BaseTabController{


    @FXML
    private CheckBox resumeCheck;
    @FXML
    private CheckBox hasAnnotationCheck;
    @FXML
    private CheckBox proteinCheck;
    @FXML
    private CheckBox noMaskingCheck;
    @FXML
    private ComboBox<String> sourceCombo;
    @FXML
    private TextField databaseDirField;
    @FXML
    private TextField libraryField;
    @FXML
    private TextField threadsField;
    @FXML
    private TextArea commandTextArea;
    @FXML
    private Tab thisTab;



    @FXML
    public void initialize() {
        DragAcceptorsManual.installTextField(databaseDirField, false, true, true, false);
        sourceCombo.getItems().addAll("Refseq", "Genbank", "All");

        threadsField.setText(ConfigManager.getConfig().getThreadNumber());
    }
    
    @FXML
    private void generateCommand() {

        ValidationResult result = K2DownloadLibraryValidator.validate(
                databaseDirField.getText(),
                libraryField.getText(),
                threadsField.getText()
        );

        if (!result.isValid()) {
            AppAlert.error("Invalide parameters", result.getMessage());
            return;
        }



        BaseTool downloadLibTool = new BaseTool();

        downloadLibTool.addOption(new ValueOption("--db", databaseDirField.getText()));

        downloadLibTool.addOption(new ValueOption("--library", libraryField.getText()));

        downloadLibTool.addOption(new IntOption("--threads", Integer.parseInt(threadsField.getText())));

        Map<String, String> sourceMap = Map.of(
                "Refseq", "refseq",
                "Genbank", "genbank",
                "All", "all"
        );

        String sourceValue = sourceCombo.getValue();
        String source = (sourceValue == null || sourceValue.isBlank()) ? null : sourceMap.get(sourceValue);

        downloadLibTool.addOption(new ValueOption("--assembly-source", source));

        downloadLibTool
                .addOption(new FlagOption("--resume", resumeCheck.isSelected()))
                .addOption(new FlagOption("--has-annotation", hasAnnotationCheck.isSelected()))
                .addOption(new FlagOption("--protein", proteinCheck.isSelected()))
                .addOption(new FlagOption("--no-masking", noMaskingCheck.isSelected()));



        String executable = PathUtils.linuxJoin(ConfigManager.getConfig().getKraken2ExcutableDir(), "k2 download-library");
        commandTextArea.setText(downloadLibTool.buildCommandPretty(executable));




        
    }


    private int terminalIndex = 1;

    @FXML
    private void commandRun() {
        super.commandRun(thisTab.getTabPane(), commandTextArea, "k2 download-library", terminalIndex++);
    }

    
}
