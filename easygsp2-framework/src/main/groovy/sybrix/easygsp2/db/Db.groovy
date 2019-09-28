package sybrix.easygsp2.db

import groovy.sql.Sql
import groovy.transform.CompileStatic
import sybrix.easygsp2.framework.ThreadBag
import sybrix.easygsp2.util.PropertiesFile
import sybrix.easygsp2.util.StringUtil

import javax.sql.DataSource
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.sql.Connection

class Db {
        private String dataSourceName
        private PropertiesFile prop
        private DataSource dataSource

        private final ThreadLocal<Sql> sqlThreadLocal = new ThreadLocal<Sql>() {
                @Override
                protected Sql initialValue() {
                        return null
                }
        }

        public Db(){
                prop = (PropertiesFile)ThreadBag.get().getApp().getAttribute(PropertiesFile.KEY)
        }

        public Db(String dataSourceName){
                this();
                this.dataSourceName = dataSourceName
        }

        public Sql newSqlInstance() {
                try {
                        if (dataSourceName == null) {
                                Sql db = sqlThreadLocal.get()
                                if (db != null && !db.getConnection().isClosed()) {
                                        return db;
                                }
                        }

                        if (sybrix.easygsp2.util.StringUtil.isEmpty(dataSourceName)) {
                                dataSourceName = "";
                        } else {
                                dataSourceName += ".";
                        }

                        String dataSourceClass = (String) prop.getProperty(dataSourceName + "datasource.class");

                        if (dataSource != null) {
                                Sql db = new Sql(dataSource)
                                return db;
                        }

                        if (dataSource == null && dataSourceClass != null) {

                                Class<?> dsClass = Class.forName(dataSourceClass);

                                dataSource = dsClass.newInstance();
                                Map<String, Object> dataSourceProperties = getDataSourceProperties(dataSourceName)
                                for (String property : dataSourceProperties.keySet()) {
                                        callMethod(dataSource, "set" + StringUtil.capFirstLetter(property), prop.getProperty(dataSourceProperties.get(property)))
                                }

                                Connection connection = dataSource.connection
                                connection.close()

                                return new Sql(dataSource)

                        } else if (dataSource == null && dataSourceClass == null) {

                                String driver = prop.getProperty(dataSourceName + "database.driver")
                                String url = prop.getProperty(dataSourceName + "database.url")
                                String pwd = prop.getProperty(dataSourceName + "database.password");
                                String username = prop.getProperty(dataSourceName + "database.username")

                                return Sql.newInstance(url, username, pwd, driver);
                        }

                } catch (Exception e) {
                        throw new RuntimeException("newSqlInstance() failed. Make sure app['database.*]' properties are set and correct." + e.getMessage(), e);
                }

                return null;
        }

        private Map<String, Object> getDataSourceProperties(String datasourceName) {
                Map<String, Object> dataSourceProperties = new HashMap<String, Object>();
                Enumeration<Object> keyset = prop.keys()

                keyset.each { key ->
                        if (key.startsWith(datasourceName + "datasource.")) {
                                if (!key.equals(datasourceName + "datasource.class")) {
                                        dataSourceProperties.put(key.substring(key.lastIndexOf(".") + 1), key);
                                }
                        }
                }

                return dataSourceProperties;
        }

        private void callMethod(Object ds, String methodName, Object parameterValue) {

                try {
                        Method method = null;

                        Method[] methods = ds.getClass().getMethods();
                        for (Method m : methods) {
                                if (m.getName().equals(methodName) && m.getParameterTypes().length == 1) {
                                        method = m;
                                }
                        }

                        Class<?> cls = method.getParameterTypes()[0];
                        if (cls.getName().contains("boolean")) {
                                cls = Boolean.class;
                        } else if (cls.getName().contains("int")) {
                                cls = Integer.class;
                        } else if (cls.getName().contains("long")) {
                                cls = Long.class;
                        } else if (cls.getName().contains("double")) {
                                cls = Double.class;
                        }

                        Constructor<?> constructor = cls.getConstructor(String.class);
                        Object val = constructor.newInstance(parameterValue.toString());

                        Method m = ds.getClass().getMethod(methodName, method.getParameterTypes()[0]);
                        m.invoke(ds, val);
                } catch (Throwable e) {
                        throw new RuntimeException("Error setting DataSource property. datasource=" + ds.toString() + ", methodName=" + methodName + ", " +
                                "url=" + parameterValue, e);
                }
        }
}
