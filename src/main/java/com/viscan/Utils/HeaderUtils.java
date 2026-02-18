package com.viscan.Utils;

import com.viscan.FileItem;

import java.util.List;
import java.util.Objects;

public class HeaderUtils {
    public static boolean hasHeader(List<FileItem> items, String title) {
        return items.stream().anyMatch(fi -> fi.isHeader() && Objects.equals(fi.getName(), title));
    }

    public static int findHeaderIndex(List<FileItem> items, String title) {
        for (int i = 0; i < items.size(); i++) {
            FileItem fi = items.get(i);
            if (fi.isHeader() && Objects.equals(fi.getName(), title)) {
                return i;
            }
        }

        return -1;
    }

    public static int findLastItemUnderHeader(List<FileItem> items, String title) {
        int headerIndex = findHeaderIndex(items, title);
        if (headerIndex < 0) return -1;

        int last = headerIndex;

        for (int i = headerIndex + 1; i < items.size(); i++) {
            FileItem fi = items.get(i);
            if (fi.isHeader()) break;
            last = i;

        }
        return last;
    }

    public static FileItem findHeaderForItem(
            List<FileItem> items,
            FileItem item
    ) {
        int index = items.indexOf(item);
        if (index <= 0) return null;

        for (int i = index - 1; i >= 0; i--) {
            FileItem fi = items.get(i);
            if (fi.isHeader()) return fi;
        }
        return null;
    }
}
