package com.yulore.dlimit;


import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;

@Slf4j
@Component
public class LimitMain {
    @PostConstruct
    public void start() {
        log.info("CosyVoice-Master: Init: redisson: {}", redisson.getConfig().useSingleServer().getDatabase());
    }

    @PreDestroy
    public void stop() {
        serviceExecutor.shutdownNow();

        log.info("CosyVoice-Master: shutdown");
    }

    @Autowired
    private RedissonClient redisson;

    private ExecutorService serviceExecutor;
}