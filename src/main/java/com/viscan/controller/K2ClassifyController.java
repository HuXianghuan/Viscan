package com.viscan.controller;


import com.viscan.ConfigManager;
import com.viscan.DragAcceptorsManual;
import com.viscan.Utils.PathParts;
import com.viscan.Utils.PathUtils;
import com.viscan.alert.AppAlert;
import com.viscan.tools.BaseTool;
import com.viscan.tools.option.FlagOption;
import com.viscan.tools.option.IntOption;
import com.viscan.tools.option.PositionOption;
import com.viscan.tools.option.ValueOption;
import com.viscan.validate.K2ClassifyValidator;
import com.viscan.validate.ValidationResult;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class K2ClassifyController extends BaseTabController {

    @FXML
    private RadioButton pairedRadio;
    @FXML
    private ListView<String> inputListView;
    @FXML
    private TextField databaseDirTextField;
    @FXML
    private TextField outputDirTextField;

    @FXML
    private CheckBox demandClassificationCheck;
    @FXML
    private CheckBox demandReportCheck;
    @FXML
    private CheckBox demandClassifiedCheck;
    @FXML
    private CheckBox demandUnclassifiedCheck;

    @FXML
    private TextField threadTextField;

    @FXML
    private TextField confidenceTextField;

    @FXML
    private CheckBox memoryMappingCheck;
    @FXML
    private CheckBox quickCheck;
    @FXML
    private CheckBox useNamesCheck;

    @FXML
    private TextArea commandTextArea;
    @FXML
    private Tab thisTab;


    @FXML
    public void initialize() {
        DragAcceptorsManual.installListView(inputListView, true, false, true, false);
        DragAcceptorsManual.installTextField(databaseDirTextField, false, true, true, false);
        DragAcceptorsManual.installTextField(outputDirTextField, false, true, true, false);

        DragAcceptorsManual.limitMaxItems(
                inputListView,
                () -> pairedRadio.isSelected() ? 2 : 1
        );

        pairedRadio.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            trimListViewToMax(inputListView, isSelected ? 2 : 1);
        });

        threadTextField.setText(ConfigManager.getConfig().getThreadNumber());
    }

    @FXML
    private void generateCommand() {

        ValidationResult result = K2ClassifyValidator.validate(
                pairedRadio.isSelected(),
                inputListView.getItems(),
                databaseDirTextField.getText(),
                outputDirTextField.getText(),
//                demandClassificationCheck.isSelected(),
//                demandReportCheck.isSelected(),
                threadTextField.getText()
        );

        if (!result.isValid()) {
            AppAlert.error("Invalide parameters", result.getMessage());
            return;
        }

        BaseTool k2ClassifyTool = new BaseTool();

        String outputDir = outputDirTextField.getText();
        Path outputDirPath = Path.of(outputDir);

        List<String> items = inputListView.getItems();
        String classifiedTag = ConfigManager.getConfig().getKraken2ClassifiedTag();
        String unclassifiedTag = ConfigManager.getConfig().getKraken2UnclassifiedTag();

        String reportTag = ConfigManager.getConfig().getKraken2ReportTag();

        k2ClassifyTool.addOption(new ValueOption("--db", databaseDirTextField.getText()));

        if (pairedRadio.isSelected()) {
            String input1 = items.get(0);
            String input2 = items.get(1);

            PathParts inputParts1 = PathParts.parse(input1);
            PathParts inputParts2 = PathParts.parse(input2);

            String commonBase = null;

            try {
                commonBase = PathUtils.extractCommonPairedFastqBase(
                        inputParts1.getNameParts().get(0),
                        inputParts2.getNameParts().get(0));
            } catch (IllegalArgumentException e) {
                AppAlert.error("Invalide parameters", e.getMessage());
                return;
            }




            List<String> commonParts = PathUtils.longestCommonSubsequence(inputParts1.getTagParts(), inputParts2.getTagParts());
            commonParts.add(0, commonBase);

            k2ClassifyTool.addOption(new PositionOption(1, input1)).addOption(new PositionOption(2, input2));

            if (demandClassificationCheck.isSelected()) {

                PathParts outputParts = new PathParts(outputDirPath, commonParts, "krk");

                k2ClassifyTool.addOption(new ValueOption("--output", outputParts.getLinuxPath()));
            } else {
                k2ClassifyTool.addOption(new ValueOption("--output", BLACK_HOLE));
            }

            if (demandReportCheck.isSelected()) {
                PathParts outputParts = new PathParts(outputDirPath, PathParts.addTagToParts(commonParts, reportTag), "txt");

                k2ClassifyTool.addOption(new ValueOption("--report", outputParts.getLinuxPath()));
            }

            if (demandClassifiedCheck.isSelected()) {
                List<String> newParts = new ArrayList<>(commonParts);
                newParts.set(0, newParts.get(0) + "#");
                newParts.add(classifiedTag);

                PathParts outputParts = new PathParts(outputDirPath, newParts, "fastq");
                k2ClassifyTool.addOption(new ValueOption("--classified-out", outputParts.getLinuxPath()));
            }

            if (demandUnclassifiedCheck.isSelected()) {
                List<String> newParts = new ArrayList<>(commonParts);
                newParts.set(0, newParts.get(0) + "#");
                newParts.add(unclassifiedTag);

                PathParts outputParts = new PathParts(outputDirPath, newParts, "fastq");
                k2ClassifyTool.addOption(new ValueOption("--unclassified-out", outputParts.getLinuxPath()));
            }

        } else { //single
            String input1 = items.get(0);
            PathParts inputParts1 = PathParts.parse(input1);
            k2ClassifyTool.addOption(new PositionOption(1, input1));

            List<String> parts = new ArrayList<>(inputParts1.getNameParts());


            if (demandClassificationCheck.isSelected()) {
                PathParts outputParts = new PathParts(outputDirPath, parts, "krk");

                k2ClassifyTool.addOption(new ValueOption("--output", outputParts.getLinuxPath()));
            } else {
                k2ClassifyTool.addOption(new ValueOption("--output", BLACK_HOLE));
            }

            if (demandReportCheck.isSelected()) {

                PathParts outputParts = new PathParts(outputDirPath, PathParts.addTagToParts(parts, reportTag), "txt");

                k2ClassifyTool.addOption(new ValueOption("--report", outputParts.getLinuxPath()));
            }

            if (demandClassifiedCheck.isSelected()) {
                List<String> newParts = new ArrayList<>(parts);
                newParts.set(0, newParts.get(0));
                newParts.add(classifiedTag);

                PathParts outputParts = new PathParts(outputDirPath, newParts, "fastq");
                k2ClassifyTool.addOption(new ValueOption("--classified-out", outputParts.getLinuxPath()));
            }

            if (demandUnclassifiedCheck.isSelected()) {
                List<String> newParts = new ArrayList<>(parts);
                newParts.set(0, newParts.get(0));
                newParts.add(unclassifiedTag);

                PathParts outputParts = new PathParts(outputDirPath, newParts, "fastq");
                k2ClassifyTool.addOption(new ValueOption("--unclassified-out", outputParts.getLinuxPath()));
            }
        }

        k2ClassifyTool.addOption(new IntOption("--threads", Integer.parseInt(threadTextField.getText())));

        k2ClassifyTool.addOption(new ValueOption("--confidence", confidenceTextField.getText()));


        k2ClassifyTool
                .addOption(new FlagOption("--paired", pairedRadio.isSelected()))
                .addOption(new FlagOption("--memory-mapping", memoryMappingCheck.isSelected()))
                .addOption(new FlagOption("--quick", quickCheck.isSelected()))
                .addOption(new FlagOption("--use-names", useNamesCheck.isSelected()));

        String k2ClassifyExecutable = PathUtils.linuxJoin(ConfigManager.getConfig().getKraken2ExcutableDir(), "k2 classify");
        commandTextArea.setText(k2ClassifyTool.buildCommandPretty(k2ClassifyExecutable));


    }

    private int terminalIndex = 1;

    @FXML
    private void commandRun() {
        super.commandRun(thisTab.getTabPane(), commandTextArea, "k2 classify", terminalIndex++);
    }


}




