package sybrix.easygsp2.framework

class ImplicitObjectInjector {
        static def addMetaProperties(m){
                m.metaClass.getRequest {
                        return ThreadBag.get().request
                }

                m.metaClass.getResponse {
                        return ThreadBag.get().response
                }

                m.metaClass.getParams {
                        return ThreadBag.get().request.getParameterMap()
                }

                m.metaClass.getSession {
                        return ThreadBag.get().request.getSession()
                }
        }
}
