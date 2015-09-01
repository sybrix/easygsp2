package sybrix.easygsp2;

import groovy.lang.*;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import org.codehaus.groovy.runtime.GroovyCategorySupport;
import org.codehaus.groovy.runtime.InvokerHelper;
import sybrix.easygsp2.categories.RoutingCategory;
import sybrix.easygsp2.data.JsonSerializer;
import sybrix.easygsp2.data.XmlSerializer;
import sybrix.easygsp2.exceptions.InvokeControllerActionException;
import sybrix.easygsp2.exceptions.NoMatchingRouteException;
import sybrix.easygsp2.exceptions.NoViewTemplateFound;
import sybrix.easygsp2.routing.Route;
import sybrix.easygsp2.templates.TemplateInfo;
import sybrix.easygsp2.templates.TemplateWriter;
import sybrix.easygsp2.util.ImplicitObjectInjector;
import sybrix.easygsp2.util.PropertiesFile;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class EasyGspFilter implements Filter {
        private static final Logger logger = Logger.getLogger(EasyGspFilter.class.getName());
        private static final String JSON_SERIALIZER_CLASS = "sybrix.easygsp2.data.JsonSerializerImpl";
        private static final String XML_SERIALIZER_CLASS = "sybrix.easygsp2.data.XmlSerializerImpl";

        private Map<String, Route> routes = new HashMap<String, Route>();
        private GroovyClassLoader groovyClassLoader;
        private PropertiesFile propertiesFile;
        private ServletContext context;
        private boolean useImplicitControllerObjects;

        protected static JsonSerializer jsonSerializerInstance;
        protected static XmlSerializer xmlSerializerInstance;

        @Override
        public void init(FilterConfig config) throws ServletException {

                try {
                        System.setProperty("easygsp.version", "@easygsp_version");

                        logger.info(
                                "\nEASYGSP_VERSION: " + System.getProperty("easygsp.version") +
                                        "\nJRE_HOME: " + System.getProperty("java.home") +
                                        "\nJAVA_VERSION: " + System.getProperty("java.version") +
                                        "\nGROOVY_VERSION: " + GroovySystem.getVersion() +
                                        "\n"
                        );

                        context = config.getServletContext();
                        groovyClassLoader = new GroovyClassLoader(this.getClass().getClassLoader());
                        propertiesFile = new PropertiesFile("classPath:easygsp.properties");
                        useImplicitControllerObjects = Boolean.parseBoolean(propertiesFile.getString("implicit.http.objects", "true"));
                        loadRoutes();
                        loadSerializers(propertiesFile);

                } catch (Throwable e) {
                        logger.log(Level.SEVERE, "error occurred in init()", e);
                }
        }

        private void loadSerializers(PropertiesFile propertiesFile) {
                String jsonSerializer = null;
                String xmlSerializer = null;

                try {
                        jsonSerializer = propertiesFile.getString("json.serializer.class", JSON_SERIALIZER_CLASS);
                        logger.config("JsonSerializer: " + jsonSerializer);
                        jsonSerializerInstance = (JsonSerializer) Class.forName(jsonSerializer).newInstance();
                } catch (Exception e) {
                        if (JSON_SERIALIZER_CLASS.equalsIgnoreCase(jsonSerializer)) {
                                logger.log(Level.SEVERE, "Unable to instantiate default json serializer: " + JSON_SERIALIZER_CLASS);
                        } else {
                                logger.log(Level.WARNING, "error occurred instantiating jsonSerializer: " + jsonSerializer + ", attempting to use default: " + JSON_SERIALIZER_CLASS);

                                try {
                                        jsonSerializerInstance = (JsonSerializer) Class.forName(jsonSerializer).newInstance();
                                } catch (Exception e1) {
                                        logger.log(Level.SEVERE, "Unable to instantiate default json serializer: " + JSON_SERIALIZER_CLASS);
                                }
                        }
                }

                try {
                        xmlSerializer = propertiesFile.getString("xml.serializer.class", XML_SERIALIZER_CLASS);
                        logger.config("XmlSerializer: " + xmlSerializer);
                        xmlSerializerInstance = (XmlSerializer) Class.forName(xmlSerializer).newInstance();
                } catch (Exception e) {
                        logger.log(Level.WARNING, "error occurred instantiating xmlSerializer: " + xmlSerializer + ", attempting to use default: " + XML_SERIALIZER_CLASS);

                        try {
                                xmlSerializerInstance = (XmlSerializer) Class.forName(xmlSerializer).newInstance();
                        } catch (Exception e1) {
                                logger.log(Level.SEVERE, "Unable to instantiate default xml serializer: " + XML_SERIALIZER_CLASS);
                        }

                }
        }


        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {

                final HttpServletRequest httpServletRequest = ((HttpServletRequest) servletRequest);
                final HttpServletResponse httpServletResponse = ((HttpServletResponse) servletResponse);
                final TemplateInfo templateInfo = new TemplateInfo();
                final String uri = ((HttpServletRequest) servletRequest).getRequestURI();

                logger.finest("uri - " + uri);


                try {
                        if (((HttpServletRequest) servletRequest).getRequestURI().equals("/favicon.ico")) {
                                httpServletRequest.getRequestDispatcher("/favicon.ico").forward(httpServletRequest, httpServletResponse);
                                return;
                        }

                        RequestThreadLocal.set(new RequestResponseObject(servletRequest, servletResponse, templateInfo));
                        logger.finest("searching for matching route for uri: " + uri);

                        final Route route = findRoute(httpServletRequest);

                        Closure closure = new Closure(groovyClassLoader) {

                                public Object call() {
                                        try {
                                                String defaultPackageName = propertiesFile.getString("controllers.package");
                                                String[] controllerAndActionName = extractControllerAndActionName("", uri);
                                                String controllerName = controllerAndActionName[0];
                                                String actionName = controllerAndActionName[1];
                                                String contentType = httpServletRequest.getContentType();
                                                int contentLength = httpServletRequest.getContentLength();

                                                Class controllerClass = null;
                                                String controllerMethod = null;

                                                if (route == null) {
                                                        logger.finest("using default controller name " + defaultPackageName + "." + controllerName + ", action name " + actionName);
                                                        try {
                                                                controllerClass = Class.forName(defaultPackageName + "." + controllerName);
                                                                controllerMethod = actionName;
                                                        } catch (Exception e) {
                                                                throw new NoMatchingRouteException(e);
                                                        }
                                                } else {
                                                        controllerClass = route.getControllerClass();
                                                        controllerMethod = route.getControllerMethod();
                                                }

                                                GroovyObject controller = (GroovyObject) controllerClass.newInstance();

                                                if (useImplicitControllerObjects) {
                                                        logger.finer("adding implicit controller objects (request, response, session, params)");
                                                        ImplicitObjectInjector.addMetaProperties(controller);
                                                }

                                                Method[] methods = controller.getClass().getMethods();

                                                for (Method m : methods) {
                                                        if (m.getName().equals(controllerMethod)) {
                                                                List<String> s = lookupParameterNames(m);
                                                                Parameter[] parameters = m.getParameters();

                                                                logger.finer("invoking method '" + m.getName() + "', parameters:" + extractParameterTypes(parameters));
                                                                logger.finest("parameter names: " + s);

                                                                Object[] params = new Object[parameters.length];

                                                                int i = 0;
                                                                for (Parameter p : parameters) {
                                                                        String parameterName = p.getName();
                                                                        if (s.size() == parameters.length) {
                                                                                parameterName = s.get(i);
                                                                        }

                                                                        Class clazz = p.getType();

                                                                        if (clazz == javax.servlet.http.HttpServletRequest.class || parameterName.equalsIgnoreCase("request")) {
                                                                                params[i] = httpServletRequest;
                                                                        } else if (clazz == HttpServletResponse.class || parameterName.equalsIgnoreCase("response")) {
                                                                                params[i] = httpServletResponse;
                                                                        } else {

                                                                                String valFromRequestParameter = httpServletRequest.getParameter(parameterName);
                                                                                if (valFromRequestParameter != null) {
                                                                                        params[i] = castToType(valFromRequestParameter, clazz);

                                                                                } else if (contentType != null && contentLength > 0 && isJson(contentType)) {
                                                                                        Object obj = null;
                                                                                        if (clazz.getTypeName().equals("java.lang.Object")) {
                                                                                                obj = jsonSerializerInstance.fromJson(httpServletRequest.getInputStream(), contentLength);
                                                                                        } else {
                                                                                                obj = jsonSerializerInstance.fromJson(httpServletRequest.getInputStream(), contentLength, clazz);
                                                                                        }
                                                                                        params[i] = obj;

                                                                                } else if (contentType != null && contentLength > 0 && isXML(contentType)) {
                                                                                        Object obj = null;
                                                                                        if (clazz.getTypeName().equals("java.lang.Object")) {
                                                                                                obj = xmlSerializerInstance.fromXml(httpServletRequest.getInputStream(), contentLength);
                                                                                        } else {
                                                                                                obj = xmlSerializerInstance.fromXml(httpServletRequest.getInputStream(), contentLength, clazz);
                                                                                        }
                                                                                        params[i] = obj;

                                                                                } else if (contentType != null && contentLength > 0 && isFormUrlEncoded(contentType)) {
                                                                                        Object obj = clazz.newInstance();
                                                                                        populateBean(obj, httpServletRequest);
                                                                                        params[i] = obj;
                                                                                }
                                                                        }
                                                                        i++;
                                                                }

                                                                Object obj = invokeControllerAction(controller, m, params);
                                                                RequestThreadLocal.get().getTemplateInfo().setErrorOccurred(false);

                                                                if (obj != null) {
                                                                        URL f = Thread.currentThread().getContextClassLoader().getResource("./../../WEB-INF/views/" + obj);

                                                                        if (f == null) {
                                                                                throw new NoViewTemplateFound("View template  '" + obj + "' not found!!!");
                                                                        }
                                                                        File requestedViewFile = new File(f.toURI());

                                                                        RequestThreadLocal.get().getTemplateInfo().setRequestUri(obj.toString());
                                                                        RequestThreadLocal.get().getTemplateInfo().setAppFolderClassPathLocation("./../../WEB-INF/views/");
                                                                        RequestThreadLocal.get().getTemplateInfo().setRequestFile(requestedViewFile);

                                                                        TemplateWriter templateWriter = new TemplateWriter(groovyClassLoader);
                                                                        templateWriter.process(httpServletResponse, templateInfo, new CustomServletBinding(context, httpServletRequest, httpServletResponse));
                                                                } else {
                                                                        logger.fine("controller method did not return a 'view' path");
                                                                }
                                                                break;
                                                        }
                                                }

                                                return null;
                                        } catch (java.lang.IllegalArgumentException e) {
                                                logger.log(Level.SEVERE, "controller", e);
                                        } catch (NoMatchingRouteException e) {
                                                httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
                                                throw e;
                                        } catch (Exception e) {
                                                httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                                logger.log(Level.SEVERE, e.getMessage(), e);
                                                throw new RuntimeException(e);
                                        }

                                        return null;
                                }
                        };

                        GroovyCategorySupport.use(CustomServletCategory.class, closure);


                } catch (Exception e) {
                        throw new ServletException(e);
                } finally {
                        RequestThreadLocal.remove();
                }


        }

        private Object invokeControllerAction(GroovyObject controller, Method m, Object[] params) throws InvokeControllerActionException {
                try {
                        return m.invoke(controller, params);
                } catch (Exception e) {
                        throw new InvokeControllerActionException("unable to invoke " + m.getName() + ", expected: " + extractParameterTypes(m.getParameters()) + " sent: " + extractParameterTypes(params));
                }
        }

        public Object populateBean(Object obj, HttpServletRequest request) {

                try {
                        Object value = null;
                        String property = null;

                        BeanInfo sourceInfo = Introspector.getBeanInfo(obj.getClass());
                        PropertyDescriptor[] sourceDescriptors = sourceInfo.getPropertyDescriptors();

                        for (int x = 0; x < sourceDescriptors.length; x++) {
                                try {

                                        if (sourceDescriptors[x].getReadMethod() != null && sourceDescriptors[x].getWriteMethod() != null) {

                                                property = sourceDescriptors[x].getName();
                                                Class params[] = sourceDescriptors[x].getWriteMethod().getParameterTypes();

                                                String val = request.getParameter(property);
                                                if (val != null) {
                                                        value = castToType(val, params[0].getClass());

                                                        if (obj instanceof GroovyObject) {
                                                                MetaClass metaClass = InvokerHelper.getMetaClass(obj);
                                                                metaClass.setProperty(obj, sourceDescriptors[x].getName(), value);
                                                        } else {
                                                                sourceDescriptors[x].getWriteMethod().invoke(obj, new Object[]{value});
                                                        }
                                                }
                                        }
                                } catch (Exception e) {
                                        logger.log(Level.WARNING, "BeanUtil.populate failed. method=" + property + ", value=" + value, e);
                                }
                        }

                } catch (Exception e) {
                        logger.log(Level.SEVERE, "Error occurred populating object from request", e);
                }
                return obj;
        }


        private Object castToType(String valFromRequestParameter, Class clazz) {
                try {
                        if (clazz == Object.class) {
                                return valFromRequestParameter;
                        } else if (clazz.getName().equals("char")) {
                                return new Character(valFromRequestParameter.toCharArray()[0]);
                        } else if (clazz.getName().equals("int")) {
                                return new Integer(valFromRequestParameter);
                        } else if (clazz.getName().equals("long")) {
                                return new Long(valFromRequestParameter);
                        } else if (clazz.getName().equals("double")) {
                                return new Double(valFromRequestParameter);
                        } else if (clazz.getName().equals("float")) {
                                return new Float(valFromRequestParameter);
                        } else if (clazz.getName().equals("short")) {
                                return new Short(valFromRequestParameter);
                        } else if (clazz.getName().equals("boolean")) {
                                return new Boolean(valFromRequestParameter);
                        }

                        Constructor c = clazz.getConstructor(String.class);
                        return c.newInstance(valFromRequestParameter);

                } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                } catch (InvocationTargetException e) {
                        e.printStackTrace();
                } catch (InstantiationException e) {
                        e.printStackTrace();
                } catch (IllegalAccessException e) {
                        e.printStackTrace();
                }
                return null;
        }

        private String extractParameterTypes(Parameter[] parameters) {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("(");
                for (Parameter p : parameters) {
                        stringBuffer.append(p.getType().getName()).append(",");
                }

                if (stringBuffer.length() > 1) {
                        stringBuffer.setLength(stringBuffer.length() - 1);
                }

                stringBuffer.append(")");
                return stringBuffer.toString();
        }

        private String extractParameterTypes(Object[] parameters) {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("(");
                for (Object p : parameters) {
                        if (p == null) {
                                stringBuffer.append("null").append(",");
                        } else {
                                stringBuffer.append(p.getClass().getName()).append("=").append(p).append(",");
                        }
                }

                if (stringBuffer.length() > 1) {
                        stringBuffer.setLength(stringBuffer.length() - 1);
                }

                stringBuffer.append(")");
                return stringBuffer.toString();
        }


        private void loadRoutes() throws IOException, URISyntaxException {

                final Class c = groovyClassLoader.parseClass(new File(this.getClass().getClassLoader().getResource("./routes.groovy").toURI()));

                Closure closure = new Closure(groovyClassLoader) {

                        public Object call() {
                                try {
                                        Binding binding = new Binding();
                                        binding.setVariable("rroutes", routes);
                                        Script routesScript = InvokerHelper.createScript(c, binding);
                                        routesScript.invokeMethod("run", new Object[]{});
                                } catch (Exception e) {
                                        logger.log(Level.SEVERE, "error in routes.groovy", e);
                                }

                                return null;
                        }
                };
                GroovyCategorySupport.use(RoutingCategory.class, closure);
        }


        private Route findRoute(HttpServletRequest servletRequest) {
                for (Route r : routes.values()) {
                        if (r.matches(servletRequest)) {
                                logger.finest("matching route found! " + r.toString());
                                return r;
                        }
                }

                logger.fine("no matching route found.");
                return null;
        }

        public void destroy() {

        }

        private static boolean isJson(String contentType) {
                return contentType.contains("application/json");
        }

        private static boolean isXML(String contentType) {
                return contentType.contains("application/xml");
        }

        private static boolean isFormUrlEncoded(String contentType) {
                return contentType.contains("application/x-www-form-urlencoded");
        }

        private static boolean isMultiPart(String contentType) {
                return contentType.contains("multipart/form-data");
        }

        private String[] extractControllerAndActionName(String contextName, String uri) {
                String[] parts = uri.split("/");
                StringBuffer controller = new StringBuffer();
                String action = "index";

                if (parts.length == 0) {
                        controller.append("Default");
                } else {
                        if (contextName.equals(parts[0])) {
                                controller.append(parts[1]);
                                if (parts.length > 2) {
                                        action = parts[2];
                                }
                        } else {
                                controller.append(parts[0]);
                                if (parts.length > 1) {
                                        action = parts[1];
                                }
                        }
                }
                controller.append("Controller");
                controller.replace(0, 1, String.valueOf(controller.charAt(0)).toUpperCase());

                return new String[]{controller.toString(), action};
        }


        //taken from playframework
        public List<String> lookupParameterNames(Method method) {
                try {
                        List<String> parameters = new ArrayList<String>();

                        ClassPool classPool = new ClassPool();
                        classPool.appendSystemPath();
                        classPool.appendClassPath(new LoaderClassPath(this.getClass().getClassLoader()));

                        CtClass ctClass = classPool.get(method.getDeclaringClass().getName());
                        CtClass[] cc = new CtClass[method.getParameterTypes().length];
                        for (int i = 0; i < method.getParameterTypes().length; i++) {
                                cc[i] = classPool.get(method.getParameterTypes()[i].getName());
                        }
                        CtMethod ctMethod = ctClass.getDeclaredMethod(method.getName(), cc);

                        // Signatures names
                        CodeAttribute codeAttribute = (CodeAttribute) ctMethod.getMethodInfo().getAttribute("Code");
                        if (codeAttribute != null) {
                                LocalVariableAttribute localVariableAttribute = (LocalVariableAttribute) codeAttribute.getAttribute("LocalVariableTable");
                                if (localVariableAttribute != null && localVariableAttribute.tableLength() >= ctMethod.getParameterTypes().length) {
                                        for (int i = 0; i < ctMethod.getParameterTypes().length + 1; i++) {
                                                String name = localVariableAttribute.getConstPool().getUtf8Info(localVariableAttribute.nameIndex(i));
                                                if (!name.equals("this")) {
                                                        parameters.add(name);
                                                }
                                        }
                                }
                        }

                        return parameters;
                } catch (Exception e) {
                        throw new RuntimeException(e);
                }
        }

        public static File toFile(String path) throws URISyntaxException {
                URL f = Thread.currentThread().getContextClassLoader().getResource(path);
                return new File(f.toURI());
        }
}
