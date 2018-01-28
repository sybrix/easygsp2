package sybrix.easygsp2.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import sybrix.easygsp2.exceptions.SerializerException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

public interface Serializer {
        public void write(Object o, HttpServletResponse httpServletResponse) throws SerializerException;

        String toString(Object o);

        Object parse(String xmlString);

        Object parse(InputStream inputStream, int length);

        Object parse(InputStream inputStream, int length, Class clazz, Type modelType);

}
