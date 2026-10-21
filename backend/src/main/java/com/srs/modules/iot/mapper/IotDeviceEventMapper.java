package com.srs.modules.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.srs.modules.iot.entity.IotDeviceEvent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IotDeviceEventMapper extends BaseMapper<IotDeviceEvent> {
}
