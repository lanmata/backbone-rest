package com.umdc.backoffice.v1.features.api.to;

import java.util.UUID;

/**
 * Lightweight feature projection for catalog browsing: it deliberately carries no role ids, so listing
 * a page of features never loads their role links.
 *
 * @param id          feature id
 * @param name        feature name
 * @param description feature description
 * @param active      whether the feature is active
 */
public record FeatureSummary(UUID id, String name, String description, boolean active) {
}
