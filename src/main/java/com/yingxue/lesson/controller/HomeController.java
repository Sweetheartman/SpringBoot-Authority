package com.yingxue.lesson.controller;

import com.yingxue.lesson.aop.annotation.MyLog;
import com.yingxue.lesson.contants.Constant;
import com.yingxue.lesson.service.HomeService;
import com.yingxue.lesson.service.UserService;
import com.yingxue.lesson.utils.DataResult;
import com.yingxue.lesson.utils.JwtTokenUtil;
import com.yingxue.lesson.vo.resp.HomeRespVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @program: permission-actual-project
 * @description: 首页控制器
 * @author: lidekun
 * @create: 2020-08-25 10:42
 **/
@RestController
@RequestMapping("/api")
@Api(tags = "首页模块", description = "首页相关模块")
public class HomeController {
    @Autowired
    private HomeService homeService;

    @GetMapping("/home")
    @ApiOperation(value = "获取首页数据接口")
    @MyLog(title = "首页模块", action = "获取首页数据")
    public DataResult<HomeRespVO> getHome(HttpServletRequest request){
        String accessToken = request.getHeader(Constant.ACCESS_TOKEN);
        String userId = JwtTokenUtil.getUserId(accessToken);
        DataResult result = DataResult.success();
        result.setData(homeService.getHome(userId));
        return result;
    }
}
