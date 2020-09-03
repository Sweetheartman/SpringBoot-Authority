package com.yingxue.lesson.controller;

import com.yingxue.lesson.exception.BusinessException;
import com.yingxue.lesson.exception.code.BaseResponseCode;
import com.yingxue.lesson.utils.DataResult;
import com.yingxue.lesson.vo.req.TestReqVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @program: company-frame
 * @description: 测试swagger控制器
 * @author: lidekun
 * @create: 2020-08-23 12:44
 **/
@RestController
@RequestMapping("/api/test")
@Api(tags = "测试模块", description = "测试接口")
public class TestController {

    @GetMapping("/swagger")
    @ApiOperation(value = "测试swagger配置")
    public String testSwagger(){
        return "测试成功";
    }

    @GetMapping("/test/data")
    @ApiOperation(value = "统一响应格式测试接口")
    public DataResult<String> test(){
        DataResult result = DataResult.success("统一响应格式测试接口");
        return result;
    }

    @GetMapping("/type")
    public DataResult testBusinessException(@RequestParam String type){
        if(!type.equals("1")){
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        }
        DataResult result = DataResult.success(type);
        return result;
    }

    @PostMapping("/test/valid")
    @ApiOperation(value = "测试校验验证器")
    public DataResult testValid(@RequestBody @Valid TestReqVO vo){
        DataResult result = DataResult.success(vo);
        return result;
    }
}
