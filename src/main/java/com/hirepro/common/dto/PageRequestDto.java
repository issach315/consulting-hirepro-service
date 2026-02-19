package com.hirepro.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.Map;

/**
 * Reusable DTO for handling pagination, sorting, and filtering requests.
 * This class provides a standard way to handle paginated queries across the application.
 *
 * @author HirePro Team
 * @version 1.0
 */
public class PageRequestDto {

    @Min(value = 0, message = "Page number must be greater than or equal to 0")
    private int page = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size must not exceed 100")
    private int size = 10;

    private String sortBy;

    @Pattern(regexp = "ASC|DESC", message = "Sort direction must be either ASC or DESC")
    private String sortDirection = "DESC";

    private String search;

    private Map<String, String> filters;

    public PageRequestDto() {
        this.filters = new HashMap<>();
    }

    public PageRequestDto(int page, int size, String sortBy, String sortDirection) {
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
        this.filters = new HashMap<>();
    }

    // Getters and Setters
    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public Map<String, String> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, String> filters) {
        this.filters = filters;
    }

    public void addFilter(String key, String value) {
        if (this.filters == null) {
            this.filters = new HashMap<>();
        }
        this.filters.put(key, value);
    }

    @Override
    public String toString() {
        return "PageRequestDto{" +
                "page=" + page +
                ", size=" + size +
                ", sortBy='" + sortBy + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                ", search='" + search + '\'' +
                ", filters=" + filters +
                '}';
    }
}