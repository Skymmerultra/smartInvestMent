-- 第1条：贵州茅台
INSERT INTO news_article (source, title, content, url, sentiment, sentiment_score, related_securities, published_at, crawled_at, created_at)
VALUES ('东方财富', '贵州茅台2025年营收突破2000亿', '贵州茅台发布2025年度财报，实现营业总收入2058.63亿元。', 'https://finance.eastmoney.com/a/20260612123456', 'POSITIVE', 0.8500, '[{"code":"600519","name":"贵州茅台","relevance":"高"}]', '2026-06-12 10:30:00', '2026-06-12 10:35:00', '2026-06-12 10:35:00');

-- 第2条：宁德时代
INSERT INTO news_article (source, title, content, url, sentiment, sentiment_score, related_securities, published_at, crawled_at, created_at)
VALUES ('同花顺', '宁德时代发布新一代钠离子电池', '宁德时代推出第三代钠离子电池产品，能量密度达到200Wh/kg。', 'https://10jqka.com.cn/news/20260612/987654', 'POSITIVE', 0.9200, '[{"code":"300750","name":"宁德时代","relevance":"高"}]', '2026-06-12 09:15:00', '2026-06-12 09:20:00', '2026-06-12 09:20:00');

-- 第3条：央行降准
INSERT INTO news_article (source, title, content, url, sentiment, sentiment_score, related_securities, published_at, crawled_at, created_at)
VALUES ('雪球', '央行宣布降准0.5个百分点', '中国人民银行宣布下调存款准备金率0.5个百分点，预计释放长期资金约1.2万亿元。', 'https://xueqiu.com/news/20260612/345678', 'POSITIVE', 0.7800, '[{"code":"601318","name":"中国平安","relevance":"高"}]', '2026-06-12 08:00:00', '2026-06-12 08:05:00', '2026-06-12 08:05:00');

-- 第4条：美国半导体管制
INSERT INTO news_article (source, title, content, url, sentiment, sentiment_score, related_securities, published_at, crawled_at, created_at)
VALUES ('东方财富', '美国对华半导体出口管制升级', '美国商务部BIS扩大对华半导体出口管制范围，新增HBM芯片和先进封装设备纳入管制清单。', 'https://finance.eastmoney.com/a/20260612123457', 'NEGATIVE', 0.1800, '[{"code":"688981","name":"中芯国际","relevance":"高"}]', '2026-06-12 07:30:00', '2026-06-12 07:35:00', '2026-06-12 07:35:00');

-- 第5条：比亚迪
INSERT INTO news_article (source, title, content, url, sentiment, sentiment_score, related_securities, published_at, crawled_at, created_at)
VALUES ('同花顺', '比亚迪海外销量连续3月突破10万辆', '比亚迪2026年5月海外销量达11.8万辆，全球新能源乘用车市场份额达到23.5%。', 'https://10jqka.com.cn/news/20260612/987655', 'POSITIVE', 0.8800, '[{"code":"002594","name":"比亚迪","relevance":"高"}]', '2026-06-12 11:00:00', '2026-06-12 11:05:00', '2026-06-12 11:05:00');

-- 第6条：原油价格大跌
INSERT INTO news_article (source, title, content, url, sentiment, sentiment_score, related_securities, published_at, crawled_at, created_at)
VALUES ('雪球', '国际原油价格大跌5%', '布伦特原油期货下跌5.2%至68.5美元每桶，OPEC+决定自7月起逐步增产。', 'https://xueqiu.com/news/20260612/345679', 'NEGATIVE', 0.2200, '[{"code":"601857","name":"中国石油","relevance":"高"}]', '2026-06-12 14:00:00', '2026-06-12 14:05:00', '2026-06-12 14:05:00');

-- 第7条：大盘综述
INSERT INTO news_article (source, title, content, url, sentiment, sentiment_score, related_securities, published_at, crawled_at, created_at)
VALUES ('东方财富', 'A股三大指数震荡收涨重回3400点', '沪指涨0.67%报3402.18点，深成指涨0.82%，创业板指涨1.13%，两市合计成交1.08万亿元。', 'https://finance.eastmoney.com/a/20260612123458', 'NEUTRAL', 0.5200, '[]', '2026-06-12 15:30:00', '2026-06-12 15:35:00', '2026-06-12 15:35:00');
