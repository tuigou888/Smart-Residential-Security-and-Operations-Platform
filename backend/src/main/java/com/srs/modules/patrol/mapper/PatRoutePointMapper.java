package com.srs.modules.patrol.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.srs.modules.patrol.entity.PatRoutePoint;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PatRoutePointMapper extends BaseMapper<PatRoutePoint> {

    /**
     * 物理删除路线下全部点位关联
     * <p>
     * 关联表存在 (route_id, point_id) 唯一索引，逻辑删除会保留记录导致重新编排时唯一键冲突，
     * 因此这里使用物理删除。
     */
    @Delete("DELETE FROM pat_route_point WHERE route_id = #{routeId}")
    int deleteByRouteId(@Param("routeId") Long routeId);
}
