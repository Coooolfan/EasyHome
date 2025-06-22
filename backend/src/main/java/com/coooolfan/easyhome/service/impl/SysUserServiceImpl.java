package com.coooolfan.easyhome.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coooolfan.easyhome.mapper.SysUserMapper;
import com.coooolfan.easyhome.pojo.dto.LoginDTO;
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
}
