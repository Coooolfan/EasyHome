package com.coooolfan.easyhome;

import com.coooolfan.easyhome.mapper.CommonKnowledgeVecMapper;
import com.coooolfan.easyhome.mapper.HouseMapper;
import com.coooolfan.easyhome.mapper.HouseVecMapper;
import com.coooolfan.easyhome.pojo.entity.House;
import com.coooolfan.easyhome.utils.EasyHomeUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.noear.solon.ai.embedding.EmbeddingModel;
import org.noear.solon.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

@Slf4j
@SpringBootTest
class EasyhomeApplicationTests {

    @Autowired
    private CommonKnowledgeVecMapper commonKnowledgeVecMapper;

    @Autowired
    private HouseMapper houseMapper;

    @Autowired
    private HouseVecMapper houseVecMapper;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Test
    void testCommonKnowledgeVecConcurrency() {
        // 起虚拟线程，允许传入并发数量
        int concurrency = 10; // 降低并发数量避免API限流

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            // 获取所有CommonKnowledgeVec数据
            val commonKnowledgeVecs = commonKnowledgeVecMapper.selectList(null);
            log.info("总共有 {} 条CommonKnowledgeVec数据需要处理", commonKnowledgeVecs.size());

            // 先清空所有向量数据
            commonKnowledgeVecMapper.clearEmbeddings();

            // 创建CompletableFuture列表来管理并发任务
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            // 按并发数量分批处理
            for (int i = 0; i < commonKnowledgeVecs.size(); i += concurrency) {
                int endIndex = Math.min(i + concurrency, commonKnowledgeVecs.size());

                // 为每一批创建虚拟线程任务
                List<CompletableFuture<Void>> batchFutures = new ArrayList<>();
                for (int j = i; j < endIndex; j++) {
                    final int index = j;
                    val commonKnowledgeVec = commonKnowledgeVecs.get(index);

                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        try {
                            log.info("虚拟线程 {} 正在处理CommonKnowledgeVec ID: {}, 内容: {}",
                                    Thread.currentThread().getName(),
                                    commonKnowledgeVec.getId(),
                                    commonKnowledgeVec.getContent().substring(0, Math.min(50, commonKnowledgeVec.getContent().length())));

                            // 真实调用embeddingModel
                            EmbeddingResponse embeddingResponse = embeddingModel.input(commonKnowledgeVec.getContent()).call();
                            float[] embedding = embeddingResponse.getData().getFirst().getEmbedding();
                            // 将float[]转换为PostgreSQL vector格式的字符串: [1,2,3,...]
                            String vectorStr = Arrays.toString(embedding);

                            // 真实写入数据库
                            commonKnowledgeVecMapper.updateHouseVec(commonKnowledgeVec.getId(), vectorStr);

                            log.info("虚拟线程 {} 完成处理CommonKnowledgeVec ID: {}, 进度: {}/{}",
                                    Thread.currentThread().getName(),
                                    commonKnowledgeVec.getId(),
                                    index + 1,
                                    commonKnowledgeVecs.size());
                        } catch (Exception e) {
                            log.error("处理CommonKnowledgeVec ID: {} 时发生错误", commonKnowledgeVec.getId(), e);
                        }
                    }, executor);

                    batchFutures.add(future);
                }

                // 等待当前批次完成
                CompletableFuture<Void> batchCompletion = CompletableFuture.allOf(
                        batchFutures.toArray(new CompletableFuture[0]));
                futures.add(batchCompletion);

                log.info("启动第 {} 批次，包含 {} 个任务", (i / concurrency) + 1, batchFutures.size());
            }

            // 等待所有批次完成
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0]));

            // 设置超时时间
            allFutures.get(300, TimeUnit.SECONDS); // 增加超时时间用于API调用

            log.info("所有CommonKnowledgeVec数据处理完成！");

        } catch (Exception e) {
            log.error("并发处理过程中发生错误", e);
            throw new RuntimeException(e);
        }
    }

    @Test
    void testHouseVecConcurrency() {
        // 起虚拟线程，允许传入并发数量
        int concurrency = 2; // 降低并发数量避免API限流

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            // 获取所有House数据
            val houses = houseMapper.selectList(null);
            log.info("总共有 {} 条House数据需要处理", houses.size());

            // 先清空所有向量数据
            houseVecMapper.clearAll();

            // 创建CompletableFuture列表来管理并发任务
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            // 按并发数量分批处理
            for (int i = 0; i < houses.size(); i += concurrency) {
                int endIndex = Math.min(i + concurrency, houses.size());

                // 为每一批创建虚拟线程任务
                List<CompletableFuture<Void>> batchFutures = new ArrayList<>();
                for (int j = i; j < endIndex; j++) {
                    final int index = j;
                    House house = houses.get(index);

                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        try {
                            log.info("虚拟线程 {} 正在处理House ID: {}, 标题: {}",
                                    Thread.currentThread().getName(),
                                    house.getId(),
                                    house.getTitle());

                            // 将House对象转换为字符串
                            String str = EasyHomeUtils.toString(house);

                            // 真实调用embeddingModel
                            EmbeddingResponse resp = embeddingModel.input(str).call();
                            float[] embedding = resp.getData().getFirst().getEmbedding();

                            // 将float[]转换为PostgreSQL vector格式的字符串: [1,2,3,...]
                            String vectorStr = Arrays.toString(embedding);

                            // 真实写入数据库
                            houseVecMapper.insertHouseVec(house.getId(), vectorStr);

                            log.info("虚拟线程 {} 完成处理House ID: {}, 进度: {}/{}",
                                    Thread.currentThread().getName(),
                                    house.getId(),
                                    index + 1,
                                    houses.size());
                        } catch (Exception e) {
                            log.error("处理House ID: {} 时发生错误", house.getId(), e);
                        }
                    }, executor);

                    batchFutures.add(future);
                }

                // 等待当前批次完成
                CompletableFuture<Void> batchCompletion = CompletableFuture.allOf(
                        batchFutures.toArray(new CompletableFuture[0]));
                futures.add(batchCompletion);

                log.info("启动第 {} 批次，包含 {} 个任务", (i / concurrency) + 1, batchFutures.size());
            }

            // 等待所有批次完成
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0]));

            // 设置超时时间
            allFutures.get(300, TimeUnit.SECONDS); // 增加超时时间用于API调用

            log.info("所有House数据处理完成！");

        } catch (Exception e) {
            log.error("并发处理过程中发生错误", e);
            throw new RuntimeException(e);
        }
    }

    @Test
    @SneakyThrows
    void testLocalEmbeddingModel() {
        val houseVec = houseVecMapper.selectById(3805).getEmbedding();
        val houseVec1 = houseVecMapper.selectById(3806).getEmbedding();
        int i= 0;
        while (true) {
            if(i==1536){
                System.out.println("ERROR");
                break;
            }
            if(houseVec[i] != houseVec1[i]){
                System.out.println("FIND DIFFERENCE AT INDEX: " + i);
                break;
            }

        }

    }
}
