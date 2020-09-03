package com.yingxue.lesson.shiro;

import com.yingxue.lesson.contants.Constant;
import com.yingxue.lesson.service.PermissionService;
import com.yingxue.lesson.service.RedisService;
import com.yingxue.lesson.service.RoleService;
import com.yingxue.lesson.utils.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: CustomRealm
 * TODO:类文件简单描述
 * @Author: 小霍
 * @UpdateUser: 小霍
 * @Version: 0.0.1
 */
@Slf4j
public class CustomRealm extends AuthorizingRealm {
    @Autowired
    private RoleService roleService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private RedisService redisService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof CustomUsernamePasswordToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String accessToken= (String) principals.getPrimaryPrincipal();
        Claims claimsFromToken = JwtTokenUtil.getClaimsFromToken(accessToken);
        SimpleAuthorizationInfo info=new SimpleAuthorizationInfo();

        /**
         * 前端页面的shiro标签，会调用这个方法进行权限验证，当权限数据发生更改时，则需要从数据库拿最新的权限数据
         * 而不是从accessToken的负载里面拿数据
         */

        String userId = JwtTokenUtil.getUserId(accessToken);
        log.info("userId={}",userId);
        // 有刷新token的标记，并且用户没有刷新过
        if(redisService.hasKey(Constant.JWT_REFRESH_KEY+userId)&&redisService.getExpire(Constant.JWT_REFRESH_KEY+userId, TimeUnit.MILLISECONDS)>JwtTokenUtil.getRemainingTime(accessToken)){
            List<String> roles=roleService.getNamesByUserId(userId);
            if(roles!=null&&!roles.isEmpty()){
                info.addRoles(roles);
            }
            List<String> permissionByUserId = permissionService.getPermissionByUserId(userId);
            if(permissionByUserId!=null&&!permissionByUserId.isEmpty()){
                info.addStringPermissions(permissionByUserId);
            }

        }else {
            if(claimsFromToken.get(Constant.PERMISSIONS_INFOS_KEY)!=null){
                info.addStringPermissions((Collection<String>) claimsFromToken.get(Constant.PERMISSIONS_INFOS_KEY));
            }
            if(claimsFromToken.get(Constant.ROLES_INFOS_KEY)!=null){
                info.addRoles((Collection<String>) claimsFromToken.get(Constant.ROLES_INFOS_KEY));
            }
        }

        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        CustomUsernamePasswordToken customUsernamePasswordToken= (CustomUsernamePasswordToken) token;
        SimpleAuthenticationInfo info=new SimpleAuthenticationInfo(customUsernamePasswordToken.getPrincipal(),customUsernamePasswordToken.getCredentials(),this.getName());
        return info;
    }
}
