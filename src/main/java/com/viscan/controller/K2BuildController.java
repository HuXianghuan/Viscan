package com.viscan.controller;

import com.viscan.ConfigManager;
import com.viscan.DragAcceptorsManual;
import com.viscan.Utils.PathUtils;
import com.viscan.alert.AppAlert;
import com.viscan.tools.BaseTool;
import com.viscan.tools.option.FlagOption;
import com.viscan.tools.option.IntOption;
import com.viscan.tools.option.ValueOption;
import com.viscan.validate.K2BuildValidator;
import com.viscan.validate.ValidationResult;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;


public class K2BuildController extends BaseTabController{


    @FXML
    private TextField databaseDirField;
    @FXML
    private TextField threadsField;
    @FXML
    private CheckBox fastBuildCheck;
    @FXML
    private CheckBox proteinCheck;
    @FXML
    private TextField kmerLenField;
    @FXML
    private TextField minimizerLenField;
    @FXML
    private TextField maxDbSizeField;
    @FXML
    private TextArea commandTextArea;
    @FXML
    private Tab thisTab;

    @FXML
    public void initialize() {
        DragAcceptorsManual.installTextField(databaseDirField, false, true, true, false);

        threadsField.setText(ConfigManager.getConfig().getThreadNumber());
    }

    @FXML
    private void generateCommand() {

        ValidationResult result = K2BuildValidator.validate(
                databaseDirField.getText(),
                threadsField.getText()
        );

        if (!result.isValid()) {
            AppAlert.error("Invalide parameters", result.getMessage());
            return;
        }

        BaseTool buildTool = new BaseTool();

        buildTool.addOption(new ValueOption("--db", databaseDirField.getText()));

        buildTool.addOption(new ValueOption("--kmer-len", kmerLenField.getText()));

        buildTool.addOption(new ValueOption("--minimizer-len", minimizerLenField.getText()));

        buildTool.addOption(new ValueOption("--max-db-size", maxDbSizeField.getText()));

        buildTool.addOption(new IntOption("--threads", Integer.parseInt(threadsField.getText())));



        buildTool
                .addOption(new FlagOption("--fast-build", fastBuildCheck.isSelected()))
                .addOption(new FlagOption("--protein", proteinCheck.isSelected()));




        String executable = PathUtils.linuxJoin(ConfigManager.getConfig().getKraken2ExcutableDir(), "k2 build");
        commandTextArea.setText(buildTool.buildCommandPretty(executable));

    }

    private int terminalIndex = 1;

    @FXML
    private void commandRun() {
        super.commandRun(thisTab.getTabPane(), commandTextArea, "k2 build", terminalIndex++);
    }


}
