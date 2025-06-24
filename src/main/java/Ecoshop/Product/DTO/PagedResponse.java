package Ecoshop.Product.DTO;

import lombok.*;

import java.util.List;
@Getter
@Setter
@Builder
public class PagedResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private boolean first;
    public PagedResponse(List<T> content, int page, int size,
                         long totalElements, int totalPages,
                         boolean last, boolean first) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.last = last;
        this.first = first;
    }

}
