package sybrix.easygsp2.security;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.crypto.MacProvider;
import sybrix.easygsp2.util.Base64;
import sybrix.easygsp2.util.PropertiesFile;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class JwtUtil {
        private Key key;
        private String alg;
        private String algType;


        public JwtUtil(String alg){
                this.alg = alg;
        }

        public void  loadKey(String keyVal){
            if (alg.equalsIgnoreCase("HS512")) {
                key = new SecretKeySpec(Base64.decode(keyVal), SignatureAlgorithm.HS512.getJcaName());
            } else if (alg.equalsIgnoreCase("HS256")) {
                key = new SecretKeySpec(Base64.decode(keyVal), SignatureAlgorithm.HS256.getJcaName());
            }
        }

        public String create(Claims claims){
                //header.payload.signature

                String subject = null;

                if (claims.contains(ClaimType.ISSUER.val())){
                        subject = claims.get(ClaimType.ISSUER.val()).getValue();
                }

                SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS512;

                String compactJws = Jwts.builder()
                        .setSubject(subject)
                        .setClaims(claims.toMap())
                        .signWith(signatureAlgorithm, key)
                        .compact();

                return compactJws;
        }

        public boolean verify(String token){
                try {
                        Jwts.parser().setSigningKey(key).parseClaimsJws(token);
                        return true;
                        //OK, we can trust this JWT
                }catch (ExpiredJwtException e){
                        return false;
                } catch (SignatureException e) {
                        //don't trust the JWT!
                        return false;
                }
        }

        public String generateKey(){
                if (alg.equalsIgnoreCase("HS512")) {
                      return Base64.encode(MacProvider.generateKey().getEncoded());
                } else if (alg.equalsIgnoreCase("HS256")) {
                        return Base64.encode(MacProvider.generateKey(SignatureAlgorithm.HS256).getEncoded());
                } else {
                        throw new RuntimeException("Not implmented");
                }
        }

}
