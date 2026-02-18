package com.viscan;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandPage {

    private final String id;
    private String title;
    private final String fxmlPath;
    private Tab tab;
    private Object controller;

    private static final Logger logger = Logger.getLogger(CommandPage.class.getName());

    public CommandPage(String fxmlPath, String id) {
        this.fxmlPath = fxmlPath;
        this.id = id;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Tab getTab() {
        return tab;
    }

    public void setTab(Tab tab) {
        this.tab = tab;
    }

    public String getFxmlPath() {
        return fxmlPath;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }


    public void loadTab() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            TabPane loadedTabPane = loader.load();
            this.controller = loader.getController();

            Tab tab = null;
            if (!loadedTabPane.getTabs().isEmpty()) {
                tab = loadedTabPane.getTabs().get(0);
                tab.setClosable(true);
            }

            this.tab = tab;
            this.title = tab.getText();

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Fail to Load FXML: " + fxmlPath, e);
            this.tab = new Tab(title + "(error)");

        }
    }
}
