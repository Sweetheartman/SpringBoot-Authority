package com.yingxue.lesson.service.impl;

import com.github.pagehelper.PageHelper;
import com.yingxue.lesson.contants.Constant;
import com.yingxue.lesson.entity.SysRole;
import com.yingxue.lesson.exception.BusinessException;
import com.yingxue.lesson.exception.code.BaseResponseCode;
import com.yingxue.lesson.mapper.SysRoleMapper;
import com.yingxue.lesson.service.*;
import com.yingxue.lesson.utils.PageUtil;
import com.yingxue.lesson.utils.TokenSetting;
import com.yingxue.lesson.vo.req.AddRoleReqVO;
import com.yingxue.lesson.vo.req.RolePageReqVO;
import com.yingxue.lesson.vo.req.RolePermissionOperationReqVO;
import com.yingxue.lesson.vo.req.RoleUpdateReqVO;
import com.yingxue.lesson.vo.resp.PageVO;
import com.yingxue.lesson.vo.resp.PermissionRespNodeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @program: permission-actual-project
 * @description: 角色服务层接口实现类
 * @author: lidekun
 * @create: 2020-08-26 11:06
 **/
@Service
@Slf4j
public class RoleServiceImpl implements RoleService {
    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private RolePermissionService rolePermissionService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private TokenSetting tokenSetting;

    @Override
    public PageVO<SysRole> pageInfo(RolePageReqVO vo) {
        PageHelper.startPage(vo.getPageNum(), vo.getPageSize());
        List<SysRole> sysRoles = sysRoleMapper.selectAll(vo);
        return PageUtil.getPageVo(sysRoles);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysRole addRole(AddRoleReqVO vo) {
        SysRole sysRole = new SysRole();
        BeanUtils.copyProperties(vo, sysRole);
        sysRole.setId(UUID.randomUUID().toString());
        sysRole.setCreateTime(new Date());
        int insert = sysRoleMapper.insertSelective(sysRole);
        if(insert != 1){
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        }
        if(vo.getPermissions() != null && !vo.getPermissions().isEmpty()){
            RolePermissionOperationReqVO rolePermissionOperationReqVO = new RolePermissionOperationReqVO();
            rolePermissionOperationReqVO.setRoleId(sysRole.getId());
            rolePermissionOperationReqVO.setPermissionIds(vo.getPermissions());
            rolePermissionService.addRolePermission(rolePermissionOperationReqVO);
        }

        return sysRole;
    }

    @Override
    public List<SysRole> selectAll() {
        return sysRoleMapper.selectAll(new RolePageReqVO());
    }

    @Override
    public SysRole detailInfo(String id) {
        // 通过角色Id获取角色信息
        SysRole sysRole = sysRoleMapper.selectByPrimaryKey(id);
        if(sysRole == null){
            log.error("传入的id:{}不合法", id);
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        }

        // 获取所有菜单权限树
        List<PermissionRespNodeVO> permissionRespNodeVOS = permissionService.selectAllTree();

        // 获取该角色所拥有的菜单权限
        List<String> permissionIdsByRoleId = rolePermissionService.getPermissionIdsByRoleId(id);

        Set<String> chexkList = new HashSet<>(permissionIdsByRoleId);

        // 遍历菜单权限树的数据
        setChecked(permissionRespNodeVOS, chexkList);
        sysRole.setPermissionRespNode(permissionRespNodeVOS);
        return sysRole;
    }

    /**
     * 获取角色所拥有的菜单权限树
     * @param list 所有的菜单权限
     * @param checkList 角色所拥有的菜单权限
     */
    private void setChecked(List<PermissionRespNodeVO> list, Set<String> checkList){

        for(PermissionRespNodeVO node:list){
            /**
             * 子集选中从它往上到跟目录都被选中，父级选中从它到它所有的叶子节点都会被选中
             * 这样我们就直接遍历最底层及就可以了
             */
            if(checkList.contains(node.getId())&&(node.getChildren()==null||node.getChildren().isEmpty())){
                node.setChecked(true);
            }
            setChecked((List<PermissionRespNodeVO>) node.getChildren(),checkList);

        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(RoleUpdateReqVO vo) {
        //保存角色基本信息
        SysRole sysRole=sysRoleMapper.selectByPrimaryKey(vo.getId());
        if (null==sysRole){
            log.error("传入 的 id:{}不合法",vo.getId());
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        }
        BeanUtils.copyProperties(vo,sysRole);
        sysRole.setUpdateTime(new Date());
        int count=sysRoleMapper.updateByPrimaryKeySelective(sysRole);
        if(count!=1){
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
        //修改该角色和菜单权限关联数据
        RolePermissionOperationReqVO reqVO=new RolePermissionOperationReqVO();
        reqVO.setRoleId(vo.getId());
        reqVO.setPermissionIds(vo.getPermissions());
        rolePermissionService.addRolePermission(reqVO);
        //标记关联用户
        List<String> userIdsBtRoleId = userRoleService.getUserIdsBtRoleId(vo.getId());
        if(!userIdsBtRoleId.isEmpty()){
            for (String userId:
                    userIdsBtRoleId) {
                /**
                 * 标记用户 在用户认证的时候判断这个是否主动刷过
                 */
                redisService.set(Constant.JWT_REFRESH_KEY+userId,userId,tokenSetting.getAccessTokenExpireTime().toMillis(), TimeUnit.MILLISECONDS);

                /**
                 * 清除用户授权数据缓存
                 */
                redisService.delete(Constant.IDENTIFY_CACHE_KEY+userId);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletedRole(String roleId) {
        // 逻辑删除角色信息
        SysRole sysRole = new SysRole();
        sysRole.setId(roleId);
        sysRole.setDeleted(0);
        sysRole.setUpdateTime(new Date());
        int i = sysRoleMapper.updateByPrimaryKeySelective(sysRole);
        if (i != 1) {
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        }

        // 删除角色和菜单权限关联的数据
        rolePermissionService.removeByRoleId(roleId);

        // 查询和该角色关联的用户
        List<String> userIdsBtRoleId = userRoleService.getUserIdsBtRoleId(roleId);

        // 删除和该角色关联的用户
        userRoleService.removeUserRoleId(roleId);

        //把跟该角色关联的用户标记起来，需要刷新token
        if(!userIdsBtRoleId.isEmpty()){
            for (String userId:
                    userIdsBtRoleId) {
                /**
                 * 标记用户 在用户认证的时候判断这个是否主动刷过
                 */
                redisService.set(Constant.JWT_REFRESH_KEY+userId,userId,tokenSetting.getAccessTokenExpireTime().toMillis(), TimeUnit.MILLISECONDS);
                /**
                 * 清除用户授权数据缓存
                 */
                redisService.delete(Constant.IDENTIFY_CACHE_KEY+userId);
            }
        }
    }



    /**
     * 根据用户id获取这个用户所有的角色名字
     * @param userId
     * @return
     */
    @Override
    public List<String> getNamesByUserId(String userId) {
        List<String> roleIdsByUserId = userRoleService.getRoleIdsByUserId(userId);
        if(roleIdsByUserId == null || roleIdsByUserId.isEmpty()){
            return null;
        }
        return sysRoleMapper.selectNamesByIds(roleIdsByUserId);
    }
}
