package sybrix.easygsp2.data;


import java.io.InputStream;

public interface XmlSerializer {
        String toXml(Object o);
        Object fromXml(String xmlString);
        Object fromXml(InputStream inputStream, long length);
        Object fromXml(InputStream inputStream, long length, Class clazz);
}
