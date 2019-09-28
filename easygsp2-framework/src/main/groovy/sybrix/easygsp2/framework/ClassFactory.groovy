package sybrix.easygsp2.framework

import sybrix.easygsp2.EasyGsp2

class ClassFactory {
        static GroovyObject create(String cls, Object app){
                create(Class.forName(cls),app)
        }

        static GroovyObject create(Class cls, Object app){
                def instance = cls.newInstance()
                if (isProperty(cls,"propertiesFile")){
                        println("propertiesFile")
                        instance.propertiesFile =  app.propertiesFile
                }

                if (isProperty(cls,"emailService")){
                        println("emailService")
                        instance.emailService =  app.emailService
                }

                if (isProperty(cls,"authenticationService")){
                        println("authenticationService")
                        instance.authenticationService =  app.getAuthenticationService()
                }

                instance
        }

        protected static boolean isProperty(Class clazz, String propertyName) {
                def metaProperty = clazz.metaClass.getMetaProperty(propertyName)
                if (metaProperty instanceof MetaBeanProperty) {
                        return true
                } else {
                        return false
                }
        }

}
