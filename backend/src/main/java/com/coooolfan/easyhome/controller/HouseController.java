package com.coooolfan.easyhome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coooolfan.easyhome.pojo.entity.House;
import com.coooolfan.easyhome.pojo.vo.HouseQueryVO;
import com.coooolfan.easyhome.service.HouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author lima
 * @version 0.0.1
 **/

@RestController
@RequestMapping("/api/houses")
@CrossOrigin
@Slf4j
@Tag(name = "房屋接口组")
public class HouseController {

    @Resource
    private HouseService houseService;

    @GetMapping("/page")
    @Operation(summary = "分页查询房屋信息")
    public ResponseEntity<IPage<House>> getHousesPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            HouseQueryVO queryVO) {
        log.info("vo: {}", queryVO);
        Page<House> page = new Page<>(current, size);
        IPage<House> housePage = houseService.getByPage(page, queryVO);
        return ResponseEntity.ok(housePage);
    }
}
