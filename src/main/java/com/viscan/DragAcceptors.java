package com.viscan;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.util.function.Consumer;


public class DragAcceptors {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void installAll(Node node) {
        if (node instanceof TextField) {
            TextField tf = (TextField) node;
            installTextField(tf);
        } else if (node instanceof ListView<?>) {
            //todo suppress
            ListView<String> list = (ListView<String>) node;
            installListView(list);
        }

    }


    public static void installTextField(TextField field) {
        install(field, path -> field.setText(path));
    }

    public static void installListView(ListView<String> listView) {
        install(listView, path -> listView.getItems().add(path));
    }

    private static void install(Node node, Consumer<String> onAccept) {

        AcceptRule rule = AcceptRule.from(node);

        node.setOnDragOver(e -> {
            Dragboard db = e.getDragboard();
            if (!db.hasString()) return;

            try {
                FileItem item = mapper.readValue(db.getString(), FileItem.class);
                if (rule.matches(item)) {
                    e.acceptTransferModes(TransferMode.COPY);
                }

            } catch (Exception ignored) {}

            e.consume();
        });

        node.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            if (!db.hasString()) {
                e.setDropCompleted(false);
                return;
            }

            try {
                FileItem item = mapper.readValue(db.getString(), FileItem.class);
                if (!rule.matches(item)) {
                    e.setDropCompleted(false);
                    return;
                }

                onAccept.accept(rule.extractPath(item));
                e.setDropCompleted(true);

            } catch (Exception ex) {
                e.setDropCompleted(false);
            }

            e.consume();

        });

    }


    private static final class AcceptRule {

        private final boolean acceptFile;
        private final boolean acceptDir;
        private final boolean acceptWin;
        private final boolean acceptLinux;

        public AcceptRule(boolean acceptFile, boolean acceptDir, boolean acceptWin, boolean acceptLinux) {
            this.acceptFile = acceptFile;
            this.acceptDir = acceptDir;
            this.acceptWin = acceptWin;
            this.acceptLinux = acceptLinux;
        }

        static AcceptRule from(Node node) {
            ObservableList<String> styles = node.getStyleClass();

            boolean file = styles.contains("accept-file");
            boolean dir = styles.contains("accept-dir");
            boolean win = styles.contains("accept-win");
            boolean linux = styles.contains("accept-linux");

            //default accepting both file and dir when no css tag
            if (!file && !dir) {
                file = true;
                dir = true;
            }

            //accept both linux and win
            if (!win && !linux) {
                win = true;
                linux = true;
            }

            return new AcceptRule(file, dir, win, linux);
        }

        boolean matches(FileItem item) {
            //input dir but element only have file tag
            if (item.isDir() && !acceptDir) return false;
            //input file but element only have dir tag
            if (!item.isDir() && !acceptFile) return false;

            return true;
        }

        String extractPath(FileItem item) {
            if (acceptLinux) {
                return item.getLinuxPath();
            }
            if (acceptWin) {
                return item.getWinPath();
            }

            //in case
            return item.getLinuxPath();

        }
    }


}
