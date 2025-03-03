package com.yulore.dlimit.controller;

import com.yulore.dlimit.ASRAgent;
import com.yulore.dlimit.ASRService;
import com.yulore.util.ExceptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/dlimit")
public class ApiController {
    @RequestMapping(value = "/select_asr", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse<String> selectAsr() {
        ApiResponse<String> resp = null;
        try {
            final long beforeSelect = System.currentTimeMillis();
            final ASRAgent agent = asrService.selectASRAgent();
            totalSelectAsrCost.addAndGet(System.currentTimeMillis() - beforeSelect);
            totalSelectAsrCount.incrementAndGet();
            log.info("select asr({}): {}/{}", agent.getName(), agent.get_connectingOrConnectedCount().get(), agent.getLimit());
            resp = ApiResponse.<String>builder().code("0000").data(agent.getName()).build();
            schedulerProvider.getObject().schedule(()->{
                        final long beforeRelease = System.currentTimeMillis();
                        final long current = agent.decConnection();
                        totalReleaseAsrCost.addAndGet(System.currentTimeMillis() - beforeRelease);
                        totalReleaseAsrCount.incrementAndGet();
                        log.info("release asr({}): {}/{}", agent.getName(), current, agent.getLimit());
                    },
                    ThreadLocalRandom.current().nextInt(_minDuration, _maxDuration), TimeUnit.SECONDS);
        } catch (final Exception ex) {
            log.warn("select_asr failed: {}", ExceptionUtil.exception2detail(ex));
            resp = ApiResponse.<String>builder().code("2000").message(ex.toString()).build();
        } finally {
            log.info("selectAsr: complete with resp: {}", resp);
        }
        return resp;
    }

    @RequestMapping(value = "/summary", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse<String> summary() {
        final StringBuilder sb = new StringBuilder();
        sb.append("avg select asr: ");
        sb.append( (float)totalSelectAsrCost.get() / Math.max(totalSelectAsrCount.get(), 1));
        sb.append(" ms / select count: ");
        sb.append(totalSelectAsrCount.get());
        sb.append('\n');
        sb.append("avg release asr: ");
        sb.append( (float)totalReleaseAsrCost.get() / Math.max(totalReleaseAsrCount.get(), 1));
        sb.append(" ms / release count: ");
        sb.append(totalReleaseAsrCount.get());
        sb.append('\n');
        return  ApiResponse.<String>builder().code("0000").data(sb.toString()).build();
    }

    @Autowired
    private ASRService asrService;

    @Value("${mock.min.duration:10}")
    private int _minDuration;

    @Value("${mock.max.duration:100}")
    private int _maxDuration;

    private final ObjectProvider<ScheduledExecutorService> schedulerProvider;
    private final AtomicLong totalSelectAsrCost = new AtomicLong(0);
    private final AtomicLong totalSelectAsrCount = new AtomicLong(0);
    private final AtomicLong totalReleaseAsrCost = new AtomicLong(0);
    private final AtomicLong totalReleaseAsrCount = new AtomicLong(0);
}
