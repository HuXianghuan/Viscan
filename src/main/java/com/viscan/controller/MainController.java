package com.viscan.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.viscan.*;
import com.viscan.Utils.HeaderUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainController {


    @FXML
    private TabPane tabPane;

    @FXML
    private ListView<FileItem> mainListView;

    @FXML
    private ToggleButton reorderButton;


    private ObservableList<FileItem> fileItems;


    private final Map<String, CommandPage> openedPages = new HashMap<>();

    private static final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() throws IOException {
        fileItems = FXCollections.observableArrayList();
        List<FileItem> loaded = FileItemStorage.load(Path.of("file-items.json"));
        fileItems.setAll(loaded);

        FilteredList<FileItem> filtered = ListViewBehaviors.installHeaderCollapse(mainListView, fileItems);

        mainListView.setItems(filtered);
        mainListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        mainListView.getSelectionModel()
                .getSelectedItems()
                .addListener((ListChangeListener<FileItem>) c -> {
                    while (c.next()) {
                        if (c.wasAdded()) {
                            for (FileItem fi : c.getAddedSubList()) {
                                if (fi.isHeader()) {
                                    int index = mainListView.getItems().indexOf(fi);
                                    if (index >= 0) {
                                        Platform.runLater(() ->
                                                mainListView.getSelectionModel().clearSelection(index)
                                        );
                                    }
                                }
                            }
                        }
                    }
                });


        ListViewBehaviors.installReorder(mainListView, reorderButton);

        ListViewBehaviors.installExternalJsonDrag(mainListView, mapper, reorderButton);

        ListViewBehaviors.installContextMenu(mainListView);

        List<String> presetHeaders = List.of("import", "fastp output", "Bowtie2 output", "Kraken2 output", "Recentrifuge output");

        for (String headerTitle : presetHeaders) {
            if (!HeaderUtils.hasHeader(fileItems, headerTitle)) {
                fileItems.add(FileItem.header(headerTitle));
            }
        }

        ListViewBehaviors.installExternalFileDrop(mainListView, fileItems, reorderButton);


    }


    private void openPage(String id) {
        if (openedPages.containsKey(id)) {
            tabPane.getSelectionModel().select(openedPages.get(id).getTab());
            return;
        }

        CommandPage newPage = PageRegistry.getPageById(id);
        newPage.loadTab();
        newPage.getTab().setOnClosed(event -> {
            openedPages.remove(id);
        });

        tabPane.getTabs().add(newPage.getTab());

        openedPages.put(id, newPage);

        tabPane.getSelectionModel().select(newPage.getTab());
    }


    @FXML
    private void handleMenuAction(ActionEvent event) {
        MenuItem source = (MenuItem) event.getSource();
        String pageId = source.getId();

        openPage(pageId);
    }


    @FXML
    private void addFileToList(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select files");
        Window window = ((Node) event.getSource()).getScene().getWindow();

        List<File> files = fc.showOpenMultipleDialog(window);

        if (files == null || files.isEmpty()) {
            return;
        }

        for (File file : files) {
            FileItem item = FileItem.fromWinPath(file);
            int insertIndex = HeaderUtils.findLastItemUnderHeader(fileItems, "import") + 1;
            fileItems.add(insertIndex, item);
        }
    }

    @FXML
    private void addDirToList(ActionEvent event) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Select a directory");

        Window window = ((Node) event.getSource()).getScene().getWindow();

        File dir = dc.showDialog(window);

        if (dir == null) {
            return;
        }

        FileItem item = FileItem.fromWinPath(dir);
        int insertIndex = HeaderUtils.findLastItemUnderHeader(fileItems, "import") + 1;
        fileItems.add(insertIndex, item);
    }


    public ObservableList<FileItem> getFileItems() {
        return fileItems;
    }


}
