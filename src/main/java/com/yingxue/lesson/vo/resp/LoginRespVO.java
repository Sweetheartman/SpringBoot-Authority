package com.yingxue.lesson.vo.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: LoginRespVO
 * TODO:类文件简单描述
 * @Author: 小霍
 * @UpdateUser: 小霍
 * @Version: 0.0.1
 */
@Data
public class LoginRespVO {
    @ApiModelProperty(value = "业务访问token")
    private String accessToken;
    @ApiModelProperty(value = "业务token刷新凭证")
    private String refreshToken;
    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value="是否是默认密码登录:1--是;2--否")
    private Integer defaultPassword;
}
