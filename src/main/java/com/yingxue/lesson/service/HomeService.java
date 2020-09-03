package com.yingxue.lesson.service;

import com.yingxue.lesson.vo.resp.HomeRespVO;

/**
 * @program: permission-actual-project
 * @description: 首页服务接口
 * @author: lidekun
 * @create: 2020-08-25 10:32
 **/
public interface HomeService {
    HomeRespVO getHome(String userId);
}
