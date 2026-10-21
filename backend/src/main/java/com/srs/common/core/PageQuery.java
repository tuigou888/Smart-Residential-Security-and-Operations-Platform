package com.srs.common.core;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分页查询基类(对应接口规范中的 pageNum / pageSize / orderBy / orderDir)
 */
@Data
@Schema(description = "分页查询参数")
public class PageQuery {

    @Schema(description = "页码, 从 1 开始", example = "1")
    private Long pageNum = 1L;

    @Schema(description = "每页条数", example = "10")
    private Long pageSize = 10L;

    @Schema(description = "排序字段(下划线命名), 为空则不排序")
    private String orderBy;

    @Schema(description = "排序方向 asc/desc")
    private String orderDir = "desc";

    public long offset() {
        long num = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        long size = (pageSize == null || pageSize < 1) ? 10 : pageSize;
        return (num - 1) * size;
    }
}
