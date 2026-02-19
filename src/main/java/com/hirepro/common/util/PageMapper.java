package com.hirepro.common.util;

import com.hirepro.common.dto.PageResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class for converting Spring Data Page objects to custom PageResponseDto.
 * This provides a clean separation between internal Spring pagination and API responses.
 *
 * @author HirePro Team
 * @version 1.0
 */
public class PageMapper {

    /**
     * Converts a Spring Data Page to PageResponseDto without transformation.
     *
     * @param <T> The type of content
     * @param page Spring Data Page object
     * @return PageResponseDto with the same content type
     */
    public static <T> PageResponseDto<T> toPageResponse(Page<T> page) {
        PageResponseDto.PageMetadata metadata = new PageResponseDto.PageMetadata(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );

        return new PageResponseDto<>(page.getContent(), metadata);
    }

    /**
     * Converts a Spring Data Page to PageResponseDto with content transformation.
     * Useful when you need to convert entities to DTOs.
     *
     * @param <T> Source type (usually entity)
     * @param <R> Target type (usually DTO)
     * @param page Spring Data Page object
     * @param mapper Function to transform each element
     * @return PageResponseDto with transformed content
     */
    public static <T, R> PageResponseDto<R> toPageResponse(Page<T> page, Function<T, R> mapper) {
        List<R> content = page.getContent()
                .stream()
                .map(mapper)
                .collect(Collectors.toList());

        PageResponseDto.PageMetadata metadata = new PageResponseDto.PageMetadata(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );

        return new PageResponseDto<>(content, metadata);
    }

    /**
     * Converts a Spring Data Page to PageResponseDto using ModelMapper.
     *
     * @param <T> Source type (usually entity)
     * @param <R> Target type (usually DTO)
     * @param page Spring Data Page object
     * @param modelMapper ModelMapper instance
     * @param targetClass Target class for mapping
     * @return PageResponseDto with mapped content
     */
    public static <T, R> PageResponseDto<R> toPageResponse(
            Page<T> page,
            org.modelmapper.ModelMapper modelMapper,
            Class<R> targetClass) {

        List<R> content = page.getContent()
                .stream()
                .map(entity -> modelMapper.map(entity, targetClass))
                .collect(Collectors.toList());

        PageResponseDto.PageMetadata metadata = new PageResponseDto.PageMetadata(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );

        return new PageResponseDto<>(content, metadata);
    }
}