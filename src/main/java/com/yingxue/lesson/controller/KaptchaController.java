package com.yingxue.lesson.controller;

import com.google.code.kaptcha.Producer;
import com.yingxue.lesson.contants.Constant;
import com.yingxue.lesson.service.RedisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @program: permission-actual-project
 * @description: 验证码的控制器
 * @author: lidekun
 * @create: 2020-09-01 15:12
 **/
@Controller
@RequestMapping("/api")
@Api(tags = "验证码模块", description = "验证码相关接口")
public class KaptchaController {
    @Autowired
    private Producer captchaProducer = null;
    @Autowired
    private RedisService redisService;

    @Value("${captcha.time}")
    private Integer captchaTime;

    @GetMapping("/captcha")
    @ApiOperation(value = "登录获取验证码接口")
    public void getKaptchaImage(HttpServletResponse response) throws IOException {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);

        //生成验证码
        String capText = captchaProducer.createText();
        String capId = UUID.randomUUID().toString();
        redisService.set(Constant.CAPTCHA_KEY+capId, capText, captchaTime, TimeUnit.SECONDS);
        response.setHeader(Constant.CAPTCHA_ID, capId);

        //向客户端写出
        BufferedImage bi = captchaProducer.createImage(capText);
        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(bi, "jpg", out);
        try {
            out.flush();
        } finally {
            out.close();
        }
    }
}
