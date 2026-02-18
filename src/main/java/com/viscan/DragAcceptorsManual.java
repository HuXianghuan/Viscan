package com.viscan;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class DragAcceptorsManual {

    private static final ObjectMapper mapper = new ObjectMapper();


    public static void installTextField(TextField field, boolean acceptFile, boolean acceptDir, boolean acceptLinux, boolean acceptWin) {
        install(field, acceptFile, acceptDir, acceptLinux, acceptWin, path -> field.setText(path));
    }

    public static void installListView(ListView<String> listView, boolean acceptFile, boolean acceptDir, boolean acceptLinux, boolean acceptWin) {
        install(listView, acceptFile, acceptDir, acceptLinux, acceptWin, path -> listView.getItems().add(path));
    }


    public static <T> void limitMaxItems(ListView<T> listView, IntSupplier maxItemsSupplier) {
        listView.addEventFilter(DragEvent.DRAG_OVER, e -> {
            if (listView.getItems().size() >= maxItemsSupplier.getAsInt()) {
                e.consume();
            }
        });

        listView.addEventFilter(DragEvent.DRAG_DROPPED, e -> {
            if (listView.getItems().size() >= maxItemsSupplier.getAsInt()) {
                e.setDropCompleted(false);
                e.consume();
            }
        });
    }


    private static void install(Node node,
                                boolean acceptFile,
                                boolean acceptDir,
                                boolean acceptLinux,
                                boolean acceptWin,
                                Consumer<String> onAccept) {

        AcceptRule rule = new AcceptRule(acceptFile, acceptDir, acceptLinux, acceptWin);

        node.setOnDragOver(e -> {
            Dragboard db = e.getDragboard();
            if (!db.hasString()) return;

            try {
                List<FileItem> items = readItems(db);
                boolean anyMatch = items.stream().anyMatch(rule::matches);


                if (anyMatch) {
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
                List<FileItem> items = readItems(db);

                for (FileItem item: items) {
                    if (!rule.matches(item)) continue;
                    onAccept.accept(rule.extractPath(item));
                }

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
        private final boolean acceptLinux;
        private final boolean acceptWin;

        public AcceptRule(boolean acceptFile, boolean acceptDir, boolean acceptLinux, boolean acceptWin) {
            this.acceptFile = acceptFile;
            this.acceptDir = acceptDir;
            this.acceptLinux = acceptLinux;
            this.acceptWin = acceptWin;
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

    private static List<FileItem> readItems(Dragboard db) throws Exception {
        String json = db.getString();

        try {
            return  mapper.readValue(
                    json,
                    mapper.getTypeFactory()
                            .constructCollectionType(List.class, FileItem.class)
            );
        } catch (Exception ignore) {
            FileItem single = mapper.readValue(json, FileItem.class);
            return List.of(single);
        }
    }

}
