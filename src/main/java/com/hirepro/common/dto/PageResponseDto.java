package com.hirepro.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * Reusable DTO for handling paginated responses.
 * This class provides a standard structure for returning paginated data across the application.
 *
 * @param <T> The type of content in the page
 * @author HirePro Team
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponseDto<T> {

    private List<T> content;
    private PageMetadata metadata;

    public PageResponseDto() {
    }

    public PageResponseDto(List<T> content, PageMetadata metadata) {
        this.content = content;
        this.metadata = metadata;
    }

    // Static factory method
    public static <T> PageResponseDto<T> of(List<T> content, PageMetadata metadata) {
        return new PageResponseDto<>(content, metadata);
    }

    // Getters and Setters
    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public PageMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(PageMetadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Inner class to hold pagination metadata
     */
    public static class PageMetadata {
        private int currentPage;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
        private boolean hasNext;
        private boolean hasPrevious;

        public PageMetadata() {
        }

        public PageMetadata(int currentPage, int pageSize, long totalElements, int totalPages) {
            this.currentPage = currentPage;
            this.pageSize = pageSize;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.first = currentPage == 0;
            this.last = currentPage == totalPages - 1;
            this.hasNext = currentPage < totalPages - 1;
            this.hasPrevious = currentPage > 0;
        }

        // Getters and Setters
        public int getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public long getTotalElements() {
            return totalElements;
        }

        public void setTotalElements(long totalElements) {
            this.totalElements = totalElements;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        public boolean isFirst() {
            return first;
        }

        public void setFirst(boolean first) {
            this.first = first;
        }

        public boolean isLast() {
            return last;
        }

        public void setLast(boolean last) {
            this.last = last;
        }

        public boolean isHasNext() {
            return hasNext;
        }

        public void setHasNext(boolean hasNext) {
            this.hasNext = hasNext;
        }

        public boolean isHasPrevious() {
            return hasPrevious;
        }

        public void setHasPrevious(boolean hasPrevious) {
            this.hasPrevious = hasPrevious;
        }
    }
}