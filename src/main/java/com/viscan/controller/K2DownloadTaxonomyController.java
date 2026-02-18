package com.viscan.controller;

import com.viscan.ConfigManager;
import com.viscan.DragAcceptorsManual;
import com.viscan.Utils.PathUtils;
import com.viscan.alert.AppAlert;
import com.viscan.tools.BaseTool;
import com.viscan.tools.option.FlagOption;
import com.viscan.tools.option.ValueOption;
import com.viscan.validate.K2DownloadTaxonomyValidator;
import com.viscan.validate.ValidationResult;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class K2DownloadTaxonomyController extends BaseTabController{

    public Tab thisTab;
    public TextField databaseDirField;
    public CheckBox proteinCheck;
    public CheckBox skipMapsCheck;
    public TextArea commandTextArea;





    @FXML
    public void initialize() {
        DragAcceptorsManual.installTextField(databaseDirField, false, true, true, false);


    }

    @FXML
    private void generateCommand() {

        ValidationResult result = K2DownloadTaxonomyValidator.validate(
                databaseDirField.getText()
        );

        if (!result.isValid()) {
            AppAlert.error("Invalide parameters", result.getMessage());
            return;
        }



        BaseTool downloadTaxoTool = new BaseTool();

        downloadTaxoTool.addOption(new ValueOption("--db", databaseDirField.getText()));

        downloadTaxoTool
                .addOption(new FlagOption("--protein", proteinCheck.isSelected()))
                .addOption(new FlagOption("--skip-maps", skipMapsCheck.isSelected()));


        String executable = PathUtils.linuxJoin(ConfigManager.getConfig().getKraken2ExcutableDir(), "k2 download-taxonomy");
        commandTextArea.setText(downloadTaxoTool.buildCommandPretty(executable));
    }

    private int terminalIndex = 1;

    @FXML
    private void commandRun() {
        super.commandRun(thisTab.getTabPane(), commandTextArea, "k2 download-taxonomy", terminalIndex++);
    }

}


