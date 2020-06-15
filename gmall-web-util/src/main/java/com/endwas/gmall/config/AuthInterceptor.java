package com.endwas.gmall.config;

import com.alibaba.fastjson.JSON;
import com.endwas.gmall.annotations.LoginRequired;
import com.endwas.gmall.util.CookieUtil;
import com.endwas.gmall.util.HttpclientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

// 拦截器对所有添加LoginRequired的注解方法拦截,不做校验判断，校验全在PassportController完成
public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    HandlerMethod handlerMethod = (HandlerMethod) handler;
    LoginRequired methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequired.class);
    // 如果为空则直接返回不需要拦截
    if (methodAnnotation == null){
        return true;
    }
    String token = "";
    // 从cookie中取出token
    String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
    if (StringUtils.isNotBlank(oldToken)){
        token = oldToken;
    }
    // 从请求参数取出token
    String newToken = request.getParameter("token");
    if (StringUtils.isNotBlank(newToken)){
        token = newToken;
    }
    // token为空或者token校验失败 status都为fail
    String status = "fail";
    Map<String, String> map = new HashMap<>();
    if(StringUtils.isNotBlank(token)){
        String ip = request.getHeader("x-forwarded-for");// 通过nginx转发的客户端ip
        if(StringUtils.isBlank(ip)){
            ip = request.getRemoteAddr();// 从request中获取ip
            if(StringUtils.isBlank(ip)){
                ip = "127.0.0.1";
            }
        }
        String result = HttpclientUtil.doGet("http://passport.gmall.com:8085/verify?token=" + token + "&currentIp=" + ip);
        map = JSON.parseObject(result, Map.class);
        status = (String)map.get("status");
    }


    // verifiy token
    if (methodAnnotation.loginSuccess()){
        // 必须要验证通过即要为true
       if (status.equals("success")){
           request.setAttribute("memberId", map.get("memberId"));
           request.setAttribute("nickname", map.get("nickname"));
           CookieUtil.setCookie(request, response, "oldToken", token, 60*60*2, true);
       } else {
           StringBuffer requestURL = request.getRequestURL();
           response.sendRedirect("http://passport.gmall.com:8085/index?ReturnUrl=" + requestURL);
           return false;
       }
    }else {
        // 不需要验证通过也可以用
        if (status.equals("success")){
            request.setAttribute("memberId", map.get("memberId"));
            request.setAttribute("nickname", map.get("nickname"));
            CookieUtil.setCookie(request, response, "oldToken", token, 60*60*2, true);
        }

    }
    return true;
}
}
