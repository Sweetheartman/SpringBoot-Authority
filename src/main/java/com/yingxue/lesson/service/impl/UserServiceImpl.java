package com.yingxue.lesson.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yingxue.lesson.contants.Constant;
import com.yingxue.lesson.entity.SysDept;
import com.yingxue.lesson.entity.SysRole;
import com.yingxue.lesson.entity.SysUser;
import com.yingxue.lesson.exception.BusinessException;
import com.yingxue.lesson.exception.code.BaseResponseCode;
import com.yingxue.lesson.mapper.SysDeptMapper;
import com.yingxue.lesson.mapper.SysUserMapper;
import com.yingxue.lesson.service.*;
import com.yingxue.lesson.utils.JwtTokenUtil;
import com.yingxue.lesson.utils.PageUtil;
import com.yingxue.lesson.utils.PasswordUtils;
import com.yingxue.lesson.utils.TokenSetting;
import com.yingxue.lesson.vo.req.*;
import com.yingxue.lesson.vo.resp.LoginRespVO;
import com.yingxue.lesson.vo.resp.PageVO;
import com.yingxue.lesson.vo.resp.UserOwnRoleRespVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @program: company-frame
 * @description: 用户接口实现类
 * @author: lidekun
 * @create: 2020-08-24 11:47
 **/
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Value("${user.defaultpassword}")
    private String defaultPassword;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysDeptMapper sysDeptMapper;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private TokenSetting tokenSetting;

    @Autowired
    private RedisService redisService;

    @Override
    public LoginRespVO login(LoginReqVO vo) {
        SysUser sysUser = sysUserMapper.selectByUsername(vo.getUsername());
        if(sysUser==null){
            throw new BusinessException(BaseResponseCode.ACCOUNT_ERROR);
        }
        if(sysUser.getStatus()==2){
            throw new BusinessException(BaseResponseCode.ACCOUNT_LOCK_TIP);
        }
        if(!PasswordUtils.matches(sysUser.getSalt(),vo.getPassword(),sysUser.getPassword())){
            throw new BusinessException(BaseResponseCode.ACCOUNT_PASSWORD_ERROR);
        }
        Map<String,Object> claims=new HashMap<>();
        claims.put(Constant.JWT_USER_NAME,sysUser.getUsername());
        claims.put(Constant.ROLES_INFOS_KEY,getRoleByUserId(sysUser.getId()));
        claims.put(Constant.PERMISSIONS_INFOS_KEY,getPermissionByUserId(sysUser.getId()));
        String accessToken= JwtTokenUtil.getAccessToken(sysUser.getId(),claims);
        log.info("accessToken={}",accessToken);

        Map<String,Object> refreshTokenClaims=new HashMap<>();
        refreshTokenClaims.put(Constant.JWT_USER_NAME,sysUser.getUsername());
        String refreshToken=null;
        if(vo.getType().equals("1")){
            refreshToken=JwtTokenUtil.getRefreshToken(sysUser.getId(),refreshTokenClaims);
        }else {
            refreshToken=JwtTokenUtil.getRefreshAppToken(sysUser.getId(),refreshTokenClaims);
        }

        log.info("refreshToken={}",refreshToken);
        LoginRespVO loginRespVO=new LoginRespVO();
        if(vo.getPassword().equals(defaultPassword)){
            loginRespVO.setDefaultPassword(1);
        }else{
            loginRespVO.setDefaultPassword(0);
        }
        loginRespVO.setUserId(sysUser.getId());
        loginRespVO.setRefreshToken(refreshToken);
        loginRespVO.setAccessToken(accessToken);
        return loginRespVO;
    }

    /**
     * 用过用户id查询拥有的角色信息
     * @param userId
     * @return       java.util.List<java.lang.String>
     */
    private List<String> getRoleByUserId(String userId){
        return roleService.getNamesByUserId(userId);
    }

    private List<String> getPermissionByUserId(String userId){

        return permissionService.getPermissionByUserId(userId);
    }

    @Override
    public PageVO<SysUser> pageInfo(UserPageReqVO vo) {
        PageHelper.startPage(vo.getPageNum(), vo.getPageSize());
        List<SysUser> sysUsers = sysUserMapper.selectAll(vo);
        for (SysUser sysUser:
             sysUsers) {
            SysDept sysDept = sysDeptMapper.selectByPrimaryKey(sysUser.getDeptId());
            if(sysDept != null){
                sysUser.setDeptName(sysDept.getName());
            }
        }
        return PageUtil.getPageVo(sysUsers);
    }

    @Override
    public void addUser(UserAddReqVO vo, String createUserId) {
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(vo, sysUser);
        sysUser.setId(UUID.randomUUID().toString());
        sysUser.setCreateTime(new Date());
        String salt = PasswordUtils.getSalt();
        String ecdPwd = PasswordUtils.encode(defaultPassword, salt);

        sysUser.setCreateId(createUserId);
        sysUser.setSalt(salt);
        sysUser.setPassword(ecdPwd);

        int i = sysUserMapper.insertSelective(sysUser);
        if(i != 1){
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
    }

    @Override
    public UserOwnRoleRespVO getUserOwnRole(String userId) {
        UserOwnRoleRespVO userOwnRoleRespVO = new UserOwnRoleRespVO();
        userOwnRoleRespVO.setAllRole(roleService.selectAll());
        userOwnRoleRespVO.setOwnRoles(userRoleService.getRoleIdsByUserId(userId));
        return userOwnRoleRespVO;
    }

    @Override
    public void setUserOwnRole(UserOwnRoleReqVO vo) {
        userRoleService.addUserRoleInfo(vo);
        /**
         * 标记用户 要主动去刷新
         */
        redisService.set(
                Constant.JWT_REFRESH_KEY+vo.getUserId(),
                vo.getUserId(),
                tokenSetting.getAccessTokenExpireTime().toMillis(),
                TimeUnit.MILLISECONDS
                );
        /**
         * 清除用户授权数据缓存
         */
        redisService.delete(Constant.IDENTIFY_CACHE_KEY+vo.getUserId());
    }

    @Override
    public String refreshToken(String refreshToken) {
        // 校验refreshToken是否有效
        // 校验refreshToken是否被加入黑名单
        if(!JwtTokenUtil.validateToken(refreshToken) || redisService.hasKey(Constant.JWT_REFRESH_TOKEN_BLACKLIST+refreshToken)){
            throw new BusinessException(BaseResponseCode.TOKEN_ERROR);
        }
        // 重新生成accessToken，重新设置负载信息(用户名，角色，权限)
        String userId = JwtTokenUtil.getUserId(refreshToken);
        String username = JwtTokenUtil.getUserName(refreshToken);
        Map<String, Object> map = new HashMap<>();
        map.put(Constant.JWT_USER_NAME, username);
        map.put(Constant.ROLES_INFOS_KEY, getRoleByUserId(userId));
        map.put(Constant.PERMISSIONS_INFOS_KEY,getPermissionByUserId(userId));

        String newAccessToken = JwtTokenUtil.getAccessToken(userId, map);
        return newAccessToken;
    }

    @Override
    public void updateUserInfo(UserUpdateReqVO vo, String operationId) {
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(vo, sysUser);
        sysUser.setUpdateTime(new Date());
        sysUser.setUpdateId(operationId);
        if(vo.getReset() == 0){
            sysUser.setPassword(null);
        }else {
            String salt=PasswordUtils.getSalt();
            String endPwd=PasswordUtils.encode(defaultPassword,salt);
            sysUser.setSalt(salt);
            sysUser.setPassword(endPwd);
        }

        int i = sysUserMapper.updateByPrimaryKeySelective(sysUser);
        if(i != 1){
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }

        if(vo.getStatus() == 2){
            redisService.set(Constant.ACCOUNT_LOCK_KEY+vo.getId(), vo.getId());
        }else{
            redisService.delete(Constant.ACCOUNT_LOCK_KEY+vo.getId());
        }
    }

    @Override
    public void deletedUsers(List<String> list, String operationId) {
        SysUser sysUser=new SysUser();
        sysUser.setUpdateId(operationId);
        sysUser.setUpdateTime(new Date());
        int i = sysUserMapper.deleteUsers(sysUser, list);
        if(i==0){
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
        for (String userId:
                list) {
            redisService.set(Constant.DELETED_USER_KEY+userId,userId,tokenSetting.getRefreshTokenExpireAppTime().toMillis(),TimeUnit.MILLISECONDS);
            /**
             * 清除用户授权数据缓存
             */
            redisService.delete(Constant.IDENTIFY_CACHE_KEY+userId);
        }

    }

    @Override
    public List<SysUser> selectUserInfoByDeptIds(List<String> deptIds) {
        return sysUserMapper.selectUserInfoByDeptIds(deptIds);
    }

    @Override
    public SysUser detailInfo(String userId) {
        return sysUserMapper.selectByPrimaryKey(userId);
    }

    @Override
    public void userUpdateDetailInfo(UserUpdateDetailInfoReqVO vo, String userId) {
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(vo, sysUser);
        sysUser.setId(userId);
        sysUser.setCreateTime(new Date());
        sysUser.setUpdateId(userId);
        int i = sysUserMapper.updateByPrimaryKeySelective(sysUser);
        if(i != 1){
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
    }

    @Override
    public void userUpdatePwd(UserUpdatePwdReqVO vo, String accessToken, String refreshToken) {
        // 校验旧密码
        String userId = JwtTokenUtil.getUserId(accessToken);
        SysUser sysUser = sysUserMapper.selectByPrimaryKey(userId);
        if(sysUser == null){
            throw new BusinessException(BaseResponseCode.TOKEN_ERROR);
        }
        if(!PasswordUtils.matches(sysUser.getSalt(), vo.getOldPwd(), sysUser.getPassword())){
            throw new BusinessException(BaseResponseCode.OLD_PASSWORD_ERROR);
        }
        // 旧密码匹配成功
        String newPwd = PasswordUtils.encode(vo.getNewPwd(), sysUser.getSalt());
        sysUser.setPassword(newPwd);
        sysUser.setUpdateId(userId);
        sysUser.setUpdateTime(new Date());
        int i = sysUserMapper.updateByPrimaryKeySelective(sysUser);
        if(i != 1){
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
        // 之前签发的token失效
        redisService.set(Constant.JWT_REFRESH_TOKEN_BLACKLIST+accessToken, userId, JwtTokenUtil.getRemainingTime(accessToken), TimeUnit.MILLISECONDS);
        redisService.set(Constant.JWT_REFRESH_IDENTIFICATION+refreshToken,userId,JwtTokenUtil.getRemainingTime(refreshToken),TimeUnit.MILLISECONDS);
        /**
         * 清除用户授权数据缓存
         */
        redisService.delete(Constant.IDENTIFY_CACHE_KEY+userId);
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        if(StringUtils.isEmpty(accessToken)||StringUtils.isEmpty(refreshToken)){
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        }
        Subject subject = SecurityUtils.getSubject();
        if(subject!=null){
            subject.logout();
        }
        String userId=JwtTokenUtil.getUserId(accessToken);
        /**
         * 把accessToken 加入黑名单
         */
        redisService.set(Constant.JWT_ACCESS_TOKEN_BLACKLIST+accessToken,userId,JwtTokenUtil.getRemainingTime(accessToken),TimeUnit.MILLISECONDS);

        /**
         * 把refreshToken 加入黑名单
         */
        redisService.set(Constant.JWT_REFRESH_IDENTIFICATION+refreshToken,userId,JwtTokenUtil.getRemainingTime(refreshToken),TimeUnit.MILLISECONDS);
    }
}
