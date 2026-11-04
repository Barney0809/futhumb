# futhumb — 点赞系统的三次演进

一个接口（点赞 / 取消点赞），三套实现。每一步都在解决上一步暴露出来的问题，也各自付出了新的代价。

比起"又一个 Spring Boot demo"，这个仓库更想讲清楚的是：**什么时候该上 Redis，什么时候才真的需要消息队列。**

## 三次演进

| 策略 | 实现类 | 核心手段 | 解决了什么 | 代价 |
| --- | --- | --- | --- | --- |
| `db` | `ThumbServiceImpl` | `synchronized` + 编程式事务 | 并发下不写坏数据，点赞数不会算错 | 锁粒度粗、所有读写直接打数据库 |
| `redis` | `ThumbRedisServiceImpl` | Redis + Lua 原子操作，计数留在缓存 | 高频点赞不再压数据库 | 变成最终一致：需要回写任务、补偿任务、对账任务 |
| `mq` | `ThumbMQServiceImpl` | Pulsar 削峰，消费者异步落库 | 突发流量不把后端打穿 | 链路变长：重复投递、死信、消费积压都要处理 |

### 一条时间线

1. **先做对**：`synchronized` + 事务，一条 `thumb` 记录 + `blog.thumb_count` 自增。能跑，但每次点赞都碰库。
2. **再做快**：把"是否点过赞"放进 Redis，用 Lua 保证"判断 + 写入"的原子性；点赞数先累加在缓存里，由定时任务批量回写。
3. **再做稳**：点赞请求只写一条消息到 Pulsar，消费者按批落库，接口响应时间不再受数据库抖动影响。

## 怎么切换

改一行配置即可，三个实现同时保留：

```properties
app.thumb.strategy=db      # 或 redis（默认）/ mq
```

也可以用环境变量 `THUMB_STRATEGY` 覆盖。

实现方式：三个类都标 `@ConditionalOnProperty(name="app.thumb.strategy", ...)`，Pulsar 消费者与定时任务则按策略条件装配——所以选 `db` 时不会去连 Pulsar，选 `mq` 时不会白跑同步任务。

## 最终一致是怎么保证的（redis / mq 模式）

| 组件 | 周期 | 作用 |
| --- | --- | --- |
| `SyncBlogId2DBJob` | 5 秒 | 把博客 ID 集合同步到 Redis，Lua 里先校验博客存在再点赞 |
| `SyncThumb2DBJob` | 10 秒 | 把 Redis 里的临时点赞记录回写数据库 |
| `SyncThumb2DBCompensatoryJob` | 每天 02:00 | 补偿上次没同步成功的临时记录 |
| `ThumbReconcileJob` | 每天 02:00 | 对账：以 `thumb` 表为准修正 `blog.thumb_count` |

设计要点：**写入走缓存、计数异步回写、每天兜底对账**。少任何一环，点赞数迟早会和明细对不上。

## 接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/thumb/do` | 点赞 |
| POST | `/api/thumb/undo` | 取消点赞 |
| GET | `/api/blog/get` | 博客详情（含当前用户是否已点赞） |
| GET | `/api/blog/list` | 博客列表 |
| POST | `/api/user/login` | 登录（示例：按 userId，用 Session 保存登录态） |
| GET | `/api/user/current-user` | 当前登录用户 |

接口文档：`http://localhost:8080/api/swagger-ui.html`（Knife4j：`/api/doc.html`）。

## 快速开始

### 1. 建库

```bash
mysql -uroot -p < sql/schema.sql
```

会创建 `thumb_db` 与 `user` / `blog` / `thumb` 三张表，并插入 1 个用户和 2 篇博客用于试玩。

### 2. 起 Redis

```bash
docker run -d -p 6379:6379 redis:7
```

### 3.（仅 mq 策略）起 Pulsar

```bash
docker run -d -p 6650:6650 -p 8081:8080 apachepulsar/pulsar:latest bin/pulsar standalone
```

### 4. 运行

```bash
mvn spring-boot:run
```

配置全部支持环境变量覆盖：`DB_PASSWORD`、`REDIS_HOST`、`REDIS_PASSWORD`、`PULSAR_SERVICE_URL`、`THUMB_STRATEGY`。

## 配置

| 配置 | 环境变量 | 默认值 |
| --- | --- | --- |
| `app.thumb.strategy` | `THUMB_STRATEGY` | `redis` |
| `spring.datasource.url` | `DB_HOST` / `DB_PORT` / `DB_NAME` | `localhost` / `3306` / `thumb_db` |
| `spring.datasource.username` | `DB_USERNAME` | `root` |
| `spring.datasource.password` | `DB_PASSWORD` | 空（须显式提供） |
| `spring.data.redis.host` | `REDIS_HOST` | `localhost` |
| `spring.data.redis.port` | `REDIS_PORT` | `6379` |
| `spring.data.redis.password` | `REDIS_PASSWORD` | 空 |
| `spring.pulsar.client.service-url` | `PULSAR_SERVICE_URL` | `pulsar://localhost:6650` |

## 测试

```bash
mvn test
```

- `RedisKeyUtilTest`：Redis key 生成规则的契约（Lua、定时任务、Service 三处都依赖它）
- `ThumbApplicationTests`：启动上下文冒烟，需要 MySQL + Redis，默认跳过：

```bash
THUMB_IT=true mvn test -Dtest=ThumbApplicationTests
```

## 已知限制

- 三套实现共用一套表结构，`db` 策略下 `thumb_count` 强一致，`redis`/`mq` 策略下是最终一致——**切换策略时已有数据的口径会变**，演示环境建议先清库。
- Pulsar 消费者按批处理（1000 条 / 10 秒），死信主题固定为 `thumb-dlq-topic`。
- `generator/CodeGenerator.java` 是本机用的代码生成工具，连接串需要自己改。
- `controller/TestController.java` 是联调用的示例接口。
- 只有 4 个测试，覆盖集中在 key 生成规则，Service 层没有单测。

## 许可

[MIT](LICENSE)
