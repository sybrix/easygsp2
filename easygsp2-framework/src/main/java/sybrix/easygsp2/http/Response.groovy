package sybrix.easygsp2.http;

import groovy.json.JsonSlurper
import sybrix.easygsp2.data.JsonSerializerImpl

/**
 * Created by davidl.smith on 11/4/14.
 */
class Response {
        Integer code
        Map<String, String> headers = [:]
        byte[] rawResponse

        def getHeaderValue(String header) {
                headers.get(header)?.get(0)
        }

        def getHeaderValues(String header) {
                headers.get(header)
        }

        def parseJson(Class cls) {
                new JsonSerializerImpl().parse(new String(rawResponse), cls)
        }

        def toJson() {
                new JsonSlurper().parseText(new String(rawResponse))
        }

        def toXML() {
                new XmlSlurper().parseText(new String(rawResponse))
        }

        String toString() {
                new String(rawResponse)
        }
}