package sybrix.easygsp2.data;


import groovy.json.JsonSlurper;
import sybrix.easygsp2.exceptions.ParameterDeserializationException;

import java.io.IOException;
import java.io.InputStream;

public class JsonSerializerImpl implements JsonSerializer {

        @Override
        public String toJson(Object o) {
                return null;
        }

        @Override
        public Object fromJson(String jsonString) {
                return null;
        }

        @Override
        public Object fromJson(InputStream inputStream, int length) throws ParameterDeserializationException {
                byte[] data = new byte[length];
                try {
                        inputStream.read(data);
                        return new JsonSlurper().parse(data);
                } catch (Exception e) {
                        throw new ParameterDeserializationException(e);
                }

        }

        @Override
        public Object fromJson(InputStream inputStream, int length, Class clazz) throws ParameterDeserializationException {
                return null;
        }
}
