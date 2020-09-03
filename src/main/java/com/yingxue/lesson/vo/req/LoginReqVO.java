package com.yingxue.lesson.vo.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author 84382
 * @ClassName: LoginReqVO
 * @Version: 0.0.1
 */
@Data
public class LoginReqVO {

    @ApiModelProperty(value = "用户名")
    @NotBlank(message = "用户名不能为空")
    private String username;
    @ApiModelProperty(value = "密码")
    @NotBlank(message = "密码不能为空")
    private String password;
    @ApiModelProperty(value = "登录类型(1:web,2:app)")
    @NotBlank(message = "登录类型不能为空")
    private String type;
}
