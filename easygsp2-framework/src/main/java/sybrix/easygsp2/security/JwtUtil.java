package sybrix.easygsp2.security;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.crypto.MacProvider;
import sybrix.easygsp2.framework.ThreadBag;
import sybrix.easygsp2.util.Base64;
import sybrix.easygsp2.util.PropertiesFile;
import sybrix.easygsp2.util.StringUtil;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

public class JwtUtil {

        public static JwtUtil instance;

        private Key key;
        private String alg;
        private String algType;
        private SignatureAlgorithm signatureAlgorithm;

        public JwtUtil(){

        }

        public JwtUtil(String alg){
                this.alg = alg;
        }

        public void loadKey(String keyVal){
            if (alg.equalsIgnoreCase("HS512")) {
                key = new SecretKeySpec(Base64.decode(keyVal), SignatureAlgorithm.HS512.getJcaName());
                    signatureAlgorithm =SignatureAlgorithm.HS512;
            } else if (alg.equalsIgnoreCase("HS256")) {
                key = new SecretKeySpec(Base64.decode(keyVal), SignatureAlgorithm.HS256.getJcaName());
                    signatureAlgorithm =SignatureAlgorithm.HS256;
            }
        }

        public String create(String username, List<String> scopes){
                //header.payload.signature

                String subject = null;
                Map<String,Object> scopesMap = new HashMap();
                scopesMap.put("scopes", scopes);

                PropertiesFile propertiesFile = (PropertiesFile)ThreadBag.get().getApp().getAttribute("__propertieFile");
                int expirySeconds =  propertiesFile.getInt("jwt.expires_in_seconds", (60 * 60 * 24)); // 24 hours default
                String issuer =  propertiesFile.getString("jwt.issuer");

                LocalDateTime currentTime = LocalDateTime.now();
                LocalDateTime expireTime = currentTime.plusSeconds(expirySeconds);
                Date expiryDate = Date.from(expireTime.toInstant(ZonedDateTime.now().getOffset()));

                String compactJws = Jwts.builder()
                        .setSubject(username)
                        .setClaims(scopesMap)
                        .setIssuer(issuer)
                        .setIssuedAt(Date.from(currentTime.toInstant(ZonedDateTime.now().getOffset())))
                        .setExpiration(expiryDate)
                        .setId(StringUtil.randomCode(8))
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
                        throw new RuntimeException("Not implemented");
                }
        }

}
