package org.yc.learn.services.rag;

import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.community.model.dashscope.QwenModelName;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;
import org.yc.learn.AppConfig;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;

/**
 * @author zhengyanchuang
 * @version 1.0
 * @description TODO
 * @date 2026/11/5 14:20
 */
public class DemoEasyRag {

    private static final String DOCUMENT_PATH =
            "/Volumes/files/workspace/ai-agent/learn-chain4j/src/main/resources/合同中心前端对接文档.md";

    /** Qdrant gRPC 地址（不是 REST 的 6333） */
    private static final String QDRANT_HOST = "localhost";
    private static final int QDRANT_GRPC_PORT = 6334;
    private static final String COLLECTION_NAME = "contract_docs";
    /** text-embedding-v3 的向量维度 */
    private static final int EMBEDDING_DIMENSION = 1024;
    /** 每次启动先清空集合，避免重复 ingest 产生重复数据 */
    private static final boolean RESET_ON_START = true;

    /**
     * This example demonstrates how to implement an "Easy RAG" (Retrieval-Augmented Generation) application.
     * By "easy" we mean that we won't dive into all the details about parsing, splitting, embedding, etc.
     * All the "magic" is hidden inside the "langchain4j-easy-rag" module.
     * <p>
     * If you want to learn how to do RAG without the "magic" of an "Easy RAG", see {@link Naive_RAG_Example}.
     */
    /**
     * 本示例演示如何实现一个 “Easy RAG”（检索增强生成）应用。
     * 所谓 “Easy”，是指我们不会深入解析、切分、向量化等细节，
     * 这些“魔法”全部封装在 langchain4j-easy-rag 模块内部。
     * <p>
     * 如果想了解不使用 Easy RAG、从零实现 RAG 的过程，请参考 {@link Naive_RAG_Example}。
     */
    interface Assistant {
        String chat(String userMessage);
    }

    public static void main(String[] args) throws Exception {
        // 1. 加载要做 RAG 的文档（.md 会被默认的 TextDocumentParser 当纯文本读取）
        Document document = loadDocument(DOCUMENT_PATH);

        // 2. 创建一个能访问这些文档的助手
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(AppConfig.qwenChatModel())
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(createContentRetriever(List.of(document)))
                .build();

        // 3. 开始对话
        startConversationWith(assistant);
    }

    /**
     * 把文档切分、向量化后写入 Qdrant，并返回基于该向量库的检索器。
     */
    private static ContentRetriever createContentRetriever(List<Document> documents) throws Exception {
        // 使用通义千问的向量模型，维度与 Qdrant 集合保持一致
        EmbeddingModel embeddingModel = QwenEmbeddingModel.builder()
                .apiKey(AppConfig.apiKey())
                .modelName(QwenModelName.TEXT_EMBEDDING_V3)
                .dimension(EMBEDDING_DIMENSION)
                .build();

        QdrantEmbeddingStore embeddingStore = createQdrantStore();

        // 重复运行 main 时先清空，避免同一个 chunk 被反复写入
        if (RESET_ON_START) {
            embeddingStore.removeAll();
            System.out.println("已清空集合：" + COLLECTION_NAME);
        }

        // 切分 + 向量化 + 写入 Qdrant
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(500, 50))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(documents);
        System.out.println("文档已写入 Qdrant：" + DOCUMENT_PATH);

        // 基于同一个向量库创建内容检索器
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .build();
    }

    /**
     * 连接 Qdrant；集合不存在时自动创建（维度、距离必须与 embedding 模型匹配）。
     */
    private static QdrantEmbeddingStore createQdrantStore() throws Exception {
        QdrantGrpcClient grpcClient = QdrantGrpcClient
                .newBuilder(QDRANT_HOST, QDRANT_GRPC_PORT, false)
                .build();
        QdrantClient client = new QdrantClient(grpcClient);

        boolean exists = client.collectionExistsAsync(COLLECTION_NAME).get();
        if (exists) {
            System.out.println("集合已存在：" + COLLECTION_NAME);
        } else {
            client.createCollectionAsync(
                            COLLECTION_NAME,
                            VectorParams.newBuilder()
                                    .setSize(EMBEDDING_DIMENSION)
                                    .setDistance(Distance.Cosine) //启动时自动检查，不存在就按 size=1024 / Cosine 创建
                                    .build())
                    .get();
            System.out.printf("集合不存在，已创建：%s（size=%d, distance=Cosine）%n",
                    COLLECTION_NAME, EMBEDDING_DIMENSION);
        }

        return QdrantEmbeddingStore.builder()
                .client(client)
                .collectionName(COLLECTION_NAME)
                .build();
    }

    /**
     * 在控制台与助手对话，输入 exit 退出。
     */
    private static void startConversationWith(Assistant assistant) {
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            while (true) {
                System.out.println("==================================================");
                System.out.print("User: ");
                System.out.flush();

                // 标准输入关闭（EOF）时结束，避免死循环
                if (!scanner.hasNextLine()) {
                    break;
                }
                String userQuery = scanner.nextLine().trim();
                System.out.println("==================================================");

                if (userQuery.isEmpty()) {
                    continue;
                }
                if ("exit".equalsIgnoreCase(userQuery)) {
                    System.out.println("会话结束。");
                    break;
                }

                String answer = assistant.chat(userQuery);
                System.out.println("Assistant: " + answer);
            }
        }
    }
}
