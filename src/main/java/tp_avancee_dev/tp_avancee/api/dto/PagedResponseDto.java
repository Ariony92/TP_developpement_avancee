package tp_avancee_dev.tp_avancee.api.dto;

import java.util.List;

public class PagedResponseDto<T> {
    private int page;
    private int size;
    private int count;
    private List<T> items;

    public PagedResponseDto() {
    }

    public PagedResponseDto(int page, int size, int count, List<T> items) {
        this.page = page;
        this.size = size;
        this.count = count;
        this.items = items;
    }

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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}
