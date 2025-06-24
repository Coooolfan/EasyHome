package com.coooolfan.easyhome.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coooolfan.easyhome.exception.ParamException;
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
            throw new AuthException("用户名和密码不能为空");
        }

        // 查询用户
        SysUser user = lambdaQuery()
                .eq(SysUser::getUsername, username)
                .oneOpt()
                .orElseThrow(() -> new AuthException("用户名或密码错误"));

        // 加密
        if (!SaSecureUtil.md5(password).equals(user.getPassword())) {
            throw new AuthException("用户名或密码错误");
        }

        return user.getId();
    }

    @Override
    public void register(RegisterDTO registerDTO) {
        if (registerDTO == null){
            throw new ParamException("注册信息不能为空");
        }

        SysUser user = SysUser.builder()
                .username(registerDTO.getUsername())
                .password(SaSecureUtil.md5(registerDTO.getPassword()))
                .email(registerDTO.getEmail())
                .role("role_user")
                .isEnable(true)
                .build();

        // 二次校验
        if (StringUtils.isAnyBlank(user.getUsername(), user.getPassword(), user.getEmail())) {
            throw new ParamException("用户名、密码和邮箱不能为空");
        }

        // 检查用户名是否已存在
        if (lambdaQuery().eq(SysUser::getUsername, user.getUsername()).exists()) {
            throw new ParamException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (lambdaQuery().eq(SysUser::getEmail, user.getEmail()).exists()) {
            throw new ParamException("邮箱已存在");
        }

        // 保存用户
        if (!save(user)) {
            throw new ParamException("注册失败，请稍后再试");
        }

        // TODO 发送欢迎邮件
    }
}
