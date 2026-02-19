package com.hirepro.common.util;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for building dynamic JPA Specifications for filtering entities.
 * This class provides reusable methods to construct complex queries based on filter criteria.
 *
 * @author HirePro Team
 * @version 1.0
 */
public class SpecificationBuilder {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Builds a specification that excludes soft-deleted records.
     *
     * @param <T> Entity type
     * @return Specification that checks deletedAt is null
     */
    public static <T> Specification<T> isNotDeleted() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isNull(root.get("deletedAt"));
    }

    /**
     * Builds a specification for global search across multiple fields.
     *
     * @param <T> Entity type
     * @param searchTerm Search term to look for
     * @param searchableFields Fields to search in
     * @return Specification for global search
     */
    public static <T> Specification<T> globalSearch(String searchTerm, String... searchableFields) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String likePattern = "%" + searchTerm.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            for (String field : searchableFields) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get(field)),
                                likePattern
                        )
                );
            }

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Builds a specification based on multiple filter criteria.
     * Supports various operators: eq, ne, gt, gte, lt, lte, like, in
     *
     * @param <T> Entity type
     * @param filters Map of field names to filter values
     * @return Specification for filtering
     */
    public static <T> Specification<T> withFilters(Map<String, String> filters) {
        return (root, query, criteriaBuilder) -> {
            if (filters == null || filters.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            for (Map.Entry<String, String> entry : filters.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (value == null || value.trim().isEmpty()) {
                    continue;
                }

                // Parse field and operator
                String field;
                String operator = "eq"; // default operator

                if (key.contains(":")) {
                    String[] parts = key.split(":");
                    field = parts[0];
                    operator = parts.length > 1 ? parts[1] : "eq";
                } else {
                    field = key;
                }

                predicates.add(buildPredicate(root, criteriaBuilder, field, operator, value));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Builds a single predicate based on field, operator, and value.
     */
    private static <T> Predicate buildPredicate(
            Root<T> root,
            CriteriaBuilder criteriaBuilder,
            String field,
            String operator,
            String value) {

        switch (operator.toLowerCase()) {
            case "eq":
                return criteriaBuilder.equal(root.get(field), value);

            case "ne":
                return criteriaBuilder.notEqual(root.get(field), value);

            case "gt":
                return buildGreaterThan(root, criteriaBuilder, field, value);

            case "gte":
                return buildGreaterThanOrEqual(root, criteriaBuilder, field, value);

            case "lt":
                return buildLessThan(root, criteriaBuilder, field, value);

            case "lte":
                return buildLessThanOrEqual(root, criteriaBuilder, field, value);

            case "like":
                return criteriaBuilder.like(
                        criteriaBuilder.lower(root.get(field)),
                        "%" + value.toLowerCase() + "%"
                );

            case "in":
                String[] values = value.split(",");
                return root.get(field).in((Object[]) values);

            case "isnull":
                return criteriaBuilder.isNull(root.get(field));

            case "isnotnull":
                return criteriaBuilder.isNotNull(root.get(field));

            default:
                return criteriaBuilder.equal(root.get(field), value);
        }
    }

    /**
     * Builds greater than predicate with type detection.
     */
    private static <T> Predicate buildGreaterThan(
            Root<T> root,
            CriteriaBuilder criteriaBuilder,
            String field,
            String value) {

        Class<?> fieldType = root.get(field).getJavaType();

        if (fieldType.equals(LocalDateTime.class)) {
            LocalDateTime dateTime = LocalDateTime.parse(value, DATE_TIME_FORMATTER);
            return criteriaBuilder.greaterThan(root.get(field), dateTime);
        } else if (fieldType.equals(LocalDate.class)) {
            LocalDate date = LocalDate.parse(value, DATE_FORMATTER);
            return criteriaBuilder.greaterThan(root.get(field), date);
        } else if (Number.class.isAssignableFrom(fieldType)) {
            return criteriaBuilder.gt(root.get(field), Double.parseDouble(value));
        } else {
            return criteriaBuilder.greaterThan(root.get(field), value);
        }
    }

    /**
     * Builds greater than or equal predicate with type detection.
     */
    private static <T> Predicate buildGreaterThanOrEqual(
            Root<T> root,
            CriteriaBuilder criteriaBuilder,
            String field,
            String value) {

        Class<?> fieldType = root.get(field).getJavaType();

        if (fieldType.equals(LocalDateTime.class)) {
            LocalDateTime dateTime = LocalDateTime.parse(value, DATE_TIME_FORMATTER);
            return criteriaBuilder.greaterThanOrEqualTo(root.get(field), dateTime);
        } else if (fieldType.equals(LocalDate.class)) {
            LocalDate date = LocalDate.parse(value, DATE_FORMATTER);
            return criteriaBuilder.greaterThanOrEqualTo(root.get(field), date);
        } else if (Number.class.isAssignableFrom(fieldType)) {
            return criteriaBuilder.ge(root.get(field), Double.parseDouble(value));
        } else {
            return criteriaBuilder.greaterThanOrEqualTo(root.get(field), value);
        }
    }

    /**
     * Builds less than predicate with type detection.
     */
    private static <T> Predicate buildLessThan(
            Root<T> root,
            CriteriaBuilder criteriaBuilder,
            String field,
            String value) {

        Class<?> fieldType = root.get(field).getJavaType();

        if (fieldType.equals(LocalDateTime.class)) {
            LocalDateTime dateTime = LocalDateTime.parse(value, DATE_TIME_FORMATTER);
            return criteriaBuilder.lessThan(root.get(field), dateTime);
        } else if (fieldType.equals(LocalDate.class)) {
            LocalDate date = LocalDate.parse(value, DATE_FORMATTER);
            return criteriaBuilder.lessThan(root.get(field), date);
        } else if (Number.class.isAssignableFrom(fieldType)) {
            return criteriaBuilder.lt(root.get(field), Double.parseDouble(value));
        } else {
            return criteriaBuilder.lessThan(root.get(field), value);
        }
    }

    /**
     * Builds less than or equal predicate with type detection.
     */
    private static <T> Predicate buildLessThanOrEqual(
            Root<T> root,
            CriteriaBuilder criteriaBuilder,
            String field,
            String value) {

        Class<?> fieldType = root.get(field).getJavaType();

        if (fieldType.equals(LocalDateTime.class)) {
            LocalDateTime dateTime = LocalDateTime.parse(value, DATE_TIME_FORMATTER);
            return criteriaBuilder.lessThanOrEqualTo(root.get(field), dateTime);
        } else if (fieldType.equals(LocalDate.class)) {
            LocalDate date = LocalDate.parse(value, DATE_FORMATTER);
            return criteriaBuilder.lessThanOrEqualTo(root.get(field), date);
        } else if (Number.class.isAssignableFrom(fieldType)) {
            return criteriaBuilder.le(root.get(field), Double.parseDouble(value));
        } else {
            return criteriaBuilder.lessThanOrEqualTo(root.get(field), value);
        }
    }

    /**
     * Combines multiple specifications with AND logic.
     */
    @SafeVarargs
    public static <T> Specification<T> and(Specification<T>... specifications) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (Specification<T> spec : specifications) {
                if (spec != null) {
                    Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);
                    if (predicate != null) {
                        predicates.add(predicate);
                    }
                }
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Combines multiple specifications with OR logic.
     */
    @SafeVarargs
    public static <T> Specification<T> or(Specification<T>... specifications) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (Specification<T> spec : specifications) {
                if (spec != null) {
                    Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);
                    if (predicate != null) {
                        predicates.add(predicate);
                    }
                }
            }
            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }
}