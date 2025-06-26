package com.coooolfan.easyhome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coooolfan.easyhome.pojo.entity.UserFavorite;

import java.util.List;

/**
 * @author lima
 * @version 0.0.1
 **/
public interface UserFavoriteService
        extends IService<UserFavorite> {
    boolean addFavorite(Long userId, Long houseId);

    boolean removeFavorite(Long userId, Long houseId);

    boolean isFavorite(Long userId, Long houseId);

    List<Long> getFavoriteHouseIds(Long userId);
}
