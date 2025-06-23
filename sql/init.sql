CREATE TABLE sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
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
('学区房 临近名校 交通便利', '教育路学府花园6号楼', 950000, 12000, 79, '2室1厅1卫', '3/6层', 2015, '南向', '简装', '学区房,地铁房,投资首选', 'https://images.unsplash.com/photo-1558618047-3c8c76ca7d13?w=400&h=300&fit=crop');
