package com.endwas.gmall.service;

import com.endwas.gmall.bean.UmsMember;

import java.util.Map;

public interface UserService {

    UmsMember login(UmsMember umsMember);

    UmsMember checkOauthUser(Map<String, String> userInfo);

    UmsMember addOauthUser(Map<String, String> userInfo);
}
