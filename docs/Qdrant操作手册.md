# Qdrant 操作手册

> 适用环境：本地 Docker + langchain4j 1.23.0
> 整理日期：2026-11-05

---

## 0. 环境与版本对照

| 组件 | 版本 | 说明 |
|---|---|---|
| langchain4j BOM | `1.23.0` | `pom.xml` 中声明 |
| langchain4j-qdrant | `1.23.0-beta33` | 由 BOM 自动管理，无需写版本 |
| io.qdrant:client | `1.17.0` | langchain4j-qdrant 内部固定 |
| **Qdrant Server** | **`v1.17.1`** | 与服务端次版本对齐，避免 proto 不兼容 |
| Embedding 模型 | `text-embedding-v3` | 维度 **1024**，distance 用 `Cosine` |

> 升级 langchain4j 后，先查新版本 `langchain4j-qdrant` POM 里的 `io.qdrant:client` 版本，服务端跟着用同一次版本。

### 本文默认约定

```bash
QDRANT=http://localhost:6333      # REST 端口
GRPC_PORT=6334                    # langchain4j / gRPC 端口
COLLECTION=contract_docs          # 集合名
DIM=1024                          # 向量维度
```

---

## 1. 核心概念

| 概念 | 类比 | 说明 |
|---|---|---|
| **Collection** | 一张表 | 向量集合容器，**创建时固定维度和距离算法** |
| **Point** | 一行记录 | `id` + `vector` + `payload` |
| **Vector** | 核心数据 | embedding 向量，长度必须等于集合维度 |
| **Payload** | 其他字段 | 原始文本、来源文件名等，可用于过滤 |
| **Snapshot** | 备份文件 | 集合或整个实例的备份 |

对应关系：

```
collection = contract_docs
point      = 文档切分后的一个 chunk
vector     = chunk 的 1024 维 embedding
payload    = { "text": "chunk 原文", "source": "合同中心前端对接文档.md" }
```

---

## 2. 安装与启动

### 2.1 启动容器

```bash
mkdir -p /Users/zhengyanchuang/qdrant/storage

docker run -d \
  --name qdrant \
  --restart unless-stopped \
  -p 6333:6333 \
  -p 6334:6334 \
  -v /Users/zhengyanchuang/qdrant/storage:/qdrant/storage \
  qdrant/qdrant:v1.17.1
```

### 2.2 端口说明

| 端口 | 用途 | 谁在用 |
|---|---|---|
| **6333** | REST API + Web Dashboard | curl / 浏览器 |
| **6334** | gRPC | **langchain4j** |

> 6334 不映射时，Java 代码会连接失败。

### 2.3 验证

```bash
curl -s http://localhost:6333/ | python3 -m json.tool
curl -s http://localhost:6333/healthz
curl -s http://localhost:6333/readyz
```

浏览器打开 <http://localhost:6333/dashboard>。

### 2.4 docker-compose（可选）

`compose.yaml`：

```yaml
services:
  qdrant:
    image: qdrant/qdrant:v1.17.1
    container_name: qdrant
    restart: unless-stopped
    ports:
      - "6333:6333"
      - "6334:6334"
    volumes:
      - /Users/zhengyanchuang/qdrant/storage:/qdrant/storage
```

```bash
docker compose up -d
```

---

## 3. Dashboard 页面操作

地址：<http://localhost:6333/dashboard>

| 区域 | 用途 |
|---|---|
| **Collections** | 列出集合，点击进入详情 |
| **Create collection** | 可视化建集合：名称、`size=1024`、`distance=Cosine` |
| **Info** | 查看向量配置、点数、状态 |
| **Points** | 浏览 / 按 id 查 / 按 payload 过滤 / 删除 |
| **Search / Query** | 输入向量做检索验证 |
| **Visualize** | 3D 投影查看向量分布（非必需） |
| **Snapshots** | 创建、下载备份 |
| **Console** | **内置 REST 控制台**，可直接发请求，适合调试 |

---

## 4. REST API 命令速查

所有示例均假设已设置：

```bash
QDRANT=http://localhost:6333
COLLECTION=contract_docs
```

### 4.1 健康检查 / 版本

```bash
curl -s $QDRANT/ | python3 -m json.tool
curl -s $QDRANT/healthz
curl -s $QDRANT/readyz
```

### 4.2 集合管理

```bash
# 列出集合
curl -s $QDRANT/collections | python3 -m json.tool

# 查看集合详情
curl -s $QDRANT/collections/$COLLECTION | python3 -m json.tool

# 创建集合（size 必须等于 embedding 维度）
curl -X PUT "$QDRANT/collections/$COLLECTION" \
  -H 'Content-Type: application/json' \
  -d '{"vectors": {"size": 1024, "distance": "Cosine"}}'

# 删除集合（危险）
curl -X DELETE "$QDRANT/collections/$COLLECTION"
```

### 4.3 写入数据（Upsert）

```bash
curl -X PUT "$QDRANT/collections/$COLLECTION/points?wait=true" \
  -H 'Content-Type: application/json' \
  -d '{
    "points": [
      {
        "id": 1,
        "vector": [0.012, -0.034, "……共 1024 个"],
        "payload": {
          "source": "合同中心前端对接文档.md",
          "text": "这里是一段 chunk 原文"
        }
      }
    ]
  }'
```

- `?wait=true`：同步写入，调试必加。
- 相同 `id` 重复写入 = 覆盖；不同 `id` = 新增。

### 4.4 向量检索

**新版 Query API（推荐）：**

```bash
curl -X POST "$QDRANT/collections/$COLLECTION/points/query" \
  -H 'Content-Type: application/json' \
  -d '{
    "query": [0.011, -0.021, "……1024 维"],
    "limit": 5,
    "with_payload": true
  }'
```

**带 payload 过滤：**

```bash
curl -X POST "$QDRANT/collections/$COLLECTION/points/query" \
  -H 'Content-Type: application/json' \
  -d '{
    "query": [0.011, "……"],
    "limit": 5,
    "with_payload": true,
    "score_threshold": 0.6,
    "filter": {
      "must": [
        {"key": "source", "match": {"value": "合同中心前端对接文档.md"}}
      ]
    }
  }'
```

**旧版 search 接口（仍可用，属于 legacy）：**

```bash
curl -X POST "$QDRANT/collections/$COLLECTION/points/search" \
  -H 'Content-Type: application/json' \
  -d '{"vector": [0.011, "……"], "limit": 5, "with_payload": true}'
```

### 4.5 过滤条件写法

| 需求 | 写法 |
|---|---|
| 精确匹配 | `{"key": "source", "match": {"value": "a.md"}}` |
| 文本包含 | `{"key": "text", "match": {"text": "合同"}}` |
| 数值范围 | `{"key": "page", "range": {"gte": 1, "lte": 10}}` |
| 指定 id | `{"has_id": [1, 2, 3]}` |
| 组合 | `{"must": [...], "should": [...], "must_not": [...]}` |

### 4.6 浏览 / 计数 / 删除

```bash
# 分页浏览（按 id，不是按相似度）
curl -X POST "$QDRANT/collections/$COLLECTION/points/scroll" \
  -H 'Content-Type: application/json' \
  -d '{"limit": 10, "with_payload": true, "with_vector": false}'

# 统计点数
curl -X POST "$QDRANT/collections/$COLLECTION/points/count" \
  -H 'Content-Type: application/json' \
  -d '{"exact": true}'

# 按 id 删除
curl -X POST "$QDRANT/collections/$COLLECTION/points/delete?wait=true" \
  -H 'Content-Type: application/json' \
  -d '{"points": [1, 2, 3]}'

# 按条件删除
curl -X POST "$QDRANT/collections/$COLLECTION/points/delete?wait=true" \
  -H 'Content-Type: application/json' \
  -d '{"filter": {"must": [{"key": "source", "match": {"value": "合同中心前端对接文档.md"}}]}}'
```

### 4.7 清空集合（保留集合本身）

```bash
curl -X POST "$QDRANT/collections/$COLLECTION/points/delete?wait=true" \
  -H 'Content-Type: application/json' \
  -d '{"filter": {}}'
```

或直接删集合重建：

```bash
curl -X DELETE "$QDRANT/collections/$COLLECTION"
curl -X PUT "$QDRANT/collections/$COLLECTION" \
  -H 'Content-Type: application/json' \
  -d '{"vectors": {"size": 1024, "distance": "Cosine"}}'
```

### 4.8 Payload 索引（数据量大时）

```bash
curl -X PUT "$QDRANT/collections/$COLLECTION/index" \
  -H 'Content-Type: application/json' \
  -d '{"field_name": "source", "field_schema": "keyword"}'
```

查看索引：

```bash
curl -s "$QDRANT/collections/$COLLECTION" | python3 -m json.tool
```

---

## 5. 备份与恢复

### 5.1 集合快照

```bash
# 创建快照
curl -X POST "$QDRANT/collections/$COLLECTION/snapshots"

# 查看快照列表
curl -s "$QDRANT/collections/$COLLECTION/snapshots" | python3 -m json.tool

# 下载快照
curl -o contract_docs.snapshot \
  "$QDRANT/collections/$COLLECTION/snapshots/<snapshot_name>"
```

### 5.2 恢复快照

```bash
curl -X POST "$QDRANT/collections/$COLLECTION/snapshots/upload?priority=snapshot" \
  -F "snapshot=@contract_docs.snapshot"
```

### 5.3 整机快照

```bash
curl -X POST "$QDRANT/snapshots"
curl -s "$QDRANT/snapshots" | python3 -m json.tool
```

---

## 6. Docker 运维

```bash
docker ps | grep qdrant          # 是否在运行
docker logs -f qdrant            # 查看日志（排查连接问题首选）
docker stats qdrant              # CPU / 内存
docker stop qdrant               # 停止
docker start qdrant              # 启动
docker restart qdrant            # 重启
docker rm -f qdrant              # 删除容器（数据在挂载目录，不会丢）
```

数据目录：`/Users/zhengyanchuang/qdrant/storage`

---

## 7. langchain4j 集成

### 7.1 依赖

```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-qdrant</artifactId>
</dependency>
```

版本由 `langchain4j-bom` 管理。

### 7.2 创建 EmbeddingStore

```java
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;

QdrantEmbeddingStore embeddingStore = QdrantEmbeddingStore.builder()
        .host("localhost")
        .port(6334)                      // gRPC 端口，不是 6333
        .collectionName("contract_docs")
        .build();
```

### 7.3 概念映射

| Qdrant | langchain4j |
|---|---|
| Collection | `QdrantEmbeddingStore.builder().collectionName(...)` |
| Point | `EmbeddingStore.addAll(embeddings, segments)` |
| Vector | `EmbeddingModel` 生成 |
| Payload | `TextSegment.metadata()` 自动写入 |
| 检索 | `EmbeddingStoreContentRetriever` / `EmbeddingStore.search(...)` |

> 建集合仍然建议手动或用 REST 完成；langchain4j 不会自动创建集合。

---

## 8. 常见问题排查

| 现象 | 原因 | 处理 |
|---|---|---|
| `Connection refused` / 超时 | 6334 未映射，或容器未启动 | 检查 `docker ps`、`-p 6334:6334` |
| `Collection ... doesn't exist` | 没建集合 | 用 4.2 创建 |
| `Vector dimension error` | 维度不一致 | 集合 `size` 与 embedding 维度都改成 1024 |
| 刚写入查不到 | 没加 `?wait=true` | 写入 URL 加 `?wait=true` |
| 反复运行后结果重复 | 每次 ingest 生成新 id | 先清空集合再 ingest |
| 想改维度 | Qdrant 不支持直接改 | 删集合重建 |
| Dashboard 打不开 | 6333 未映射 | 检查端口映射 |
| 检索结果很差 | 换了 embedding 模型或没重建集合 | 维度必须与模型一致，重建并重灌数据 |

---

## 9. 日常 Cheatsheet

```bash
# 服务是否活着
curl -s http://localhost:6333/healthz

# 有哪些集合
curl -s http://localhost:6333/collections | python3 -m json.tool

# 集合有多少条
curl -s -X POST http://localhost:6333/collections/contract_docs/points/count \
  -H 'Content-Type: application/json' -d '{"exact":true}'

# 清空集合
curl -s -X POST "http://localhost:6333/collections/contract_docs/points/delete?wait=true" \
  -H 'Content-Type: application/json' -d '{"filter":{}}'

# 删集合
curl -X DELETE http://localhost:6333/collections/contract_docs

# 建集合
curl -X PUT http://localhost:6333/collections/contract_docs \
  -H 'Content-Type: application/json' \
  -d '{"vectors":{"size":1024,"distance":"Cosine"}}'

# 看日志
docker logs -f qdrant
```

---

## 10. 注意事项

1. **维度在创建集合时定死**，之后无法修改。
2. **6333 是 REST，6334 是 gRPC**，不要混用。
3. 数据在挂载目录里，删容器不等于删数据。
4. 生产/认真使用时固定镜像版本，不要用 `latest`。
5. 调试写入一律加 `?wait=true`。
6. 反复运行 ingest 前先清空集合，避免重复 chunk。
