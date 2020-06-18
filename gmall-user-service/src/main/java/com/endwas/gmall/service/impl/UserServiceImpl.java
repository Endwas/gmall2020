package com.endwas.gmall.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.endwas.gmall.bean.UmsMember;
import com.endwas.gmall.bean.UmsMemberReceiveAddress;
import com.endwas.gmall.mapper.UmsMemberMapper;
import com.endwas.gmall.mapper.UmsMemberReceiveAddressMapper;
import com.endwas.gmall.service.UserService;
import com.endwas.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UmsMemberMapper umsMemberMapper;

    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public UmsMember login(UmsMember umsMember) {
        String LoginKey = "user:" + umsMember.getUsername() + umsMember.getPassword() + ":info";
        Jedis jedis = null;
        UmsMember umsMember1 = new UmsMember();
        try {
            jedis = redisUtil.getJedis();
            if (jedis != null) {
                String umsInfo = jedis.get(LoginKey);
                if (StringUtils.isNotBlank(umsInfo)) {
                    umsMember1 = JSON.parseObject(umsInfo, UmsMember.class);
                    return umsMember1;
                }
            }
            umsMember1 = umsMemberMapper.selectOne(umsMember);
            if (umsMember1 != null) {
                jedis.setex("user:" + umsMember.getPassword() + umsMember.getUsername() + ":info", 60 * 60 * 24, JSON.toJSONString(umsMember1));

            }
            return umsMember1;
        } finally {
            jedis.close();
        }

    }

    @Override
    public UmsMember addOauthUser(Map<String, String> userInfo) {
        UmsMember umsMember = new UmsMember();
        umsMember.setSourceUid(userInfo.get("idstr"));
        umsMember.setMemberLevelId("1");
        umsMember.setUsername(userInfo.get("name"));
        umsMember.setNickname(userInfo.get("screen_name"));
        umsMember.setStatus(1);
        umsMember.setCreateTime(new Date());
        umsMember.setGender(userInfo.get("gender").equals("m") ? 1 : 0);
        umsMember.setCity(userInfo.get("location"));
        umsMember.setAccessToken(userInfo.get("access_token"));
        umsMember.setSourceType(2);
        umsMemberMapper.insertSelective(umsMember);
        return umsMember;

    }

    @Override
    public List<UmsMemberReceiveAddress> getUserAddress(String memberId) {
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(memberId);
        List<UmsMemberReceiveAddress> addresses = umsMemberReceiveAddressMapper.select(umsMemberReceiveAddress);
        return addresses;
    }

    @Override
    public UmsMemberReceiveAddress getReceiveAddressById(String receiveAddressId) {
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setId(receiveAddressId);
        UmsMemberReceiveAddress address = umsMemberReceiveAddressMapper.selectOne(umsMemberReceiveAddress);
        return address;

    }


    @Override
    public UmsMember checkOauthUser(Map<String, String> userInfo) {
        UmsMember umsMember = new UmsMember();
        umsMember.setSourceUid(userInfo.get("idstr"));
        UmsMember oauthMember = umsMemberMapper.selectOne(umsMember);
        return oauthMember;
    }
}
