package com.yingxue.lesson.service.impl;

import com.alibaba.fastjson.JSON;
import com.yingxue.lesson.entity.SysDept;
import com.yingxue.lesson.entity.SysUser;
import com.yingxue.lesson.mapper.SysDeptMapper;
import com.yingxue.lesson.mapper.SysUserMapper;
import com.yingxue.lesson.service.HomeService;
import com.yingxue.lesson.service.PermissionService;
import com.yingxue.lesson.vo.resp.HomeRespVO;
import com.yingxue.lesson.vo.resp.PermissionRespNodeVO;
import com.yingxue.lesson.vo.resp.UserInfoRespVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: permission-actual-project
 * @description: 首页服务接口实现类
 * @author: lidekun
 * @create: 2020-08-25 10:33
 **/
@Service
public class HomeServiceImpl implements HomeService {
    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private SysDeptMapper sysDeptMapper;

    @Override
    public HomeRespVO getHome(String userId) {
        HomeRespVO homeRespVO = new HomeRespVO();

        List<PermissionRespNodeVO> list = permissionService.permissionTreeList(userId);
        homeRespVO.setMenus(list);
        SysUser sysUser = sysUserMapper.selectByPrimaryKey(userId);
        SysDept sysDept = sysDeptMapper.selectByPrimaryKey(sysUser.getDeptId());

        UserInfoRespVO userInfoRespVO = new UserInfoRespVO();
        if(sysUser != null){
            BeanUtils.copyProperties(sysUser, userInfoRespVO);
            userInfoRespVO.setDeptName(sysDept.getName());
        }
        homeRespVO.setUserInfoVO(userInfoRespVO);
        return homeRespVO;
    }
}
