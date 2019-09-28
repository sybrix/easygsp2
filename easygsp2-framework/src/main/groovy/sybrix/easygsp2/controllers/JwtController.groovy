package sybrix.easygsp2.controllers


import sybrix.easygsp2.exceptions.BadRequestException
import sybrix.easygsp2.http.HttpResponse
import sybrix.easygsp2.http.HttpStatus
import sybrix.easygsp2.models.CreateClientRequest
import sybrix.easygsp2.models.NewScopeRequest
import sybrix.easygsp2.models.TokenResponse
import sybrix.easygsp2.security.AuthenticationService
import sybrix.easygsp2.security.ClaimType
import sybrix.easygsp2.security.Claims
import sybrix.easygsp2.security.JwtUtil
import sybrix.easygsp2.util.Base64
import sybrix.easygsp2.util.PropertiesFile

import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.time.LocalDateTime
import java.time.ZoneId

class JwtController {
        PropertiesFile propertiesFile
        AuthenticationService authenticationService

        public TokenResponse generateToken(HttpServletRequest request, HttpServletResponse response) {
                String authorizationHeader = request.getHeader("Authorization");

                if (authorizationHeader ==null){
                        throw new BadRequestException("Missing \"Authorization\" header")
                }
                String decodedAuthorizationHeader = new String(Base64.decode(authorizationHeader.substring(6)))

                def parts = decodedAuthorizationHeader.split(":")
                String emailAddress = parts[0]
                String password = parts[1]

                PropertiesFile propertiesFile = (PropertiesFile) request.getServletContext().getAttribute(PropertiesFile.KEY)
                Long expirySeconds = propertiesFile.getLong("jwt.expires_in_seconds", (60 * 60 * 24))

                authenticate(emailAddress, password, expirySeconds, request.getServletContext())
        }

        public TokenResponse authenticate(String emailAddress, String password, Long expirySeconds, ServletContext context) {

                Long profileId = authenticationService.validateCredentials(emailAddress, password)

                // 24 hours default

                LocalDateTime currentTime = LocalDateTime.now()
                LocalDateTime expireTime = currentTime.plusSeconds(expirySeconds);
                Date expiryDate = Date.from(expireTime.atZone(ZoneId.systemDefault()).toInstant());

                Claims claims = new Claims()
                claims.add(ClaimType.SUBJECT, emailAddress)
                claims.add(ClaimType.EXPIRATION_TIMESTAMP, String.valueOf(expiryDate.time))
                claims.add(ClaimType.EXPIRATION_TIMESTAMP, String.valueOf(expiryDate.time))
                claims.add(ClaimType.PROFILE_ID, profileId.toString())

                def scopes = []
                authenticationService.getRoles(profileId.toString()).each {
                        scopes.add(it)
                }

                TokenResponse tokenResponse = new TokenResponse()
                tokenResponse.idToken = JwtUtil.instance.create(emailAddress, claims, scopes)
                tokenResponse.username = emailAddress
                return tokenResponse
        }

        def createClient(CreateClientRequest createClientRequest, HttpServletRequest request) {
                //AuthenticationService authenticationUtil = request.getServletContext().getAttribute("authenticationUtil")
                def client = authenticationService.addClient(createClientRequest.name)

                new HttpResponse(HttpStatus.CREATED, client)
        }

        def addScope(NewScopeRequest clientRequest, HttpServletRequest request) {
                //AuthenticationService authenticationUtil = request.getServletContext().getAttribute("authenticationUtil")

                def client = authenticationService.insertScope(clientRequest.clientId, clientRequest.scope)
                new HttpResponse(HttpStatus.CREATED, client)
        }
}


