package com.qataskmanager.automation_sut.dto;

import java.util.List;

public class PaginationDtos {
    public record PagedResponse<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last
    ) {
    }
}
