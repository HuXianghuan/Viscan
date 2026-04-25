package com.viscan.controller;

import com.kodedu.terminalfx.Terminal;
import com.kodedu.terminalfx.TerminalBuilder;
import com.kodedu.terminalfx.TerminalTab;
import com.kodedu.terminalfx.config.TerminalConfig;
import com.viscan.ConfigManager;
import com.viscan.DragAcceptorsManual;
import com.viscan.Utils.PathParts;
import com.viscan.Utils.PathUtils;
import com.viscan.Utils.WslPathConverter;
import com.viscan.path.LinuxPath;
import com.viscan.path.WindowsPath;
import com.viscan.alert.AppAlert;
import com.viscan.tools.BaseTool;
import com.viscan.tools.CommandPipeline;
import com.viscan.tools.option.FlagOption;
import com.viscan.tools.option.IntOption;
import com.viscan.tools.option.PositionOption;
import com.viscan.tools.option.ValueOption;
import com.viscan.validate.Flow1Validator;
import com.viscan.validate.ValidationResult;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Flow1Controller extends BaseTabController {



    public static class Sample {
        private final ObservableList<String> files = FXCollections.observableArrayList();

        public ObservableList<String> getFiles() {
            return files;
        }

    }

    @FXML
    private RadioButton pairedRadio;

    @FXML
    private TableView<Sample> caseInputTable;
    @FXML
    private TableColumn<Sample, Sample> caseInputColumn;
    @FXML
    private TableColumn<Sample, Void> caseActionColumn;

    @FXML
    private TableView<Sample> controlInputTable;
    @FXML
    private TableColumn<Sample, Sample> controlInputColumn;
    @FXML
    private TableColumn<Sample, Void> controlActionColumn;

    @FXML
    private TextField projectNameField;
    @FXML
    private TextField theadsNumField;
    @FXML
    private TextField projectDirField;
    @FXML
    private CheckBox fastpDemandHtmlCheck;
    @FXML
    private CheckBox fastpDemandJsonCheck;
    @FXML
    private TextField krakenHostDbDirField;
    @FXML
    private TextField krakenHostConfidenceField;
    @FXML
    private TextField krakenPathogenDbDirField;
    @FXML
    private TextField krakenPathogenConfidenceField;
    @FXML
    private TextField taxdumpDirField;
    @FXML
    private TextField rcfMinScoreField;
    @FXML
    private TextField rcfControlMinScoreField;
    @FXML
    private ComboBox<String> rcfScoreMethodCombo;
    @FXML
    private Button generateButton;


    @FXML
    private TextArea commandTextArea;
    @FXML
    private Tab thisTab;


    @FXML
    public void initialize() {
        DragAcceptorsManual.installTextField(projectDirField, false, true, false, true);
        DragAcceptorsManual.installTextField(krakenHostDbDirField, false, true, true, false);
        DragAcceptorsManual.installTextField(krakenPathogenDbDirField, false, true, true, false);
        DragAcceptorsManual.installTextField(taxdumpDirField, false, true, true, false);

        rcfScoreMethodCombo.getItems().addAll("SHEL", "LENGTH", "LOGLENGTH", "NORMA", "KRAKEN");

        theadsNumField.setText(ConfigManager.getConfig().getThreadNumber());



        caseInputTable.setItems(FXCollections.observableArrayList());
        controlInputTable.setItems(FXCollections.observableArrayList());

        setupSampleColumn(caseInputColumn);
        setupSampleColumn(controlInputColumn);

        setupActionColumn(caseActionColumn);
        setupActionColumn(controlActionColumn);

        caseActionColumn.setMinWidth(60);
        caseActionColumn.setMaxWidth(60);
        caseActionColumn.setResizable(false);
        caseInputColumn.prefWidthProperty()
                .bind(caseInputTable.widthProperty().subtract(caseActionColumn.getMaxWidth()));

        controlActionColumn.setMinWidth(60);
        controlActionColumn.setMaxWidth(60);
        controlActionColumn.setResizable(false);
        controlInputColumn.prefWidthProperty()
                .bind(controlInputTable.widthProperty().subtract(controlActionColumn.getMaxWidth()));


        pairedRadio.selectedProperty().addListener((obs, oldValue, newValue) -> {
            caseInputTable.getItems().clear();
            controlInputTable.getItems().clear();
        });




    }

    private void setupSampleColumn(TableColumn<Sample, Sample> col) {
        col.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue()));

        col.setCellFactory(c -> new TableCell<>() {
            private final ListView<String> listView = new ListView<>();
            {
                listView.setPrefHeight(2 * 24 + 1);
                DragAcceptorsManual.installListView(listView, true, false, true, false);
            }

            @Override
            protected void updateItem(Sample item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    listView.setItems(item.getFiles());

                    DragAcceptorsManual.limitMaxItems(listView, () -> pairedRadio.isSelected() ? 2 : 1);

                    setGraphic(listView);
                }
            }
        });
    }


    private void setupActionColumn(TableColumn<Sample, Void> actionCol) {
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteButton = new Button("delete");
            private final HBox box = new HBox(deleteButton);

            {
                box.setAlignment(Pos.CENTER);
                deleteButton.setOnAction(e -> {
                    Sample sample = getTableView().getItems().get(getIndex());
                    getTableView().getItems().remove(sample);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }


    @FXML
    private void addSampleToCase() {
        Sample sample = new Sample();
        caseInputTable.getItems().add(sample);

    }
    @FXML
    private void addSampleToControl() {
        Sample sample = new Sample();
        controlInputTable.getItems().add(sample);

    }


    @FXML
    private void generateCommand() {



        List<List<String>> caseFiles = caseInputTable.getItems().stream()
                .map(sample -> List.copyOf(sample.getFiles()))
                .toList();

        List<List<String>> controlFiles = controlInputTable.getItems().stream()
                .map(sample -> List.copyOf(sample.getFiles()))
                .toList();

        ValidationResult result = Flow1Validator.validate(
                pairedRadio.isSelected(),
                caseFiles,
                controlFiles,
                projectNameField.getText(),
                projectDirField.getText(),
                theadsNumField.getText(),
                krakenHostDbDirField.getText(),
                krakenHostConfidenceField.getText(),
                krakenPathogenDbDirField.getText(),
                krakenPathogenConfidenceField.getText(),
                taxdumpDirField.getText(),
                rcfScoreMethodCombo.getValue(),
                rcfMinScoreField.getText(),
                rcfControlMinScoreField.getText()
                );


        if (!result.isValid()) {
            AppAlert.error("Invalide parameters", result.getMessage());
            return;
        }


        CommandPipeline pipeline = new CommandPipeline();

        WindowsPath projectDirWin = new WindowsPath(projectDirField.getText());
        LinuxPath projectDirLinux = projectDirWin.toMnt();

        String cleanTag = ConfigManager.getConfig().getFastpCleanTag();
        String unclassifiedTag = ConfigManager.getConfig().getKraken2UnclassifiedTag();

        LinuxPath fastpOutPath = projectDirLinux.resolve("fastp_out");
        LinuxPath krakenHostRemovalOutPath = projectDirLinux.resolve("kraken2_host_remove_out");
        LinuxPath krakenClassifyOutPath = projectDirLinux.resolve("kraken2_classify_out");
        LinuxPath recentrifugeOutPath = projectDirLinux.resolve("recentrifuge_out");







        List<List<String>> allSampleFiles = new ArrayList<>();
        allSampleFiles.addAll(caseFiles);
        allSampleFiles.addAll(controlFiles);




        if (pairedRadio.isSelected()) { // paired mode

            List<String> commonBases = new ArrayList<>();
            for (List<String> files : allSampleFiles) {
                commonBases.add(getCommonBase(PathParts.parse(files.get(0)), PathParts.parse(files.get(1))));
            }

            List<List<String>> fastpOutFiles = new ArrayList<>();

            for (int i = 0; i < commonBases.size(); i++) { //fastp

                List<String> files = allSampleFiles.get(i);

                String input1 = files.get(0);
                String input2 = files.get(1);

                PathParts inputParts1 = PathParts.parse(input1);
                PathParts inputParts2 = PathParts.parse(input2);

                PathParts fastpOutParts1;
                PathParts fastpOutParts2;
                List<String> fastpOutNames1 = new ArrayList<>(inputParts1.getNameParts());
                List<String> fastpOutNames2 = new ArrayList<>(inputParts2.getNameParts());

                if (inputParts1.getSuffix().equals("gz")) {
                    String suffix1 = fastpOutNames1.remove(fastpOutNames1.size() - 1);
                    fastpOutParts1 = new PathParts(fastpOutPath, fastpOutNames1, suffix1);

                    String suffix2 = fastpOutNames2.remove(fastpOutNames2.size() - 1);
                    fastpOutParts2 = new PathParts(fastpOutPath, fastpOutNames2, suffix2);
                } else {
                    fastpOutParts1 = new PathParts(fastpOutPath, fastpOutNames1, inputParts1.getSuffix());
                    fastpOutParts2 = new PathParts(fastpOutPath, fastpOutNames2, inputParts2.getSuffix());
                }

                fastpOutParts1.getNameParts().add(cleanTag);
                fastpOutParts2.getNameParts().add(cleanTag);



                String fastpOut1 = fastpOutParts1.getLinuxPath();
                String fastpOut2 = fastpOutParts2.getLinuxPath();

                fastpOutFiles.add(List.of(fastpOut1, fastpOut2));

                BaseTool fastpTool = new BaseTool();
                fastpTool.addOption(new ValueOption("--in1", inputParts1.getLinuxPath()));
                fastpTool.addOption(new ValueOption("--in2", inputParts2.getLinuxPath()));

                fastpTool.addOption(new ValueOption("--out1", fastpOut1));
                fastpTool.addOption(new ValueOption("--out2", fastpOut2));

                if (fastpDemandHtmlCheck.isSelected()) {
                    PathParts fastpHtmlParts = new PathParts(fastpOutPath, List.of(commonBases.get(i)), "html");
                    fastpTool.addOption(new ValueOption("--html", fastpHtmlParts.getLinuxPath()));
                } else {
                    fastpTool.addOption(new ValueOption("--html", BLACK_HOLE));
                }

                if (fastpDemandJsonCheck.isSelected()) {
                    PathParts fastpJsonParts = new PathParts(fastpOutPath, List.of(commonBases.get(i)), "json");
                    fastpTool.addOption(new ValueOption("--json", fastpJsonParts.getLinuxPath()));
                } else {
                    fastpTool.addOption(new ValueOption("--json", BLACK_HOLE));
                }

                fastpTool.addOption(new IntOption("--thread", Integer.parseInt(theadsNumField.getText())));

                pipeline.add(fastpTool.buildCommandPretty(PathUtils.linuxJoin(ConfigManager.getConfig().getFastpExecutableDir(), "fastp")));

            }

            List<List<String>> krakenHostOutFiles = new ArrayList<>();



            for (int i = 0; i < commonBases.size(); i++) { //kraken host

                BaseTool k2ClassifyTool = new BaseTool();

                List<String> files = fastpOutFiles.get(i);

                String input1 = files.get(0);
                String input2 = files.get(1);
                PathParts inputParts1 = PathParts.parse(input1);
                PathParts inputParts2 = PathParts.parse(input2);

                k2ClassifyTool.addOption(new ValueOption("--db", krakenHostDbDirField.getText()));
                k2ClassifyTool.addOption(new PositionOption(1, input1)).addOption(new PositionOption(2, input2));

                List<String> commonParts = PathUtils.longestCommonSubsequence(inputParts1.getTagParts(), inputParts2.getTagParts());
                commonParts.add(0, commonBases.get(i) + "#");

                PathParts krakenUnalignParts = new PathParts(krakenHostRemovalOutPath, PathParts.addTagToParts(commonParts, unclassifiedTag), "fastq");
                String krakenUnalign = krakenUnalignParts.getLinuxPath();

                k2ClassifyTool.addOption(new ValueOption("--unclassified-out", krakenUnalign));
                k2ClassifyTool.addOption(new ValueOption("--output", BLACK_HOLE));

                k2ClassifyTool.addOption(new IntOption("--threads", Integer.parseInt(theadsNumField.getText())));
                k2ClassifyTool.addOption(new ValueOption("--confidence", krakenHostConfidenceField.getText()));

                k2ClassifyTool
                        .addOption(new FlagOption("--paired", true))
                        .addOption(new FlagOption("--memory-mapping", true));

                krakenHostOutFiles.add(List.of(krakenUnalign.replace("#", "_1"), krakenUnalign.replace("#", "_2")));

                pipeline.add(k2ClassifyTool.buildCommandPretty(PathUtils.linuxJoin(ConfigManager.getConfig().getKraken2ExcutableDir(), "k2 classify")));

            }

            List<String> krakenClassifyOutFiles = new ArrayList<>();


            for (int i = 0; i < commonBases.size(); i++) { //kraken pathogen

                BaseTool k2ClassifyTool = new BaseTool();

                List<String> files = krakenHostOutFiles.get(i);

                String input1 = files.get(0);
                String input2 = files.get(1);
                PathParts inputParts1 = PathParts.parse(input1);
                PathParts inputParts2 = PathParts.parse(input2);

                k2ClassifyTool.addOption(new ValueOption("--db", krakenPathogenDbDirField.getText()));
                k2ClassifyTool.addOption(new PositionOption(1, input1)).addOption(new PositionOption(2, input2));

                List<String> commonParts = PathUtils.longestCommonSubsequence(inputParts1.getTagParts(), inputParts2.getTagParts());
                commonParts.add(0, commonBases.get(i));

                PathParts krakenClassificationParts = new PathParts(krakenClassifyOutPath, commonParts, "krk");
                String krakenClassification = krakenClassificationParts.getLinuxPath();
                k2ClassifyTool.addOption(new ValueOption("--output", krakenClassification));

                k2ClassifyTool.addOption(new IntOption("--threads", Integer.parseInt(theadsNumField.getText())));
                k2ClassifyTool.addOption(new ValueOption("--confidence", krakenPathogenConfidenceField.getText()));

                k2ClassifyTool
                        .addOption(new FlagOption("--paired", true))
                        .addOption(new FlagOption("--memory-mapping", true));

                krakenClassifyOutFiles.add(krakenClassification);


                pipeline.add(k2ClassifyTool.buildCommandPretty(PathUtils.linuxJoin(ConfigManager.getConfig().getKraken2ExcutableDir(), "k2 classify")));

            }

            //recentrifuge

            BaseTool rcfTool = new BaseTool();

            rcfTool.addOption(new ValueOption("--nodespath", taxdumpDirField.getText()));

            int caseCount = caseFiles.size();
            int controlCount = controlFiles.size();

            for (int i = caseCount; i < caseCount + controlCount ; i++) { //place control samples first
                rcfTool.addOption(new ValueOption("--kraken", krakenClassifyOutFiles.get(i)));
            }

            rcfTool.addOption(new IntOption("--controls", controlCount));

            for (int i = 0; i < caseCount; i++) {
                rcfTool.addOption(new ValueOption("--kraken", krakenClassifyOutFiles.get(i)));
            }

            Map<String, String> scoreMap = Map.of(
                    "SHEL", "SHEL",
                    "LENGTH", "LENGTH",
                    "LOGLENGTH", "LOGLENGTH",
                    "NORMA", "NORMA",
                    "KRAKEN", "KRAKEN"
            );

            String scoreValue = rcfScoreMethodCombo.getValue();
            String scoreMethod = (scoreValue == null || scoreValue.isBlank()) ? null : scoreMap.get(scoreValue);
            rcfTool.addOption(new ValueOption("--scoring", scoreMethod));

            rcfTool.addOption(new ValueOption("--minscore", rcfMinScoreField.getText()));
            rcfTool.addOption(new ValueOption("--ctrlminscore", rcfControlMinScoreField.getText()));

            rcfTool.addOption(new ValueOption("--outprefix", recentrifugeOutPath.resolve(projectNameField.getText()).toString()));

            pipeline.add(rcfTool.buildCommandPretty(PathUtils.linuxJoin(ConfigManager.getConfig().getRecentrifugeExecutableDir(), "rcf")));



        } else { //single

            List<String> bases = new ArrayList<>();
            for (List<String> file : allSampleFiles) {
                bases.add(PathParts.parse(file.get(0)).getFileName());
            }

            List<String> fastpOutFiles = new ArrayList<>();

            for (int i = 0; i < bases.size(); i++) { //fastp
                String input = allSampleFiles.get(i).get(0);

                PathParts inputParts = PathParts.parse(input);

                List<String> newNames = new ArrayList<>(inputParts.getNameParts());
                PathParts fastpOutParts;

                if (inputParts.getSuffix().equals("gz")) {
                    String newSuffix = newNames.remove(newNames.size() - 1);
                    fastpOutParts = new PathParts(fastpOutPath, newNames, newSuffix);
                } else {
                    fastpOutParts = new PathParts(fastpOutPath, newNames, inputParts.getSuffix());
                }

                fastpOutParts.getNameParts().add(cleanTag);

                String fastpOut = fastpOutParts.getLinuxPath();

                fastpOutFiles.add(fastpOut);

                BaseTool fastpTool = new BaseTool();
                fastpTool.addOption(new ValueOption("--in1", inputParts.getLinuxPath()));
                fastpTool.addOption(new ValueOption("--out1", fastpOut));

                if (fastpDemandHtmlCheck.isSelected()) {
                    PathParts fastpHtmlParts = new PathParts(fastpOutPath, bases, "html");
                    fastpTool.addOption(new ValueOption("--html", fastpHtmlParts.getLinuxPath()));
                } else {
                    fastpTool.addOption(new ValueOption("--html", BLACK_HOLE));
                }

                if (fastpDemandJsonCheck.isSelected()) {
                    PathParts fastpJsonParts = new PathParts(fastpOutPath, bases, "json");
                    fastpTool.addOption(new ValueOption("--json", fastpJsonParts.getLinuxPath()));
                } else {
                    fastpTool.addOption(new ValueOption("--json", BLACK_HOLE));
                }

                fastpTool.addOption(new IntOption("--thread", Integer.parseInt(theadsNumField.getText())));

                pipeline.add(fastpTool.buildCommandPretty(PathUtils.linuxJoin(ConfigManager.getConfig().getFastpExecutableDir(), "fastp")));


            }
            List<String> krakenHostOutFiles = new ArrayList<>();

            for (int i = 0; i < bases.size(); i++) { //kraken host

                BaseTool k2ClassifyTool = new BaseTool();

                String input = fastpOutFiles.get(i);

                PathParts inputParts = PathParts.parse(input);

                k2ClassifyTool.addOption(new ValueOption("--db", krakenHostDbDirField.getText()));
                k2ClassifyTool.addOption(new PositionOption(1, input));

                List<String> parts = new ArrayList<>(inputParts.getNameParts());

                PathParts krakenUnalignParts = new PathParts(krakenHostRemovalOutPath, PathParts.addTagToParts(parts, unclassifiedTag), "fastq");
                String krakenUnalign = krakenUnalignParts.getLinuxPath();

                k2ClassifyTool.addOption(new ValueOption("--unclassified-out", krakenUnalign));
                k2ClassifyTool.addOption(new ValueOption("--output", BLACK_HOLE));

                k2ClassifyTool.addOption(new IntOption("--threads", Integer.parseInt(theadsNumField.getText())));
                k2ClassifyTool.addOption(new ValueOption("--confidence", krakenHostConfidenceField.getText()));

                k2ClassifyTool
                        .addOption(new FlagOption("--memory-mapping", true));

                krakenHostOutFiles.add(krakenUnalign);
                pipeline.add(k2ClassifyTool.buildCommandPretty(PathUtils.linuxJoin(ConfigManager.getConfig().getKraken2ExcutableDir(), "k2 classify")));


            }

            List<String> krakenClassifyOutFiles = new ArrayList<>();


            for (int i = 0; i < bases.size(); i++) { //kraken pathogen

                BaseTool k2ClassifyTool = new BaseTool();

                String input = krakenHostOutFiles.get(i);

                PathParts inputParts = PathParts.parse(input);

                k2ClassifyTool.addOption(new ValueOption("--db", krakenPathogenDbDirField.getText()));
                k2ClassifyTool.addOption(new PositionOption(1, input));

                List<String> parts = new ArrayList<>(inputParts.getNameParts());

                PathParts krakenClassificationParts = new PathParts(krakenClassifyOutPath, parts, "krk");
                String krakenClassification = krakenClassificationParts.getLinuxPath();

                k2ClassifyTool.addOption(new ValueOption("--output", krakenClassification));

                k2ClassifyTool.addOption(new IntOption("--threads", Integer.parseInt(theadsNumField.getText())));
                k2ClassifyTool.addOption(new ValueOption("--confidence", krakenPathogenConfidenceField.getText()));

                k2ClassifyTool
                        .addOption(new FlagOption("--memory-mapping", true));

                krakenClassifyOutFiles.add(krakenClassification);


                pipeline.add(k2ClassifyTool.buildCommandPretty(PathUtils.linuxJoin(ConfigManager.getConfig().getKraken2ExcutableDir(), "k2 classify")));
            }


            //recentrifuge


            BaseTool rcfTool = new BaseTool();

            rcfTool.addOption(new ValueOption("--nodespath", taxdumpDirField.getText()));

            int caseCount = caseFiles.size();
            int controlCount = controlFiles.size();

            for (int i = caseCount; i < caseCount + controlCount ; i++) { //place control samples first
                rcfTool.addOption(new ValueOption("--kraken", krakenClassifyOutFiles.get(i)));
            }
            rcfTool.addOption(new IntOption("--controls", controlCount));

            for (int i = 0; i < caseCount; i++) {
                rcfTool.addOption(new ValueOption("--kraken", krakenClassifyOutFiles.get(i)));
            }

            Map<String, String> scoreMap = Map.of(
                    "SHEL", "SHEL",
                    "LENGTH", "LENGTH",
                    "LOGLENGTH", "LOGLENGTH",
                    "NORMA", "NORMA",
                    "KRAKEN", "KRAKEN"
            );

            String scoreValue = rcfScoreMethodCombo.getValue();
            String scoreMethod = (scoreValue == null || scoreValue.isBlank()) ? null : scoreMap.get(scoreValue);
            rcfTool.addOption(new ValueOption("--scoring", scoreMethod));

            rcfTool.addOption(new ValueOption("--minscore", rcfMinScoreField.getText()));
            rcfTool.addOption(new ValueOption("--ctrlminscore", rcfControlMinScoreField.getText()));

            rcfTool.addOption(new ValueOption("--outprefix", recentrifugeOutPath.resolve(projectNameField.getText()).toString()));

            pipeline.add(rcfTool.buildCommandPretty(PathUtils.linuxJoin(ConfigManager.getConfig().getRecentrifugeExecutableDir(), "rcf")));



        }

        // both mode
        commandTextArea.setText(pipeline.build());


    }

    private int terminalIndex = 1;

    @FXML
    private void commandRun() {

        String scriptPathLinux;

        try {
            Path projectDir = Path.of(projectDirField.getText());

            Files.createDirectories(projectDir.resolve("fastp_out"));
            Files.createDirectories(projectDir.resolve("kraken2_host_remove_out"));
            Files.createDirectories(projectDir.resolve("kraken2_classify_out"));
            Files.createDirectories(projectDir.resolve("recentrifuge_out"));

            Path script = projectDir.resolve("run.sh");

            Files.writeString(
                    script,
                    commandTextArea.getText(),
                    StandardCharsets.UTF_8
            );

            scriptPathLinux = new WindowsPath(script.toAbsolutePath().toString()).toMnt().toString();


        } catch (IOException e) {
            AppAlert.error("Directory creation failed", e.getMessage());
            return;
        }

        Path workDir = Path.of(ConfigManager.getConfig().getWorkDir());
        TerminalConfig config = new TerminalConfig();

        TerminalBuilder builder = new TerminalBuilder(config);
        builder.setTerminalPath(workDir);

        config.setWindowsTerminalStarter(ConfigManager.getConfig().getWslExecutable());


        TerminalTab terminalTab = builder.newTerminal();
        String fullTitle = "flow1" + " #" + terminalIndex++;
        terminalTab.setText(fullTitle);

        TabPane tabPane = thisTab.getTabPane();
        tabPane.getTabs().add(terminalTab);

        terminalTab.onTerminalFxReady(() -> {
            Terminal terminal = terminalTab.getTerminal();
            terminal.command("bash " + scriptPathLinux +  "\r");

            tabPane.getSelectionModel().select(terminalTab);

        });
    }


    private String getCommonBase(PathParts pathParts1, PathParts pathParts2) {
        String commonBase;
        try {
            commonBase = PathUtils.extractCommonPairedFastqBase(
                    pathParts1.getNameParts().get(0),
                    pathParts2.getNameParts().get(0));
        } catch (IllegalArgumentException e) {
            AppAlert.error("Invalid parameters", e.getMessage());
            throw e;
        }
            return commonBase;
    }


}
