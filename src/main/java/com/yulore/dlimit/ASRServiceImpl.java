package com.yulore.dlimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@Service
@RequiredArgsConstructor
class ASRServiceImpl implements ASRService {

    @PostConstruct
    public void start() {

        initASRAgents();
    }

    @PreDestroy
    public void stop() throws InterruptedException {

        log.info("NlsServiceImpl: shutdown");
    }

    private void initASRAgents() {
        _asrAgents.clear();
        if (_all_asr != null) {
            for (Map.Entry<String, String> entry : _all_asr.entrySet()) {
                log.info("asr: {} / {}", entry.getKey(), entry.getValue());
                final String[] values = entry.getValue().split(" ");
                log.info("asr values detail: {}", Arrays.toString(values));
                final ASRAgent agent = ASRAgent.parse(redisson, entry.getKey(), entry.getValue());
                if (null == agent) {
                    log.warn("asr init failed by: {}/{}", entry.getKey(), entry.getValue());
                } else {
                    _asrAgents.add(agent);
                }
            }
        }
        log.info("asr agent init, count:{}", _asrAgents.size());
    }

    @Override
    public ASRAgent selectASRAgent() {
        for (ASRAgent agent : _asrAgents) {
            final ASRAgent selected = agent.checkAndSelectIfHasIdle();
            if (null != selected) {
                log.info("select asr({}): {}/{}", agent.getName(), agent.get_connectingOrConnectedCount().get(), agent.getLimit());
                return selected;
            }
        }
        throw new RuntimeException("all asr agent has full");
    }

    // 新增 Redisson 客户端引用
    private final RedissonClient redisson;

    @Value("#{${aliyun.asr}}")
    private Map<String,String> _all_asr;

    final List<ASRAgent> _asrAgents = new ArrayList<>();

    private final ObjectProvider<ScheduledExecutorService> schedulerProvider;
}
