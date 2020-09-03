package com.yingxue.lesson.service;

import com.yingxue.lesson.entity.SysPermission;
import com.yingxue.lesson.vo.req.PermissionAddReqVO;
import com.yingxue.lesson.vo.req.PermissionUpdateReqVO;
import com.yingxue.lesson.vo.resp.PermissionRespNodeVO;

import java.util.List;

/**
 * @program: permission-actual-project
 * @description: 权限服务接口
 * @author: lidekun
 * @create: 2020-08-25 14:50
 **/
public interface PermissionService {
    List<SysPermission> selectAll();
    List<PermissionRespNodeVO> selectAllMenuByTree();
    SysPermission addPermission(PermissionAddReqVO vo);
    List<PermissionRespNodeVO> permissionTreeList(String userId);
    List<PermissionRespNodeVO> selectAllTree();
    void updatePermission(PermissionUpdateReqVO vo);
    void deletedPermission(String permissionId);
    List<String> getPermissionByUserId(String userId);
    List<SysPermission> getPermissions(String userId);
}
