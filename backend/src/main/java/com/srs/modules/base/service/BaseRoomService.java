package com.srs.modules.base.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.core.PageQuery;
import com.srs.modules.base.entity.BaseRoom;
import com.srs.modules.base.mapper.BaseRoomMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 房屋管理(含 Excel 批量导入导出)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BaseRoomService extends ServiceImpl<BaseRoomMapper, BaseRoom> {

    private final BaseStructureService structureService;

    public IPage<BaseRoom> pageRooms(PageQuery q, Long communityId, Long buildingId, Long unitId,
                                     String roomNo, Integer ownerStatus, Integer roomType) {
        Page<BaseRoom> page = new Page<>(q.getPageNum(), q.getPageSize());
        IPage<BaseRoom> result = baseMapper.selectPage(page, new LambdaQueryWrapper<BaseRoom>()
                .eq(communityId != null, BaseRoom::getCommunityId, communityId)
                .eq(buildingId != null, BaseRoom::getBuildingId, buildingId)
                .eq(unitId != null, BaseRoom::getUnitId, unitId)
                .like(roomNo != null && !roomNo.isBlank(), BaseRoom::getRoomNo, roomNo)
                .eq(ownerStatus != null, BaseRoom::getOwnerStatus, ownerStatus)
                .eq(roomType != null, BaseRoom::getRoomType, roomType)
                .orderByAsc(BaseRoom::getFloor, BaseRoom::getRoomNo));
        fillNames(result.getRecords());
        return result;
    }

    public List<BaseRoom> roomsOf(Long unitId) {
        return baseMapper.selectList(new LambdaQueryWrapper<BaseRoom>()
                .eq(unitId != null, BaseRoom::getUnitId, unitId)
                .orderByAsc(BaseRoom::getRoomNo));
    }

    public void saveRoom(BaseRoom entity) {
        if (entity.getId() == null) {
            baseMapper.insert(entity);
        } else {
            baseMapper.updateById(entity);
        }
    }

    public void remove(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            baseMapper.deleteByIds(ids);
        }
    }

    /** Excel 批量导入: 小区编码/楼栋名/单元名/房号/楼层/面积/类型 */
    public int importRooms(MultipartFile file, Long defaultCommunityId) throws IOException {
        try (InputStream in = file.getInputStream()) {
            List<RoomExcel> rows = new ArrayList<>();
            EasyExcel.read(in, RoomExcel.class, new com.alibaba.excel.event.AnalysisEventListener<RoomExcel>() {
                @Override
                public void invoke(RoomExcel data, com.alibaba.excel.context.AnalysisContext context) {
                    rows.add(data);
                }

                @Override
                public void doAfterAllAnalysed(com.alibaba.excel.context.AnalysisContext context) {
                    // no-op
                }
            }).sheet().doRead();

            int count = 0;
            for (RoomExcel row : rows) {
                if (row.getRoomNo() == null || row.getRoomNo().isBlank()) {
                    continue;
                }
                BaseRoom room = new BaseRoom();
                room.setCommunityId(defaultCommunityId);
                room.setBuildingId(row.getBuildingId());
                room.setUnitId(row.getUnitId());
                room.setRoomNo(row.getRoomNo());
                room.setFloor(row.getFloor());
                room.setArea(row.getArea());
                room.setRoomType(row.getRoomType() == null ? 1 : row.getRoomType());
                room.setOwnerStatus(0);
                room.setStatus(1);
                baseMapper.insert(room);
                count++;
            }
            return count;
        }
    }

    public void exportRooms(HttpServletResponse response, Long communityId) throws IOException {
        List<BaseRoom> list = baseMapper.selectList(new LambdaQueryWrapper<BaseRoom>()
                .eq(communityId != null, BaseRoom::getCommunityId, communityId));
        fillNames(list);
        List<RoomExcel> rows = list.stream().map(r -> {
            RoomExcel e = new RoomExcel();
            e.setBuildingId(r.getBuildingId());
            e.setUnitId(r.getUnitId());
            e.setRoomNo(r.getRoomNo());
            e.setFloor(r.getFloor());
            e.setArea(r.getArea());
            e.setRoomType(r.getRoomType());
            return e;
        }).toList();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String fileName = URLEncoder.encode("房屋清单", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), RoomExcel.class).sheet("房屋清单").doWrite(rows);
    }

    private void fillNames(List<BaseRoom> list) {
        if (list.isEmpty()) {
            return;
        }
        List<BaseRoom> all = baseMapper.selectList(null);
        Map<Long, String> buildingNames = structureService.list().stream()
                .collect(Collectors.toMap(com.srs.modules.base.entity.BaseBuilding::getId,
                        com.srs.modules.base.entity.BaseBuilding::getBuildingName, (a, b) -> a));
        for (BaseRoom r : list) {
            r.setBuildingName(buildingNames.get(r.getBuildingId()));
        }
        Map<Long, String> unitNames = new java.util.HashMap<>();
        for (Long unitId : all.stream().map(BaseRoom::getUnitId).distinct().toList()) {
            var unit = structureService.unitsOf(null).stream()
                    .filter(u -> u.getId().equals(unitId)).findFirst();
            unit.ifPresent(u -> unitNames.put(u.getId(), u.getUnitName()));
        }
        for (BaseRoom r : list) {
            r.setUnitName(unitNames.get(r.getUnitId()));
        }
    }

    @Data
    public static class RoomExcel {

        @ExcelProperty("楼栋ID")
        private Long buildingId;

        @ExcelProperty("单元ID")
        private Long unitId;

        @ExcelProperty("房号")
        private String roomNo;

        @ExcelProperty("楼层")
        private Integer floor;

        @ExcelProperty("建筑面积")
        private BigDecimal area;

        @ExcelProperty("房屋类型(1住宅 2商铺 3车位 4储藏室 5其他)")
        private Integer roomType;
    }
}
