package com.umdc.backoffice.util;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Stable wire envelope for server-side paginated listings.
 *
 * @param content       the items of the requested page
 * @param page          zero-based page index
 * @param size          requested page size
 * @param totalElements total number of items matching the filters
 * @param totalPages    total number of pages for {@code size}
 * @param <T>           item type
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
