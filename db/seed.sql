-- ============================================================
-- 智能投研系统 种子数据脚本
-- 插入默认角色、测试管理员账号
-- 任务：T-01 MySQL 数据库表结构初始化
-- ============================================================

USE smart_investment;

-- ============================================================
-- 1. 默认角色数据
-- ============================================================
INSERT INTO sys_role (role_name, description) VALUES
    ('INVESTOR', '普通投资者，可查看行情、新闻、研报等公开数据'),
    ('ANALYST',  '分析师，可上传财报、配置预警阈值、查看产业链深度分析'),
    ('ADMIN',    '系统管理员，可管理用户、角色分配、系统配置');

-- ============================================================
-- 2. 测试管理员账号
--    用户名：admin
--    密码：  admin123 (BCrypt加密)
--    角色：  ADMIN
-- ============================================================
INSERT INTO sys_user (username, password, role, status) VALUES
    ('admin', '$2b$12$XqbP6nViFS8d/cPuybnGCOqp.VvrmkmOHHh8P1RZYNo1ZdPnPmieG', 'ADMIN', 1);

-- 管理员关联角色（sys_user_role）
INSERT INTO sys_user_role (user_id, role_id)
    SELECT u.id, r.id
    FROM sys_user u, sys_role r
    WHERE u.username = 'admin' AND r.role_name = 'ADMIN';

-- ============================================================
-- 3. 默认预警阈值（管理员初始配置）
--    为 admin 用户创建三种风险类型的默认阈值
-- ============================================================
INSERT INTO alert_config (user_id, risk_type, var_threshold, es_threshold, notify_method, is_active)
    SELECT u.id, 'MARKET',    0.0500, 0.0700, 'WEBSOCKET', 1
    FROM sys_user u WHERE u.username = 'admin';

INSERT INTO alert_config (user_id, risk_type, var_threshold, es_threshold, notify_method, is_active)
    SELECT u.id, 'CREDIT',    0.0300, 0.0500, 'WEBSOCKET', 1
    FROM sys_user u WHERE u.username = 'admin';

INSERT INTO alert_config (user_id, risk_type, var_threshold, es_threshold, notify_method, is_active)
    SELECT u.id, 'LIQUIDITY', 0.0400, 0.0600, 'WEBSOCKET', 1
    FROM sys_user u WHERE u.username = 'admin';
