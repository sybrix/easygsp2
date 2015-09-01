package sybrix.easygsp2.util;

import groovy.lang.Closure;
import groovy.sql.Sql;
import sybrix.easygsp2.db.CurrentSQLInstance;

import javax.servlet.ServletContext;
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

        public static Object executeProcedure(String procedureName, List parameters) throws Exception {
                Sql db = newSqlInstance();
                StringBuffer s = createProcedureCall(procedureName, parameters);

                logger.log(Level.FINE, "execute procedure call: " + s.toString() + " parameters:" + parameters);
                return db.call(s.toString(), parameters);
        }

        public static Object executeProcedure(String procedureName, List parameters, Closure returnClosure) throws Exception {
                Sql db = newSqlInstance();
                StringBuffer s = createProcedureCall(procedureName, parameters);

                logger.log(Level.FINE, "execute procedure call: " + s.toString() + " parameters:" + parameters);
                db.call(s.toString(), parameters, returnClosure);

                return returnClosure.call();
        }

        private static StringBuffer createProcedureCall(String procedureName, List parameters) {
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
                                Sql db = CurrentSQLInstance.get();
                                if (db != null) {
                                        return db;
                                }
                        }

//                        ServletContextImpl app = RequestThreadInfo.get().getApplication();
//                        if (dataSourceName == null) {
//                                dataSourceName = "";
//                        } else {
//                                dataSourceName += ".";
//                        }
//
//                        ds = app.getAttribute("__dataSource_" + dataSourceName);
//                        String dataSourceClass = (String) app.getAttribute(dataSourceName + "datasource.class");
//
//                        if (ds != null && dataSourceClass != null) {
//                                return new Sql((DataSource) ds);
//                        }
//
//                        if (ds == null && dataSourceClass != null) {
//
//                                String url = (String) app.getAttribute(dataSourceName + "datasource.url");
//                                String pwd = (String) app.getAttribute(dataSourceName + "datasource.password");
//                                String username = (String) app.getAttribute(dataSourceName + "datasource.username");
//
//                                Class dsClass = Class.forName(dataSourceClass);
//
//                                ds = dsClass.newInstance();
//                                Map<String, String> dataSourceProperties = getDataSourceProperties(app, dataSourceName);
//                                for (String property : dataSourceProperties.keySet()) {
//                                        callMethod(ds, "set" + StringUtil.capFirstLetter(property), app.getAttribute(dataSourceProperties.get(property)));
//                                }
////                                callMethod(ds, "setUserName", username);
////                                callMethod(ds, "setPassword", pwd);
////                                callMethod(ds, "setDatabase", url);
////                                callMethod(ds, "setMaxIdleTime", 30);
////                                callMethod(ds, "setPooling", true);
////                                callMethod(ds, "setMinPoolSize", 5);
////                                callMethod(ds, "setMaxPoolSize", 30);
////                                callMethod(ds, "setLoginTimeout", 10);
//
//                                app.setAttribute("__dataSource_" + dataSourceName, ds);
//                                return new Sql((DataSource) ds);
//
//                        } else if (ds == null && dataSourceClass == null) {
//
//                                String driver = (String) app.getAttribute(dataSourceName + "database.driver");
//                                String url = (String) app.getAttribute(dataSourceName + "database.url");
//                                String pwd = (String) app.getAttribute(dataSourceName + "database.password");
//                                String username = (String) app.getAttribute(dataSourceName + "database.username");
//
//                                return Sql.newInstance(url, username, pwd, driver);
//
//                        }


                } catch (Exception e) {
                        throw new RuntimeException("newSqlInstance() failed. Make sure app['database.*]' properties are set and correct." + e.getMessage(), e);
                }

                return null;
        }

        private static Map<String, String> getDataSourceProperties(ServletContext app, String datasourceName) {
                Map dataSourceProperties = new HashMap();
//                Map attributes = app.getAttributes();
//                for (Object key : attributes.keySet()) {
//                        //Object val = attributes.get(key);
//                        if (key.toString().startsWith(datasourceName + "datasource.")) {
//                                if (!key.equals(datasourceName + "datasource.class")) {
//                                        dataSourceProperties.put(key.toString().substring(key.toString().lastIndexOf(".") + 1), key);
//                                }
//                        }
//                }

                return dataSourceProperties;

        }

}
