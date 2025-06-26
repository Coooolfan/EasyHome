package com.coooolfan.easyhome.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coooolfan.easyhome.constant.HouseConstant;
import com.coooolfan.easyhome.constant.PublishConstant;
import com.coooolfan.easyhome.mapper.HouseMapper;
import com.coooolfan.easyhome.mapper.HouseRecordMapper;
import com.coooolfan.easyhome.mapper.HouseVecMapper;
import com.coooolfan.easyhome.pojo.dto.HouseDTO;
import com.coooolfan.easyhome.pojo.entity.House;
import com.coooolfan.easyhome.pojo.entity.HouseRecord;
import com.coooolfan.easyhome.service.HouseRecordService;
import jakarta.annotation.Resource;
import lombok.val;
import org.noear.solon.ai.embedding.EmbeddingModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * @author lima
 * @version 0.0.1
 **/
@Service
public class HouseRecordServiceImpl
        extends ServiceImpl<HouseRecordMapper, HouseRecord>
        implements HouseRecordService {

    @Resource
    private HouseMapper houseMapper;

    @Resource
    private HouseVecMapper houseVecMapper;

    @Resource
    private EmbeddingModel embed;

    @Override
    public void publish(Long userId, HouseDTO dto) {
        HouseRecord record = new HouseRecord();
        BeanUtils.copyProperties(dto, record);
        record.setUserId(userId);
        record.setStatus(PublishConstant.PENDING);
        this.save(record);
    }

    @Override
    public void markAsReceived(HouseDTO dto) {
        LambdaUpdateWrapper<HouseRecord> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(HouseRecord::getTitle, dto.getTitle())
                .eq(HouseRecord::getAddress, dto.getAddress())
                .set(HouseRecord::getStatus, PublishConstant.RECEIVED);
        this.update(wrapper);
    }

    @Override
    public List<HouseRecord> getByUserId(Long userId) {
        return this.lambdaQuery()
                .eq(HouseRecord::getUserId, userId)
                .orderByDesc(HouseRecord::getCreatedAt)
                .list();
    }

    @Override
    public List<HouseRecord> getPendingRecords() {
        return this.lambdaQuery()
                .eq(HouseRecord::getStatus, PublishConstant.PENDING)
                .orderByDesc(HouseRecord::getCreatedAt)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void review(Long id, boolean pass, String reason) {
        HouseRecord record = this.getById(id);
        if (record == null) {
            throw new RuntimeException("记录不存在，ID=" + id);
        }

        if (pass) {
            record.setStatus(PublishConstant.APPROVED);
            record.setReason(reason);
            House house = House.builder()
                    .title(record.getTitle())
                    .address(record.getAddress())
                    .area(record.getArea())
                    .floor(record.getFloor())
                    .image(record.getImage())
                    .price(record.getPrice())
                    .rooms(record.getRooms())
                    .buildYear(record.getBuildYear())
                    .decoration(record.getDecoration())
                    .orientation(record.getOrientation())
                    .unitPrice(record.getUnitPrice())
                    .build();

            try {
                houseMapper.insert(house);
                val embedding = embed.input(house
                                .toString()).call()
                        .getData().getFirst()
                        .getEmbedding();
                houseVecMapper.insertHouseVec(house.getId(), Arrays.toString(embedding));

            }catch (Exception e) {
                log.error(HouseConstant.FAIL_ADD);
                throw new RuntimeException(e);
            }
        } else {
            record.setStatus(PublishConstant.REJECTED);
            record.setReason(reason);
        }
        this.updateById(record);
    }

    @Override
    public List<HouseRecord> getReceivedRecords() {
        return this.lambdaQuery()
                .eq(HouseRecord::getStatus, PublishConstant.RECEIVED)
                .orderByDesc(HouseRecord::getCreatedAt)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeWithVecById(Long id) {
        this.removeById(id);
    }

}
