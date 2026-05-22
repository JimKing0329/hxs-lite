package com.hxs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hxs.model.entity.StudySituationEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学习情况记录表 Mapper
 */
@Mapper
public interface StudySituationMapper extends BaseMapper<StudySituationEntity> {
}
