package sybrix.easygsp2.security

import sybrix.easygsp2.exceptions.LoginFailedException
import sybrix.easygsp2.exceptions.UnauthorizedException

import javax.servlet.http.HttpServletRequest

interface AuthenticationService {
        public Long validateCredentials(String username, String password) throws UnauthorizedException

        public List<String> getRoles(String id)

        public Client addClient(String name)


}
