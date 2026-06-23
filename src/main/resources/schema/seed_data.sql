-- ============================================================
-- TalkBlog 种子数据脚本
-- 插入默认用户、专栏(series)及示例文章
-- 可重复执行（幂等）
-- ============================================================

-- 1. 默认用户（密码：123456，BCrypt 哈希）
INSERT INTO users (username, email, password_hash, status, login_type, created_at, updated_at)
SELECT '管理员', 'admin@talkblog.com',
       '$2a$10$NAiSlMRKOpKCBK1VD/Z.Ze2wjRzwxXIXENi7x6TOdR1qoqfZBgUJy',
       1, 'EMAIL', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@talkblog.com');

INSERT INTO users (username, email, password_hash, status, login_type, created_at, updated_at)
SELECT '张三', 'zhangsan@talkblog.com',
       '$2a$10$NAiSlMRKOpKCBK1VD/Z.Ze2wjRzwxXIXENi7x6TOdR1qoqfZBgUJy',
       1, 'EMAIL', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'zhangsan@talkblog.com');

INSERT INTO users (username, email, password_hash, status, login_type, created_at, updated_at)
SELECT '李四', 'lisi@talkblog.com',
       '$2a$10$NAiSlMRKOpKCBK1VD/Z.Ze2wjRzwxXIXENi7x6TOdR1qoqfZBgUJy',
       1, 'EMAIL', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'lisi@talkblog.com');

-- 分配默认角色 USER
INSERT INTO user_roles (user_id, role_id)
SELECT u.user_id, r.role_id FROM users u, roles r
WHERE u.email = 'admin@talkblog.com' AND r.role_name = 'USER'
  AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.user_id AND ur.role_id = r.role_id);

INSERT INTO user_roles (user_id, role_id)
SELECT u.user_id, r.role_id FROM users u, roles r
WHERE u.email = 'zhangsan@talkblog.com' AND r.role_name = 'USER'
  AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.user_id AND ur.role_id = r.role_id);

INSERT INTO user_roles (user_id, role_id)
SELECT u.user_id, r.role_id FROM users u, roles r
WHERE u.email = 'lisi@talkblog.com' AND r.role_name = 'USER'
  AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.user_id AND ur.role_id = r.role_id);

-- 给管理员分配 ADMIN 角色
INSERT INTO user_roles (user_id, role_id)
SELECT u.user_id, r.role_id FROM users u, roles r
WHERE u.email = 'admin@talkblog.com' AND r.role_name = 'ADMIN'
  AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.user_id AND ur.role_id = r.role_id);


-- 2. 专栏（Series）
INSERT INTO series (name, description, author_id, created_at, updated_at)
SELECT '技术前沿', '分享最新技术趋势、编程语言特性、架构设计与最佳实践。',
       (SELECT user_id FROM users WHERE email = 'admin@talkblog.com' LIMIT 1),
       NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM series WHERE name = '技术前沿');

INSERT INTO series (name, description, author_id, created_at, updated_at)
SELECT '科技洞察', '关注科技行业动态、产品评测与创新技术解读。',
       (SELECT user_id FROM users WHERE email = 'admin@talkblog.com' LIMIT 1),
       NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM series WHERE name = '科技洞察');

INSERT INTO series (name, description, author_id, created_at, updated_at)
SELECT '生活随笔', '记录日常生活点滴、旅行见闻与生活感悟。',
       (SELECT user_id FROM users WHERE email = 'zhangsan@talkblog.com' LIMIT 1),
       NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM series WHERE name = '生活随笔');

INSERT INTO series (name, description, author_id, created_at, updated_at)
SELECT '财经观察', '解读宏观经济形势、投资理财策略与市场分析。',
       (SELECT user_id FROM users WHERE email = 'admin@talkblog.com' LIMIT 1),
       NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM series WHERE name = '财经观察');

INSERT INTO series (name, description, author_id, created_at, updated_at)
SELECT '教育思考', '探讨教育理念、学习方法与教育行业变革。',
       (SELECT user_id FROM users WHERE email = 'zhangsan@talkblog.com' LIMIT 1),
       NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM series WHERE name = '教育思考');

INSERT INTO series (name, description, author_id, created_at, updated_at)
SELECT '文化漫谈', '品味文学艺术、历史文化与当代文化现象。',
       (SELECT user_id FROM users WHERE email = 'lisi@talkblog.com' LIMIT 1),
       NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM series WHERE name = '文化漫谈');

INSERT INTO series (name, description, author_id, created_at, updated_at)
SELECT '体育世界', '追踪体育赛事、运动健康与竞技精神。',
       (SELECT user_id FROM users WHERE email = 'lisi@talkblog.com' LIMIT 1),
       NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM series WHERE name = '体育世界');

INSERT INTO series (name, description, author_id, created_at, updated_at)
SELECT '数码评测', '深度评测最新数码产品，分享使用体验与购买建议。',
       (SELECT user_id FROM users WHERE email = 'admin@talkblog.com' LIMIT 1),
       NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM series WHERE name = '数码评测');

INSERT INTO series (name, description, author_id, created_at, updated_at)
SELECT '游戏天地', '热门游戏推荐、攻略心得与游戏文化讨论。',
       (SELECT user_id FROM users WHERE email = 'zhangsan@talkblog.com' LIMIT 1),
       NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM series WHERE name = '游戏天地');

INSERT INTO series (name, description, author_id, created_at, updated_at)
SELECT '影视娱乐', '影视剧评、综艺推荐与娱乐资讯。',
       (SELECT user_id FROM users WHERE email = 'lisi@talkblog.com' LIMIT 1),
       NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM series WHERE name = '影视娱乐');


-- 3. 文章数据
-- 辅助：获取各系列和用户的 ID
DO $$
DECLARE
    admin_id BIGINT;
    zhangsan_id BIGINT;
    lisi_id BIGINT;
    tech_series_id BIGINT;
    science_series_id BIGINT;
    lifestyle_series_id BIGINT;
    finance_series_id BIGINT;
    education_series_id BIGINT;
    culture_series_id BIGINT;
    sports_series_id BIGINT;
    digital_series_id BIGINT;
    gaming_series_id BIGINT;
    entertainment_series_id BIGINT;
    blog_count INT;
BEGIN
    SELECT user_id INTO admin_id FROM users WHERE email = 'admin@talkblog.com';
    SELECT user_id INTO zhangsan_id FROM users WHERE email = 'zhangsan@talkblog.com';
    SELECT user_id INTO lisi_id FROM users WHERE email = 'lisi@talkblog.com';

    SELECT id INTO tech_series_id FROM series WHERE name = '技术前沿';
    SELECT id INTO science_series_id FROM series WHERE name = '科技洞察';
    SELECT id INTO lifestyle_series_id FROM series WHERE name = '生活随笔';
    SELECT id INTO finance_series_id FROM series WHERE name = '财经观察';
    SELECT id INTO education_series_id FROM series WHERE name = '教育思考';
    SELECT id INTO culture_series_id FROM series WHERE name = '文化漫谈';
    SELECT id INTO sports_series_id FROM series WHERE name = '体育世界';
    SELECT id INTO digital_series_id FROM series WHERE name = '数码评测';
    SELECT id INTO gaming_series_id FROM series WHERE name = '游戏天地';
    SELECT id INTO entertainment_series_id FROM series WHERE name = '影视娱乐';

    SELECT COUNT(1) INTO blog_count FROM blogs WHERE series_id = tech_series_id;

    IF blog_count > 0 THEN
        RETURN;
    END IF;

    -- ===== 技术前沿（5篇） =====
    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('深入理解 Java 21 虚拟线程：原理与实践',
     'Java 21 正式发布了虚拟线程（Virtual Threads），这是 Project Loom 多年孵化的成果。本文将深入探讨虚拟线程的实现原理、使用场景以及性能对比。\n\n## 什么是虚拟线程\n虚拟线程是 Java 平台对轻量级线程的实现，由 JVM 管理而非操作系统。与传统的平台线程相比，虚拟线程的创建成本极低，可以轻松创建数十万个并发任务。\n\n## 性能对比\n在我们的基准测试中，使用虚拟线程处理 10000 个并发请求，内存消耗仅为平台线程的 1/10。\n\n## 最佳实践\n1. 避免使用 ThreadLocal（虚拟线程池会复用）\n2. 使用结构化并发管理任务生命周期\n3. 注意同步块的使用',
     admin_id, tech_series_id, 1, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('Rust 生命周期注解：从入门到精通',
     'Rust 的所有权系统是其最独特也最具挑战性的特性。生命周期注解是 Rust 类型系统的重要组成部分，确保引用的有效性。\n\n## 基础概念\n生命周期注解并不改变引用的实际生命周期，而是描述了多个引用之间的关系。\n\n## 常见模式\n- 函数签名中的生命周期：`fn longest(x: &str, y: &str) -> &str`\n- 结构体中的生命周期\n- 生命周期省略规则\n\n## 实际案例\n通过一个 JSON 解析器的实现，展示生命周期在实际项目中的应用。',
     admin_id, tech_series_id, 1, NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('Kubernetes 调度器深度剖析：如何选择最优节点',
     'K8s 调度器负责将 Pod 分配到最合适的 Node 上。本文从源码层面剖析调度器的核心流程。\n\n## 调度框架\n调度器采用"调度框架"设计，包含过滤阶段（Predicates）和打分阶段（Priorities）。\n\n## 过滤策略\n- Pod 资源请求 vs 节点可用资源\n- 节点选择器和亲和性规则\n- 污点和容忍度\n- 端口冲突检查\n\n## 自定义调度器\n通过实现自定义调度插件，可以满足特定业务场景的调度需求。',
     admin_id, tech_series_id, 1, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('PostgreSQL 16 新特性：性能提升与开发者体验',
     'PostgreSQL 16 带来了诸多令人兴奋的特性更新。本文整理了最值得关注的改进。\n\n## 逻辑复制增强\n- 支持双向逻辑复制\n- 并行逻辑复制大幅提升同步性能\n- 发布/订阅的更多控制选项\n\n## 查询性能优化\n- 更智能的并行查询计划\n- FULL 外连接性能提升 30%\n- 窗口函数优化\n\n## 开发者体验\n- SQL/JSON 构造函数语法增强\n- pg_stat_io 视图提供更细粒度的 I/O 统计',
     zhangsan_id, tech_series_id, 1, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('微服务架构下的 API 网关选型指南',
     'API 网关是微服务架构的关键组件。本文对比了主流网关方案的技术特点与适用场景。\n\n## Spring Cloud Gateway\n- 基于 WebFlux 的非阻塞模型\n- 与 Spring 生态无缝集成\n- 路由配置灵活\n\n## Kong\n- 基于 OpenResty 的高性能网关\n- 丰富的插件生态\n- K8s Ingress Controller 支持\n\n## APISIX\n- Apache 顶级项目\n- 动态路由与热加载\n- 多语言插件支持\n\n## 选型建议\n根据团队技术栈、性能需求和运维能力选择最适合的方案。',
     zhangsan_id, tech_series_id, 1, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day');


    -- ===== 科技洞察（4篇） =====
    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('AI 大模型时代的软件工程变革',
     '以 ChatGPT、Claude 为代表的大语言模型正在深刻改变软件开发的方方面面。\n\n## 代码生成\nAI 辅助编码已从简单的代码补全发展到能够生成完整的功能模块。GitHub Copilot、Codeium 等工具大幅提升了开发效率。\n\n## 测试自动化\nAI 可以基于代码自动生成单元测试用例，识别边界条件和异常场景。\n\n## 未来展望\nAI 不会取代程序员，但会用 AI 的程序员将会取代不会用 AI 的程序员。',
     admin_id, science_series_id, 1, NOW() - INTERVAL '9 days', NOW() - INTERVAL '9 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('量子计算入门：Qubit、叠加态与量子纠缠',
     '量子计算是计算范式的革命性转变。本文用通俗易懂的方式介绍量子计算的核心概念。\n\n## Qubit 与传统比特的区别\n传统比特只有 0 或 1 两种状态，而 Qubit 可以处于 0 和 1 的叠加态。\n\n## 量子纠缠\n当两个 Qubit 纠缠后，无论相距多远，测量其中一个会立即影响另一个的状态。\n\n## 当前进展\nIBM、Google、微软等公司都在量子计算领域投入巨资。当前最先进的量子处理器已突破 1000 Qubit。',
     admin_id, science_series_id, 1, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('自动驾驶的 L2 到 L4：我们走到哪一步了？',
     '自动驾驶分级标准由 SAE International 制定。从 L2 的部分自动化到 L4 的高度自动化，每一步都是技术突破。\n\n## 当前主流方案\n- 纯视觉方案（Tesla）\n- 激光雷达融合方案（Waymo、百度 Apollo）\n- 高精地图辅助方案\n\n## 技术挑战\n1. 边缘情况处理（Corner Cases）\n2. 传感器融合的可靠性\n3. 决策算法的可解释性\n\n## 法规进展\n多个国家和地区已开始制定自动驾驶相关法律法规。',
     zhangsan_id, science_series_id, 1, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('Web3 与去中心化：泡沫还是未来？',
     'Web3 概念在过去几年经历了过山车式的发展。本文冷静分析 Web3 的技术本质与商业价值。\n\n## 区块链的实质\n区块链本质上是一个分布式账本技术，其核心价值在于去中心化信任。\n\n## 主要应用场景\n- DeFi 去中心化金融\n- NFT 数字藏品\n- DAO 去中心化自治组织\n\n## 挑战与反思\n可扩展性、用户体验、监管合规是 Web3 面临的三大挑战。单纯炒作概念无法构建可持续的生态。',
     admin_id, science_series_id, 1, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');


    -- ===== 生活随笔（5篇） =====
    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('在京都体验茶道：一期一会的哲学',
     '日本茶道中的"一期一会"理念，提醒我们珍惜每一次相遇。在京都的茶室中，我深深体会到了这种独特的文化美学。\n\n## 茶道礼仪\n从进入茶室的方式到喝茶的动作，每一个步骤都有其独特的含义。\n\n## 和敬清寂\n千利休提出的茶道四规：和、敬、清、寂，不仅是茶道的精神，更是一种生活态度。\n\n## 感悟\n在快节奏的现代生活中，偶尔停下脚步，专注地做一件事，本身就是一种奢侈。',
     zhangsan_id, lifestyle_series_id, 1, NOW() - INTERVAL '11 days', NOW() - INTERVAL '11 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('我的极简主义生活实验：30天扔掉100件物品',
     '受到《断舍离》的启发，我决定进行为期30天的极简生活实验。每天至少扔掉或捐赠一件不再需要的物品。\n\n## 实验结果\n30天共清理了127件物品，包括衣物、书籍、电子产品和各种杂物。\n\n## 收获\n1. 居住空间更加宽敞舒适\n2. 减少了选择疲劳\n3. 节省了整理时间\n4. 意识到自己真正需要的东西其实很少\n\n## 反思\n极简不是目的，而是手段。真正的目的是把时间和精力留给最重要的人和事。',
     zhangsan_id, lifestyle_series_id, 1, NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('城市里的阳台菜园：从种子到餐桌',
     '在城市公寓中开辟一个小小的阳台菜园，是我这两年最成功的项目之一。\n\n## 适合种植的蔬菜\n- 小番茄（推荐樱桃番茄品种）\n- 生菜（生长周期短，约30天可收获）\n- 香草（薄荷、罗勒、迷迭香）\n- 辣椒（观赏性和实用性兼备）\n\n## 新手建议\n从最容易成活的品种开始，逐步积累经验。土壤和光照是关键因素。',
     zhangsan_id, lifestyle_series_id, 1, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('跑步一年：从5公里到半马的蜕变',
     '一年前我还是个跑800米就喘不过气的运动小白。经过一年的坚持，现在我完成了人生第一个半程马拉松。\n\n## 训练方法\n- 每周3-4次跑步\n- 遵循10%原则（每周跑量增加不超过10%）\n- 交叉训练预防受伤\n\n## 装备推荐\n一双好的跑鞋是最重要的投资。根据足弓类型选择合适的跑鞋。\n\n## 心得体会\n跑步教会我的不是如何跑得更快，而是如何坚持。',
     lisi_id, lifestyle_series_id, 1, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('独处是一种能力：如何享受一个人的时光',
     '在这个永远在线、随时连接的时代，独处反而成了一种稀缺的能力。\n\n## 独处的价值\n独处不是孤独，而是一种主动选择。它让我们有机会与自己对话，整理思绪，恢复能量。\n\n## 如何开始\n- 每周安排一段"数字排毒"时间\n- 培养一个可以独自完成的爱好\n- 学习冥想和正念\n\n## 推荐书单\n- 《孤独：回归自我》\n- 《安静：内向性格的力量》\n- 《心流：最优体验心理学》',
     zhangsan_id, lifestyle_series_id, 1, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day');


    -- ===== 财经观察（4篇） =====
    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('定投指数基金：普通人的财富自由之路',
     '对于大多数没有专业金融背景的普通人来说，定投指数基金是最省心也最有效的长期投资策略。\n\n## 为什么是指数基金\n- 管理费用低（平均0.15%）\n- 分散风险（覆盖整个市场）\n- 长期收益稳定（标普500年化约10%）\n\n## 定投策略\n固定在每月发薪日投资固定金额，无需择时。\n\n## 注意事项\n1. 保持投资纪律\n2. 不要在市场下跌时恐慌卖出\n3. 长期持有（至少5年以上）',
     admin_id, finance_series_id, 1, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('2024年全球经济展望：机遇与挑战',
     '2024年全球经济面临多重不确定性，但也蕴含新的增长机遇。\n\n## 主要趋势\n1. 通胀逐步回落，但仍高于目标水平\n2. 央行货币政策转向在即\n3. AI驱动的生产力革命\n4. 地缘政治风险持续\n\n## 投资建议\n- 关注AI相关产业链\n- 增持高质量债券\n- 适度配置黄金等避险资产\n- 新兴市场存在低估值机会',
     admin_id, finance_series_id, 1, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('个人税务筹划：合法节税的10个技巧',
     '合理合法的税务筹划可以帮助你最大程度保留劳动成果。\n\n## 工薪族的节税策略\n1. 充分利用专项附加扣除（子女教育、大病医疗、住房贷款利息等）\n2. 参与企业年金计划\n3. 购买商业健康保险\n4. 合理安排年终奖发放方式\n\n## 自由职业者的注意事项\n- 合理划分经营所得与劳务报酬\n- 利用小型微利企业税收优惠\n- 合规取得成本发票\n\n> 提示：税务筹划必须在法律框架内进行，避免偷税漏税。',
     admin_id, finance_series_id, 1, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('房地产市场的冰与火：未来五年怎么走？',
     '中国房地产市场正在经历深刻的调整期。本文从多个维度分析未来趋势。\n\n## 人口因素\n人口负增长和老龄化将长期影响住房需求。\n\n## 政策转向\n从"房住不炒"到"保交楼"，政策重心正在从调控转向稳定。\n\n## 区域分化\n核心城市的核心地段仍具保值潜力，但普涨时代已经结束。\n\n## 建议\n自住需求可以择机入市，投资需求需要更加谨慎。',
     admin_id, finance_series_id, 1, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');


    -- ===== 教育思考（4篇） =====
    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('双减政策后的教育新生态',
     '双减政策实施两年来，基础教育领域发生了深刻的变化。\n\n## 学科培训的退潮\n曾经的教培巨头纷纷转型或退出，学科类校外培训机构压减超过90%。\n\n## 素质教育的兴起\n编程、艺术、体育等素质教育需求快速增长。\n\n## 家校共育\n家长的角色开始回归，家庭教育的重要性被重新认识。\n\n## 思考\n教育的本质不是筛选，而是唤醒每个人内在的潜能。',
     zhangsan_id, education_series_id, 1, NOW() - INTERVAL '9 days', NOW() - INTERVAL '9 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('如何培养孩子的自主学习能力',
     '自主学习能力是决定一个人终身成长的关键素质。作为家长，我们应该如何培养孩子的这项能力？\n\n## 从兴趣出发\n当孩子对某个领域产生真正的兴趣时，学习就不再是被动的任务。\n\n## 方法建议\n1. 提供选择权：让孩子参与学习计划的制定\n2. 设置合理目标：分解大目标为可执行的小步骤\n3. 鼓励自我评价：而不是事事依赖外部评价\n4. 允许失败：把错误视为学习的一部分\n\n## 家长的角色\n家长应该是引导者而非指令者，是资源提供者而非评判者。',
     zhangsan_id, education_series_id, 1, NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('在线教育的未来：AI个性化学习的可能性',
     'AI技术正在为在线教育带来前所未有的个性化学习体验。\n\n## 自适应学习系统\n基于知识图谱和机器学习算法，系统可以实时评估学生的掌握程度并动态调整学习路径。\n\n## AI辅导教师\nGPT等大语言模型可以作为24小时在线的个性化辅导教师，解答学生的问题。\n\n## 数据驱动的教学决策\n学习分析技术帮助教师及时识别学习困难的学生，进行精准干预。\n\n## 挑战\n数据隐私、算法偏见、技术依赖是需要认真对待的问题。',
     zhangsan_id, education_series_id, 1, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('读《终身成长》：固定型思维 vs 成长型思维',
     '斯坦福大学心理学教授卡罗尔·德韦克的《终身成长》揭示了两种思维模式对人生的深远影响。\n\n## 固定型思维\n认为能力是固定不变的，害怕失败，回避挑战。\n\n## 成长型思维\n相信能力可以通过努力提升，拥抱挑战，从失败中学习。\n\n## 如何转变\n1. 觉察自己的固定型思维触发点\n2. 把"我不会"改成"我还没学会"\n3. 重视过程而非结果\n4. 从他人的成功中学习而非感到威胁',
     lisi_id, education_series_id, 1, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day');


    -- ===== 文化漫谈（4篇） =====
    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('从《千里江山图》看宋代美学的当代意义',
     '王希孟的《千里江山图》是中国山水画的巅峰之作。这幅画不仅是艺术杰作，更蕴含了深刻的哲学思想。\n\n## 画面解读\n长卷以散点透视法展现千里江山，青山绿水间点缀着村舍、桥梁、舟船和人物。\n\n## 宋代美学\n宋代美学追求"平淡天真"，强调内在精神表达而非外在形式模仿。\n\n## 当代启示\n在这个追求速度和效率的时代，宋代美学提醒我们慢下来，感受生活中的诗意。',
     lisi_id, culture_series_id, 1, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('日本动漫中的传统文化符号',
     '日本动漫不仅是流行文化的重要载体，也是传统文化传播的桥梁。\n\n## 神社与妖怪\n从《千与千寻》到《虫师》，神道教文化元素贯穿其中。\n\n## 武士道精神\n《浪客剑心》《鬼灭之刃》等作品传承了武士道的精神内核。\n\n## 茶道与花道\n日常场景中不经意间展现的传统文化细节，构成了日本动漫的独特魅力。\n\n## 全球影响\n日本动漫让全世界年轻人对日本传统文化产生了兴趣。',
     lisi_id, culture_series_id, 1, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('为什么我们需要人文精神？',
     '在科技高速发展的今天，人文精神的价值被严重低估了。\n\n## 什么是人文精神\n人文精神强调人的价值、尊严和潜力，关注人的精神世界和情感体验。\n\n## 科技与人文的平衡\n单纯的技术进步无法解决所有问题。我们需要人文精神来指导科技的发展方向。\n\n## 如何培养人文素养\n- 阅读经典文学作品\n- 欣赏艺术和音乐\n- 学习历史哲学\n- 保持批判性思考\n\n人文精神不是可有可无的装饰，而是让技术真正服务于人的指南针。',
     lisi_id, culture_series_id, 1, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('城市记忆：那些正在消失的老街巷',
     '每座城市都有一些承载着历史记忆的老街巷。它们正在现代化的浪潮中逐渐消失。\n\n## 老北京胡同\n胡同是北京城的毛细血管，承载了数百年的市井生活。\n\n## 上海弄堂\n石库门建筑是上海独特的城市记忆，也是海派文化的活化石。\n\n## 保护与更新\n如何在城市更新中保留历史记忆，是每个城市面临的共同课题。\n\n## 记录的意义\n用文字和镜头记录这些即将消失的风景，是对城市记忆的一种保存。',
     lisi_id, culture_series_id, 1, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');


    -- ===== 体育世界（4篇） =====
    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('NBA 2024-25赛季前瞻：新格局下的争冠版图',
     '新赛季的 NBA 格局发生了巨大变化。多支球队通过交易和选秀重塑了阵容。\n\n## 东部形势\n凯尔特人依然强势，雄鹿和76人面临变数，魔术和步行者有望崛起。\n\n## 西部混战\n掘金、勇士、湖人、快船、太阳、独行侠，西部前六的竞争将异常激烈。\n\n## 新星涌现\n文班亚马、霍姆格伦等新生代球员将带来更多看点。\n\n## 预测\n东部看好凯尔特人，西部看好掘金。',
     lisi_id, sports_series_id, 1, NOW() - INTERVAL '9 days', NOW() - INTERVAL '9 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('马拉松训练指南：从零到首马的完整计划',
     '完成一场马拉松是许多跑者的梦想。这份训练指南将帮助你科学备战。\n\n## 训练周期\n建议16-20周的训练周期，分为基础期、提升期、巅峰期和减量期。\n\n## 每周训练安排\n- 3次轻松跑（建立有氧基础）\n- 1次间歇跑（提升速度）\n- 1次长距离跑（增强耐力）\n- 2次力量训练（预防受伤）\n\n## 比赛策略\n前10公里控制配速，中间20公里保持节奏，最后12公里根据状态调整。\n\n## 恢复建议\n完赛后充分休息，补充营养，逐步恢复训练。',
     lisi_id, sports_series_id, 1, NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('电子竞技入奥：传统体育与数字竞技的融合',
     '电子竞技作为正式项目进入亚运会，标志着数字竞技获得了主流认可。\n\n## 发展历程\n从被质疑到被认可，电子竞技用了20年时间。\n\n## 入奥挑战\n1. 游戏版本更新频繁，需要找到稳定的竞技版本\n2. 不同国家对于游戏内容审查标准不一\n3. 传统体育界对电子竞技的接受度仍需提高\n\n## 前景展望\n尽管面临挑战，但电竞入奥只是时间问题。数字原住民一代正在成为社会主流。',
     lisi_id, sports_series_id, 1, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('力量训练入门：避开新手最常见的5个错误',
     '力量训练是健身的基础。但很多新手在入门阶段容易犯一些常见错误。\n\n## 错误1：动作姿势不正确\n核心问题：借力代偿，目标肌群发力感不足。\n解决方案：从小重量开始，注重动作质量。\n\n## 错误2：忽视热身\n核心问题：增加受伤风险，影响训练效果。\n解决方案：训练前进行5-10分钟动态拉伸。\n\n## 错误3：过度训练\n核心问题：不给肌肉足够的恢复时间。\n解决方案：同一肌群间隔48小时再训练。\n\n## 错误4：忽视营养\n核心问题：训练不配合饮食，效果大打折扣。\n解决方案：保证足够的蛋白质摄入。\n\n## 错误5：急于求成\n核心问题：追求快速效果导致受伤。\n解决方案：循序渐进，享受进步的过程。',
     lisi_id, sports_series_id, 1, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day');


    -- ===== 数码评测（5篇） =====
    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('MacBook Pro M3 Max 深度使用一个月体验',
     '搭载 M3 Max 芯片的 MacBook Pro 是我用过最强的笔记本电脑。本文分享一个月来的真实使用感受。\n\n## 性能表现\nM3 Max 的多核性能比 M2 Max 提升了约 30%，GPU 性能提升更明显。\n\n## 续航能力\n正常办公使用可以坚持 12-15 小时，重度视频剪辑约 6-8 小时。\n\n## 不足之处\n1. 价格昂贵\n2. 部分专业软件尚未完全适配\n3. 升级内存和 SSD 成本极高\n\n## 适合人群\n视频创作者、软件开发者、AI 研究人员。',
     admin_id, digital_series_id, 1, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('Sony WH-1000XM5  vs AirPods Max：降噪耳机终极对决',
     '两款顶级降噪耳机的全方位对比评测。\n\n## 降噪效果\nSony XM5 的低频降噪更胜一筹，AirPods Max 的中高频降噪更好。\n\n## 音质表现\n- Sony XM5：声音自然均衡，解析力出色\n- AirPods Max：声场开阔，低频有力\n\n## 佩戴舒适度\nSony XM5 仅 250g，长时间佩戴更舒适。AirPods Max 385g 明显偏重。\n\n## 生态整合\n如果你深度使用 Apple 生态，AirPods Max 的无缝切换体验无可替代。\n\n## 结论\n综合性价比和舒适度，Sony XM5 是更好的选择。',
     admin_id, digital_series_id, 1, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('2024年最佳机械键盘推荐：从入门到退烧',
     '机械键盘的世界丰富多彩。本文按预算和需求推荐最具性价比的选择。\n\n## 入门级（300元以下）\n- Keychron C1 Pro：TKL 布局，热插拔轴座\n- RK R87：性价比之王，Gasket结构\n\n## 进阶级（500-1000元）\n- 京东京造 Q1：铝坨坨，QMK 固件\n- NuPhy Air75：矮轴，便携\n\n## 高端（1000元以上）\n- HHKB Professional：静电容，编程神器\n- Rama Works：艺术品级别的做工\n\n## 轴体选择建议\n打字为主选静音轴或茶轴，游戏为主选线性轴。',
     admin_id, digital_series_id, 1, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('家庭网络搭建指南：Mesh组网 vs AC+AP',
     '全屋 WiFi 覆盖是现代家庭的刚需。两种主流方案各有优劣。\n\n## Mesh 组网\n优点：部署简单，支持无线回程，漫游切换无缝。\n推荐品牌：TP-Link Deco、华硕 AiMesh、小米 Mesh。\n\n## AC + AP\n优点：性能稳定，覆盖面积大，支持更多终端。\n推荐方案：Ubiquiti UniFi、TP-Link 企业级。\n\n## 选型建议\n- 100平米以下：单个高端路由器即可\n- 100-200平米：Mesh 组网\n- 200平米以上或别墅：AC + AP\n- 租房用户：Mesh 组网更灵活',
     admin_id, digital_series_id, 1, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('相机选购指南：全画幅 vs APS-C vs 手机',
     '在手机摄影日益强大的今天，我们还需要相机吗？\n\n## 全画幅\n- 优势：画质最佳，弱光表现优秀，景深控制好\n- 劣势：体积大，重量大，价格贵\n- 推荐：Sony A7M4、Nikon Z6 III、Canon R6 II\n\n## APS-C\n- 优势：性价比较高，体积适中\n- 劣势：高感性能不如全画幅\n- 推荐：Fuji X-T5、Sony A6700\n\n## 手机\n- 优势：随身携带，计算摄影强大\n- 劣势：物理限制，长焦和专业创作受限\n\n## 结论\n如果你是摄影爱好者或专业人士，相机仍是必需品。如果只是记录生活，旗舰手机足够了。',
     admin_id, digital_series_id, 1, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day');


    -- ===== 游戏天地（4篇） =====
    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('《黑神话：悟空》——中国3A游戏的里程碑',
     '游戏科学的《黑神话：悟空》是中国第一款真正意义上的3A游戏，其影响远超游戏本身。\n\n## 技术实力\n基于虚幻引擎5开发，画面表现达到国际一流水准。\n\n## 文化输出\n以西游记为背景，向全球玩家展示了中国神话的魅力。\n\n## 市场表现\n首发销量突破1000万份，创造了国产游戏的历史记录。\n\n## 行业影响\n证明了中国人也能做出世界级的3A游戏，激励了更多团队投入高品质游戏开发。',
     zhangsan_id, gaming_series_id, 1, NOW() - INTERVAL '9 days', NOW() - INTERVAL '9 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('独立游戏开发日志：从零开始制作一款像素风RPG',
     '作为一名业余游戏开发者，我花了6个月时间制作了第一款独立游戏。\n\n## 开发工具\n- 引擎：Godot 4（开源免费）\n- 美术：Aseprite（像素画工具）\n- 音乐：FL Studio\n- 音效：sfxr\n\n## 踩过的坑\n1. 一开始野心太大，功能规划过于复杂\n2. 忽视了游戏测试的重要性\n3. 美术迭代花费了大量时间\n\n## 收获\n- 完整经历了一个游戏项目的全流程\n- 学会合理规划项目范围\n- 结识了一批独立游戏开发者朋友\n\n游戏已上架 Steam，虽然销量不高，但实现了自己的梦想。',
     zhangsan_id, gaming_series_id, 1, NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('从《艾尔登法环》看开放世界设计的艺术',
     'FromSoftware 的《艾尔登法环》重新定义了开放世界游戏的设计标准。\n\n## 探索的乐趣\n与其他开放世界游戏不同，法环没有密密麻麻的问号标记。探索本身就是奖励。\n\n## 难度与成就感\n高难度设置让每一次胜利都充满成就感。\n\n## 叙事方式\n碎片化叙事让玩家自己拼凑故事，增加了探索的驱动力。\n\n## 设计启示\n信任玩家的探索本能，不过度引导，反而能带来更深刻的游戏体验。',
     zhangsan_id, gaming_series_id, 1, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('游戏手柄选购指南：Xbox vs PS5 vs Switch Pro',
     '好的游戏手柄可以大幅提升游戏体验。三大主流平台的官方手柄各有特色。\n\n## Xbox Wireless Controller\n- 优点：人体工学最佳，PC兼容性最好\n- 缺点：功能相对基础\n- 适合人群：PC玩家、FPS爱好者\n\n## DualSense\n- 优点：自适应扳机，触觉反馈，功能创新\n- 缺点：续航较差，PC支持有限\n- 适合人群：PS5玩家、追求新体验的玩家\n\n## Switch Pro Controller\n- 优点：续航最长（约40小时），HD震动\n- 缺点：十字键位置有争议\n- 适合人群：Switch玩家、2D平台游戏爱好者\n\n## 推荐\nPC玩家首选Xbox手柄，追求创新体验选DualSense。',
     lisi_id, gaming_series_id, 1, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day');


    -- ===== 影视娱乐（4篇） =====
    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('2024年必看国产剧推荐：品质在线的十佳剧集',
     '2024年国产剧市场佳作频出，本文精选了十部不容错过的优质剧集。\n\n## 年度最佳：《繁花》\n王家卫导演的首部电视剧，画面精美，叙事细腻。\n\n## 现实主义力作：《漫长的季节》\n悬疑外壳下是对时代变迁的深刻反思。\n\n## 其他推荐\n- 《三体》：科幻改编的标杆\n- 《狂飙》：扫黑题材的突破\n- 《去有风的地方》：治愈系田园剧\n- 《山海情》：脱贫攻坚的真实写照\n\n国产剧正在经历从量到质的转变。',
     lisi_id, entertainment_series_id, 1, NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('《奥本海默》影评：诺兰的巅峰之作',
     '克里斯托弗·诺兰的《奥本海默》是一部关于毁灭与救赎的史诗。\n\n## 叙事结构\n诺兰标志性的非线性叙事，三条时间线交织推进。\n\n## 基里安·墨菲的表演\n他精准地展现了奥本海默从自信到悔恨的心理变化。\n\n## 视觉语言\n黑白与彩色画面的切换，IMAX 摄影的震撼效果。\n\n## 主题思考\n电影不仅讲述了原子弹的诞生，更探讨了科学家在道德与责任之间的挣扎。\n\n## 评分：9.5/10',
     lisi_id, entertainment_series_id, 1, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('从《歌手2024》看音乐综艺的进化',
     '《歌手2024》作为老牌音乐综艺的全新升级，展现了音乐节目的新可能。\n\n## 赛制创新\n引入揭榜挑战和袭榜机制，增加了节目的不确定性和观赏性。\n\n## 阵容亮点\n国内外实力歌手的碰撞，华语音乐与国际音乐的交流。\n\n## 技术升级\n全景声制作、虚拟现实舞台等新技术手段的运用。\n\n## 音乐综艺的未来\n观众对音乐综艺的要求越来越高，真唱、真现场、真实力成为核心竞争力。',
     lisi_id, entertainment_series_id, 1, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

    INSERT INTO blogs (title, content, author_id, series_id, status, created_at, updated_at) VALUES
    ('Netflix 2024年最值得期待的原创剧集',
     'Netflix 在2024年继续加大原创内容的投入，以下是年度最值得关注的剧集。\n\n## 《三体》英文版\n三体三部曲的改编备受期待，制作规模创 Netflix 历史之最。\n\n## 《怪奇物语》第五季\n最终季，一切谜题将揭晓。\n\n## 《王冠》第六季\n讲述戴安娜王妃逝世后的王室故事。\n\n## 亚洲原创\n- 《寄生兽：灰色部队》：韩国改编版\n- 《忍者之家》：日本家庭动作剧\n- 《此时此刻》：华语爱情单元剧',
     lisi_id, entertainment_series_id, 1, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day');

END $$;
