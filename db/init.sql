-- ============================================================
-- 智能投研系统 数据库初始化脚本
-- 依据：架构设计文档 第六章 数据库设计
-- 任务：T-01 MySQL 数据库表结构初始化
-- 共18张业务表
-- ============================================================

CREATE DATABASE IF NOT EXISTS smart_investment
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smart_investment;

-- ============================================================
-- 1. 认证授权模块 (T-05/T-11)
-- ============================================================

-- 1.1 用户表
CREATE TABLE sys_user (
    id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    username    VARCHAR(50)     NOT NULL                 COMMENT '用户名',
    password    VARCHAR(255)    NOT NULL                 COMMENT 'BCrypt加密密码',
    role        ENUM('INVESTOR','ANALYST','ADMIN')
                                NOT NULL                 COMMENT '用户角色',
    status      TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：0禁用/1启用',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME        NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE  KEY uk_username (username),
            KEY idx_status   (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 1.2 角色表
CREATE TABLE sys_role (
    id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    role_name   VARCHAR(50)     NOT NULL                 COMMENT '角色名称',
    description VARCHAR(200)    NULL                     COMMENT '角色描述',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_name (role_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 1.3 用户角色关联表
CREATE TABLE sys_user_role (
    id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    user_id     BIGINT          NOT NULL                 COMMENT '用户ID',
    role_id     BIGINT          NOT NULL                 COMMENT '角色ID',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE  KEY uk_user_role (user_id, role_id),
            KEY idx_user_id   (user_id),
            KEY idx_role_id   (role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- ============================================================
-- 2. 财报分析模块 (T-12)
-- ============================================================

-- 2.1 财报记录表
CREATE TABLE fin_report (
    id            BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
    company_code  VARCHAR(20)   NOT NULL                 COMMENT '公司代码',
    company_name  VARCHAR(200)  NULL                     COMMENT '公司名称',
    report_type   ENUM('ANNUAL','QUARTER','MONTHLY')
                                NOT NULL                 COMMENT '报告类型',
    report_period VARCHAR(10)   NOT NULL                 COMMENT '报告期（如2025Q4）',
    file_url      VARCHAR(500)  NULL                     COMMENT '原文件URL',
    parse_status  TINYINT       NOT NULL DEFAULT 0       COMMENT '解析状态：0=PENDING/1=COMPLETED/2=FAILED',
    error_msg     VARCHAR(500)  NULL                     COMMENT '解析失败原因',
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME      NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
            KEY idx_company_code  (company_code),
            KEY idx_report_period (report_period),
            KEY idx_parse_status  (parse_status),
            KEY idx_created_at    (created_at),
            KEY idx_company_period (company_code, report_period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='财报记录表';

-- 2.2 财务指标表
CREATE TABLE fin_indicator (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    report_id       BIGINT          NOT NULL                 COMMENT '关联财报ID',
    indicator_name  VARCHAR(100)    NOT NULL                 COMMENT '指标名称（营业收入/净利润/总资产/总负债/净资产等）',
    indicator_value DECIMAL(20,4)   NOT NULL                 COMMENT '指标值',
    unit            VARCHAR(20)     NULL                     COMMENT '单位（元/万元/%）',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
            KEY idx_report_indicator (report_id, indicator_name),
    CONSTRAINT fk_indicator_report FOREIGN KEY (report_id) REFERENCES fin_report(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='财务指标表';

-- 2.3 非财务信息表
CREATE TABLE fin_non_fin_info (
    id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    report_id   BIGINT          NOT NULL                 COMMENT '关联财报ID',
    info_type   VARCHAR(50)     NOT NULL                 COMMENT '信息类型（BUSINESS_STRATEGY/NEW_PRODUCT/MANAGEMENT_CHANGE等）',
    info_content TEXT           NOT NULL                 COMMENT '信息内容',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
            KEY idx_report_info (report_id, info_type),
    CONSTRAINT fk_nonfin_report FOREIGN KEY (report_id) REFERENCES fin_report(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='非财务信息表';

-- ============================================================
-- 3. 行情研报模块 (T-13)
-- ============================================================

-- 3.1 行情快照表
CREATE TABLE market_snapshot (
    id            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    security_code VARCHAR(20)     NOT NULL                 COMMENT '证券代码',
    security_type ENUM('STOCK','BOND','FUND')
                                  NOT NULL                 COMMENT '证券类型',
    security_name VARCHAR(100)    NULL                     COMMENT '证券名称',
    price         DECIMAL(15,4)   NOT NULL                 COMMENT '价格',
    change_pct    DECIMAL(10,4)   NULL                     COMMENT '涨跌幅',
    volume        BIGINT          NULL                     COMMENT '成交量',
    snapshot_time DATETIME        NOT NULL                 COMMENT '快照时间',
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
            KEY idx_security_code  (security_code),
            KEY idx_snapshot_time  (snapshot_time),
            KEY idx_security_snap  (security_code, snapshot_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='行情快照表';

-- 3.2 研报记录表
CREATE TABLE research_report (
    id            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    security_code VARCHAR(20)     NOT NULL                 COMMENT '证券代码',
    title         VARCHAR(500)    NOT NULL                 COMMENT '研报标题',
    author        VARCHAR(100)    NULL                     COMMENT '作者/机构',
    content       TEXT            NULL                     COMMENT '研报内容',
    rating        VARCHAR(20)     NULL                     COMMENT '评级（买入/增持/中性/减持/卖出）',
    target_price  DECIMAL(15,4)   NULL                     COMMENT '目标价',
    published_at  DATETIME        NOT NULL                 COMMENT '发布时间',
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
            KEY idx_security_code  (security_code),
            KEY idx_published_at   (published_at),
            KEY idx_security_pub   (security_code, published_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='研报记录表';

-- 3.3 研报观点表
CREATE TABLE research_viewpoint (
    id                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    report_id         BIGINT          NOT NULL                 COMMENT '关联研报ID',
    viewpoint_summary VARCHAR(500)    NOT NULL                 COMMENT '观点摘要',
    sentiment         ENUM('POSITIVE','NEGATIVE','NEUTRAL')
                                      NULL                     COMMENT '观点情感倾向',
    key_factors       JSON            NULL                     COMMENT '关键影响因素',
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
            KEY idx_report_id  (report_id),
            KEY idx_sentiment  (sentiment),
    CONSTRAINT fk_viewpoint_report FOREIGN KEY (report_id) REFERENCES research_report(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='研报观点表';

-- ============================================================
-- 4. 产业链模块 (T-14)
-- ============================================================

-- 4.1 产业链节点表
CREATE TABLE industry_chain_node (
    id            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    node_name     VARCHAR(200)    NOT NULL                 COMMENT '节点名称',
    node_type     ENUM('COMPANY','INDUSTRY','PRODUCT','FACTOR')
                                  NOT NULL                 COMMENT '节点类型',
    security_code VARCHAR(20)     NULL                     COMMENT '关联证券代码',
    layer         INT             NOT NULL DEFAULT 0       COMMENT '产业链层级',
    description   TEXT            NULL                     COMMENT '节点描述',
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
            KEY idx_node_type      (node_type),
            KEY idx_security_code  (security_code),
            KEY idx_layer_type     (layer, node_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产业链节点表';

-- 4.2 产业链关系边表
CREATE TABLE industry_chain_edge (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    source_node_id  BIGINT          NOT NULL                 COMMENT '源节点ID',
    target_node_id  BIGINT          NOT NULL                 COMMENT '目标节点ID',
    relation_type   ENUM('SUPPLY','TECH_DEPENDENCY','MARKET_COMPETITION','CAUSAL')
                                    NOT NULL                 COMMENT '关系类型',
    weight          DECIMAL(5,4)    NULL DEFAULT 1.0000      COMMENT '关系权重（0~1）',
    evidence        VARCHAR(500)    NULL                     COMMENT '关系来源证据',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
            KEY idx_source_node (source_node_id),
            KEY idx_target_node (target_node_id),
    UNIQUE  KEY uk_edge         (source_node_id, target_node_id, relation_type),
    CONSTRAINT fk_edge_source FOREIGN KEY (source_node_id) REFERENCES industry_chain_node(id) ON DELETE CASCADE,
    CONSTRAINT fk_edge_target FOREIGN KEY (target_node_id) REFERENCES industry_chain_node(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产业链关系边表';

-- ============================================================
-- 5. 趋势预测模块 (T-15)
-- ============================================================

-- 5.1 预测结果表
CREATE TABLE trend_prediction (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    security_code   VARCHAR(20)     NOT NULL                 COMMENT '证券代码',
    predict_period  ENUM('DAILY','WEEKLY','MONTHLY','QUARTERLY')
                                    NOT NULL                 COMMENT '预测周期',
    predict_price   DECIMAL(15,4)   NOT NULL                 COMMENT '预测价格',
    confidence      DECIMAL(5,4)    NULL                     COMMENT '置信度（0~1）',
    factors         JSON            NULL                     COMMENT '影响因素详情',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
            KEY idx_security_code    (security_code),
            KEY idx_predict_period   (predict_period),
            KEY idx_security_period_time (security_code, predict_period, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预测结果表';

-- 5.2 投资策略表
CREATE TABLE trend_strategy (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    prediction_id   BIGINT          NOT NULL                 COMMENT '关联预测结果ID',
    security_code   VARCHAR(20)     NOT NULL                 COMMENT '证券代码',
    strategy_type   VARCHAR(50)     NOT NULL                 COMMENT '策略类型（BUY/SELL/HOLD）',
    strategy_reason TEXT            NULL                     COMMENT '策略理由',
    risk_level      VARCHAR(20)     NULL                     COMMENT '风险等级（LOW/MEDIUM/HIGH）',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
            KEY idx_prediction_id  (prediction_id),
            KEY idx_security_code  (security_code),
            KEY idx_security_time  (security_code, created_at),
    CONSTRAINT fk_strategy_prediction FOREIGN KEY (prediction_id) REFERENCES trend_prediction(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投资策略表';

-- ============================================================
-- 6. 风险评估模块 (T-16)
-- ============================================================

-- 6.1 风险快照记录表
CREATE TABLE risk_record (
    id            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    portfolio_id  BIGINT          NOT NULL                 COMMENT '组合ID',
    risk_type     ENUM('MARKET','CREDIT','LIQUIDITY')
                                  NOT NULL                 COMMENT '风险类型',
    var_value     DECIMAL(15,4)   NOT NULL                 COMMENT 'VaR值',
    es_value      DECIMAL(15,4)   NOT NULL                 COMMENT 'ES值',
    threshold     DECIMAL(15,4)   NOT NULL                 COMMENT '阈值',
    is_alerted    TINYINT         NOT NULL DEFAULT 0       COMMENT '是否已预警：0否/1是',
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
            KEY idx_portfolio_id   (portfolio_id),
            KEY idx_is_alerted     (is_alerted),
            KEY idx_created_at     (created_at),
            KEY idx_portfolio_risk_time (portfolio_id, risk_type, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='风险快照记录表';

-- 6.2 预警阈值配置表
CREATE TABLE alert_config (
    id            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    user_id       BIGINT          NOT NULL                 COMMENT '用户ID',
    risk_type     ENUM('MARKET','CREDIT','LIQUIDITY')
                                  NOT NULL                 COMMENT '风险类型',
    var_threshold DECIMAL(15,4)   NOT NULL                 COMMENT 'VaR阈值',
    es_threshold  DECIMAL(15,4)   NOT NULL                 COMMENT 'ES阈值',
    notify_method ENUM('WEBSOCKET','EMAIL')
                                  NOT NULL DEFAULT 'WEBSOCKET' COMMENT '通知方式',
    is_active     TINYINT         NOT NULL DEFAULT 1       COMMENT '是否启用：0否/1是',
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME        NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_risk (user_id, risk_type),
           KEY idx_user_id   (user_id),
    CONSTRAINT fk_alert_config_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预警阈值配置表';

-- 6.3 预警事件表
CREATE TABLE alert_event (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    user_id         BIGINT          NOT NULL                 COMMENT '用户ID',
    risk_type       ENUM('MARKET','CREDIT','LIQUIDITY')
                                    NOT NULL                 COMMENT '风险类型',
    alert_level     VARCHAR(20)     NOT NULL                 COMMENT '预警级别（WARNING/CRITICAL）',
    trigger_value   DECIMAL(15,4)   NOT NULL                 COMMENT '触发值',
    threshold_value DECIMAL(15,4)   NOT NULL                 COMMENT '阈值',
    alert_content   TEXT            NULL                     COMMENT '预警内容描述',
    is_read         TINYINT         NOT NULL DEFAULT 0       COMMENT '是否已读：0否/1是',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
            KEY idx_user_id        (user_id),
            KEY idx_created_at     (created_at),
            KEY idx_user_time      (user_id, created_at),
            KEY idx_risk_level     (risk_type, alert_level),
    CONSTRAINT fk_alert_event_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预警事件表';

-- ============================================================
-- 7. 新闻管理模块 (T-17)
-- ============================================================

-- 7.1 新闻资讯表
CREATE TABLE news_article (
    id                  BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    source              VARCHAR(50)     NOT NULL                 COMMENT '来源（东方财富/同花顺/雪球）',
    title               VARCHAR(500)    NOT NULL                 COMMENT '标题',
    content             TEXT            NULL                     COMMENT '内容',
    url                 VARCHAR(500)    NULL                     COMMENT '原文链接',
    sentiment           ENUM('POSITIVE','NEGATIVE','NEUTRAL')
                                        NULL                     COMMENT '情感倾向',
    sentiment_score     DECIMAL(5,4)    NULL                     COMMENT '情感得分（0~1）',
    related_securities  JSON            NULL                     COMMENT '相关证券列表',
    published_at        DATETIME        NOT NULL                 COMMENT '发布时间',
    crawled_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '爬取时间',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
            KEY idx_source           (source),
            KEY idx_published_at     (published_at),
            KEY idx_sentiment        (sentiment),
            KEY idx_source_pub       (source, published_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='新闻资讯表';

-- 7.2 新闻情感标签表
CREATE TABLE news_sentiment (
    id               BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    news_id          BIGINT          NOT NULL                 COMMENT '关联新闻ID',
    sentiment_label  ENUM('POSITIVE','NEGATIVE','NEUTRAL')
                                     NOT NULL                 COMMENT '情感标签',
    sentiment_score  DECIMAL(5,4)    NOT NULL                 COMMENT '情感得分（0~1）',
    related_security VARCHAR(20)     NULL                     COMMENT '相关证券代码',
    ai_model         VARCHAR(50)     NULL                     COMMENT '分析模型',
    analyzed_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分析时间',
    PRIMARY KEY (id),
            KEY idx_news_id           (news_id),
            KEY idx_related_security  (related_security),
            KEY idx_news_label        (news_id, sentiment_label),
    CONSTRAINT fk_sentiment_news FOREIGN KEY (news_id) REFERENCES news_article(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='新闻情感标签表';
