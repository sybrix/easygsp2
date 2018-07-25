package sybrix.easygsp2.http

import groovy.transform.CompileStatic
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType
import sybrix.easygsp2.data.JsonSerializerImpl

@CompileStatic
class RestClient {
        public static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8"
        private JsonSerializerImpl jsonSerializer = new JsonSerializerImpl()

        OkHttpClient client = new OkHttpClient();

        private String serviceEndpoint
        private Map<String, String> requestHeaders = [:]
        private Response response = new Response()

        String token

        RestClient(String serviceEndpoint) {
                this.serviceEndpoint = serviceEndpoint
                //setHeader("Content-Type", JSON_CONTENT_TYPE)
                setHeader("Accept", JSON_CONTENT_TYPE)
        }

        Response doPost(String url, Object payload) {
                doRequest("POST", url, payload)
        }

        Response doPut(String url, Object payload) {
                doRequest("PUT", url, payload)
        }

        Response doDelete(String url, Object payload) {
                doRequest("DELETE", url, payload)
        }

        Response doPatch(String url, Object payload) {
                doRequest("PATCH", url, payload)
        }

        Response doRequest(String method, String url, Object payload) {
                RequestBody body = RequestBody.create(MediaType.parse(JSON_CONTENT_TYPE), payload instanceof String ? (String) payload : jsonSerializer.toString(payload));
                Request.Builder builder = new Request.Builder()

                requestHeaders.keySet().each {
                        builder.addHeader(it, requestHeaders.get(it))
                }

                Request request = builder
                        .url(url)
                        .method(method, body)
                        .build();

                okhttp3.Response response = client.newCall(request).execute();
                new Response(code: response.code(), rawResponse: response.body().bytes(), headers: requestHeaders)

        }

        Response doGet(String url) {

                Request.Builder builder = new Request.Builder()

                requestHeaders.keySet().each {
                        builder.addHeader(it, requestHeaders.get(it))
                }

                Request request = builder
                        .url(url)
                        .build();

                okhttp3.Response response = client.newCall(request).execute();
                new Response(code: response.code(), rawResponse: response.body().bytes(), headers: requestHeaders)

        }


        def setHeader(String headerName, String value) {
                requestHeaders[headerName] = value
        }

}



