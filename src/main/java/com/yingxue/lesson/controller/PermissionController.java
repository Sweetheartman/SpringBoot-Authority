package com.yingxue.lesson.controller;

import com.yingxue.lesson.aop.annotation.MyLog;
import com.yingxue.lesson.entity.SysPermission;
import com.yingxue.lesson.service.PermissionService;
import com.yingxue.lesson.utils.DataResult;
import com.yingxue.lesson.vo.req.PermissionAddReqVO;
import com.yingxue.lesson.vo.req.PermissionUpdateReqVO;
import com.yingxue.lesson.vo.resp.PermissionRespNodeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @program: permission-actual-project
 * @description: 权限控制前
 * @author: lidekun
 * @create: 2020-08-25 14:53
 **/
@RestController
@RequestMapping("/api")
@Api(tags = "组织管理-菜单权限管理", description = "菜单权限管理相关接口")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @GetMapping("/permissions")
    @ApiOperation(value = "获取所有的菜单权限数据")
    @MyLog(title = "组织管理-菜单权限管理", action = "获取所有的菜单权限数据")
    @RequiresPermissions("sys:permission:list")
    public DataResult<List<SysPermission>> getAllPermission(){
        DataResult result = DataResult.success();
        result.setData(permissionService.selectAll());
        return result;
    }

    @GetMapping("/permission/tree")
    @ApiOperation(value = "菜单权限树接口-只递归查询到菜单")
    @MyLog(title = "组织管理-菜单权限管理", action = "菜单权限树接口-只递归查询到菜单")
    @RequiresPermissions(value = {"sys:permission:update","sys:permission:add"},logical = Logical.OR)
    public DataResult<List<PermissionRespNodeVO>> getAllPermissionTreeExBtn(){
        DataResult result=DataResult.success();
        result.setData(permissionService.selectAllMenuByTree());
        return result;
    }

    @PostMapping("/permission")
    @ApiOperation(value = "新增菜单权限接口")
    @MyLog(title = "组织管理-菜单权限管理", action = "新增菜单权限接口")
    @RequiresPermissions("sys:permission:add")
    public DataResult<SysPermission> addPermission(@RequestBody @Valid PermissionAddReqVO vo){
        DataResult result = DataResult.success();
        result.setData(permissionService.addPermission(vo));
        return result;
    }

    @GetMapping("/permission/tree/all")
    @ApiOperation(value = "菜单权限树接口-递归查询所有")
    @MyLog(title = "组织管理-菜单权限管理", action = "菜单权限树接口-递归查询所有")
    @RequiresPermissions(value = {"sys:role:update","sys:role:add"},logical = Logical.OR)
    public DataResult<List<PermissionRespNodeVO>> getAllPermissionTree(){
        DataResult result=DataResult.success();
        result.setData(permissionService.selectAllTree());
        return result;
    }

    @PutMapping("/permission")
    @ApiOperation(value = "编辑菜单权限接口")
    @MyLog(title = "组织管理-菜单权限管理",action = "编辑菜单权限接口")
    @RequiresPermissions("sys:permission:update")
    public DataResult updatePermission(@RequestBody @Valid PermissionUpdateReqVO vo){
        permissionService.updatePermission(vo);
        DataResult result=DataResult.success();
        return result;
    }

    @DeleteMapping("/permission/{permissionId}")
    @ApiOperation(value = "删除菜单权限接口")
    @ApiImplicitParam(name = "permissionId" ,value = "权限Id", required = true)
    @MyLog(title = "组织管理-菜单权限管理",action = "删除菜单权限接口")
    @RequiresPermissions("sys:permission:delete")
    public DataResult deletedPermission(@PathVariable("permissionId") String permissionId){
        DataResult result=DataResult.success();
        permissionService.deletedPermission(permissionId);
        return result;
    }
}
