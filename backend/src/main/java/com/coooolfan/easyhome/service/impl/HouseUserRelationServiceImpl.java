package com.coooolfan.easyhome.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coooolfan.easyhome.mapper.HouseUserRelationMapper;
import com.coooolfan.easyhome.pojo.entity.HouseUserRelation;
import com.coooolfan.easyhome.service.HouseUserRelationService;
import org.springframework.stereotype.Service;

/**
 * @author lima
 * @version 0.0.1
 **/
@Service
public class HouseUserRelationServiceImpl
        extends ServiceImpl<HouseUserRelationMapper, HouseUserRelation>
        implements HouseUserRelationService {
}
