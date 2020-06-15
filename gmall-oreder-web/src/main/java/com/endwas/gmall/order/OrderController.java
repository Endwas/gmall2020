package com.endwas.gmall.order;

import com.endwas.gmall.annotations.LoginRequired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class OrderController {


    @RequestMapping("toTrade")
    @LoginRequired(loginSuccess = true)
    public String toTrade(HttpServletRequest httpServletRequest){
        String memberId = (String) httpServletRequest.getAttribute("memberId");
        String nickname = (String) httpServletRequest.getAttribute("nickname");
        return "toTrade";
    }
}
