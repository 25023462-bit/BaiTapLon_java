package com.bidplaza.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CsvExporter {

    public static void export(String[] headers, List<String[]> rows,
                              String filePath) throws IOException {
        Files.createDirectories(Path.of(filePath).getParent());
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(
                    new FileOutputStream(filePath), StandardCharsets.UTF_8))) {

            // UTF-8 BOM for Excel compatibility
            writer.print('\ufeff');

            // Header row
            writer.println(String.join(",", headers));

            // Data rows
            for (String[] row : rows) {
                writer.println(Arrays.stream(row)
                    .map(cell -> "\"" + (cell == null ? "" : cell.replace("\"", "\"\"")) + "\"")
                    .collect(Collectors.joining(",")));
            }
        }
    }

    public static String buildDefaultPath(String filename) {
        return System.getProperty("user.home")
            + File.separator + "Downloads"
            + File.separator + filename + "_"
            + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            + ".csv";
    }
}
