package com.srs.modules.base.controller;

import com.srs.common.anno.OperLog;
import com.srs.common.core.PageQuery;
import com.srs.common.core.PageResult;
import com.srs.common.core.R;
import com.srs.modules.base.entity.BaseRoom;
import com.srs.modules.base.service.BaseRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "房屋管理")
@RestController
@RequestMapping("/api/v1/base/room")
@RequiredArgsConstructor
public class BaseRoomController {

    private final BaseRoomService roomService;

    @Operation(summary = "房屋列表")
    @PreAuthorize("@ss.has('base:room:list')")
    @GetMapping("/list")
    public R<PageResult<BaseRoom>> list(PageQuery query,
                                        @RequestParam(required = false) Long communityId,
                                        @RequestParam(required = false) Long buildingId,
                                        @RequestParam(required = false) Long unitId,
                                        @RequestParam(required = false) String roomNo,
                                        @RequestParam(required = false) Integer ownerStatus,
                                        @RequestParam(required = false) Integer roomType) {
        return R.data(PageResult.of(
                roomService.pageRooms(query, communityId, buildingId, unitId, roomNo, ownerStatus, roomType)));
    }

    @Operation(summary = "房屋详情")
    @PreAuthorize("@ss.has('base:room:query')")
    @GetMapping("/{id}")
    public R<BaseRoom> detail(@PathVariable Long id) {
        return R.data(roomService.getById(id));
    }

    @Operation(summary = "新增/修改房屋")
    @PreAuthorize("@ss.has('base:room:add')")
    @OperLog(module = "房屋管理", businessType = 1)
    @PostMapping
    public R<Void> save(@RequestBody BaseRoom entity) {
        roomService.saveRoom(entity);
        return R.ok("保存成功");
    }

    @Operation(summary = "删除房屋")
    @PreAuthorize("@ss.has('base:room:remove')")
    @OperLog(module = "房屋管理", businessType = 3)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        roomService.remove(ids);
        return R.ok("删除成功");
    }

    @Operation(summary = "Excel 批量导入房屋")
    @PreAuthorize("@ss.has('base:room:import')")
    @OperLog(module = "房屋管理", businessType = 1)
    @PostMapping("/import")
    public R<Map<String, Object>> importRooms(@RequestParam("file") MultipartFile file,
                                              @RequestParam(required = false) Long communityId) {
        try {
            int count = roomService.importRooms(file, communityId);
            return R.data(Map.of("total", count));
        } catch (Exception e) {
            return R.fail(500, "导入失败: " + e.getMessage());
        }
    }

    @Operation(summary = "导出房屋清单")
    @PreAuthorize("@ss.has('base:room:export')")
    @GetMapping("/export")
    public void export(HttpServletResponse response, @RequestParam(required = false) Long communityId) {
        try {
            roomService.exportRooms(response, communityId);
        } catch (Exception e) {
            throw new IllegalStateException("导出失败: " + e.getMessage());
        }
    }
}
