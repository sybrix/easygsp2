package sybrix.easygsp2.controllers

import sybrix.easygsp2.models.TokenResponse
import sybrix.easygsp2.security.ClaimType
import sybrix.easygsp2.security.Claims
import sybrix.easygsp2.security.JwtUtil
import sybrix.easygsp2.util.PropertiesFile

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.time.Instant
import java.time.LocalDateTime

class JwtController {

    public TokenResponse generateToken(HttpServletRequest request, HttpServletResponse response) {
        String authorizationHeader = request.getHeader("Authorization");
        String contentLength = request.getHeader("Content-Length");

        PropertiesFile propertiesFile = (PropertiesFile)request.getServletContext().getAttribute("__propertieFile")
        int expirySeconds =  propertiesFile.getInt("jwt.expires_in_seconds", (60 * 60 * 24)) // 24 hours default

        LocalDateTime currentTime = LocalDateTime.now()
        LocalDateTime expireTime = currentTime.plusSeconds(expirySeconds);
        Date expiryDate = Date.from(expireTime.toInstant());

        Claims claims = new Claims()
        claims.add(ClaimType.SUBJECT,"")
        claims.add(ClaimType.EXPIRATION_TIMESTAMP,expiryDate.time)

        TokenResponse tokenResponse = new TokenResponse()
        tokenResponse.idToken  = JwtUtil.instance.create(claims)

        tokenResponse
    }
}
