/*
 *  @(#)TemplateDocumentModelTest.java
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

package com.umdc.backoffice.v1.report.api.to;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

class TemplateDocumentModelTest {

    @Test
    @DisplayName("Test getters, setters and toString of TemplateDocumentModel")
    void gettersAndSetters() {
        var model = new TemplateDocumentModel();
        var created = LocalDate.of(2026, 1, 14);
        var modified = LocalDate.of(2026, 2, 1);

        model.setVersionId(1L);
        model.setTemplateName("welcome-letter");
        model.setDescription("Welcome letter template");
        model.setCreatedBy("lmata");
        model.setCreatedTimestamp(created);
        model.setLastModifiedBy("jdoe");
        model.setLastModifiedTimestamp(modified);

        Assertions.assertAll("Test Getters and Setters",
                () -> Assertions.assertEquals(1L, model.getVersionId()),
                () -> Assertions.assertEquals("welcome-letter", model.getTemplateName()),
                () -> Assertions.assertEquals("Welcome letter template", model.getDescription()),
                () -> Assertions.assertEquals("lmata", model.getCreatedBy()),
                () -> Assertions.assertEquals(created, model.getCreatedTimestamp()),
                () -> Assertions.assertEquals("jdoe", model.getLastModifiedBy()),
                () -> Assertions.assertEquals(modified, model.getLastModifiedTimestamp()),
                () -> Assertions.assertTrue(model.toString().contains("welcome-letter"))
        );
    }

    @Test
    @DisplayName("default constructor leaves every field null")
    void defaultConstructor() {
        var model = new TemplateDocumentModel();

        Assertions.assertAll("Test default state",
                () -> Assertions.assertNull(model.getVersionId()),
                () -> Assertions.assertNull(model.getTemplateName()),
                () -> Assertions.assertNull(model.getDescription()),
                () -> Assertions.assertNull(model.getCreatedBy()),
                () -> Assertions.assertNull(model.getCreatedTimestamp()),
                () -> Assertions.assertNull(model.getLastModifiedBy()),
                () -> Assertions.assertNull(model.getLastModifiedTimestamp())
        );
    }
}
