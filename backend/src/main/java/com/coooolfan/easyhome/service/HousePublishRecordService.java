package com.coooolfan.easyhome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coooolfan.easyhome.pojo.dto.HouseDTO;
import com.coooolfan.easyhome.pojo.entity.HousePublishRecord;

import java.util.List;

/**
 * @author lima
 * @version 0.0.1
 **/
public interface HousePublishRecordService
        extends IService<HousePublishRecord> {
    void publish(Long loginId, HouseDTO dto);
    void markAsReceived(HouseDTO dto);
    List<HousePublishRecord> getByUserId(Long loginId);
    List<HousePublishRecord> getPendingRecords();
    void review(Long id, boolean pass, String reason);
    List<HousePublishRecord> getReceivedRecords();
}
