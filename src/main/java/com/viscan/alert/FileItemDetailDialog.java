package com.viscan.alert;

import com.viscan.FileItem;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.Instant;
import java.time.ZoneId;

public final class FileItemDetailDialog {
    public static void show(FileItem fi) {
        Stage stage = new Stage();
        stage.setTitle("File Detail");
        stage.setResizable(false);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(12));

        int row = 0;

        row = addRow(grid, row, "Name:", fi.getName());
        row = addRow(grid, row, "Type:", fi.isDir() ? "Directory" : "File");
        row = addRow(grid, row, "Size:", formatSize(fi.getFileSize()));
        row = addRow(grid, row, "Last Modified:", formatTime(fi.getLastModified()));

        row = addSeparator(grid, row);

        row = addRow(grid, row, "Windows Path:", fi.getWinPath());
        row = addRow(grid, row, "Linux Path:", fi.getLinuxPath());


        Scene scene = new Scene(grid);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }


    private static int addRow(GridPane grid, int row, String key, String value) {
        Label keyLabel = new Label(key);
        keyLabel.setStyle("-fx-font-weight: bold;");

        TextField valueField = new TextField(value == null ? "" : value);
        valueField.setEditable(false);
        valueField.setPrefColumnCount(40);

        grid.add(keyLabel, 0, row);
        grid.add(valueField, 1, row);

        return row + 1;
    }
    private static int addSeparator(GridPane grid, int row) {
        Separator sep = new Separator();
        grid.add(sep, 0, row, 2, 1);
        return row + 1;
    }

    private static String formatSize(long size) {
        if (size < 0) return "—";
        double v = size;
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int idx = 0;
        while (v >= 1024 && idx < units.length - 1) {
            v /= 1024;
            idx++;
        }
        return String.format("%.2f %s", v, units[idx]);
    }

    private static String formatTime(long millis) {
        if (millis <= 0) return "—";
        return Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .toString()
                .replace('T', ' ');
    }


}
