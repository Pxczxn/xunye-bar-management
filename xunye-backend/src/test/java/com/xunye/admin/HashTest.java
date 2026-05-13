package com.xunye.admin;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class HashTest {
    @Test public void check() {
        var e = new BCryptPasswordEncoder();
        String[] hashes = {
            "$2a$10$/ZjbQ5cj6HmAyAemJQIkrO8/MGpP.5FR0XVjLRlwfnFxBRlzGaRlG",
            "$2a$10$OzVP9a.7TL4sxhXfY9huW.14cKHQmgfM4QiXAmtHPvV980mhoRRFW"
        };
        String[] names = {"manager", "staff"};
        for (int i = 0; i < hashes.length; i++) {
            for (String p : new String[]{"123456","admin123","manager123","staff123","111111"}) {
                if (e.matches(p, hashes[i])) System.out.println(names[i] + " MATCH: " + p);
            }
        }
    }
}
