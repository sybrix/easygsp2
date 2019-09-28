package sybrix.easygsp2.data;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import groovy.json.JsonSlurper;
import sybrix.easygsp2.exceptions.ParameterDeserializationException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

public class JsonSerializerImpl implements Serializer {

        public void write(Object o, OutputStream outputStream) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                        //httpServletResponse.setHeader("Transfer-Encoding", "chunked");
                        //httpServletResponse.setHeader("Content-Type", "application/json; charset=utf-8");
                        //httpServletResponse.setHeader("Connection", "keep-alive");

                        //ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

//                        ChunkedOutputStream chunkedOutputStream = new ChunkedOutputStream(httpServletResponse.getOutputStream());
                        objectMapper.writeValue(outputStream, o);
//                        chunkedOutputStream.finish();

//                        httpServletResponse.getOutputStream().flush();

                        //System.out.println(new String(byteArrayOutputStream.toByteArray()));
                } catch (Exception e) {
                        throw new RuntimeException("error converting object to json, " + e.getMessage(), e);
                }
        }

        public String toString(Object o) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                        return objectMapper.writeValueAsString(o);
                } catch (Exception e) {
                        throw new RuntimeException("error converting object to json, " + e.getMessage(), e);
                }
        }

        public Object parse(String jsonString, Class cls) {
                try {
                        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                        return objectMapper.readValue(jsonString, cls);
                } catch (IOException e) {
                        throw new RuntimeException("Unable to readValue from json string: " + jsonString);
                }
        }

        @Override
        public Object parse(String xmlString) {
                return null;
        }

        public Object parse(InputStream inputStream, int length) throws ParameterDeserializationException {
                byte[] data = new byte[length];
                try {
                        inputStream.read(data);
                        return new JsonSlurper().parse(data);
                } catch (Exception e) {
                        throw new ParameterDeserializationException(e);
                }
        }

        public Object parse(InputStream inputStream, int length, Class collectionClass, Type modelType) throws ParameterDeserializationException {
                byte[] data = new byte[length];
                try {
                        inputStream.read(data);

                        ObjectMapper objectMapper = new ObjectMapper()
                                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                        ObjectReader reader = null;
                        JavaType type = null;
                        if (modelType != null && collectionClass != null) {
                                JavaType javaType = objectMapper.getTypeFactory().constructType(modelType);
                                type = objectMapper.getTypeFactory().constructCollectionType(collectionClass, javaType);
                                reader = objectMapper.readerFor(type);
                        } else if (modelType != null) {
                                reader = objectMapper.readerFor(objectMapper.getTypeFactory().constructType(modelType));
                        } else {
                                reader = objectMapper.readerFor(collectionClass);
                        }

                        return reader.readValue(new String(data));
                } catch (Exception e) {
                        throw new ParameterDeserializationException(e);
                }
        }

}
