package com.viscan;

import javafx.scene.control.ListCell;


public class FileItemCell extends ListCell<FileItem> {



    public FileItemCell() {

    }

    @Override
    protected void updateItem(FileItem item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            this.setText(null);
        } else {
            this.setText(item.getName());
        }


    }


}
