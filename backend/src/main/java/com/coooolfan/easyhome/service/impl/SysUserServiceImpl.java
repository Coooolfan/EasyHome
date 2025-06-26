package com.coooolfan.easyhome.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coooolfan.easyhome.constant.AuthConstant;
import com.coooolfan.easyhome.exception.RegisterException;
import com.coooolfan.easyhome.mapper.SysUserMapper;
import com.coooolfan.easyhome.pojo.dto.LoginDTO;
import com.coooolfan.easyhome.pojo.dto.ProfileDTO;
import com.coooolfan.easyhome.pojo.dto.RegisterDTO;
import com.coooolfan.easyhome.pojo.entity.SysUser;
import com.coooolfan.easyhome.pojo.vo.UserVO;
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
                .phone(registerDTO.getPhone())
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

    @Override
    public UserVO getUserInfo(Long userId) {
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在，ID=" + userId);
        }

        return UserVO.builder()
                .username(user.getUsername())
                .phone(user.getPhone())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Override
    public void updateProfileById(Long userId, ProfileDTO profileDTO) {
        SysUser sysUser = SysUser.builder()
                .id(userId)
                .email(profileDTO.getEmail())
                .phone(profileDTO.getPhone())
                .username(profileDTO.getUsername())
                .build();
        this.updateById(sysUser);
    }

    @Override
    public void updatePasswordById(Long userId, String oldPassword, String newPassword) {
        SysUser sysUser = this.getById(userId);
        if (sysUser == null) {
            throw new RuntimeException("用户不存在，ID=" + userId);
        }

        if (!SaSecureUtil.md5(oldPassword).equals(sysUser.getPassword())) {
            throw new RuntimeException(AuthConstant.OLD_PASSWORD_ERROR);
        }

        sysUser.setPassword(SaSecureUtil.md5(newPassword));
        if (!this.updateById(sysUser)) {
            throw new RuntimeException("更新密码失败，请稍后再试");
        }
    }
}
