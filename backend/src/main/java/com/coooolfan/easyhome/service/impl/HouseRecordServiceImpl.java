package com.coooolfan.easyhome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coooolfan.easyhome.constant.HouseConstant;
import com.coooolfan.easyhome.constant.PublishConstant;
import com.coooolfan.easyhome.mapper.HouseMapper;
import com.coooolfan.easyhome.mapper.HouseRecordMapper;
import com.coooolfan.easyhome.mapper.HouseUserRelationMapper;
import com.coooolfan.easyhome.mapper.HouseVecMapper;
import com.coooolfan.easyhome.pojo.dto.HouseDTO;
import com.coooolfan.easyhome.pojo.entity.*;
import com.coooolfan.easyhome.service.HouseRecordService;
import com.coooolfan.easyhome.utils.EmbeddingUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.noear.solon.ai.embedding.EmbeddingModel;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * @author lima
 * @version 0.0.1
 **/
@Service
@Slf4j
public class HouseRecordServiceImpl
        extends ServiceImpl<HouseRecordMapper, HouseRecord>
        implements HouseRecordService {

    @Resource
    private HouseMapper houseMapper;

    @Resource
    private HouseVecMapper houseVecMapper;

    @Resource
    private EmbeddingModel embed;

    @Resource
    private HouseUserRelationMapper houseUserRelationMapper;

    @Resource
    private HouseRecordMapper houseRecordMapper;

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
    public List<HouseRecord> getApprovedRecords() {
        return this.lambdaQuery()
                .eq(HouseRecord::getStatus, PublishConstant.APPROVED)
                .orderByDesc(HouseRecord::getCreatedAt)
                .list();
    }

    @Override
    public Long getUserIdByRecordId(Long id) {
        HouseRecord record = this.getById(id);
        if (record == null) {
            throw new RuntimeException("记录不存在，ID=" + id);
        }
        return record.getUserId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeWithVecByRecordId(Long recordId) {
        HouseRecord record = this.getById(recordId);
        if (record == null) {
            throw new RuntimeException("记录不存在，ID=" + recordId);
        }

        // 删除房屋记录
        this.removeById(recordId);

        // 删除房屋向量
        LambdaQueryWrapper<HouseVec> vecQueryWrapper = new LambdaQueryWrapper<>();
        vecQueryWrapper.eq(HouseVec::getHouseId, record.getHouseId());
        houseVecMapper.delete(vecQueryWrapper);

        // 删除房屋信息
        houseMapper.deleteById(record.getHouseId());

        // 删除房屋用户关系
        LambdaQueryWrapper<HouseUserRelation> relationQueryWrapper = new LambdaQueryWrapper<>();
        relationQueryWrapper.eq(HouseUserRelation::getHouseId, record.getHouseId());
        houseUserRelationMapper.delete(relationQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean review(Long id, boolean pass, String reason) {
        log.info("审核房屋记录，ID={}, 通过={}", id, pass);
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
                    .userId(record.getUserId())
                    .tag(record.getTag())
                    .build();

            try {
                houseMapper.insert(house);
                var embedding = embed.input(house
                                .toString()).call()
                        .getData().getFirst()
                        .getEmbedding();
                embedding = EmbeddingUtils.cut(embedding);
                houseVecMapper.insertHouseVec(house.getId(), Arrays.toString(embedding));

                HouseUserRelation houseUserRelation = new HouseUserRelation();
                houseUserRelation.setHouseId(house.getId());
                houseUserRelation.setUserId(record.getUserId());
                houseUserRelationMapper.insert(houseUserRelation);
                record.setHouseId(house.getId());
            }catch (Exception e) {
                log.error(HouseConstant.FAIL_ADD);
                throw new RuntimeException(e);
            }
            this.updateById(record);
            return true;

        } else {
            record.setStatus(PublishConstant.REJECTED);
            record.setReason(reason);
            houseRecordMapper.updateById(record);
            return false;
        }
    }

    @Override
    public List<HouseRecord> getReceivedRecords() {
        return this.lambdaQuery()
//                .eq(HouseRecord::getStatus, PublishConstant.RECEIVED)
                .orderByDesc(HouseRecord::getCreatedAt)
                .list();
    }


}
