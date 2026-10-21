package com.srs.modules.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.srs.modules.iot.entity.IotDevice;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IotDeviceMapper extends BaseMapper<IotDevice> {
}
