package com.bank.dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {
    private List<T> data;
    private int pageNumber;
    private long totalElements;
    private int totalPages;
}
