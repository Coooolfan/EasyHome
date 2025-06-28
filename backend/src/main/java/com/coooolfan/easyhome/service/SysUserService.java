package com.coooolfan.easyhome.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coooolfan.easyhome.pojo.dto.LoginDTO;
import com.coooolfan.easyhome.pojo.dto.ProfileDTO;
import com.coooolfan.easyhome.pojo.dto.RegisterDTO;
import com.coooolfan.easyhome.pojo.dto.UserQueryDTO;
import com.coooolfan.easyhome.pojo.entity.House;
import com.coooolfan.easyhome.pojo.entity.SysUser;
import com.coooolfan.easyhome.pojo.vo.UserVO;
import jakarta.security.auth.message.AuthException;

import java.util.List;


/**
 * @author lima
 * @version 0.0.1
 **/
public interface SysUserService
        extends IService<SysUser> {
    Long login(LoginDTO loginDTO) throws AuthException;
    void register(RegisterDTO registerDTO);

    UserVO getUserInfo(Long userId);

    void updateProfileById(Long userId, ProfileDTO profileDTO);

    void updatePasswordById(Long userId, String oldPassword, String newPassword);

    List<SysUser> getUsersByPage(Page<SysUser> page, UserQueryDTO userQueryDTO);
}
