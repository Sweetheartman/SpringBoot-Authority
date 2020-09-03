package com.yingxue.lesson.controller;
import com.yingxue.lesson.aop.annotation.MyLog;
import com.yingxue.lesson.entity.SysLog;
import com.yingxue.lesson.service.LogService;
import com.yingxue.lesson.utils.DataResult;
import com.yingxue.lesson.vo.req.SysLogPageReqVO;
import com.yingxue.lesson.vo.resp.PageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * @program: permission-actual-project
 * @description: 日志记录的控制器
 * @author: lidekun
 * @create: 2020-08-28 19:57
 **/
@RestController
@RequestMapping("/api")
@Api(tags = "系统管理-日志管理",description = "日志管理相关接口")
public class LogController {

    @Autowired
    private LogService logService;

    @PostMapping("/logs")
    @ApiOperation(value = "分页查找操作日志接口")
    @RequiresPermissions("sys:log:list")
    public DataResult<PageVO<SysLog>> pageInfo(@RequestBody SysLogPageReqVO vo){
        PageVO<SysLog> sysLogPageVO = logService.pageInfo(vo);
        DataResult result=DataResult.success();
        result.setData(sysLogPageVO);
        return result;
    }
    @DeleteMapping("/log")
    @ApiOperation(value = "删除日志接口")
    @MyLog(title = "系统管理-日志管理",action = "删除日志接口")
    @ApiImplicitParam(name = "logIds", value = "日志Id集合", allowMultiple = true, dataType = "String", required = true)
    @RequiresPermissions("sys:log:delete")
    public DataResult deletedLog(@RequestBody List<String> logIds){
        logService.deletedLog(logIds);
        DataResult result=DataResult.success();
        return result;
    }
}
