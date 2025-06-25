package com.coooolfan.easyhome.config;

import cn.dev33.satoken.stp.StpInterface;
import com.coooolfan.easyhome.pojo.entity.SysUser;
import com.coooolfan.easyhome.service.SysUserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lima
 * @version 0.0.1
 **/
@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private SysUserService sysUserService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return List.of();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> list = new ArrayList<>();
        Long userId = Long.valueOf((String) loginId);
        SysUser user = sysUserService.getById(userId);
        list.add(user.getRole());
        return list;
    }
}
