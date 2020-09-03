package com.yingxue.lesson.controller;

import cn.shuibo.annotation.Decrypt;
import com.github.pagehelper.PageInfo;
import com.yingxue.lesson.aop.annotation.MyLog;
import com.yingxue.lesson.contants.Constant;
import com.yingxue.lesson.entity.SysUser;
import com.yingxue.lesson.service.UserService;
import com.yingxue.lesson.utils.DataResult;
import com.yingxue.lesson.utils.JwtTokenUtil;
import com.yingxue.lesson.vo.req.*;
import com.yingxue.lesson.vo.resp.LoginRespVO;
import com.yingxue.lesson.vo.resp.PageVO;
import com.yingxue.lesson.vo.resp.UserOwnRoleRespVO;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * @program: company-frame
 * @description: 用户控制器
 * @author: lidekun
 * @create: 2020-08-24 11:43
 **/
@RestController
@RequestMapping("/api")
@Api(tags = "组织管理-用户管理", description = "用户模块相关接口")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @ApiOperation(value = "用户登录接口")
    @PostMapping("/user/login")
    @Decrypt
    public DataResult<LoginRespVO> login(@RequestBody @Valid LoginReqVO vo){
        DataResult result = DataResult.success();
        result.setData(userService.login(vo));
        return result;
    }

    @PostMapping("/users")
    @ApiOperation(value = "分页用户查询接口")
    @MyLog(title = "组织管理-用户管理", action = "分页用户查询接口")
    @RequiresPermissions("sys:user:list")
    public DataResult<PageVO<SysUser>> pageInfoDataResult(@RequestBody UserPageReqVO vo){
        DataResult result = DataResult.success();
        result.setData(userService.pageInfo(vo));
        return result;
    }

    @PostMapping("/user")
    @ApiOperation(value = "新增用户接口")
    @MyLog(title = "组织管理-用户管理", action = "新增用户接口")
    @RequiresPermissions("sys:user:add")
    public DataResult addUser(@RequestBody @Valid UserAddReqVO vo, HttpServletRequest request){
        String accessToken = request.getHeader(Constant.ACCESS_TOKEN);
        String userId = JwtTokenUtil.getUserId(accessToken);
        DataResult result = DataResult.success();
        userService.addUser(vo, userId);
        return result;
    }

    @GetMapping("/user/roles/{userId}")
    @ApiOperation(value = "查询用户拥有的角色数据接口")
    @MyLog(title = "组织管理-用户管理", action = "查询用户拥有的角色数据接口")
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(name = "userId", value = "用户id", required = true)
            }
    )
    @RequiresPermissions("sys:user:role:update")
    public DataResult<UserOwnRoleRespVO> getUserOwnRole(@PathVariable("userId") String userId){
        DataResult result = DataResult.success();
        result.setData(userService.getUserOwnRole(userId));
        return result;
    }

    @PutMapping("/user/roles")
    @ApiOperation(value = "保存用户拥有的角色信息接口")
    @MyLog(title = "组织管理-用户管理", action = "保存用户拥有的角色信息接口")
    @RequiresPermissions("sys:user:role:update")
    public DataResult saveUserOwnRole(@RequestBody @Valid UserOwnRoleReqVO vo){
        DataResult result = DataResult.success();
        userService.setUserOwnRole(vo);
        return result;
    }

    @GetMapping("/user/token")
    @ApiOperation(value = "刷新accessToken接口")
    @MyLog(title = "组织管理-用户管理", action = "刷新accessToken接口")
    public DataResult<String> refreshAccessToken(HttpServletRequest request){
        String refreshToken = request.getHeader(Constant.REFRESH_TOKEN);
        String newAccessToken = userService.refreshToken(refreshToken);
        DataResult result = DataResult.success();
        result.setData(newAccessToken);
        return result;
    }

    @PutMapping("/user")
    @ApiOperation(value = "列表修改用户信息接口")
    @MyLog(title = "组织管理-用户管理", action = "列表修改用户信息接口")
    @Decrypt
    @RequiresPermissions("sys:user:update")
    public DataResult updateUserInfo(@RequestBody @Valid UserUpdateReqVO vo, HttpServletRequest request){
        String accessToken = request.getHeader(Constant.ACCESS_TOKEN);
        String userId = JwtTokenUtil.getUserId(accessToken);
        userService.updateUserInfo(vo, userId);
        DataResult result = DataResult.success();
        return result;
    }

    @DeleteMapping("/user")
    @ApiOperation(value = "批量/删除用户接口")
    @MyLog(title = "组织管理-用户管理", action = "批量/删除用户接口")
    @RequiresPermissions("sys:user:delete")
    public DataResult deletedUsers(@RequestBody @ApiParam(value = "用户id集合") List<String> list, HttpServletRequest request){
        String accessToken=request.getHeader(Constant.ACCESS_TOKEN);
        String operationId=JwtTokenUtil.getUserId(accessToken);
        userService.deletedUsers(list,operationId);
        DataResult result=DataResult.success();
        return result;
    }

    @GetMapping("/user/logout")
    @ApiOperation(value = "用户退出登录")
    @MyLog(title = "组织管理-用户管理", action = "用户退出登录接口")
    public DataResult logout(HttpServletRequest request){
        try {
            String accessToken=request.getHeader(Constant.ACCESS_TOKEN);
            String refreshToken=request.getHeader(Constant.REFRESH_TOKEN);
            userService.logout(accessToken,refreshToken);
        } catch (Exception e) {
            log.error("logout:{}",e);
        }
        return DataResult.success();
    }

    @GetMapping("/user/info")
    @ApiOperation(value = "用户信息详情接口")
    @MyLog(title = "组织管理-用户管理", action = "用户信息详情接口")
    public DataResult<SysUser> detailInfo(HttpServletRequest request){
        String accessToken = request.getHeader(Constant.ACCESS_TOKEN);
        String userId = JwtTokenUtil.getUserId(accessToken);
        DataResult result = DataResult.success();
        result.setData(userService.detailInfo(userId));
        return result;
    }

    @PutMapping("/user/info")
    @ApiOperation(value = "保存个人信息接口")
    @MyLog(title = "组织管理-用户管理", action = "保存个人信息接口")
    public DataResult saveUserInfo(
            @RequestBody UserUpdateDetailInfoReqVO vo,
            HttpServletRequest request){
        String userId = JwtTokenUtil.getUserId(request.getHeader(Constant.ACCESS_TOKEN));
        DataResult result = DataResult.success();
        userService.userUpdateDetailInfo(vo, userId);
        return result;
    }

    @PutMapping("/user/pwd")
    @ApiOperation(value = "修改个人密码接口")
    @MyLog(title = "组织管理-用户管理", action = "修改个人密码接口")
    @Decrypt
    public DataResult updatePwd(@RequestBody @Valid UserUpdatePwdReqVO vo, HttpServletRequest request){

        String accessToken = request.getHeader(Constant.ACCESS_TOKEN);
        String refreshToken = request.getHeader(Constant.REFRESH_TOKEN);
        userService.userUpdatePwd(vo, accessToken, refreshToken);
        return DataResult.success();
    }
}
