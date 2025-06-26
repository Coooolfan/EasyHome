package com.coooolfan.easyhome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coooolfan.easyhome.pojo.dto.HouseDTO;
import com.coooolfan.easyhome.pojo.entity.HouseRecord;

import java.util.List;

/**
 * @author lima
 * @version 0.0.1
 **/
public interface HouseRecordService
        extends IService<HouseRecord> {
    void publish(Long loginId, HouseDTO dto);
    void markAsReceived(HouseDTO dto);
    List<HouseRecord> getByUserId(Long loginId);

    void review(Long id, boolean pass, String reason);
    List<HouseRecord> getReceivedRecords();
    List<HouseRecord> getPendingRecords();
    List<HouseRecord> getApprovedRecords();
}
