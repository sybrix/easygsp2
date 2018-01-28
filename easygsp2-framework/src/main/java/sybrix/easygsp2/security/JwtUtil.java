package sybrix.easygsp2.security;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;

import java.security.Key;
import java.util.Map;

public class JwtUtil {

        public String create(Map<String,Object> claims){
                Key key = MacProvider.generateKey();
//
//                String compactJws = Jwts.builder()
//                        .setSubject("Joe")
//                        .setClaims()
//                        .signWith(SignatureAlgorithm.HS512, key)
//                        .compact();

                return null;
        }


}
