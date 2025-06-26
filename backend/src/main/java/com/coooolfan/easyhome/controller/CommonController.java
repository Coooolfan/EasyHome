package com.coooolfan.easyhome.controller;

import com.coooolfan.easyhome.response.Result;
import com.coooolfan.easyhome.utils.UploadUtil;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author lima
 * @version 0.0.1
 **/
@RestController
@RequestMapping("/api/common")
public class CommonController {

    private final UploadUtil uploadUtil;

    public CommonController(UploadUtil uploadUtil) {
        this.uploadUtil = uploadUtil;
    }

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return Result.error("未选择文件");
        }
        String url = uploadUtil.uploadImage(file);
        return Result.ok(url);
    }

}
