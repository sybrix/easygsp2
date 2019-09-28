package sybrix.easygsp2.http

import groovy.json.JsonSlurper
import org.codehaus.groovy.runtime.GStringImpl

class ControllerResponse {
        Boolean isString = false
        Boolean isJsonSluper = false
        Boolean isXMLSlurper = false
        Boolean isEntity = false
        Integer httpCode
        Object entity
        boolean forwarded = false
        boolean redirected = false
        String url

        void setEntity(Object entity) {
                this.entity = entity
        }

        String getViewTemplate() {
                if (isString)
                        return entity as String
        }

        boolean forwarded() {
                return forwarded
        }

        boolean redirected() {
                return redirected
        }


        static ControllerResponse createForwardedResponse(String url) {
                ControllerResponse controllerResponse = new ControllerResponse()
                controllerResponse.forwarded = true
        }

        static ControllerResponse parse(obj) {
                if (obj instanceof HttpResponse) {
                        return new ControllerResponse(

                                httpCode: (obj as HttpResponse).code,
                                isEntity: (obj as HttpResponse).entity != null,
                                entity: obj
                        )
                } else if (obj instanceof String || obj instanceof GStringImpl) {
                        String s = (String) obj
                        if (s.trim().startsWith("forward:")) {
                                return new ControllerResponse(forwarded: true, url:s.trim().substring(8).trim())
                        }
                        if (s.trim().startsWith("redirect:")) {
                                return new ControllerResponse(redirected: true, url:s.trim().substring(8).trim())
                        }

                        return new ControllerResponse(
                                isString: true,
                                entity: obj
                        )
                } else if (obj instanceof Collection) {
                        return new ControllerResponse(
                                isEntity: true,
                                entity: obj
                        )
                } else if (obj instanceof JsonSlurper) {
                        return new ControllerResponse(
                                isJsonSluper: true,
                                entity: obj
                        )
                } else if (obj instanceof XmlSlurper) {
                        return new ControllerResponse(
                                isXMLSlurper: true,
                                entity: obj
                        )
                } else if (obj instanceof ControllerResponse) {
                        return obj
                } else {
                        return new ControllerResponse(
                                isEntity: true,
                                entity: obj,
                                httpCode: HttpStatus.OK.val()
                        )
                }
        }
}
