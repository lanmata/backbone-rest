/*
 * @(#)TemplateDocumentHandler.java.
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
package com.prx.backoffice.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prx.backoffice.v1.report.api.to.TemplateDocumentModel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * TemplateDocumentHandler.
 *
 * @author &lt;a href='mailto:luis.antonio.mata@gmail.com'&gt;Luis Antonio Mata&lt;/a&gt;
 * @version 1.0.0, 18-01-2022
 * @since 11
 */
@Component
@RequiredArgsConstructor
public class TemplateDocumentConverter implements Converter<String, TemplateDocumentModel> {
    private final ObjectMapper objectMapper;

    /**
     * Converter from {@link String} to {@link TemplateDocumentModel}.
     *
     * @param source Object type {@link String}.
     * @return Object type {@link TemplateDocumentModel}.
     */
    @Override
    @SneakyThrows
    public TemplateDocumentModel convert(String source) {
        return objectMapper.readValue(source, TemplateDocumentModel.class);
    }
}
