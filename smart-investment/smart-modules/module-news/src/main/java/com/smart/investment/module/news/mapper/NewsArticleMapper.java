package com.smart.investment.module.news.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smart.investment.module.news.entity.NewsArticle;
import org.apache.ibatis.annotations.Mapper;

/**
 * 新闻资讯 Mapper
 */
@Mapper
public interface NewsArticleMapper extends BaseMapper<NewsArticle> {
}
