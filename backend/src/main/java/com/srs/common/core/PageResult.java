package com.srs.common.core;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * 统一分页结果
 */
@Data
@Schema(description = "分页结果")
public class PageResult<T> {

    @Schema(description = "数据列表")
    private List<T> records = Collections.emptyList();

    @Schema(description = "总条数")
    private Long total = 0L;

    @Schema(description = "页码")
    private Long pageNum = 1L;

    @Schema(description = "每页条数")
    private Long pageSize = 10L;

    @Schema(description = "总页数")
    private Long pages = 0L;

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> r = new PageResult<>();
        r.setRecords(page.getRecords());
        r.setTotal(page.getTotal());
        r.setPageNum(page.getCurrent());
        r.setPageSize(page.getSize());
        r.setPages(page.getPages());
        return r;
    }

    public static <S, T> PageResult<T> of(IPage<S> page, Function<S, T> converter) {
        PageResult<T> r = new PageResult<>();
        r.setRecords(page.getRecords().stream().map(converter).toList());
        r.setTotal(page.getTotal());
        r.setPageNum(page.getCurrent());
        r.setPageSize(page.getSize());
        r.setPages(page.getPages());
        return r;
    }

    public static <T> PageResult<T> empty() {
        return new PageResult<>();
    }
}
