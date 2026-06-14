package com.smart.investment.module.risk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smart.investment.module.risk.entity.AlertEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AlertEventMapper extends BaseMapper<AlertEvent> {

    List<AlertEvent> selectByUserId(@Param("userId") Long userId);

    List<AlertEvent> selectUnreadByUserId(@Param("userId") Long userId);

    List<AlertEvent> selectByUserIdAndRiskType(@Param("userId") Long userId, @Param("riskType") String riskType);

    int updateReadStatus(@Param("id") Long id, @Param("isRead") Integer isRead);

    int updateAllReadByUserId(@Param("userId") Long userId);
}