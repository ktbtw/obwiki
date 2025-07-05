-- AI对话会话表
CREATE TABLE ai_chat_session (
    id BIGINT PRIMARY KEY COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(255) COMMENT '会话标题',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    message_count INT DEFAULT 0 COMMENT '消息数量',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time),
    INDEX idx_update_time (update_time)
) COMMENT 'AI对话会话表';

-- AI对话消息表
CREATE TABLE ai_chat_message (
    id BIGINT PRIMARY KEY COMMENT '消息ID',
    session_id BIGINT NOT NULL COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role VARCHAR(20) NOT NULL COMMENT '消息角色：user/assistant',
    content TEXT NOT NULL COMMENT '消息内容',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    model VARCHAR(100) COMMENT '模型名称',
    temperature DECIMAL(3,2) COMMENT '温度参数',
    max_tokens INT COMMENT '最大token数',
    is_stream BOOLEAN DEFAULT FALSE COMMENT '是否流式响应',
    response_time BIGINT COMMENT '响应时间（毫秒）',
    INDEX idx_session_id (session_id),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time),
    INDEX idx_role (role)
) COMMENT 'AI对话消息表'; 