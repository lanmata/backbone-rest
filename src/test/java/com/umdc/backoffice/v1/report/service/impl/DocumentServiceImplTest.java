/*
 *  @(#)DocumentServiceImplTest.java
 *
 *  Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *
 *   All rights to this product are owned by Luis Antonio Mata Mata and may only
 *  be used under the terms of its associated license document. You may NOT
 *  copy, modify, sublicense, or distribute this source file or portions of
 *  it unless previously authorized in writing by Luis Antonio Mata Mata.
 *  In any event, this notice and the above copyright must always be included
 *  verbatim with this file.
 */
package com.umdc.backoffice.v1.report.service.impl;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Luis Mata
 */
class DocumentServiceImplTest {

    @TempDir
    Path tempDir;

    private DocumentServiceImpl documentService;

    @BeforeEach
    void setUp() {
        documentService = new DocumentServiceImpl(tempDir.toString());
    }

    @Test
    @DisplayName("process replaces a {placeholder} in a paragraph and returns 200")
    void processReplacesParagraphPlaceholder() throws IOException {
        MockMultipartFile template = docxWithParagraph("Hello {name}!");

        ResponseEntity<org.springframework.core.io.Resource> response =
                documentService.process(Map.of("name", "Alice"), template);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().exists());
        assertTrue(readParagraphText(response.getBody().getInputStream()).contains("Hello Alice!"));
    }

    @Test
    @DisplayName("process replaces a {placeholder} inside a table cell")
    void processReplacesTablePlaceholder() throws IOException {
        MockMultipartFile template = docxWithTableCell("{city}");

        ResponseEntity<org.springframework.core.io.Resource> response =
                documentService.process(Map.of("city", "Managua"), template);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(readParagraphText(response.getBody().getInputStream()).contains("Managua"));
    }

    @Test
    @DisplayName("process returns 400 when the uploaded file is not a valid .docx")
    void processReturnsBadRequestForInvalidFile() {
        MockMultipartFile garbage = new MockMultipartFile("documentTemplate", "not-a-docx.docx",
                "application/octet-stream", "this is not a zip".getBytes());

        ResponseEntity<org.springframework.core.io.Resource> response =
                documentService.process(Map.of(), garbage);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("process strips path-traversal segments from the uploaded file name")
    void processSanitizesPathTraversalInOriginalFilename() throws IOException {
        Path outsideDir = tempDir.getParent().resolve("outside-" + System.nanoTime());
        java.nio.file.Files.createDirectories(outsideDir);
        MockMultipartFile template = docxWithParagraphAndName("no placeholders",
                "../" + outsideDir.getFileName() + "/evil.docx");

        ResponseEntity<org.springframework.core.io.Resource> response =
                documentService.process(Map.of(), template);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        // The generated file must land inside tempDir, never in the traversal target.
        assertTrue(response.getBody().getFile().toPath().normalize().startsWith(tempDir));
        assertTrue(java.nio.file.Files.list(outsideDir).findAny().isEmpty());
    }

    @Test
    @DisplayName("findPlaceholderValues finds a ~{placeholder}~ in a paragraph")
    void findPlaceholderValuesFindsParagraphPlaceholder() throws IOException {
        MockMultipartFile template = docxWithParagraph("Dear ~{firstname}~,");

        ResponseEntity<java.util.List<String>> response = documentService.findPlaceholderValues(template);

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("~{firstname}~"));
    }

    @Test
    @DisplayName("findPlaceholderValues finds a ~{placeholder}~ inside a table cell")
    void findPlaceholderValuesFindsTablePlaceholder() throws IOException {
        MockMultipartFile template = docxWithTableCell("~{amount}~");

        ResponseEntity<java.util.List<String>> response = documentService.findPlaceholderValues(template);

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertTrue(response.getBody().stream().anyMatch(v -> v.contains("~{amount}~")));
    }

    @Test
    @DisplayName("findPlaceholderValues returns 404 when no placeholders are present")
    void findPlaceholderValuesReturnsNotFoundWhenNonePresent() throws IOException {
        MockMultipartFile template = docxWithParagraph("No placeholders here.");

        ResponseEntity<java.util.List<String>> response = documentService.findPlaceholderValues(template);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private MockMultipartFile docxWithParagraph(String text) throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText(text);
            return toMultipartFile(document);
        }
    }

    private MockMultipartFile docxWithParagraphAndName(String text, String originalFilename) throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText(text);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.write(out);
            return new MockMultipartFile("documentTemplate", originalFilename,
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document", out.toByteArray());
        }
    }

    private MockMultipartFile docxWithTableCell(String text) throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFTable table = document.createTable(1, 1);
            XWPFParagraph paragraph = table.getRow(0).getCell(0).getParagraphs().get(0);
            XWPFRun run = paragraph.createRun();
            run.setText(text);
            return toMultipartFile(document);
        }
    }

    private MockMultipartFile toMultipartFile(XWPFDocument document) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.write(out);
        return new MockMultipartFile("documentTemplate", "template.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", out.toByteArray());
    }

    private String readParagraphText(java.io.InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder sb = new StringBuilder();
            document.getBodyElements().forEach(el -> {
                if (el instanceof XWPFParagraph p) {
                    sb.append(p.getText());
                } else if (el instanceof XWPFTable t) {
                    t.getRows().forEach(row -> row.getTableCells()
                            .forEach(cell -> cell.getParagraphs().forEach(p -> sb.append(p.getText()))));
                }
            });
            return sb.toString();
        }
    }
}
