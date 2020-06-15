package com.endwas.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.endwas.gmall.annotations.LoginRequired;
import com.endwas.gmall.bean.UmsMember;
import com.endwas.gmall.service.UserService;
import com.endwas.gmall.util.HttpclientUtil;
import com.endwas.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {
    @Reference
    UserService userService;

    @RequestMapping("vlogin")
    public String vlogin(String code, HttpServletRequest httpServletRequest){

        //用weibo返回的code去请求拿到access_token
        HashMap<String, String> map = new HashMap();
        map.put("client_id", "371630812");
        map.put("client_secret", "beca0f68afcb5122db1252005c9bbac7");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://passport.gmall.com:8085/vlogin");
        map.put("code", code);
        String token = HttpclientUtil.doPost("https://api.weibo.com/oauth2/access_token?", map);
        Map<String, String> tokenMap = JSON.parseObject(token, Map.class);

        //用token和用户id拿到对应用户信息
        String access_token = tokenMap.get("access_token");
        String uid = tokenMap.get("uid");
        String weiboInfo = HttpclientUtil.doGet("https://api.weibo.com/2/users/show.json?"+"access_token="+access_token+"&uid="+uid);
        Map<String, String> userInfo = JSON.parseObject(weiboInfo, Map.class);
        userInfo.put("access_token", access_token);


        UmsMember umsMember = userService.checkOauthUser(userInfo);
        if (umsMember == null){
            umsMember = userService.addOauthUser(userInfo);
        }

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("memberId", umsMember.getId());
        userMap.put("nickname", umsMember.getNickname());

        String ip = httpServletRequest.getHeader("x-forwarded-for");// 通过nginx转发的客户端ip
        if (StringUtils.isBlank(ip)) {
            ip = httpServletRequest.getRemoteAddr();// 从request中获取ip
            if (StringUtils.isBlank(ip)) {
                ip = "127.0.0.1";
            }
        }
        token = JwtUtil.encode("2020endwas-gmall", userMap, ip);
        return "redirect:http://search.gmall.com:8083/index?token="+token;
    }


    @RequestMapping("index")
    public String index(String ReturnUrl, ModelMap modelMap){
        modelMap.put("ReturnUrl", ReturnUrl);
        return "index";
    }

    @RequestMapping("verify")
    @ResponseBody
    public String verify(String token, String currentIp){
        Map<String, Object> map = new HashMap<>();

        Map<String, Object> decode = JwtUtil.decode(token, "2020endwas-gmall", currentIp);
        if (decode != null){
            map.put("status","success");
            map.put("memberId",(String)decode.get("memberId"));
            map.put("nickname",(String)decode.get("nickname"));
        } else {
            map.put("status","fail");

        }
        String json = JSON.toJSONString(map);

        return json;


    }



    @RequestMapping("login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest httpServletRequest){
        String token = "fail";
        UmsMember umsMemberLogin  = userService.login(umsMember);
        if (umsMemberLogin != null){
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("memberId", umsMemberLogin.getId());
            userMap.put("nickname", umsMemberLogin.getNickname());

            String ip = httpServletRequest.getHeader("x-forwarded-for");// 通过nginx转发的客户端ip
            if (StringUtils.isBlank(ip)) {
                ip = httpServletRequest.getRemoteAddr();// 从request中获取ip
                if (StringUtils.isBlank(ip)) {
                    ip = "127.0.0.1";
                }
            }
            token = JwtUtil.encode("2020endwas-gmall", userMap, ip);


        }
        return token;
    }

}
