package com.coooolfan.easyhome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coooolfan.easyhome.mapper.UserFavoriteMapper;
import com.coooolfan.easyhome.pojo.entity.UserFavorite;
import com.coooolfan.easyhome.service.UserFavoriteService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lima
 * @version 0.0.1
 **/
@Service
public class UserFavoriteServiceImpl
        extends ServiceImpl<UserFavoriteMapper, UserFavorite>
        implements UserFavoriteService {
    @Override
    public boolean addFavorite(Long userId, Long houseId) {
        UserFavorite userFavorite = new UserFavorite();
        userFavorite.setUserId(userId);
        userFavorite.setHouseId(houseId);
        return save(userFavorite);
    }

    @Override
    public boolean removeFavorite(Long userId, Long houseId) {
        return remove(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getHouseId, houseId));
    }

    @Override
    public boolean isFavorite(Long userId, Long houseId) {
        return count(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getHouseId, houseId)) > 0;
    }

    @Override
    public List<Long> getFavoriteHouseIds(Long userId) {
        return list(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId))
                .stream()
                .map(UserFavorite::getHouseId)
                .toList();
    }


}
