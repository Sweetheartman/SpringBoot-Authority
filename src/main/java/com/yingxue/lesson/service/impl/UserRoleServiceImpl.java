package com.yingxue.lesson.service.impl;

import com.yingxue.lesson.entity.SysUserRole;
import com.yingxue.lesson.exception.BusinessException;
import com.yingxue.lesson.exception.code.BaseResponseCode;
import com.yingxue.lesson.mapper.SysUserRoleMapper;
import com.yingxue.lesson.service.UserRoleService;
import com.yingxue.lesson.vo.req.UserOwnRoleReqVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @program: permission-actual-project
 * @description: 用户角色服务层接口实现类
 * @author: lidekun
 * @create: 2020-08-27 11:31
 **/
@Service
public class UserRoleServiceImpl implements UserRoleService {
    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Override
    public List<String> getRoleIdsByUserId(String userId) {
        return sysUserRoleMapper.getRoleIdsByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUserRoleInfo(UserOwnRoleReqVO vo) {
        // 删除关联的数据
        sysUserRoleMapper.removeRoleByUserId(vo.getUserId());
        if(vo.getRoleIds() == null || vo.getRoleIds().isEmpty()){
            return;
        }
        List<SysUserRole> list = new ArrayList<>();
        for (String roleId:
             vo.getRoleIds()) {
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setId(UUID.randomUUID().toString());
            sysUserRole.setCreateTime(new Date());
            sysUserRole.setUserId(vo.getUserId());
            sysUserRole.setRoleId(roleId);
            list.add(sysUserRole);
        }
        int i = sysUserRoleMapper.batchInsertUserRole(list);
        if(i == 0){
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
    }

    @Override
    public List<String> getUserIdsByRoleIds(List<String> roleIds) {
        return sysUserRoleMapper.getUserIdsByRoleIds(roleIds);
    }

    @Override
    public List<String> getUserIdsBtRoleId(String roleId) {
        return sysUserRoleMapper.getUserIdsByRoleId(roleId);
    }

    @Override
    public int removeUserRoleId(String roleId) {
        return sysUserRoleMapper.removeUserRoleId(roleId);
    }
}
