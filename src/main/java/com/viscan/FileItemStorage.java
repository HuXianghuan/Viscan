package com.viscan;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileItemStorage {
    private static final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public static void save(List<FileItem> items, Path jsonFile) throws IOException {
        if (jsonFile == null) throw new IllegalArgumentException("jsonFile cannot be null");

        mapper.writeValue(jsonFile.toFile(), items);
    }


    public static List<FileItem> load(Path jsonFile) throws IOException{
        if (jsonFile == null) throw  new IllegalArgumentException("jsonFile cannot be null");

        if (!Files.exists(jsonFile)) {
            return List.of();
        }

        return mapper.readValue(jsonFile.toFile(), new TypeReference<List<FileItem>>() {});
    }

}
