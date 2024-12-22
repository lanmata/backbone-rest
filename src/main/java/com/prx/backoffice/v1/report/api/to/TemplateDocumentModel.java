/*
 *  @(#)TemplateDocumentModel.java
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

package com.prx.backoffice.v1.report.api.to;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.LocalDate;

/**
 * TemplateDocumentModel.
 *
 * @author &lt;a href='mailto:luis.antonio.mata@gmail.com'&gt;Luis Antonio Mata&lt;/a&gt;
 * @version 1.0.0, 14-01-2022
 * @since 11
 */
@JsonNaming
public class TemplateDocumentModel {
    private Long versionId;
    private String templateName;
    private String description;
    private String createdBy;
    private LocalDate createdTimestamp;
    private String lastModifiedBy;
    private LocalDate lastModifiedTimestamp;

    /**
     * Default Constructor
     */
    public TemplateDocumentModel() {
        // Default Constructor
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDate getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(LocalDate createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public LocalDate getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }

    public void setLastModifiedTimestamp(LocalDate lastModifiedTimestamp) {
        this.lastModifiedTimestamp = lastModifiedTimestamp;
    }

    @Override
    public String toString() {
        return "TemplateDocumentModel{" +
                "versionId=" + versionId +
                ", templateName='" + templateName + '\'' +
                ", description='" + description + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", createdTimestamp=" + createdTimestamp +
                ", lastModifiedBy='" + lastModifiedBy + '\'' +
                ", lastModifiedTimestamp=" + lastModifiedTimestamp +
                '}';
    }
}
