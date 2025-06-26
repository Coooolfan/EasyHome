package com.coooolfan.easyhome.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.coooolfan.easyhome.pojo.entity.House;
import com.coooolfan.easyhome.response.Result;
import com.coooolfan.easyhome.service.HouseService;
import com.coooolfan.easyhome.service.UserFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author lima
 * @version 0.0.1
 **/
@Slf4j
@RestController
@RequestMapping("/api/favorites")
@Tag(name = "收藏接口")
public class FavoriteController {

    @Resource
    private UserFavoriteService userFavoriteService;

    @Resource
    private HouseService houseService;

    @SaCheckLogin
    @PostMapping("/add")
    @Operation(summary = "添加收藏")
    public Result<Boolean> addFavorite(@RequestParam Long houseId) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("用户 {} 收藏房源 {}", userId, houseId);
        boolean result = userFavoriteService.addFavorite(userId, houseId);
        return Result.ok(result);
    }

    @SaCheckLogin
    @DeleteMapping("/remove")
    @Operation(summary = "取消收藏")
    public Result<Boolean> removeFavorite(@RequestParam Long houseId) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("用户 {} 取消收藏房源 {}", userId, houseId);
        boolean result = userFavoriteService.removeFavorite(userId, houseId);
        return Result.ok(result);
    }

    @SaCheckLogin
    @GetMapping("/check")
    @Operation(summary = "检查是否已收藏")
    public Result<Boolean> checkFavorite(@RequestParam Long houseId) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean isFavorite = userFavoriteService.isFavorite(userId, houseId);
        return Result.ok(isFavorite);
    }

    @SaCheckLogin
    @GetMapping("/list")
    @Operation(summary = "获取收藏列表")
    public Result<List<House>> listFavorites() {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("获取用户 {} 的收藏列表", userId);

        // 获取收藏的房源ID列表
        List<Long> houseIds = userFavoriteService.getFavoriteHouseIds(userId);

        // 如果列表为空，直接返回空列表
        if (houseIds.isEmpty()) {
            return Result.ok(List.of());
        }

        // 获取房源详情
        List<House> houses = houseService.listByIds(houseIds);
        return Result.ok(houses);
    }
}
