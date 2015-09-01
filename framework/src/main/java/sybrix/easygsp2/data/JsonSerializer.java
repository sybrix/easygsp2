package sybrix.easygsp2.data;

import sybrix.easygsp2.exceptions.ParameterDeserializationException;

import java.io.InputStream;

public interface JsonSerializer {
        String toJson(Object o);

        Object fromJson(String jsonString) throws ParameterDeserializationException;

        Object fromJson(InputStream inputStream, int length) throws ParameterDeserializationException;

        Object fromJson(InputStream inputStream, int length, Class clazz) throws ParameterDeserializationException;


//        Object fromJson(InputStream inputStream);
}