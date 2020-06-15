package com.endwas.gmall.passport;

import com.endwas.gmall.util.JwtUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPassportWebApplicationTests {

    @Test
    public void contextLoads() {
        Map<String, Object> map = new HashMap<>();
        map.put("memberId", 1);
        map.put("nickname", "endwas");
        String encode = JwtUtil.encode("2020endwas-gmall", map, "127.0.0.1");
        System.err.println(encode);
        Map<String, Object> decode = JwtUtil.decode(encode, "2020endwas-gmall", "127.0.0.1");
        System.err.println(decode.toString());

    }

}
