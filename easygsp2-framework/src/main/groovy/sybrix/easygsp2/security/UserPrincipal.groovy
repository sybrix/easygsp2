package sybrix.easygsp2.security

import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import sybrix.easygsp2.collections.CaseInsensitiveList

import javax.security.auth.Subject
import java.security.Key
import java.security.Principal


class UserPrincipal implements Principal {
        public static final String USER_PRINCIPAL_KEY = "__userPrincipal"

        Boolean authenticated = false
        List<String> roles = new CaseInsensitiveList()
        String jwt
        String username
        Date tokenExpiration
        String authType
        Long id

        UserPrincipal() {
                authType = "FORM_AUTH"
        }

        UserPrincipal(Key key, String jwt) {

                authType = "BASIC_AUTH"
                Jws<io.jsonwebtoken.Claims> claims = Jwts.parser().setSigningKey(key).parseClaimsJws(jwt);
                io.jsonwebtoken.Claims body = claims.getBody()
                id = Long.parseLong(body.get(ClaimType.PROFILE_ID.val()))

                username = body.getSubject()

                tokenExpiration = body.getExpiration()
                if (System.currentTimeMillis() < tokenExpiration.getTime()) {
                        authenticated = true
                }
                String scopes = body.get("scope", String.class)
                if (scopes != null) {
                        scopes.split(" ").each {
                                roles.add(it)
                        }
                }
        }

        @Override
        String getName() {
                return username
        }

        @Override
        boolean implies(Subject subject) {
                return super.implies(subject)
        }
}
