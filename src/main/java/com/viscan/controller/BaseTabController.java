package com.viscan.controller;

import com.kodedu.terminalfx.Terminal;
import com.kodedu.terminalfx.TerminalBuilder;
import com.kodedu.terminalfx.TerminalTab;
import com.kodedu.terminalfx.config.TerminalConfig;
import com.viscan.ConfigManager;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.nio.file.Path;


public class BaseTabController {

    @FXML
    protected AnchorPane tabRoot;

    protected String BLACK_HOLE = "/dev/null";


    @FXML
    public void initialize() {

    }

    public static void trimListViewToMax(ListView<String> listView, int maxItems) {
        ObservableList<String> items = listView.getItems();
        if (items.size() > maxItems) {
            items.remove(maxItems, items.size());
        }
    }


    @FXML
    protected void handleClear(ActionEvent event) {
        Button btn = (Button) event.getSource();
        String targetId = (String) btn.getUserData();

        Node target = tabRoot.lookup("#" + targetId);

        if (target instanceof TextInputControl tic) {
            tic.clear();
        } else if (target instanceof ListView<?> lv) {
            lv.getItems().clear();
        } else if (target instanceof ComboBox<?> cb) {
        cb.getSelectionModel().clearSelection();
    }

    }



    protected void commandRun(TabPane tabPane, TextArea commandTextArea, String title, int index) {
        Path workDir = Path.of(ConfigManager.getConfig().getWorkDir());
        TerminalConfig config = new TerminalConfig();

        TerminalBuilder builder = new TerminalBuilder(config);
        builder.setTerminalPath(workDir);

        config.setWindowsTerminalStarter(ConfigManager.getConfig().getWslExecutable());

        TerminalTab terminalTab = builder.newTerminal();
        String fullTitle = title + " #" + index;
        terminalTab.setText(fullTitle);

        tabPane.getTabs().add(terminalTab);

        terminalTab.onTerminalFxReady(() -> {
            Terminal terminal = terminalTab.getTerminal();
            terminal.command(commandTextArea.getText() + "\r");

            tabPane.getSelectionModel().select(terminalTab);

        });


    }

    protected static String quote(String text) {
        return "\"" + text + "\"";
    }



}
