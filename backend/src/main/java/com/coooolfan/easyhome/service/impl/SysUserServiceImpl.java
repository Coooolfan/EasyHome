package com.coooolfan.easyhome.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coooolfan.easyhome.constant.AuthConstant;
import com.coooolfan.easyhome.exception.RegisterException;
import com.coooolfan.easyhome.mapper.SysUserMapper;
import com.coooolfan.easyhome.pojo.dto.LoginDTO;
import com.coooolfan.easyhome.pojo.dto.RegisterDTO;
import com.coooolfan.easyhome.pojo.entity.SysUser;
import com.coooolfan.easyhome.service.SysUserService;
import jakarta.security.auth.message.AuthException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;


/**
 * @author lima
 * @version 0.0.1
 **/
@Service
public class SysUserServiceImpl
        extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {

    @Override
    public Long login(LoginDTO loginDTO) throws AuthException {
        String password = loginDTO.getPassword();
        String username = loginDTO.getUsername();

        // 二次校验
        if (StringUtils.isAnyBlank(username, password)) {
            throw new AuthException(AuthConstant.USERNAME_AND_PASSWORD_CANNOT_BLANK);
        }

        // 查询用户
        SysUser user = lambdaQuery()
                .eq(SysUser::getUsername, username)
                .oneOpt()
                .orElseThrow(() -> new AuthException(AuthConstant.USERNAME_AND_PASSWORD_ERROR));

        // 加密
        if (!SaSecureUtil.md5(password).equals(user.getPassword())) {
            throw new AuthException(AuthConstant.USERNAME_AND_PASSWORD_ERROR);
        }

        return user.getId();
    }

    @Override
    public void register(RegisterDTO registerDTO) {
        if (registerDTO == null){
            throw new RegisterException(AuthConstant.CANNOT_REGISTER_WITHOUT_USERNAME);
        }

        SysUser user = SysUser.builder()
                .username(registerDTO.getUsername())
                .password(SaSecureUtil.md5(registerDTO.getPassword()))
                .email(registerDTO.getEmail())
                .role(AuthConstant.ROLE_USER)
                .isEnable(true)
                .build();

        // 二次校验
        if (StringUtils.isAnyBlank(user.getUsername(), user.getPassword(), user.getEmail())) {
            throw new RegisterException(AuthConstant.SOMETHING_BLANK);
        }

        // 检查用户名是否已存在
        if (lambdaQuery().eq(SysUser::getUsername, user.getUsername()).exists()) {
            throw new RegisterException(AuthConstant.USERNAME_ALREADY_EXISTS);
        }

        // 保存用户
        if (!save(user)) {
            throw new RegisterException(AuthConstant.REGISTER_FAIL);
        }

        // TODO 发送欢迎邮件
    }
}
