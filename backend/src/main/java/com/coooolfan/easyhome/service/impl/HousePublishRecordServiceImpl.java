package com.coooolfan.easyhome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coooolfan.easyhome.constant.PublishConstant;
import com.coooolfan.easyhome.mapper.HousePublishRecordMapper;
import com.coooolfan.easyhome.pojo.dto.HouseDTO;
import com.coooolfan.easyhome.pojo.entity.HousePublishRecord;
import com.coooolfan.easyhome.service.HousePublishRecordService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @author lima
 * @version 0.0.1
 **/
@Service
public class HousePublishRecordServiceImpl
        extends ServiceImpl<HousePublishRecordMapper, HousePublishRecord>
        implements HousePublishRecordService {

    @Override
    public void publish(Long userId, HouseDTO dto) {
        HousePublishRecord record = new HousePublishRecord();
        BeanUtils.copyProperties(dto, record);
        record.setUserId(userId);
        record.setStatus(PublishConstant.PENDING);
        this.save(record);
    }

    @Override
    public void markAsReceived(HouseDTO dto) {
        LambdaUpdateWrapper<HousePublishRecord> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(HousePublishRecord::getTitle, dto.getTitle())
                .eq(HousePublishRecord::getAddress, dto.getAddress())
                .set(HousePublishRecord::getStatus, PublishConstant.RECEIVED);
        this.update(wrapper);
    }

    @Override
    public List<HousePublishRecord> getByUserId(Long userId) {
        return this.lambdaQuery()
                .eq(HousePublishRecord::getUserId, userId)
                .orderByDesc(HousePublishRecord::getCreatedAt)
                .list();
    }

    @Override
    public List<HousePublishRecord> getPendingRecords() {
        return this.lambdaQuery()
                .eq(HousePublishRecord::getStatus, PublishConstant.PENDING)
                .orderByDesc(HousePublishRecord::getCreatedAt)
                .list();
    }

    @Override
    public void review(Long id, boolean pass, String reason) {
        HousePublishRecord record = this.getById(id);
        if (record == null) {
            throw new RuntimeException("记录不存在，ID=" + id);
        }

        if (pass) {
            record.setStatus(PublishConstant.APPROVED);
            record.setReason(null);
        } else {
            record.setStatus(PublishConstant.REJECTED);
            record.setReason(reason);
        }
        this.updateById(record);
    }

    @Override
    public List<HousePublishRecord> getReceivedRecords() {
        return this.lambdaQuery()
                .eq(HousePublishRecord::getStatus, PublishConstant.RECEIVED)
                .orderByDesc(HousePublishRecord::getCreatedAt)
                .list();
    }

}
