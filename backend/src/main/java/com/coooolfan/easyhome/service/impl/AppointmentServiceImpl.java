package com.coooolfan.easyhome.service.impl;

import com.coooolfan.easyhome.mapper.AppointmentRecordMapper;
import com.coooolfan.easyhome.mapper.HouseMapper;
import com.coooolfan.easyhome.mapper.HouseUserRelationMapper;
import com.coooolfan.easyhome.mapper.SysUserMapper;
import com.coooolfan.easyhome.pojo.dto.AppointmentDTO;
import com.coooolfan.easyhome.pojo.entity.*;
import com.coooolfan.easyhome.service.AppointmentService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * @author lima
 * @version 0.0.1
 **/
@Service
@Slf4j
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    @Resource
    private final AppointmentRecordMapper appointmentRecordMapper;

    @Resource
    private final HouseMapper houseMapper;

    @Resource
    private final HouseUserRelationMapper houseUserRelationMapper;

    @Resource
    private final SysUserMapper sysUserMapper;

    @Override
    public void handle(AppointmentDTO dto) {
        // 校验房源是否存在
        House house = houseMapper.selectById(dto.getHouseId());
        if (house == null) {
            throw new RuntimeException("房源不存在");
        }
        // 保存预约记录
        AppointmentRecord record = AppointmentRecord.builder()
                .houseId(dto.getHouseId())
                .name(dto.getName())
                .phone(dto.getPhone())
                .date(LocalDate.parse(dto.getDate()))
                .time(dto.getTime())
                .remarks(dto.getRemarks())
                .status("PENDING")
                .userId(dto.getUserId())
                .build();
        log.info("用户 {} 预约房源 {}: {}",dto.getUserId() , dto.getHouseId(), record);
        appointmentRecordMapper.insert(record);

        // TODO 通知房主（假设房主手机号在房源中）
//        smsService.send(house.getPhone(), "您有新预约来自：" + dto.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewResult review(Long id, Boolean pass, String reason) {
        // 查询预约记录
        AppointmentRecord record = appointmentRecordMapper.selectById(id);
        if (record == null) {
            throw new RuntimeException("预约记录不存在");
        }

        if (!"PENDING".equals(record.getStatus())) {
            throw new RuntimeException("该预约记录已处理");
        }
        record.setStatus(pass ? "CONFIRMED" : "CANCELLED");
        appointmentRecordMapper.updateById(record);
        String houseName = houseMapper.selectById(record.getHouseId()).getTitle();

        // 获取房源信息
        Long userId = houseUserRelationMapper.getUserIdByHouseId(record.getHouseId());

        // todo 二选一
//        if (userId != null) {
//            throw new ReviewException("房源不存在或未绑定用户");
//        }
//        String phone = sysUserMapper.selectById(userId).getPhone();
        String phone = null;
        if (userId != null){
            phone = sysUserMapper.selectById(userId).getPhone();
        }

        if (pass) {
            return new ReviewResult(true, record.getUserId(), phone, houseName);
        }
        return new ReviewResult(false, record.getUserId(), null, houseName);
    }


}
