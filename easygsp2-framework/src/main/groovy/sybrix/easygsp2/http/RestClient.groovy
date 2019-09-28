package sybrix.easygsp2.http

import groovy.transform.CompileStatic
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import sybrix.easygsp2.data.JsonSerializerImpl

@CompileStatic
class RestClient {
        public static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8"
        private JsonSerializerImpl jsonSerializer = new JsonSerializerImpl()

        OkHttpClient client

        private String host
        private Map<String, String> requestHeaders = [:]
        private RestResponse response = new RestResponse()

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override public void log(String message) {
                        println(message);
                }
        });

        String token

        RestClient(String host) {
                this.host = host.endsWith("/") ? host.substring(0, host.length() - 1) : host

                //setHeader("Content-Type", JSON_CONTENT_TYPE)
                setHeader("Accept", JSON_CONTENT_TYPE)
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                client = new OkHttpClient.Builder().addInterceptor(logging).build();
        }

        RestResponse doPost(String url, Object payload) {
                doRequest("POST", url, payload)
        }

        RestResponse doPost(String url) {
                doRequest("POST", url, null)
        }

        RestResponse doPut(String url, Object payload) {
                doRequest("PUT", url, payload)
        }

        RestResponse doDelete(String url, Object payload) {
                doRequest("DELETE", url, payload)
        }

        RestResponse doPatch(String url, Object payload) {
                doRequest("PATCH", url, payload)
        }

        RestResponse doRequest(String method, String url, Object payload) {
                RequestBody body = null

                if (requestHeaders["Content-Type"]?.contains("x-www-form-urlencoded")) {
                        body = RequestBody.create(MediaType.parse("x-www-form-urlencoded"), convert(payload));
                } else {
                        requestHeaders.put("Content-Type",JSON_CONTENT_TYPE)
                        body = RequestBody.create(MediaType.parse(JSON_CONTENT_TYPE), convert(payload));
                }

                Request.Builder builder = new Request.Builder()

                requestHeaders.keySet().each {
                        builder.addHeader(it, requestHeaders.get(it))
                }

                Request request = builder
                        .url(host + (url.startsWith("/") ? url : "/" + url))
                        .method(method, body)
                        .build();

                okhttp3.Response response = client.newCall(request).execute();
                Map<String,String> headers = [:]

                response.headers().asList().each {
                        headers[it.first] = it.second
                }
                new RestResponse(code: response.code(), rawResponse: response.body().bytes(), headers: headers)


        }

        RestResponse doGet(String url) {

                Request.Builder builder = new Request.Builder()

                requestHeaders.keySet().each {
                        builder.addHeader(it, requestHeaders.get(it))
                }

                Request request = builder
                        .url(host + (url.startsWith("/") ? url : "/" + url))
                        .build();

                okhttp3.Response response = client.newCall(request).execute();
                Map<String,String> headers = [:]

                response.headers().asList().each {
                        headers[it.first] = it.second
                }
                new RestResponse(code: response.code(), rawResponse: response.body().bytes(), headers: headers)

        }


        def setHeader(String headerName, String value) {
                requestHeaders[headerName] = value
        }

        String convert(payload){
                if (payload instanceof String)
                        return (String)payload
                else if (payload  instanceof GString)
                        return payload.toString()
                else {
                        return jsonSerializer.toString(payload)
                }
        }

}



