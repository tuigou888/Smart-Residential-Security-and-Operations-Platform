package com.srs.modules.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.srs.modules.base.entity.BaseVehicle;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BaseVehicleMapper extends BaseMapper<BaseVehicle> {
}
