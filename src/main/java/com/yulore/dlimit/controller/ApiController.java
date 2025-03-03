package com.yulore.dlimit.controller;

import com.yulore.dlimit.ASRAgent;
import com.yulore.dlimit.ASRService;
import com.yulore.util.ExceptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

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
            final ASRAgent agent = asrService.selectASRAgent();
            resp = ApiResponse.<String>builder().code("0000").data(agent.getName()).build();
            schedulerProvider.getObject().schedule(agent::decConnection,
                    ThreadLocalRandom.current().nextInt(10, 100), TimeUnit.SECONDS);
        } catch (final Exception ex) {
            log.warn("select_asr failed: {}", ExceptionUtil.exception2detail(ex));
            resp = ApiResponse.<String>builder().code("2000").message(ex.toString()).build();
        } finally {
            log.info("selectAsr: complete with resp: {}", resp);
        }
        return resp;
    }

    @Autowired
    private ASRService asrService;

    private final ObjectProvider<ScheduledExecutorService> schedulerProvider;
}
