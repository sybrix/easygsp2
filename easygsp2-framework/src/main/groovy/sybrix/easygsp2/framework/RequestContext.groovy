package sybrix.easygsp2.framework

import groovy.transform.ToString
import sybrix.easygsp2.security.UserPrincipal
import sybrix.easygsp2.util.PropertiesFile

import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.security.Principal

@ToString
class RequestContext {
        HttpServletResponse response
        HttpServletRequest request
        ServletContext app
        Principal userPrincipal
        PropertiesFile properties
}
