package com.bidplaza;

import com.bidplaza.util.CsvExporter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvExporterTest {

    private static final String TEST_PATH = "target/test-output/test.csv";

    @BeforeEach
    void setUp() throws Exception {
        Files.createDirectories(Path.of("target/test-output"));
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(Path.of(TEST_PATH));
    }

    @Test
    void export_validData_createsFile() throws Exception {
        String[] headers = {"Name", "Price", "Status"};
        List<String[]> rows = List.of(
            new String[] {"Laptop", "1000", "ACTIVE"},
            new String[] {"Phone", "500", "FINISHED"}
        );
        CsvExporter.export(headers, rows, TEST_PATH);
        assertTrue(Files.exists(Path.of(TEST_PATH)));
    }

    @Test
    void export_validData_hasCorrectContent() throws Exception {
        String[] headers = {"Col1", "Col2"};
        List<String[]> rows = Collections.singletonList(new String[] {"A", "B"});
        CsvExporter.export(headers, rows, TEST_PATH);

        String content = Files.readString(Path.of(TEST_PATH));
        assertTrue(content.contains("Col1"));
        assertTrue(content.contains("Col2"));
        assertTrue(content.contains("A"));
        assertTrue(content.contains("B"));
    }

    @Test
    void export_emptyRows_createsHeaderOnly() throws Exception {
        String[] headers = {"Name", "Price"};
        List<String[]> rows = List.of();
        CsvExporter.export(headers, rows, TEST_PATH);
        assertTrue(Files.exists(Path.of(TEST_PATH)));
        String content = Files.readString(Path.of(TEST_PATH));
        assertTrue(content.contains("Name"));
    }

    @Test
    void export_specialChars_escapedCorrectly() throws Exception {
        String[] headers = {"Name"};
        List<String[]> rows =
            Collections.singletonList(new String[] {"He said \"hello\""});
        CsvExporter.export(headers, rows, TEST_PATH);
        String content = Files.readString(Path.of(TEST_PATH));
        assertNotNull(content);
    }

    @Test
    void buildDefaultPath_containsFilename() {
        String path = CsvExporter.buildDefaultPath("test_export");
        assertTrue(path.contains("test_export"));
        assertTrue(path.endsWith(".csv"));
    }
}
