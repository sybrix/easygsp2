package sybrix.easygsp2.security;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;

import java.security.Key;
import java.util.Map;

public class JwtUtil {

        public String create(String subject, Map<String,Object> claims){
                Key key = MacProvider.generateKey();

                if (claims.containsKey("jti")){
                }

                SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS512;

                String compactJws = Jwts.builder()
                        .setSubject(subject)
                        .setClaims(claims)
                        .signWith(signatureAlgorithm, key)
                        .compact();

                return compactJws;
        }


}
