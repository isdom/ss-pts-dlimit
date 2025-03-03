package com.yulore.dlimit;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;

import java.util.concurrent.atomic.AtomicInteger;

@Data
@ToString
@Slf4j
public class ASRAgent {
    final String name;
    int limit = 0;

    final AtomicInteger _connectingOrConnectedCount = new AtomicInteger(0);
    final AtomicInteger _connectedCount = new AtomicInteger(0);

    final RAtomicLong _rCounter;

    // 新增 Redisson 客户端引用
    private final RedissonClient redisson;

    // 分布式计数器键名模板
    private static final String COUNTER_KEY_TEMPLATE = "asr_agent_counter:%s";

    public ASRAgent(final String name, final RedissonClient redisson) {
        this.name = name;
        this.redisson = redisson;

        // 获取分布式计数器
        final String counterKey = String.format(COUNTER_KEY_TEMPLATE, this.name);
        this._rCounter = redisson.getAtomicLong(counterKey);
    }

    public static ASRAgent parse(RedissonClient redissonClient, final String accountName, final String values) {
        final String[] kvs = values.split(" ");
        final ASRAgent agent = new ASRAgent(accountName, redissonClient);

        for (String kv : kvs) {
            final String[] ss = kv.split("=");
            if (ss.length == 2) {
                if (ss[0].equals("limit")) {
                    agent.setLimit(Integer.parseInt(ss[1]));
                }
            }
        }
        if (agent.getLimit() != 0) {
            return agent;
        } else {
            return null;
        }
    }

    public ASRAgent checkAndSelectIfHasIdle() {
        while (true) {
            // int currentCount = _connectingOrConnectedCount.get();
            long current = _rCounter.get();
            if (current >= limit) {
                // 已经超出限制的并发数
                return null;
            }

            //if (_connectingOrConnectedCount.compareAndSet(currentCount, currentCount + 1)) {
            // 原子递增操作
            if (_rCounter.compareAndSet(current, current + 1)) {
                // 更新本地计数器用于监控
                _connectingOrConnectedCount.set((int) current + 1);
                // 当前的值设置成功，表示 已经成功占用了一个 并发数
                return this;
            }
            // 若未成功占用，表示有别的线程进行了分配，从头开始检查是否还满足可分配的条件
        }
    }

    public long decConnection() {
        // 减少 连接中或已连接的计数
        final long current = _rCounter.decrementAndGet();
        // 更新本地计数器用于监控
        _connectingOrConnectedCount.decrementAndGet();
        return current;
    }

    public void incConnected() {
        // 增加 已连接的计数
        _connectedCount.incrementAndGet();
    }

    public void decConnected() {
        // 减少 已连接的计数
        _connectedCount.decrementAndGet();
    }
}
