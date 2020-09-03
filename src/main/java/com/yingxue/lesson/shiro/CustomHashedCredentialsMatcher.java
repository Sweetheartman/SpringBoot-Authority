package com.yingxue.lesson.shiro;

import com.yingxue.lesson.contants.Constant;
import com.yingxue.lesson.exception.BusinessException;
import com.yingxue.lesson.exception.code.BaseResponseCode;
import com.yingxue.lesson.service.RedisService;
import com.yingxue.lesson.utils.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * @program: permission-actual-project
 * @description: JWT的验证器
 * @author: lidekun
 * @create: 2020-08-22 11:46
 **/
@Slf4j
public class CustomHashedCredentialsMatcher extends HashedCredentialsMatcher {
    /**
     * 授权数据在用户登录和请求刷新token的时候添加，shiro会从用户的token里面拿到权限数据，并将权限数据
     * 缓存到redis，所以在做一些权限相关的操作时需要清空权限的缓存数据
     * 1.更新权限时
     * 2.删除权限时
     * 3.更新角色时
     * 4.删除角色时
     * 5.首页用户修改密码时
     * 6.后台管理员删除用户时
     * 7.后台管理员赋予用户角色时
     */
    @Autowired
    private RedisService redisService;
    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        CustomUsernamePasswordToken customUsernamePasswordToken= (CustomUsernamePasswordToken) token;
        String accessToken= (String) customUsernamePasswordToken.getCredentials();
        String userId= JwtTokenUtil.getUserId(accessToken);
        log.info("doCredentialsMatch....userId={}",userId);
        /**
         * 判断用户是否被删除(删除用户时添加标记)
         * 过期时间为app的refreshToken的过期时间
         */
        if(redisService.hasKey(Constant.DELETED_USER_KEY+userId)){
            throw new BusinessException(BaseResponseCode.ACCOUNT_HAS_DELETED_ERROR);
        }
        /**
         * 判断是否被锁定(编辑用户时添加标记)
         * 无过期时间，解除锁定时删除redis的key
         */
        if(redisService.hasKey(Constant.ACCOUNT_LOCK_KEY+userId)){
            throw new BusinessException(BaseResponseCode.ACCOUNT_LOCK);
        }

        /**
         * 判断用户是否退出登录，退出登录即加入黑名单(用户退出服务层接口添加,用户修改密码时标记)
         * 过期时间分别是accessToken和refreshToken的剩余时间。
         * 用户accessToken过期使用refreshToken刷新换取时也需要检测refreshToken
         * 是否在黑名单中
         */
        if(redisService.hasKey(Constant.JWT_ACCESS_TOKEN_BLACKLIST+accessToken)){
            throw new BusinessException(BaseResponseCode.TOKEN_ERROR);
        }

        //校验token
        if(!JwtTokenUtil.validateToken(accessToken)){
            throw new BusinessException(BaseResponseCode.TOKEN_PAST_DUE);
        }
        /**
         * 判断用户是否被标记了(修改，删除菜单权限信息时或者给用户新增角色，编辑角色,删除角色)
         * 过期时间为accessToken的过期时间
         */
        if(redisService.hasKey(Constant.JWT_REFRESH_KEY+userId)){
            /**
             * 判断用户是否已经刷新过，如果让用户主动刷新accessToken的key剩余时间
             * 大于当前accessToken的剩余时间，则说明用户还未主动刷新accessToken;
             * 反之，让用户主动刷新accessToken的key剩余时间小于当前accessToken的
             * 剩余时间，则说明用户已经主动刷新过accesssToken
             */
            if(redisService.getExpire(Constant.JWT_REFRESH_KEY+userId, TimeUnit.MILLISECONDS)>JwtTokenUtil.getRemainingTime(accessToken)){
                throw new BusinessException(BaseResponseCode.TOKEN_PAST_DUE);
            }
        }
        return true;
    }
}
