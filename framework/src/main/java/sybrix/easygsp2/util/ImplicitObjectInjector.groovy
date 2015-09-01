package sybrix.easygsp2.util

import sybrix.easygsp2.RequestThreadLocal


class ImplicitObjectInjector {
        static def addMetaProperties(m){
                m.metaClass.getRequest {
                        return RequestThreadLocal.get().request
                }

                m.metaClass.getResponse {
                        return RequestThreadLocal.get().response
                }

                m.metaClass.getParams {
                        return RequestThreadLocal.get().request.getParameterMap()
                }

                m.metaClass.getSession {
                        return RequestThreadLocal.get().request.getSession()
                }
        }
}
