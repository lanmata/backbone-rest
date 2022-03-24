/*
 * @(#)$file.className.java.
 *
 * Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *
 * All rights to this product are owned by Luis Antonio Mata Mata and may only
 * be used under the terms of its associated license document. You may NOT
 * copy, modify, sublicense, or distribute this source file or portions of
 * it unless previously authorized in writing by Luis Antonio Mata Mata.
 * In any event, this notice and the above copyright must always be included
 * verbatim with this file.
 */

package com.prx.backoffice.v1.report.service.impl;

import com.prx.backoffice.v1.report.service.DocumentService;
import com.prx.commons.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * DocumentServiceImpl.
 *
 * @author &lt;a href='mailto:luis.antonio.mata@gmail.com'&gt;Luis Antonio Mata&lt;/a&gt;
 * @version 1.0.0, 27-12-2021
 * @since 11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
    private static final String BACKUP_PATH = "\\ambients\\tempo\\templates\\backup\\";
    private static final String TEMPLATE_PATH = "\\ambients\\tempo\\templates\\";
    private static final String CURLY_BRACE_OPEN = "{";
    private static final String CURLY_BRACE_CLOSE = "}";
    private static final String REGEX = "~\\{\\w+\\}~";

    @Override
    public ResponseEntity<Resource> process(Map<String, String> values, MultipartFile documentTemplate) {
        try {
            var filenameResult = writeDocument(documentTemplate, values);
            if(null != filenameResult && !filenameResult.isEmpty()){
                return ResponseEntity.ok(new FileSystemResource(BACKUP_PATH + filenameResult));
            }
        } catch (Exception e) {
            log.warn("Fail read file.", e);
        }
        return ResponseEntity.badRequest().build();
    }

    @Override
    public ResponseEntity<List<String>> findPlaceholderValues(MultipartFile documentTemplate) {
        var result = findPlaceholder(documentTemplate);
        if(result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.FOUND).body(result);
    }

    private List<String> findPlaceholder(MultipartFile documentTemplate) {
        List<String> placeholdersResult = new ArrayList<>();
        try (XWPFDocument xwpfDocument = new XWPFDocument(documentTemplate.getInputStream())) {
            final var elementList = xwpfDocument.getBodyElements();
            elementList.forEach(iBodyElement -> {
                if(iBodyElement instanceof XWPFParagraph) {
                    var runs = ((XWPFParagraph) iBodyElement).getRuns();
                    if(null != runs) {
                        runs.forEach(xwpfRun -> placeholdersResult.add(find(xwpfRun)));
                    }
                } else if(iBodyElement instanceof XWPFTable) {
                    var rows = ((XWPFTable)  iBodyElement).getRows();
                    rows.forEach(xwpfTableRow -> xwpfTableRow.getTableCells()
                            .forEach(xwpfTableCell -> xwpfTableCell.getParagraphs()
                                    .forEach(xwpfParagraph -> placeholdersResult.addAll(find(xwpfParagraph)))));
                }
            });
        } catch (IOException e) {
            log.error("Can't write the templateDocument parameter.", e);
        }
        return placeholdersResult;
    }

    private String writeDocument(MultipartFile documentTemplate, Map<String, String> placeholders){
        var dateFormatValue = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateUtil.PATTERN_DATETIME_YYMMDDHHMMSS));
        String filenameResult = null;
        try (XWPFDocument xwpfDocument = new XWPFDocument(documentTemplate.getInputStream())) {
            final var elementList = xwpfDocument.getBodyElements();
            elementList.forEach(iBodyElement -> {
                if(iBodyElement instanceof XWPFParagraph) {
                    var runs = ((XWPFParagraph) iBodyElement).getRuns();
                    if(null != runs) {
                        runs.forEach(xwpfRun -> replace(xwpfRun, placeholders));
                    }
                } else if(iBodyElement instanceof XWPFTable) {
                    var rows = ((XWPFTable)  iBodyElement).getRows();
                    rows.forEach(xwpfTableRow -> xwpfTableRow.getTableCells()
                            .forEach(xwpfTableCell -> xwpfTableCell.getParagraphs()
                                    .forEach(xwpfParagraph -> replace(xwpfParagraph, placeholders))));
                }
            });
            filenameResult =  dateFormatValue + documentTemplate.getOriginalFilename();
            try(var fileOutputStream = new FileOutputStream(BACKUP_PATH + filenameResult)){
                xwpfDocument.write(fileOutputStream);
            }
        } catch (IOException e) {
            log.error("Can't write the templateDocument parameter.", e);
        }
        return filenameResult;
    }

    private void replace(XWPFRun xwpfRun, Map<String, String> placeholders) {
        var text = xwpfRun.getText(0);
        if (null != text) {
            placeholders.forEach((key, value) -> {
                var placeholder = CURLY_BRACE_OPEN + key + CURLY_BRACE_CLOSE;
                if (text.contains(placeholder)) {
                    xwpfRun.setText(text.replace(placeholder, value), 0);
                }
            });
        }
    }

    private void replace(XWPFParagraph xwpfParagraph, Map<String, String> placeholders) {
        var runs = xwpfParagraph.getRuns();
        runs.forEach(xwpfRun -> {
            replace(xwpfRun, placeholders);
        });
    }

    private List<String> find(XWPFParagraph xwpfParagraph) {
        List<String> result = new ArrayList<>();
        xwpfParagraph.getRuns().forEach(xwpfRun -> {
            var list = find(xwpfRun);
            if(!list.isEmpty()){
                result.add(list);
            }
        });
        return result;
    }

    private String find(XWPFRun xwpfRun){
        StringBuilder result = new StringBuilder("");
        var pattern = Pattern.compile(REGEX);
        var text = xwpfRun.getText(0);
        if (null != text) {
            var matcher = pattern.matcher(text);
            while (matcher.find()) {
                result.append(text.substring(matcher.start(), matcher.end()));
            }
        }
        return result.toString();
    }
}
