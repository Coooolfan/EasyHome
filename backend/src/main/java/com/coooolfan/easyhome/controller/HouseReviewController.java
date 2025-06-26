package com.coooolfan.easyhome.controller;

import com.coooolfan.easyhome.pojo.entity.HousePublishRecord;
import com.coooolfan.easyhome.response.Result;
import com.coooolfan.easyhome.service.HousePublishRecordService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author lima
 * @version 0.0.1
 **/
@RestController
@RequestMapping("/api/admin/review")
@Tag(name = "房源审核接口")
@AllArgsConstructor
public class HouseReviewController {

    @Resource
    private HousePublishRecordService housePublishRecordService;

    @GetMapping("/pending")
    public Result<List<HousePublishRecord>> getPending() {
        return Result.ok(housePublishRecordService.getPendingRecords());
    }

    @GetMapping("/received")
    public Result<List<HousePublishRecord>> getReceived(){
        return Result.ok(housePublishRecordService.getReceivedRecords());
    }

    @PostMapping("/approve")
    public Result<String> approve(@RequestParam Long id,
                                  @RequestParam Boolean pass,
                                  @RequestParam(required = false) String reason) {
        housePublishRecordService.review(id, pass, reason);
        return Result.ok("处理完成");
    }
}

