package com.coooolfan.easyhome.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coooolfan.easyhome.pojo.entity.House;
import com.coooolfan.easyhome.pojo.vo.HouseQueryVO;
import com.coooolfan.easyhome.service.HouseService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

/**
 * @author lima
 * @version 0.0.1
 **/

@RestController
@RequestMapping("/api/houses")
@CrossOrigin
@Slf4j
public class HouseController {

    @Resource
    private HouseService houseService;

    @GetMapping("/page")
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
