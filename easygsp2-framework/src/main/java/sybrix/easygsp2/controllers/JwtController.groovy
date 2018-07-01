package sybrix.easygsp2.controllers

import sybrix.easygsp2.models.TokenResponse
import sybrix.easygsp2.security.ClaimType
import sybrix.easygsp2.security.Claims
import sybrix.easygsp2.security.JwtUtil

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtController {

    public TokenResponse generateToken(HttpServletRequest request, HttpServletResponse response) {
        String authorizationHeader = request.getHeader("Authorization");
        String contentLength = request.getHeader("Content-Length");

        Claims claims = new Claims()
        claims.add(ClaimType.SUBJECT,"")

        TokenResponse tokenResponse = new TokenResponse()
        tokenResponse.idToken  = JwtUtil.instance.create(claims)

        tokenResponse
    }
}
