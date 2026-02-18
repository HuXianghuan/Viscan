package com.viscan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viscan.Utils.HeaderUtils;
import com.viscan.alert.AppAlert;
import com.viscan.alert.FileItemDetailDialog;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.MenuItem;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ListViewBehaviors {
    private ListViewBehaviors() {}

    public static void installReorder(ListView<FileItem> listView, ToggleButton reorderButton) {
        listView.setCellFactory(lv -> {
            ListCell<FileItem> cell = new ListCell<>() {
                @Override
                protected void updateItem(FileItem item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                        getStyleClass().remove("list-header-cell");
                        return;
                    }

                    if (item.isHeader()) {
                        String arrow = item.isCollapsed() ? "▸ " : "▾ ";
                        setText(arrow + item.getName());


                        if (!getStyleClass().contains("list-header-cell")) {
                            getStyleClass().add("list-header-cell");
                        }
                        setFocusTraversable(false);
                    } else {
                        setText(item.getName());
                        getStyleClass().remove("list-header-cell");
                        setFocusTraversable(true);
                    }
                }
            };

            cell.setOnMouseClicked(e -> {
                FileItem item = cell.getItem();
                if (item == null || !item.isHeader()) return;

                if (e.getClickCount() == 1) {
                    item.setCollapsed(!item.isCollapsed());

                    Object obj = listView.getProperties().get("filteredItems");
                    if (obj instanceof FilteredList<?> fl) {
                        @SuppressWarnings("unchecked")
                        FilteredList<FileItem> filtered = (FilteredList<FileItem>) fl;

                        filtered.setPredicate(f -> {
                            if (f.isHeader()) return true;
                            FileItem h = HeaderUtils.findHeaderForItem(
                                    (List<FileItem>) filtered.getSource(), f
                            );
                            return h == null || !h.isCollapsed();
                        });
                    }
                }
            });


            cell.setOnDragDetected(event -> {
                if (cell.getItem() == null || cell.getItem().isHeader() || !reorderButton.isSelected()) return;

                ObservableList<FileItem> selectItems = listView.getSelectionModel().getSelectedItems();
                if (selectItems.isEmpty()) return;

                Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent cc = new ClipboardContent();
                cc.putString("__REORDER__");
                db.setContent(cc);

                listView.getProperties().put("draggedItems", new ArrayList<>(selectItems));
                event.consume();
            });

            cell.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasString() && "__REORDER__".equals(db.getString()) && reorderButton.isSelected() && cell.getItem() != null) {
                    event.acceptTransferModes(TransferMode.MOVE);
                    event.consume();
                }

            });


            cell.setOnDragDropped(event -> {
                if (!reorderButton.isSelected()) return;

                Dragboard db = event.getDragboard();
                if (!db.hasString() || !db.getString().equals("__REORDER__")) {
                    event.setDropCompleted(false);
                    return;
                }

                Object obj = listView.getProperties().get("draggedItems");
                if (!(obj instanceof List<?>dragged)) {
                    event.setDropCompleted(false);
                    return;
                }


                List<FileItem> draggedItems = new ArrayList<>();
                for (Object o : dragged) if (o instanceof FileItem fi) draggedItems.add(fi);

                if (draggedItems.isEmpty()) {
                    event.setDropCompleted(false);
                    return;
                }
                Object filteredObj = listView.getProperties().get("filteredItems");
                if (!(filteredObj instanceof FilteredList<?> fl)) {
                    event.setDropCompleted(false);
                    return;
                }

                @SuppressWarnings("unchecked")
                ObservableList<FileItem> masterItems = (ObservableList<FileItem>) ((FilteredList<FileItem>) fl).getSource();

                int dropIndex = masterItems.indexOf(cell.getItem());
                int firstIndex = masterItems.indexOf(draggedItems.get(0));
                if (firstIndex < dropIndex) dropIndex = dropIndex - draggedItems.size() + 1;

                masterItems.removeAll(draggedItems);
                masterItems.addAll(dropIndex, draggedItems);

                FilteredList<FileItem> filtered = (FilteredList<FileItem>) listView.getProperties().get("filteredItems");

                listView.getSelectionModel().clearSelection();

                for (FileItem fi : draggedItems) {
                    int idx = filtered.indexOf(fi);
                    if (idx >= 0) listView.getSelectionModel().select(idx);
                }

                listView.getProperties().remove("draggedItems");
                event.setDropCompleted(true);
                event.consume();
            });

            return cell;
        });

    }

    public static void installExternalJsonDrag(ListView<FileItem> listView, ObjectMapper mapper, ToggleButton reorderButton) {
        listView.setOnDragDetected(event -> {
            if (listView.getSelectionModel().getSelectedItems().isEmpty()) return;
            if (reorderButton.isSelected()) return; //reoder mode takes priority

            Dragboard db = listView.startDragAndDrop(TransferMode.COPY);
            ClipboardContent cc = new ClipboardContent();

            try {
                cc.putString(mapper.writeValueAsString(listView.getSelectionModel().getSelectedItems()));
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            db.setContent(cc);
            event.consume();
        });
    }




    public static void installExternalFileDrop(
            ListView<FileItem> listView,
            ObservableList<FileItem> fileItems,
            ToggleButton reorderButton
    ) {
        listView.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();

            if (db.hasFiles() && !reorderButton.isSelected()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        listView.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();

            if (!db.hasFiles() || reorderButton.isSelected()) {
                event.setDropCompleted(false);
                return;
            }

            List<File> files = new ArrayList<>(db.getFiles());
            files.sort(Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));

            if (files == null || files.isEmpty()) {
                event.setDropCompleted(false);
                return;
            }

            int insertIndex = HeaderUtils.findLastItemUnderHeader(fileItems, "import") + 1;

            for (File file : files) {
                FileItem item = FileItem.fromWinPath(file);
                fileItems.add(insertIndex++, item);
            }

            event.setDropCompleted(true);
            event.consume();
        });
    }



    public static void installContextMenu(ListView<FileItem> listView) {
        ContextMenu menu = new ContextMenu();

        MenuItem openExplorer = new MenuItem("Open in explorer");
        MenuItem remove = new MenuItem("Remove selected");
        MenuItem detail = new MenuItem("Detail information");

        openExplorer.setOnAction(e -> {
            List<FileItem> selected = listView.getSelectionModel().getSelectedItems();
            if (selected.size() != 1) return;

            FileItem fi = selected.get(0);
            if (fi.isHeader()) return;

            File file = new File(fi.getWinPath());
            if (!file.exists()) {
                AppAlert.error("File not found", "The file has been deleted or moved.");
                return;
            }

            try {
                Desktop desktop = Desktop.getDesktop();
                if (file.isDirectory()) {
                    desktop.open(file);
                } else {
                    File parent = file.getParentFile();
                    if (parent != null && parent.exists()) {
                        desktop.open(parent);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        remove.setOnAction(e -> {
            List<FileItem> toRemove = new ArrayList<>(
                    listView.getSelectionModel()
                            .getSelectedItems()
                            .stream()
                            .filter(fi -> !fi.isHeader())
                            .toList()
            );

            Object obj = listView.getProperties().get("filteredItems");
            if (obj instanceof FilteredList<?> fl) {
                @SuppressWarnings("unchecked")
                FilteredList<FileItem> filtered = (FilteredList<FileItem>) fl;
                filtered.getSource().removeAll(toRemove);
            }

        });

        detail.setOnAction(e -> {
            FileItem fi = listView.getSelectionModel().getSelectedItem();
            if (fi == null || fi.isHeader()) return;

            FileItemDetailDialog.show(fi);
        });

        menu.getItems().addAll(
                openExplorer,
                new SeparatorMenuItem(),
                detail,
                new SeparatorMenuItem(),
                remove
        );

        menu.setOnShowing(e -> {
            List<FileItem> selected = listView.getSelectionModel().getSelectedItems();
            boolean single = selected.size() == 1;
            boolean valid = single && !selected.get(0).isHeader();

            openExplorer.setDisable(!valid);
            detail.setDisable(!valid);
            remove.setDisable(selected.isEmpty());
        });

        listView.setContextMenu(menu);


    }

    public static FilteredList<FileItem> installHeaderCollapse(
            ListView<FileItem> listView,
            ObservableList<FileItem> masterItems
    ) {
        FilteredList<FileItem> filtered = new FilteredList<>(masterItems);

        filtered.setPredicate(item -> {
            if (item.isHeader()) return true;

            FileItem header = HeaderUtils.findHeaderForItem(masterItems, item);
            return header == null || !header.isCollapsed();
        });

        listView.getProperties().put("filteredItems", filtered);

        return filtered;
    }


}
