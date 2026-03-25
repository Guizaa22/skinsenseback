package beauty_center.common.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated response wrapper for list endpoints.
 * Provides consistent pagination structure across the API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {

    private List<T> content;          // The actual data items
    private int pageNumber;           // Current page (0-indexed)
    private int pageSize;             // Items per page
    private long totalElements;       // Total number of items
    private int totalPages;           // Total number of pages
    private boolean hasNext;          // Whether there's a next page
    private boolean hasPrevious;      // Whether there's a previous page
    private boolean isFirst;          // Whether this is the first page
    private boolean isLast;           // Whether this is the last page

    /**
     * Factory method to create PaginatedResponse from Spring Page
     */
    public static <T> PaginatedResponse<T> from(org.springframework.data.domain.Page<T> page) {
        return PaginatedResponse.<T>builder()
            .content(page.getContent())
            .pageNumber(page.getNumber())
            .pageSize(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .isFirst(page.isFirst())
            .isLast(page.isLast())
            .build();
    }
}
