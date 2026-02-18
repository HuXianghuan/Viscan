package com.viscan.controller;

import com.viscan.ConfigManager;
import com.viscan.DragAcceptorsManual;
import com.viscan.Utils.PathUtils;
import com.viscan.alert.AppAlert;
import com.viscan.tools.BaseTool;
import com.viscan.tools.option.FlagOption;
import com.viscan.tools.option.IntOption;
import com.viscan.tools.option.ValueOption;
import com.viscan.validate.RcfValidator;
import com.viscan.validate.ValidationResult;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.Map;


public class RcfController extends BaseTabController {

    @FXML
    private ComboBox<String> classiferCombo;
    @FXML
    private ListView<String> caseInputListView;
    @FXML
    private ListView<String> controlInputListView;
    @FXML
    private TextField taxdumpDirTextField;
    @FXML
    private TextField outputDirTextField;
    @FXML
    private TextField outputPrefixTextField;

    @FXML
    private ComboBox<String> scoreCombo;
    @FXML
    private TextField minScoreTextField;
    @FXML
    private TextField controlMinScoreTextField;

    @FXML
    private CheckBox avoidCrossCheck;

    @FXML
    private TextArea commandTextArea;

    @FXML
    private Tab thisTab;
    private int terminalIndex = 1;

    @FXML
    public void initialize() {

        DragAcceptorsManual.installListView(caseInputListView, true, false, true, false);
        DragAcceptorsManual.installListView(controlInputListView, true, false, true, false);

        DragAcceptorsManual.installTextField(taxdumpDirTextField, false, true, true, false);
        DragAcceptorsManual.installTextField(outputDirTextField, false, true, true, false);

        classiferCombo.getItems().addAll("Kraken2", "Centrifuge", "CLARK");
        classiferCombo.getSelectionModel().select("Kraken2");


        scoreCombo.getItems().addAll("SHEL", "LENGTH", "LOGLENGTH", "NORMA", "KRAKEN", "CLARK_C", "CLARK_G", "GENERIC");
    }

    @FXML
    private void generateCommand() {
        ValidationResult result = RcfValidator.validate(
                caseInputListView.getItems(),
                taxdumpDirTextField.getText(),
                outputDirTextField.getText(),
                outputPrefixTextField.getText(),
                scoreCombo.getValue(),
                minScoreTextField.getText(),
                controlMinScoreTextField.getText()
        );

        if (!result.isValid()) {
            AppAlert.error("Invalide parameters", result.getMessage());
            return;
        }

        BaseTool rcfTool = new BaseTool();

        String outputDir = outputDirTextField.getText();
        List<String> caseInputs = caseInputListView.getItems();
        List<String> controlInputs = controlInputListView.getItems();


        rcfTool.addOption(new ValueOption("--nodespath", taxdumpDirTextField.getText()));

        Map<String, String> classifierMap = Map.of(
                "Kraken2", "--kraken",
                "CLARK", "--clark",
                "Centrifuge", "--file"
        );

        String inputOptionName = classifierMap.get(classiferCombo.getValue());


        for (String controlInput : controlInputs) {
            rcfTool.addOption(new ValueOption(inputOptionName, controlInput));
        }

        rcfTool.addOption(new IntOption("--controls", controlInputs.size()));

        for (String caseInput : caseInputs) {
            rcfTool.addOption(new ValueOption(inputOptionName, caseInput));
        }

        Map<String, String> scoreMap = Map.of(
                "SHEL", "SHEL",
                "LENGTH", "LENGTH",
                "LOGLENGTH", "LOGLENGTH",
                "NORMA", "NORMA",
                "KRAKEN", "KRAKEN",
                "CLARK_C", "CLARK_C",
                "CLARK_G", "CLARK_G",
                "GENERIC", "GENERIC"
        );
        String scoreValue = scoreCombo.getValue();
        String scoreMethod = (scoreValue == null || scoreValue.isBlank()) ? null : scoreMap.get(scoreValue);
        rcfTool.addOption(new ValueOption("--scoring", scoreMethod));

        rcfTool.addOption(new ValueOption("--minscore", minScoreTextField.getText()));
        rcfTool.addOption(new ValueOption("--ctrlminscore", controlMinScoreTextField.getText()));


        rcfTool.addOption(new ValueOption("--outprefix", PathUtils.linuxJoin(outputDirTextField.getText(), outputPrefixTextField.getText())));

        rcfTool.addOption(new FlagOption("--avoidcross", avoidCrossCheck.isSelected()));


        String rcfExecutable = PathUtils.linuxJoin(ConfigManager.getConfig().getRecentrifugeExecutableDir(), "rcf");
        commandTextArea.setText(rcfTool.buildCommandPretty(rcfExecutable));


    }

    @FXML
    private void commandRun() {
        super.commandRun(thisTab.getTabPane(), commandTextArea, "Recentrifuge", terminalIndex++);
    }


}
