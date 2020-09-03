package com.yingxue.lesson.service;

import com.yingxue.lesson.entity.SysLog;
import com.yingxue.lesson.vo.req.SysLogPageReqVO;
import com.yingxue.lesson.vo.resp.PageVO;

import java.util.List;

/**
 * @program: permission-actual-project
 * @description: 日志服务层接口
 * @author: lidekun
 * @create: 2020-08-28 19:37
 **/
public interface LogService {
    PageVO<SysLog> pageInfo(SysLogPageReqVO vo);

    void deletedLog(List<String> logIds);
}
