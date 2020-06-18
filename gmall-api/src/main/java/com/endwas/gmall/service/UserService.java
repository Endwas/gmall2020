package com.endwas.gmall.service;

import com.endwas.gmall.bean.UmsMember;
import com.endwas.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;
import java.util.Map;

public interface UserService {

    UmsMember login(UmsMember umsMember);

    UmsMember checkOauthUser(Map<String, String> userInfo);

    UmsMember addOauthUser(Map<String, String> userInfo);

    List<UmsMemberReceiveAddress> getUserAddress(String memberId);

    UmsMemberReceiveAddress getReceiveAddressById(String receiveAddressId);
}
