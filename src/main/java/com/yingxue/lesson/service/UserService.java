package com.yingxue.lesson.service;

import com.github.pagehelper.PageInfo;
import com.yingxue.lesson.entity.SysUser;
import com.yingxue.lesson.vo.req.*;
import com.yingxue.lesson.vo.resp.LoginRespVO;
import com.yingxue.lesson.vo.resp.PageVO;
import com.yingxue.lesson.vo.resp.UserOwnRoleRespVO;

import java.util.List;

/**
 * @program: company-frame
 * @description: 用户接口
 * @author: lidekun
 * @create: 2020-08-24 11:46
 **/
public interface UserService {

    LoginRespVO login(LoginReqVO vo);
    PageVO<SysUser> pageInfo(UserPageReqVO vo);
    void addUser(UserAddReqVO vo, String cerateUserId);
    UserOwnRoleRespVO getUserOwnRole(String userId);
    void setUserOwnRole(UserOwnRoleReqVO vo);

    String refreshToken(String refreshToken);

    void updateUserInfo(UserUpdateReqVO vo, String operationId);

    void deletedUsers(List<String> list, String operationId);

    List<SysUser> selectUserInfoByDeptIds(List<String> deptIds);

    SysUser detailInfo(String userId);

    //个人用户编辑信息接口
    void userUpdateDetailInfo(UserUpdateDetailInfoReqVO vo,String userId);

    void userUpdatePwd(UserUpdatePwdReqVO vo,String accessToken,String refreshToken);

    void logout(String accessToken,String refreshToken);
}
