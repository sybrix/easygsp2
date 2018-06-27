package sybrix.easygsp2.db.firebird;

import groovy.lang.Closure;
import groovy.sql.Sql;
import sybrix.easygsp2.framework.ThreadBag;
import sybrix.easygsp2.util.StringUtil;

import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by dsmith on 8/16/15.
 */
public class Db {
        private static final Logger logger = Logger.getLogger(Db.class.getName());

        public static Sql newSqlInstance() {
                return newSqlInstance(null);
        }

        public static Object executeProcedure(String procedureName) throws Exception {
                Sql db = newSqlInstance();
                String s = "{call " + procedureName + ")}";

                logger.log(Level.FINE, "execute procedure call: " + s);
                return db.call(s);
        }

        public static Object executeProcedure(String procedureName, List<Object> parameters) throws Exception {
                Sql db = newSqlInstance();
                StringBuffer s = createProcedureCall(procedureName, parameters);

                logger.log(Level.FINE, "execute procedure call: " + s.toString() + " parameters:" + parameters);
                return db.call(s.toString(), parameters);
        }

        public static Object executeProcedure(String procedureName, List<Object> parameters, Closure<?> returnClosure) throws Exception {
                Sql db = newSqlInstance();
                StringBuffer s = createProcedureCall(procedureName, parameters);

                logger.log(Level.FINE, "execute procedure call: " + s.toString() + " parameters:" + parameters);
                db.call(s.toString(), parameters, returnClosure);

                return returnClosure.call();
        }

        private static StringBuffer createProcedureCall(String procedureName, List<Object> parameters) {
                StringBuffer s = new StringBuffer();

                for (int i = 0; i < parameters.size(); i++) {
                        s.append(",?");
                }

                s.setCharAt(0, '(');
                s.insert(0, "{call " + procedureName);
                s.append(")}");
                return s;
        }

        public static Sql newSqlInstance(String dataSourceName) {
                Object ds = null;

                try {
                        if (dataSourceName == null) {
                                Sql db = ThreadBag.get().getSql();
                                if (db != null && !db.getConnection().isClosed()) {
                                        return db;
                                }
                        }

                        ServletContext app = ThreadBag.get().getApp();
                        if (dataSourceName == null) {
                                dataSourceName = "";
                        } else {
                                dataSourceName += ".";
                        }

                        ds = app.getAttribute("__dataSource_" + dataSourceName);
                        String dataSourceClass = (String) app.getAttribute(dataSourceName + "datasource.class");

                        if (ds != null && dataSourceClass != null) {
                                Sql db = new Sql((DataSource) ds);
                                return db;
                        }

                        if (ds == null && dataSourceClass != null) {

//                                String url = (String) app.getAttribute(dataSourceName + "datasource.url");
//                                String pwd = (String) app.getAttribute(dataSourceName + "datasource.password");
//                                String username = (String) app.getAttribute(dataSourceName + "datasource.username");

                                Class<?> dsClass = Class.forName(dataSourceClass);

                                ds = dsClass.newInstance();
                                Map<String, String> dataSourceProperties = getDataSourceProperties(app, dataSourceName);
                                for (String property : dataSourceProperties.keySet()) {
                                        callMethod(ds, "set" + StringUtil.capFirstLetter(property), app.getAttribute(dataSourceProperties.get(property)));
                                }
//                                callMethod(ds, "setUserName", username);
//                                callMethod(ds, "setPassword", pwd);
//                                callMethod(ds, "setDatabase", url);
//                                callMethod(ds, "setMaxIdleTime", 30);
//                                callMethod(ds, "setPooling", true);
//                                callMethod(ds, "setMinPoolSize", 5);
//                                callMethod(ds, "setMaxPoolSize", 30);
//                                callMethod(ds, "setLoginTimeout", 10);

                                app.setAttribute("__dataSource_" + dataSourceName, ds);


                                return new Sql((DataSource) ds);

                        } else if (ds == null && dataSourceClass == null) {

                                String driver = (String) app.getAttribute(dataSourceName + "database.driver");
                                String url = (String) app.getAttribute(dataSourceName + "database.url");
                                String pwd = (String) app.getAttribute(dataSourceName + "database.password");
                                String username = (String) app.getAttribute(dataSourceName + "database.username");

                                return Sql.newInstance(url, username, pwd, driver);
                        }

                } catch (Exception e) {
                        throw new RuntimeException("newSqlInstance() failed. Make sure app['database.*]' properties are set and correct." + e.getMessage(), e);
                }

                return null;
        }

        private static Map<String, String> getDataSourceProperties(ServletContext app, String datasourceName) {
                Map<String,String> dataSourceProperties = new HashMap<String,String>();
                Enumeration<String> enumeration = app.getAttributeNames();

                while (enumeration.hasMoreElements()) {
                        String key = (String) enumeration.nextElement();
                        if (key.toString().startsWith(datasourceName + "datasource.")) {
                                if (!key.equals(datasourceName + "datasource.class")) {
                                        dataSourceProperties.put(key.toString().substring(key.toString().lastIndexOf(".") + 1), key);
                                }
                        }
                }

                return dataSourceProperties;
        }

        private static void callMethod(Object ds, String methodName, Object parameterValue) {

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

        public static void withTransaction(final Closure<?> closure) {
                Sql db = newSqlInstance(null);
                java.sql.Connection connection = null;

                try {
                        connection = db.getConnection() == null ? db.getDataSource().getConnection() : db.getConnection();

                        connection.setAutoCommit(false);

                        ThreadBag.get().setSql(db);

                        closure.call();

                        connection.commit();
                } catch (Throwable e) {
                        try {
                                connection.rollback();
                        } catch (SQLException e1) {

                        }
                        throw new RuntimeException("Db.withTransaction closure failed. " + e.getMessage(), e);
                } finally {
                        if (db != null)
                                db.close();

                        ThreadBag.get().setSql(null);
                }
        }
}
