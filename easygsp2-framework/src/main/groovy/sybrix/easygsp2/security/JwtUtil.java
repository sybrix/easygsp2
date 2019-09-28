package sybrix.easygsp2.security;


import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.crypto.MacProvider;
import sybrix.easygsp2.framework.ThreadBag;
import sybrix.easygsp2.util.Base64;
import sybrix.easygsp2.util.PropertiesFile;
import sybrix.easygsp2.util.StringUtil;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class JwtUtil {

        private static final Logger logger = java.util.logging.Logger.getLogger(JwtUtil.class.getName());

        public static JwtUtil instance;

        private static Key key;
        private String alg;
        private String algType;
        private SignatureAlgorithm signatureAlgorithm;

        public JwtUtil() {

        }

        public JwtUtil(String alg) {
                this.alg = alg;
        }

        public void loadKey(String keyVal) {
                if (alg.equalsIgnoreCase("HS512")) {
                        key = new SecretKeySpec(Base64.decode(keyVal), SignatureAlgorithm.HS512.getJcaName());
                        signatureAlgorithm = SignatureAlgorithm.HS512;
                } else if (alg.equalsIgnoreCase("HS256")) {
                        key = new SecretKeySpec(Base64.decode(keyVal), SignatureAlgorithm.HS256.getJcaName());
                        signatureAlgorithm = SignatureAlgorithm.HS256;
                }
        }

        public String create(String username, Claims claims, List<String> scopes) {
                //header.payload.signature

                String subject = username;

                StringBuilder sb = new StringBuilder();
                for (String s : scopes) {
                        sb.append(" ").append(s);
                }
                if (sb.length() > 0) {
                        claims.add("scope", sb.substring(1));
                }
                PropertiesFile propertiesFile = (PropertiesFile) ThreadBag.get().getRequest().getServletContext().getAttribute(PropertiesFile.KEY);
                int expirySeconds = propertiesFile.getInt("jwt.expires_in_seconds", (60 * 60 * 24)); // 24 hours default
                String issuer = propertiesFile.getString("jwt.issuer");

                LocalDateTime currentTime = LocalDateTime.now();
                LocalDateTime expireTime = currentTime.plusSeconds(expirySeconds);
                Date expiryDate = Date.from(expireTime.toInstant(ZonedDateTime.now().getOffset()));

                String compactJws = Jwts.builder()
                        .setSubject(username)
                        .setClaims(claims.toMap())
                        .setIssuer(issuer)
                        .setIssuedAt(Date.from(currentTime.toInstant(ZonedDateTime.now().getOffset())))
                        .setExpiration(expiryDate)
                        .setId(StringUtil.randomCode(8))
                        .signWith(signatureAlgorithm, key)
                        .compact();

                return compactJws;
        }

        public boolean verify(String token) {
                try {
                        Jwts.parser().setSigningKey(key).parseClaimsJws(token);
                        return true;
                        //OK, we can trust this JWT
                } catch (ExpiredJwtException e) {
                        return false;
                } catch (SignatureException e) {
                        //don't trust the JWT!
                        return false;
                }
        }

        public String generateKey() {
                if (alg.equalsIgnoreCase("HS512")) {
                        return Base64.encode(MacProvider.generateKey().getEncoded());
                } else if (alg.equalsIgnoreCase("HS256")) {
                        return Base64.encode(MacProvider.generateKey(SignatureAlgorithm.HS256).getEncoded());
                } else {
                        throw new RuntimeException("Not implemented");
                }
        }

//        public static Boolean isJwtAuthorized(HttpServletRequest request) {
//                String header = request.getHeader("Authorization");
//                if (header != null) {
//                        if (header.startsWith("Bearer")) {
//                                // todo - we should check the token
//
//                                return true;
//                        }
//                }
//
//                Cookie c = getCookie("mto", request);
//                if (c != null) {
//                        String val = c.getValue().split("\\|")[0];
//                        return true;
//                }
//
//                return false;
//        }

        public static Jws<io.jsonwebtoken.Claims> parseJwtClaims(HttpServletRequest request) {
                try {
                        String header = request.getHeader("Authorization");
                        if (header != null) {
                                if (header.startsWith("Bearer")) {
                                        Jws<io.jsonwebtoken.Claims> claims = Jwts.parser().setSigningKey(key).parseClaimsJws(header.substring(6).trim());
                                        return claims;
                                }
                        }

                        Cookie c = getCookie("mto", request);
                        if (c != null) {
                                String val = c.getValue().split("\\|")[0];
                                Jws<io.jsonwebtoken.Claims> claims = Jwts.parser().setSigningKey(key).parseClaimsJws(val);
                                return claims;

                        }

                } catch (Exception e) {
                        logger.severe("jwt parsing failed. " + e.getMessage());
                }
                return null;
        }

        public static Cookie getCookie(String cookieName, HttpServletRequest request) {

                Cookie c[] = request.getCookies();
                if (c != null) {
                        for (int x = 0; x < c.length; x++) {
                                if (c[x].getName().equals(cookieName)) {
                                        return c[x];
                                }
                        }
                }
                return null;
        }

        public static String extractJwt(HttpServletRequest request) {
                String header = request.getHeader("Authorization");
                if (header != null) {
                        if (header.startsWith("Bearer")) {
                                return header.substring(7);
                        }
                }

                return "";
        }

        public static UserPrincipal extractUserPrincipal(HttpServletRequest request) {
                if (ThreadBag.get().getUserPrincipal() != null) {
                        return ThreadBag.get().getUserPrincipal();
                }

                String header = request.getHeader("Authorization");
                if (header != null) {
                        if (header.startsWith("Bearer")) {
                                String jwt = header.substring(7);
                                UserPrincipal userPrincipal = new UserPrincipal(key, jwt);
                                ThreadBag.get().setUserPrincipal(userPrincipal);
                                return userPrincipal;
                        }
                }

                return new UserPrincipal();
        }

}
