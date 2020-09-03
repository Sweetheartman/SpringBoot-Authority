package com.yingxue.lesson.interceptor;

import com.yingxue.lesson.contants.Constant;
import com.yingxue.lesson.exception.BusinessException;
import com.yingxue.lesson.exception.code.BaseResponseCode;
import com.yingxue.lesson.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * @program: permission-actual-project
 * @description: 验证码拦截器
 * @author: lidekun
 * @create: 2020-09-01 15:41
 **/
public class CaptchaInterceptor implements HandlerInterceptor {
    @Autowired
    private RedisService redisService;

    @Value("${captcha.time}")
    private Integer captchaTime;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 判断用户是否携带验证码
        String captchaId = request.getHeader(Constant.CAPTCHA_ID);
        String captchaText = request.getHeader(Constant.CAPTCHA_TEXT);
        // 验证码为空
        if(StringUtils.isEmpty(captchaText) || StringUtils.isEmpty(captchaId)){
            throw new BusinessException(BaseResponseCode.CAPTCHA_NOT_NULL);
        }

        // 判断验证码的验证次数
        if(redisService.hasKey(Constant.CAPTCHA_COUNT + captchaId)){
            /**
             * 验证码不论验证正确与否只允许验证一次，如果验证码验证次数的key剩余时间大于当前验证码key的剩余时间，
             * 说明当前验证码以及验证过，提示用户重新刷新验证码
             */
            if(redisService.getExpire(Constant.CAPTCHA_COUNT + captchaId, TimeUnit.SECONDS) >  redisService.getExpire(Constant.CAPTCHA_KEY + captchaId, TimeUnit.SECONDS)){
                throw new BusinessException(BaseResponseCode.CAPTCHA_EXPIRED);
            }
        }

        // 验证码过期
        if(!redisService.hasKey(Constant.CAPTCHA_KEY+captchaId)){
            throw new BusinessException(BaseResponseCode.CAPTCHA_EXPIRED);
        }
        // 验证码不正确
        if(!redisService.get(Constant.CAPTCHA_KEY+captchaId).equals(captchaText)){
            /**
             * 添加验证次数超出限制的标识存入redis，过期时间为验证码的过期时间
             */
            redisService.set(Constant.CAPTCHA_COUNT + captchaId, "1", captchaTime, TimeUnit.SECONDS);
            throw new BusinessException(BaseResponseCode.CAPTCHA_INCORRECT);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
