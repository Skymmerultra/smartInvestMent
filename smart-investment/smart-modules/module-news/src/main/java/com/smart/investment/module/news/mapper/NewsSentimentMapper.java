package com.smart.investment.module.news.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smart.investment.module.news.entity.NewsSentiment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 新闻情感标签 Mapper
 */
@Mapper
public interface NewsSentimentMapper extends BaseMapper<NewsSentiment> {
}
