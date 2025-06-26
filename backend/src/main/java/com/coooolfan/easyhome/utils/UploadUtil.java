package com.coooolfan.easyhome.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * @author lima
 */
@Slf4j
@Component
public class UploadUtil {

    @Value("${aliyun.oss.aliDomain}")
    private String aliDomain;

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyID;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    public String uploadImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return "No file uploaded";
        }

        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String uuid = UUID.randomUUID().toString().replace("-", "");
        String newFileName = uuid + ext;

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyID, accessKeySecret);
        ossClient.putObject(bucketName, newFileName, file.getInputStream());
        ossClient.shutdown();
        log.info("File uploaded successfully: {}", aliDomain + newFileName);
        return aliDomain + "/" + newFileName;
    }
}
