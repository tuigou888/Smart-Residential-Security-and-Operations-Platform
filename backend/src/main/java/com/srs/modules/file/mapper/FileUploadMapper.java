package com.srs.modules.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.srs.modules.file.entity.FileUpload;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileUploadMapper extends BaseMapper<FileUpload> {
}
