package com.smart.investment.module.risk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smart.investment.module.risk.entity.RiskRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RiskRecordMapper extends BaseMapper<RiskRecord> {

    List<RiskRecord> selectByPortfolioId(@Param("portfolioId") Long portfolioId);

    List<RiskRecord> selectRecentByPortfolioId(@Param("portfolioId") Long portfolioId, @Param("limit") Integer limit);

    List<RiskRecord> selectByRiskType(@Param("riskType") String riskType);
}