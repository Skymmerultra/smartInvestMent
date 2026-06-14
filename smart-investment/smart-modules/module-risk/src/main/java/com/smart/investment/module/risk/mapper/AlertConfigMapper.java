package com.smart.investment.module.risk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smart.investment.module.risk.entity.AlertConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AlertConfigMapper extends BaseMapper<AlertConfig> {

    AlertConfig selectByUserIdAndRiskType(@Param("userId") Long userId, @Param("riskType") String riskType);

    List<AlertConfig> selectByUserId(@Param("userId") Long userId);

    List<AlertConfig> selectActiveConfigs();
}