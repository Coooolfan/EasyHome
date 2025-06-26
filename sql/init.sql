CREATE TABLE sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    role VARCHAR(100) NOT NULL,
    is_enable BOOLEAN NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);



-- 新建房源表
CREATE TABLE houses (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    address VARCHAR(300) NOT NULL,
    price INTEGER NOT NULL,
    unit_price INTEGER NOT NULL,
    area INTEGER NOT NULL,
    rooms VARCHAR(50) NOT NULL,
    floor VARCHAR(20),
    build_year INTEGER,
    orientation VARCHAR(20),
    decoration VARCHAR(20),
    tag VARCHAR(500), 
    image VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 插入测试数据时
INSERT INTO houses (title, address, price, unit_price, area, rooms, floor, build_year, orientation, decoration, tag, image) VALUES
('高新区幸福小区 精装三居室', '高新区幸福小区3号楼2单元501', 1200000, 15000, 80, '3室2厅1卫', '5/6层', 2018, '南北', '精装', '地铁房,学区房,精装修', 'https://images.unsplash.com/photo-1560184897-ae75f418493e?w=400&h=300&fit=crop'),
('市中心豪华公寓 拎包入住', '市中心CBD核心区A座2008', 2500000, 25000, 100, '2室2厅2卫', '20/25层', 2020, '南向', '豪装', '豪华装修,高层景观,商务区', 'https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?w=400&h=300&fit=crop'),
('学区房 临近名校 交通便利', '教育路学府花园6号楼', 950000, 12000, 79, '2室1厅1卫', '3/6层', 2015, '南向', '简装', '学区房,地铁房,投资首选', 'https://images.unsplash.com/photo-1558618047-3c8c76ca7d13?w=400&h=300&fit=crop'),
('绿地花园 一室一厅 温馨小窝', '绿地花园9号楼1单元205', 780000, 13000, 60, '1室1厅1卫', '2/18层', 2017, '东南', '简装', '地铁房,首套房,刚需房', 'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=400&h=300&fit=crop'),
('湖景一号 高档小区 精装修', '湖景一号小区12栋1603', 3200000, 28000, 114, '3室2厅2卫', '16/28层', 2021, '南北', '精装', '湖景房,智能家居,健身房', 'https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=400&h=300&fit=crop'),
('城南新区 刚需三房 采光好', '城南新区建安路58号', 1450000, 14500, 100, '3室1厅1卫', '7/12层', 2016, '南北', '中装', '公园旁,近地铁,首付低', 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=400&h=300&fit=crop'),
('东湖别墅 花园洋房 环境优美', '东湖别墅区松林路12号', 4500000, 30000, 150, '4室3厅2卫', '3/3层', 2019, '四向', '豪装', '别墅,花园,双车位', 'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=400&h=300&fit=crop'),
('火车站旁 小户型 投资好选择', '火车站广场路28号公寓楼', 650000, 13000, 50, '1室1厅1卫', '10/20层', 2010, '南向', '简装', '交通便利,投资回报高,租金稳定', 'https://images.unsplash.com/photo-1554995207-c18c203602cb?w=400&h=300&fit=crop'),
('科技园区 复式公寓 创业优选', '科技园区创业大厦B座803', 1800000, 18000, 100, '2室2厅1卫', 'LOFT/2层', 2018, '北向', '精装', 'LOFT,近写字楼,配套齐全', 'https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?w=400&h=300&fit=crop'),
('老城区 历史风貌 文艺情怀', '老城区文化街45号', 1100000, 11000, 100, '2室1厅1卫', '2/3层', 1995, '东西', '简装', '历史建筑,文艺氛围,老城区', 'https://images.unsplash.com/photo-1605276374104-dee2a0ed3cd6?w=400&h=300&fit=crop'),
('滨江豪宅 江景房 一线江景', '滨江大道一号1幢3201', 5800000, 38000, 153, '4室2厅3卫', '32/40层', 2022, '南向', '豪装', '江景房,豪华会所,五星物业', 'https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?w=400&h=300&fit=crop'),
('梅园新村 性价比高 中间楼层', '梅园新村12号楼3单元402', 980000, 12000, 82, '2室2厅1卫', '4/7层', 2008, '南北', '中装', '成熟小区,配套齐全,拎包入住', 'https://images.unsplash.com/photo-1569152811536-fb47aced4d1e?w=400&h=300&fit=crop'),
('大学城 青年公寓 租金回报高', '大学城西路青年公寓3栋506', 720000, 12000, 60, '2室1厅1卫', '5/12层', 2015, '南向', '精装', '大学旁,租金高,投资回报率高', 'https://images.unsplash.com/photo-1580587771525-78b9dba3b914?w=400&h=300&fit=crop'),
('阳光家园 低总价三房 刚需首选', '阳光家园6号楼2单元802', 1050000, 10500, 100, '3室1厅1卫', '8/18层', 2010, '南向', '简装', '低总价,刚需,首套优选', 'https://images.unsplash.com/photo-1513584684374-8bab748fbf90?w=400&h=300&fit=crop'),
('金色家园 南北通透 双阳台', '金色家园8号楼1单元1101', 1680000, 16000, 105, '3室2厅1卫', '11/18层', 2013, '南北', '精装', '南北通透,双阳台,采光好', 'https://images.unsplash.com/photo-1494526585095-c41746248156?w=400&h=300&fit=crop'),
('星河湾 豪华装修 品质小区', '星河湾小区9号楼1801', 3200000, 26000, 123, '3室2厅2卫', '18/30层', 2016, '南向', '豪装', '名牌物业,品质小区,豪华装修', 'https://images.unsplash.com/photo-1512917774080-9991f1c4c750?w=400&h=300&fit=crop'),
('青园小区 老式小区 交通便利', '青园小区5号楼3单元201', 860000, 8600, 100, '2室1厅1卫', '2/6层', 1998, '东西', '简装', '老式小区,交通便利,性价比高', 'https://images.unsplash.com/photo-1574739782594-db4ead022697?w=400&h=300&fit=crop'),
('万达广场 商圈中心 年轻人首选', '万达广场公寓楼B座1206', 1280000, 16000, 80, '2室1厅1卫', '12/22层', 2014, '南向', '精装', '商圈中心,购物方便,年轻人首选', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=400&h=300&fit=crop'),
('汇景新城 复式楼 空间感强', '汇景新城12号楼2单元1801', 2100000, 17500, 120, '3室2厅2卫', '18-19/20层', 2015, '南北', '精装', '复式结构,空间感强,舒适度高', 'https://images.unsplash.com/photo-1513694203232-719a280e022f?w=400&h=300&fit=crop'),
('新城花园 花园洋房 带露台', '新城花园A区12号', 2800000, 20000, 140, '4室2厅2卫', '3/3层', 2017, '四向', '精装', '花园洋房,露台,环境优美', 'https://images.unsplash.com/photo-1523217582562-09d0def993a6?w=400&h=300&fit=crop'),
('临湖别墅 独栋别墅 私家庭院', '临湖别墅区15号', 6500000, 32500, 200, '5室3厅3卫', '3/3层', 2021, '四向', '豪装', '独栋别墅,私家庭院,湖景房', 'https://images.unsplash.com/photo-1576941089067-2de3c901e126?w=400&h=300&fit=crop'),
('公园一号 中高层 视野开阔', '公园一号小区6号楼1单元1503', 1820000, 17000, 107, '3室2厅1卫', '15/28层', 2018, '南北', '精装', '公园景观,视野开阔,环境好', 'https://images.unsplash.com/photo-1583608205776-bfd35f0d9f83?w=400&h=300&fit=crop'),
('地铁口 小户型 投资自住两相宜', '地铁3号线旁嘉禾公寓2栋608', 920000, 15333, 60, '1室1厅1卫', '6/12层', 2019, '东南', '精装', '地铁房,小户型,自住投资两相宜', 'https://images.unsplash.com/photo-1556912172-45b7abe8b7e1?w=400&h=300&fit=crop');


CREATE EXTENSION vector;

CREATE TABLE houses_vec (
    id BIGSERIAL PRIMARY KEY,
    house_id BIGINT NOT NULL,
    embedding vector(1536) NOT NULL
);

CREATE INDEX ON houses_vec USING hnsw(embedding vector_l2_ops);

-- 创建房源推送记录表
CREATE TABLE house_records (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    address VARCHAR(300) NOT NULL,
    price INTEGER NOT NULL,
    area INTEGER NOT NULL,
    unit_price INTEGER NOT NULL,
    rooms VARCHAR(50) NOT NULL,
    floor VARCHAR(20),
    build_year INTEGER,
    orientation VARCHAR(20),
    decoration VARCHAR(20),
    tag VARCHAR(500),  -- 存储标签数组
    image VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING(待审核), APPROVED(已通过), REJECTED(已拒绝)
    reason VARCHAR(200),  -- 拒绝原因(如果有)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 添加索引
CREATE INDEX idx_house_publish_records_user_id ON house_publish_records(user_id);
CREATE INDEX idx_house_publish_records_status ON house_publish_records(status);


-- 创建房屋和用户对应关系表
CREATE TABLE house_user_relation (
    id BIGSERIAL PRIMARY KEY,
    house_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- 外键约束
    CONSTRAINT fk_house FOREIGN KEY (house_id) REFERENCES houses(id) ON DELETE CASCADE,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
    
);

-- 添加索引以提高查询性能
CREATE INDEX idx_house_user_relation_house_id ON house_user_relation(house_id);
CREATE INDEX idx_house_user_relation_user_id ON house_user_relation(user_id);


CREATE TABLE user_favorites (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    house_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_house UNIQUE (user_id, house_id)
);

-- 添加注释
COMMENT ON TABLE user_favorites IS '用户收藏表';
COMMENT ON COLUMN user_favorites.user_id IS '用户ID';
COMMENT ON COLUMN user_favorites.house_id IS '房源ID';
COMMENT ON COLUMN user_favorites.created_at IS '创建时间';
COMMENT ON CONSTRAINT uk_user_house ON user_favorites IS '确保用户不会重复收藏同一房源';

-- 添加索引以提高查询性能
CREATE INDEX idx_user_favorites_user_id ON user_favorites(user_id);
CREATE INDEX idx_user_favorites_house_id ON user_favorites(house_id);

-- 添加外键约束
ALTER TABLE user_favorites ADD CONSTRAINT fk_user_favorites_user_id 
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE;
ALTER TABLE user_favorites ADD CONSTRAINT fk_user_favorites_house_id 
    FOREIGN KEY (house_id) REFERENCES houses(id) ON DELETE CASCADE;