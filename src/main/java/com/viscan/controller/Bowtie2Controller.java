package com.viscan.controller;

import com.viscan.ConfigManager;
import com.viscan.DragAcceptorsManual;
import com.viscan.Utils.PathParts;
import com.viscan.Utils.PathUtils;
import com.viscan.alert.AppAlert;
import com.viscan.tools.BaseTool;
import com.viscan.tools.option.IntOption;
import com.viscan.tools.option.ValueOption;
import com.viscan.validate.Bowtie2Validator;
import com.viscan.validate.ValidationResult;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.nio.file.Path;
import java.util.List;

public class Bowtie2Controller extends BaseTabController {

    @FXML
    private RadioButton pairedRadio;
    @FXML
    private ListView<String> inputListView;
    @FXML
    private TextField indexDirTextField;
    @FXML
    private TextField indexPrefixTextField;
    @FXML
    private TextField outputDirTextField;
    @FXML
    private CheckBox demandSamCheck;
    @FXML
    private CheckBox demandUnalignCheck;
    @FXML
    private CheckBox demandAlignCheck;
    @FXML
    private TextField threadTextField;

    @FXML
    private TextArea commandTextArea;

    @FXML
    private Tab thisTab;


    @FXML
    public void initialize() {
        DragAcceptorsManual.installListView(inputListView, true, false, true, false);
        DragAcceptorsManual.installTextField(indexDirTextField, false, true, true, false);
        DragAcceptorsManual.installTextField(outputDirTextField, false, true, true, false);

        DragAcceptorsManual.limitMaxItems(
                inputListView,
                () -> pairedRadio.isSelected() ? 2 : 1
        );

        pairedRadio.selectedProperty().addListener((obs, wasSelected, isSelected) ->{
            trimListViewToMax(inputListView, isSelected ? 2 : 1);
        });

        threadTextField.setText(ConfigManager.getConfig().getThreadNumber());


    }


    @FXML
    private void generateCommand() {

        ValidationResult result = Bowtie2Validator.validate(
                pairedRadio.isSelected(),
                inputListView.getItems(),
                indexDirTextField.getText(),
                indexPrefixTextField.getText(),
                outputDirTextField.getText(),
                demandSamCheck.isSelected(),
                demandUnalignCheck.isSelected(),
                demandAlignCheck.isSelected(),
                threadTextField.getText()
        );

        if (!result.isValid()) {
            AppAlert.error("Invalide parameters", result.getMessage());
            return;
        }

        BaseTool bowtie2Tool = new BaseTool();

        String outputDir = outputDirTextField.getText();
        Path outputDirPath = Path.of(outputDir);

        List<String> items = inputListView.getItems();

        String unalignTag = ConfigManager.getConfig().getBowtie2UnalignTag();
        String alignTag = ConfigManager.getConfig().getBowtie2AlignTag();

        bowtie2Tool.addOption(new ValueOption("-x", PathUtils.linuxJoin(
                indexDirTextField.getText(),
                indexPrefixTextField.getText()
                )));

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

            bowtie2Tool.addOption(new ValueOption("-1", input1)).addOption(new ValueOption("-2", input2));



            if (demandSamCheck.isSelected()) {
                PathParts samParts = new PathParts(outputDirPath, commonParts, "sam");
                bowtie2Tool.addOption(new ValueOption("-S", samParts.getLinuxPath()));
            } else {
                bowtie2Tool.addOption(new ValueOption("-S", BLACK_HOLE));
            }

            if (demandUnalignCheck.isSelected()) {
                PathParts unalignParts = new PathParts(outputDirPath, PathParts.addTagToParts(commonParts, unalignTag), inputParts1.getSuffix());
                bowtie2Tool.addOption(new ValueOption("--un-conc", unalignParts.getLinuxPath()));
            }

            if (demandAlignCheck.isSelected()) {
                PathParts alignParts = new PathParts(outputDirPath, PathParts.addTagToParts(commonParts, alignTag), inputParts2.getSuffix());
                bowtie2Tool.addOption(new ValueOption("--al-conc", alignParts.getLinuxPath()));
            }


        } else { //single
            String input1 = items.get(0);

            PathParts inputParts1 = PathParts.parse(input1);

            List<String> newParts = inputParts1.getNameParts();

            bowtie2Tool.addOption(new ValueOption("-U", input1));
            if (demandSamCheck.isSelected()) {
                PathParts samParts = new PathParts(outputDirPath, newParts, "sam");
                bowtie2Tool.addOption(new ValueOption("-S", samParts.getLinuxPath()));
            } else {
                bowtie2Tool.addOption(new ValueOption("-S", BLACK_HOLE));
            }

            if (demandUnalignCheck.isSelected()) {
                PathParts unalignParts = new PathParts(outputDirPath, PathParts.addTagToParts(newParts, unalignTag), inputParts1.getSuffix());
                bowtie2Tool.addOption(new ValueOption("--un", unalignParts.getLinuxPath()));
            }

            if (demandAlignCheck.isSelected()) {
                PathParts alignParts = new PathParts(outputDirPath, PathParts.addTagToParts(newParts, alignTag), inputParts1.getSuffix());
                bowtie2Tool.addOption(new ValueOption("--al", alignParts.getLinuxPath()));
            }


        }

        bowtie2Tool.addOption(new IntOption("--threads", Integer.parseInt(threadTextField.getText())));

        String bowtie2Executable = PathUtils.linuxJoin(ConfigManager.getConfig().getBowtie2ExecutableDir(), "bowtie2");
        commandTextArea.setText(bowtie2Tool.buildCommandPretty(bowtie2Executable));








    }

    private int terminalIndex = 1;

    @FXML
    private void commandRun() {
        super.commandRun(thisTab.getTabPane(), commandTextArea, "bowtie2", terminalIndex++);
    }


}
