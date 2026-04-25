package com.viscan.controller;

import com.viscan.ConfigManager;
import com.viscan.DragAcceptorsManual;
import com.viscan.Utils.PathParts;
import com.viscan.Utils.PathUtils;
import com.viscan.path.LinuxPath;
import com.viscan.alert.AppAlert;
import com.viscan.tools.BaseTool;
import com.viscan.tools.option.IntOption;
import com.viscan.tools.option.ValueOption;
import com.viscan.validate.FastpValidator;
import com.viscan.validate.ValidationResult;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;

public class FastpController extends BaseTabController {
    @FXML
    private RadioButton pairedRadio;

    @FXML
    private ListView<String> inputListView;
    @FXML
    private TextField outputTextField;
    @FXML
    private TextField threadTextField;
    @FXML
    private TextArea commandTextArea;

    @FXML
    private Tab thisTab;




    @FXML
    public void initialize() {
        DragAcceptorsManual.installTextField(outputTextField, false, true, true, false);
        DragAcceptorsManual.installListView(inputListView, true, false, true, false);
        DragAcceptorsManual.installTextField(outputTextField, false, true, true, false);

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

        //validate the sheet first
        ValidationResult result = FastpValidator.validate(
                pairedRadio.isSelected(),
                inputListView.getItems(),
                outputTextField.getText(),
                threadTextField.getText()
        );

        if (!result.isValid()) {
            AppAlert.error("Invalide parameters", result.getMessage());
            return;
        }


        //command generate
        BaseTool fastpTool = new BaseTool();


        String outputDir = outputTextField.getText();
        LinuxPath outputDirPath = new LinuxPath(outputDir);

        List<String> items = inputListView.getItems();

        String cleanTag = ConfigManager.getConfig().getFastpCleanTag();

        if (pairedRadio.isSelected()) {
            String input1 = items.get(0);
            String input2 = items.get(1);

            PathParts inputParts1 = PathParts.parse(input1);
            PathParts inputParts2 = PathParts.parse(input2);

            fastpTool.addOption(new ValueOption("--in1", inputParts1.getLinuxPath()));
            fastpTool.addOption(new ValueOption("--in2", inputParts2.getLinuxPath()));

            PathParts outputParts1;
            PathParts outputParts2;



            if (inputParts1.getSuffix().equals("gz")) {
                List<String> outName1 = new ArrayList<>(inputParts1.getNameParts());
                String suffix1 = outName1.remove(outName1.size() - 1);
                outputParts1 = new PathParts(outputDirPath, outName1, suffix1);

                List<String> outName2 = new ArrayList<>(inputParts2.getNameParts());
                String suffix2 = outName2.remove(outName2.size() - 1);
                outputParts2 = new PathParts(outputDirPath, outName2, suffix2);

            } else {

                outputParts1 = inputParts1.copy().withParent(outputDirPath);

                outputParts2 = inputParts2.copy().withParent(outputDirPath);
            }

            outputParts1.getNameParts().add(cleanTag);
            outputParts2.getNameParts().add(cleanTag);

            fastpTool.addOption(new ValueOption("--out1", outputParts1.getLinuxPath()));
            fastpTool.addOption(new ValueOption("--out2", outputParts2.getLinuxPath()));

            String commonBase = null;

            try {
                commonBase = PathUtils.extractCommonPairedFastqBase(
                        inputParts1.getNameParts().get(0),
                        inputParts2.getNameParts().get(0));
            } catch (IllegalArgumentException e) {
                AppAlert.error("Invalide parameters", e.getMessage());
                return;
            }

            List<String> commonParts = PathUtils.longestCommonSubsequence(outputParts1.getTagParts(), outputParts2.getTagParts());
            commonParts.add(0, commonBase);

            PathParts jsonParts = new PathParts(outputDirPath, commonParts, "json");
            PathParts htmlParts = new PathParts(outputDirPath, commonParts, "html");

            fastpTool.addOption(new ValueOption("--json", jsonParts.getLinuxPath()));
            fastpTool.addOption(new ValueOption("--html", htmlParts.getLinuxPath()));

        } else { //single
            String input1 = items.get(0);

            PathParts inputParts1 = PathParts.parse(input1);

            List<String> newNames = new ArrayList<>(inputParts1.getNameParts());
            PathParts outputParts1;


            fastpTool.addOption(new ValueOption("--in1", inputParts1.getLinuxPath()));

            if (inputParts1.getSuffix().equals("gz")) {
                String newSuffix = newNames.remove(newNames.size() - 1);
                outputParts1 = new PathParts(outputDirPath, newNames, newSuffix);

            } else {
                outputParts1 = new PathParts(outputDirPath, newNames, inputParts1.getSuffix());
            }


            PathParts.addTagToParts(outputParts1.getNameParts(), cleanTag);




            fastpTool.addOption(new ValueOption("--out1", outputParts1.getLinuxPath()));


            PathParts jsonParts = new PathParts(outputDirPath, newNames, "json");
            PathParts htmlParts = new PathParts(outputDirPath, newNames, "html");

            fastpTool.addOption(new ValueOption("--json", jsonParts.getLinuxPath()));
            fastpTool.addOption(new ValueOption("--html", htmlParts.getLinuxPath()));
        }




        fastpTool.addOption(new IntOption("--thread", Integer.parseInt(threadTextField.getText())));

        commandTextArea.setText(fastpTool.buildCommandPretty(PathUtils.linuxJoin(ConfigManager.getConfig().getFastpExecutableDir(), "fastp")));

    }


    private int terminalIndex = 1;

    @FXML
    private void commandRun() {
        super.commandRun(thisTab.getTabPane(), commandTextArea, "fastp", terminalIndex++);
    }







}
