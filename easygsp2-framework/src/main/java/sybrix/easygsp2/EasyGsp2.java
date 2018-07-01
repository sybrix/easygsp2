package sybrix.easygsp2;

import groovy.json.JsonSlurper;
import groovy.lang.*;
import groovy.util.GroovyScriptEngine;
import groovy.util.XmlSlurper;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.GroovyCategorySupport;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeElementsScanner;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import sybrix.easygsp2.anno.Api;
import sybrix.easygsp2.anno.Content;
import sybrix.easygsp2.anno.Secured;
import sybrix.easygsp2.categories.RoutingCategory;
import sybrix.easygsp2.controllers.JwtController;
import sybrix.easygsp2.data.Serializer;
import sybrix.easygsp2.data.XmlSerializer;
import sybrix.easygsp2.db.firebird.Model;
import sybrix.easygsp2.exceptions.*;
import sybrix.easygsp2.fileupload.FileUpload;
import sybrix.easygsp2.framework.*;
import sybrix.easygsp2.http.MediaType;
import sybrix.easygsp2.routing.MethodAndRole;
import sybrix.easygsp2.routing.Routes;
import sybrix.easygsp2.routing.UrlParameter;
import sybrix.easygsp2.security.EasyGspServletRequest;
import sybrix.easygsp2.security.JwtUtil;
import sybrix.easygsp2.templates.RequestError;
import sybrix.easygsp2.templates.TemplateInfo;
import sybrix.easygsp2.templates.TemplateWriter;
import sybrix.easygsp2.util.PropertiesFile;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sybrix.easygsp2.util.StringUtil;

/**
 * Created by dsmith on 7/19/16.
 */
public class EasyGsp2 {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(EasyGsp2.class);

    private static final String JSON_SERIALIZER_CLASS = "sybrix.easygsp2.data.JsonSerializerImpl";
    private static final String XML_SERIALIZER_CLASS = "sybrix.easygsp2.data.XmlSerializerImpl";
    public static final String ROUTE_REQUEST_ATTR = "__route__";

    public static final MimeType APPLICATION_JSON = parseMimeType("application/json");
    public static final MimeType APPLICATION_XML = parseMimeType("application/xml");
    public static final MimeType APPLICATION_HTML_TEXT = parseMimeType("text/html");

    private static Map<String, sybrix.easygsp2.routing.Route> routes = new HashMap<String, sybrix.easygsp2.routing.Route>();
    private GroovyClassLoader groovyClassLoader;
    private PropertiesFile propertiesFile;
    private ServletContext context;

    protected Serializer jsonSerializerInstance;
    protected XmlSerializer xmlSerializerInstance;
    protected List<Pattern> ignoredUrlPatterns;

    private Set<String> classesWithApiAnnotation = new HashSet<String>();
    private Map<String, String> methods = new HashMap<String, String>();
    private boolean isServlet = false;
    private AppListener appListener;

    private Validator validator;

    static {

    }

    public EasyGsp2(FilterConfig config) {
        init(config.getServletContext(), config);
    }

    public EasyGsp2(ServletConfig config) {
        init(config.getServletContext(), config);
    }

    public void init(ServletContext servletContext, Object config) {
        try {

            System.setProperty("easygsp.version", "@easygsp_version");

            logger.info(
                    "\nEASYGSP_VERSION: " + System.getProperty("easygsp.version") +
                            "\nJRE_HOME: " + System.getProperty("java.home") +
                            "\nJAVA_VERSION: " + System.getProperty("java.version") +
                            "\nGROOVY_VERSION: " + GroovySystem.getVersion() +
                            "\n"
            );
            propertiesFile = new PropertiesFile("classPath:easygsp.properties");
            this.context = servletContext;
            if (config instanceof ServletConfig) {
                isServlet = true;
            }

            // delete tmp dir
            File folder = new File(propertiesFile.getString("easygsp.tmp.dir", System.getProperty("java.io.tmpdir")));
            if (folder.exists()) {
                    FileUtils.cleanDirectory(folder);
            } else{
                    folder.mkdirs();
            }

            CompilerConfiguration configuration = new CompilerConfiguration();
            configuration.setTargetDirectory(getCompiledClassesDir());
            //configuration.setRecompileGroovySource(propertiesFile.getString("mode", "dev1").equalsIgnoreCase("dev"));

            groovyClassLoader = new GroovyClassLoader(this.getClass().getClassLoader(), configuration);
            //groovyClassLoader.addURL(getSourcePath(propertiesFile));

            //useImplicitControllerObjects = Boolean.parseBoolean(propertiesFile.getString("implicit.http.objects", "true"));
            ignoredUrlPatterns = new ArrayList<Pattern>();

            ThreadBag.set(new ThreadVariables(this.context, null, null, routes, null, null, groovyClassLoader));
            loadDefaultRoutes();
            loadApiMethods(propertiesFile);
            loadUnannotatedClasses(propertiesFile);
            loadSerializers(propertiesFile);
            loadPropertiesIntoContext(servletContext, propertiesFile);
            loadJwtValues(propertiesFile);

            String appListenerClass = null;
            if (config instanceof ServletConfig) {
                appListenerClass = ((ServletConfig) config).getInitParameter("appListener");
            } else if (config instanceof FilterConfig) {
                appListenerClass = ((FilterConfig) config).getInitParameter("appListener");
            }
            // load DB String queries
            ThreadBag.set(new ThreadVariables(servletContext));
            Model.initStringQueries();

            if (appListenerClass != null) {
                Class cls = Class.forName(appListenerClass, false, groovyClassLoader);
                appListener = (AppListener) cls.newInstance();
                appListener.onApplicationStart(servletContext);
            }

            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            validator = factory.getValidator();

        } catch (Throwable e) {
            logger.error("error occurred easygsp init()", e);
        } finally {
            ThreadBag.remove();
        }
    }

    private void loadDefaultRoutes() {
        Routes.add("GET", "/token", JwtController.class, "generateToken", new Class[]{HttpServletRequest.class,HttpServletResponse.class}, null, new String[]{MediaType.JSON}, new String[]{MediaType.JSON});
    }

    private void loadJwtValues(PropertiesFile propertiesFile) {

        JwtUtil jwtUtil = new JwtUtil(propertiesFile.getString("jwt.alg","HS256"));
        String key = propertiesFile.getString("jwt.key");
        if (key == null){
                logger.info("no jwt key found. generating key....");
                key = jwtUtil.generateKey();
                logger.info("generated jwt key, HS256: "+ key);
        }

        jwtUtil.loadKey(key);
        JwtUtil.instance = jwtUtil;

    }

    protected AppListener getAppListener() {
        return appListener;
    }

    public String getCompiledClassesDir() {
        try {
            File f = new File(this.getClass().getClassLoader().getResource("./../../WEB-INF/classes").toURI());
            logger.info("compiled classed dir: " + f.getAbsolutePath());
            return f.getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException("failed on getCompiledClassesDir()", e);
        }
    }

    private URL getSourcePath(PropertiesFile propertiesFile) {
        String dir = propertiesFile.getProperty("source.dir", null);
        URL url = null;
        try {
            if (dir != null) {
                url = new File(dir).toURI().toURL();
            } else {
                url = this.getClass().getClassLoader().getResource("./../../");
            }

            logger.info("source path: " + url.getFile());
            return url;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Failed to determine source directory.  " + e.getMessage(), e);
        }
    }

    public boolean doRequest(final EasyGspServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) throws ServletException {

        final String uri = httpServletRequest.getRequestURI();

        logger.debug("processing web request, method: " + httpServletRequest.getMethod() + ", uri: " + uri);
        Boolean continueFilterChain = false;

        try {

            for (Pattern s : ignoredUrlPatterns) {
                if (s.matcher(uri).matches()) {
                    logger.info("url: " + uri + " found on ignore list");
                    return true;
                }
            }

            // default it to null, if you set it we'll know
            httpServletResponse.setContentType(null);

            final boolean isMultiPart = isMultiPart(httpServletRequest.getContentType());
            final List<FileItem> uploads = FileUpload.parseFileUploads(httpServletRequest, propertiesFile);

            ThreadBag.set(new ThreadVariables(this.context, httpServletRequest, httpServletResponse, routes, null, null, groovyClassLoader));
            logger.debug("searching for matching route for uri: " + uri);

            final sybrix.easygsp2.routing.Route route = findRoute(httpServletRequest);
            if (route == null) {
                throw new NoMatchingRouteException("No route found for: " + uri);
            }

            Closure closure = new Closure(groovyClassLoader) {

                Class controllerClass = null;

                public Object call() {
                    try {

                        //String[] controllerAndActionName = extractControllerAndActionName("", uri);
                        //String controllerName = controllerAndActionName[0];
                        // String actionName = controllerAndActionName[1];
                        String contentType = httpServletRequest.getContentType();
                        int contentLength = httpServletRequest.getContentLength();


                        Method m = null;

                        controllerClass = route.getControllerClass();
                        m = route.getControllerMethod();


                        GroovyObject controller = (GroovyObject) controllerClass.newInstance();

//                                                if (useImplicitControllerObjects) {
//                                                        logger.info("adding implicit controllers objects (request, response, session, params)");
//                                                        ImplicitObjectInjector.addMetaProperties(controllers);
//                                                }

                        List<String> s = lookupParameterNames(m, groovyClassLoader);
                        Parameter[] parameters = m.getParameters();

                        logger.debug("invoking method " + controllerClass.getName() + "." + m.getName() + "(), parameters: " + extractParameterTypes(parameters));
                        logger.debug("parameter names: " + s);

                        Object[] params = new Object[parameters.length];

                        int i = 0;
                        for (Parameter p : parameters) {
                            String parameterName = p.getName();
                            if (s.size() == parameters.length) {
                                parameterName = s.get(i);
                            }

                            Class clazz = p.getType();
                            Type genericType = extractGenericType(p);

                            if (clazz == javax.servlet.http.HttpServletRequest.class || parameterName.equalsIgnoreCase("request")) {
                                params[i] = httpServletRequest;
                            } else if (clazz == HttpServletResponse.class || parameterName.equalsIgnoreCase("response")) {
                                params[i] = httpServletResponse;
                            } else if ((clazz == List.class && genericType == FileItem.class) || parameterName.equalsIgnoreCase("uploads") && isMultiPart) {
                                params[i] = uploads;
                            } else if ((clazz == FileItem.class) && isMultiPart) {
                                params[i] = getFileItemByName(uploads, parameterName);
                            } else {

                                String valFromRequestParameter = null;

                                if (route.getParameters().size() > 0) {
                                    UrlParameter urlParameter = route.getParameters().get(parameterName);
                                    if (urlParameter.getValue() != null) {
                                        valFromRequestParameter = urlParameter.getValue();
                                    } else if (valFromRequestParameter == null) {
                                        valFromRequestParameter = httpServletRequest.getParameter(parameterName);
                                    }
                                }

                                if (valFromRequestParameter != null) {
                                    params[i] = castToType(valFromRequestParameter, clazz);

                                } else if (contentLength > 0 && isJson(contentType)) {

                                    Object obj = null;
                                    if (clazz.getTypeName().equals("java.lang.Object")) {
                                        obj = jsonSerializerInstance.parse(httpServletRequest.getInputStream(), contentLength);
                                    } else {

                                        obj = jsonSerializerInstance.parse(httpServletRequest.getInputStream(), contentLength, clazz, genericType);
                                    }
                                    params[i] = obj;

                                } else if (contentLength > 0 && isXML(contentType)) {
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

                        List errors = validateParameters(params);
                        if (errors.size() > 0) {
                            throw new BadRequestException(errors);
                        }

                        Object obj = invokeControllerAction(controller, m, params, route);
                        logger.debug("controllers returned: " + obj);

                        Object redirectIssued = httpServletRequest.getAttribute("__redirect__");

                        if (Boolean.TRUE.equals(redirectIssued)) {
                            return false;
                        }

                        boolean isApiController = isApiController(controllerClass);

                        MimeType returnContentType = null;
                        if (httpServletResponse.getContentType() == null) {
                            returnContentType = calculateContentType(obj, route, httpServletResponse, httpServletRequest, isApiController);
                            if (!isContentTypeAccepted(httpServletRequest, returnContentType)) {
                                httpServletResponse.setContentType(null);
                                throw new NoMatchingAcceptHeaderExcepton();
                            } else {
                                httpServletResponse.setContentType(returnContentType.toString());
                            }
                        } else {
                            returnContentType = new MimeType(httpServletResponse.getContentType());
                        }

                        logger.debug("return content type: " + returnContentType.toString());

                        if (returnContentType.getBaseType().equals(APPLICATION_HTML_TEXT.getBaseType())) {
                            logger.debug("processing view " + obj);
                            ThreadBag.get().getTemplateInfo().setErrorOccurred(false);
                            return processControllerResponse(obj, httpServletResponse, httpServletRequest);

                        } else {
                            if (returnContentType.getBaseType().equals(APPLICATION_JSON.getBaseType())) {
                                //httpServletResponse.flushBuffer();
                                if (obj != null) {
                                    logger.debug("sending json response to client");
                                    jsonSerializerInstance.write(obj, httpServletResponse);
                                    //httpServletResponse.flushBuffer();
                                } else {
                                    logger.debug("controllers method returned null object, that's a 200 OK");
                                }
                            }
                        }

                        return false;
                    } catch (BadRequestException e) {
                        try {
                            boolean isApiController = isApiController(controllerClass);
                            MimeType returnContentType = calculateContentType(null, route, httpServletResponse, httpServletRequest, isApiController);
                            httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            String msg = null;

                            if (returnContentType.getBaseType().equals(APPLICATION_XML.getBaseType())) {
                                //msg = xmlSerializerInstance.toString(e.getConstraintErrors());
                                logger.error(" IMPLEMENT XML toString on Serializer");
                            } else {
                                msg = jsonSerializerInstance.toString(e.getConstraintErrors());
                            }
                            httpServletResponse.getOutputStream().write(msg.getBytes());

                        } catch (Exception x) {
                            logger.debug("error occurred writing back to client on bad request", e);
                        }

                        return false;
                    } catch (java.lang.IllegalArgumentException e) {
                        logger.error("controllers", e);
                    } catch (NoMatchingRouteException e) {
                        httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        throw e;
                    } catch (NoMatchingAcceptHeaderExcepton e) {
                        httpServletResponse.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
                        throw e;

                    } catch (Exception e) {
                        httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        logger.error(e.getMessage(), e);
                        throw new RuntimeException(e);
                    }

                    return null;
                }
            };

            continueFilterChain = (Boolean) GroovyCategorySupport.use(CustomServletCategory.class, closure);

        } catch (NoMatchingRouteException e) {
            httpServletResponse.setStatus(404);
            continueFilterChain = false;
        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            try {
                if (appListener != null) {
                    appListener.onRequestEnd(httpServletRequest);
                }
            } catch (Throwable e) {

            }
            ThreadBag.remove();
        }

        return continueFilterChain;
    }

    private List<ValidationMessage> validateParameters(Object[] params) {

        List constraintViolations = new ArrayList();

        for (Object o : params) {
            if (o instanceof ServletContext || o instanceof FileItem || o instanceof ServletRequest || o instanceof ServletResponse) {
                continue;
            }

            Set _constraintViolations = validator.validate(o);
            for (Object c : _constraintViolations) {
                ConstraintViolation constraintViolation = ((ConstraintViolation) c);
                ValidationMessage vm = new ValidationMessage(constraintViolation.getPropertyPath().toString(), constraintViolation.getMessage());
                constraintViolations.add(vm);
            }
        }

        return constraintViolations;
    }

    private MimeType calculateContentType(Object obj, sybrix.easygsp2.routing.Route route, HttpServletResponse response, HttpServletRequest request, Boolean isApiCall) throws MimeTypeParseException {
        // if there's only one, the use it
        if (route.getReturns().length == 1) {
            return new MimeType(route.getReturns()[0]);
        } else if (route.getReturns().length == 0 && obj instanceof String) {
            return APPLICATION_HTML_TEXT;
        }

        // if you set it manually use it
        if (response.getContentType() != null && response.getContentType().length() > 0) {
            return new MimeType(response.getContentType());
        }

        MimeType defaultMimeType = null;
        if (isApiCall) {
            defaultMimeType = new MimeType(propertiesFile.getString("api.default.response.contentType", "application/json"));
        } else {
            defaultMimeType = new MimeType(propertiesFile.getString("default.response.contentType", "text/html"));
        }

        // if you don't set, figure it out based on what you returned from the method
        if (route.getReturns().length == 0) {
            if (obj instanceof JsonSlurper || obj instanceof Collection) {
                return APPLICATION_JSON;
            } else if (obj instanceof XmlSlurper) {
                return APPLICATION_XML;
            } else {
                if (defaultMimeType.getBaseType().equals(APPLICATION_JSON.getBaseType())) {
                    return APPLICATION_JSON;
                } else if (defaultMimeType.getBaseType().equals(APPLICATION_XML.getBaseType())) {
                    return APPLICATION_XML;
                } else {
                    return defaultMimeType;
                }
            }
        }

        return defaultMimeType;
    }

    //        private boolean isContentTypeAccepted(){
//
//                String accept = request.getHeader("Accept");
//                String[] acceptsHeader = {};
//
//                if (accept != null) {
//                        acceptsHeader = accept.split(",");
//                }
//
//                // no accepts header
//                if (acceptsHeader == null) {
//                        acceptsHeader = route.getReturns().length == 0 ? defaultContentType.split(",") : route.getReturns()[0].split(",");
//                }
//
//                String[] returns = route.getReturns();
//                String[] returnContentType = null;
//                if (returns.length == 0) {
//                        returnContentType = defaultContentType.split(",");
//                }
//
//
//                MimeType mimeType = findMimeTypeMatch(acceptsHeader, returnContentType);
//
//                if (mimeType != null) {
//                        response.setContentType(mimeType.toString());
//                        return mimeType.toString();
//                } else {
//                        logger.info("unable to determine response content type");
//                        return null;
//                }
//
//        }
    private boolean isContentTypeAccepted(HttpServletRequest request, MimeType soughtMimeType) {
        try {
            if (request.getHeader("Accept") == null) {
                return false;
            }

            for (String r : request.getHeader("Accept").split(",")) {
                MimeType mimeType = new MimeType(r);

                if (soughtMimeType.match(mimeType) || mimeType.getPrimaryType().equals("*")) {
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            logger.debug(e.getMessage(), e);
            return false;
        }

    }

    private MimeType findMimeTypeMatch(String[] acceptsHeader, String[] returnContentType) {
        try {
            if (returnContentType == null) {
                return null;
            }
            for (String r : returnContentType) {
                MimeType returnMime = new MimeType(r);
                for (String a : acceptsHeader) {
                    if (returnMime.match(a)) {
                        return returnMime;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private boolean compare(String s1, String s2) {
        return s1.trim().toLowerCase().equals(s2.trim().toLowerCase());
    }

    private FileItem getFileItemByName(List<FileItem> uploads, String parameterName) {
        for (FileItem i : uploads) {
            if (i.getFieldName().equals(parameterName)) {
                return i;
            }
        }

        return null;
    }

    private Type extractGenericType(Parameter p) {
        try {
            return ((ParameterizedTypeImpl) p.getParameterizedType()).getActualTypeArguments()[0];
        } catch (Exception e) {
            return null;
        }
    }

    private boolean processControllerResponse(Object obj, HttpServletResponse httpServletResponse, HttpServletRequest httpServletRequest) throws URISyntaxException, IOException, ServletException {
        ThreadBag.get().setTemplateRequest(true);
        Binding binding = ThreadBag.get().getBinding();

        if (binding == null) {
            binding = new CustomServletBinding(context, httpServletRequest, httpServletResponse);
        }

        if (obj.toString().endsWith(".jsp") && isServlet) {

            RequestDispatcher rd = httpServletRequest.getServletContext().getRequestDispatcher("/indexs.jsp");
            rd.forward(httpServletRequest, httpServletResponse);

        } else if (obj.toString().endsWith(".jsp") && isServlet == false) {
            RequestDispatcher rd = httpServletRequest.getServletContext().getRequestDispatcher("/indexs.jsp");
            rd.forward(httpServletRequest, httpServletResponse);
            return true;
        } else if (!obj.toString().endsWith(".jsp")) {

            URL f = Thread.currentThread().getContextClassLoader().getResource("./../../WEB-INF/views/" + obj);

            if (f == null) {
                throw new NoViewTemplateFound("View template  '" + obj.toString() + "' not found!!!");
            }

            ThreadBag.get().setViewFolder(f.getPath());
            File requestedViewFile = new File(f.toURI());
            ThreadBag.get().setTemplateFilePath(requestedViewFile.getAbsolutePath());

            ThreadBag.get().getTemplateInfo().setRequestUri(obj.toString());
            ThreadBag.get().getTemplateInfo().setAppFolderClassPathLocation("./../../WEB-INF/views/");
            ThreadBag.get().getTemplateInfo().setRequestFile(requestedViewFile);

            try {
                TemplateWriter templateWriter = new TemplateWriter(groovyClassLoader);
                templateWriter.process(httpServletResponse, ThreadBag.get().getTemplateInfo(), binding);
                return false;

            } catch (Throwable e) {
                logger.debug(e.getMessage(), e);
                sendError(500, binding, e.getCause());
                return false;
            }

            //return true;
        }

        return true;
    }

    private static void doNameCheckWarning(String requestURI) {
        if (requestURI.endsWith("session.gspx") || requestURI.endsWith("app.gspx") || requestURI.endsWith("response.gspx") || requestURI.endsWith("request.gspx") || requestURI.endsWith("context.gspx") || requestURI.endsWith("application.gspx")) {

            logger.debug("warning - pages(" + requestURI + ") named the same as implicit objects could produce errors");
        }
    }


    protected void sendError(int errorCode, Binding binding, Throwable e) {
        try {

            ThreadBag.get().getTemplateInfo().setErrorOccurred(true);
            try {
              //  ThreadBag.get().getResponse().sendError(errorCode, e.getMessage());
            } catch (IllegalStateException il) {

            }

            RequestError requestError = ThreadBag.get().getRequestError();
            requestError.setErrorCode(errorCode);
            requestError.setScriptPath(ThreadBag.get().getTemplateFilePath());
            requestError.setErrorMessage(e.getMessage() == null ? "" : e.getMessage());
            requestError.setException(e, "", "");

            StackTraceElement stackTraceElement = findErrorInStackTrace(binding, e);
            String errorSource = highlightErrorSource(stackTraceElement);
            requestError.setSource(errorSource.toString());

            String path = ThreadBag.get().getTemplateFilePath();//.replace(appPath, "").replace(appPath2, "");
            if (stackTraceElement != null) {

                String lineNumberMessage = "Error occurred in <span class=\"path\">" + path + "</span> @ lineNumber: " + (stackTraceElement.getLineNumber());
                requestError.setLineNumberMessage(lineNumberMessage);
                requestError.setLineNumber(stackTraceElement.getLineNumber());
            } else {
                requestError.setLineNumberMessage("");
                if (ThreadBag.get().isTemplateRequest()) {
                    String lineNumberMessage = "Error occurred in <span class=\"path\">" + path + "</span>";
                    requestError.setLineNumberMessage(lineNumberMessage);
                }
                requestError.setLineNumber(-1);
            }

            File errorFile = getErrorFileContents(errorCode);
            ThreadBag.get().getTemplateInfo().setRequestFile(errorFile);

            binding.setVariable("exceptionName", e.getClass().getName());
            binding.setVariable("requestError", requestError);
            processErrorTemplateRequest(ThreadBag.get().getResponse(), ThreadBag.get().getTemplateInfo(), binding);

            ThreadBag.get().getResponse().flushBuffer();

        } catch (Throwable e1) {
            logger.debug(e1.getMessage(), e1);
            //sendError(errorCode, response, e, stackTraceElement, binding);
        }
    }

    private String highlightErrorSource(StackTraceElement stackTraceElement) {
        StringBuffer lineBuffer = new StringBuffer();

        if (stackTraceElement != null) {
            try {
                File f = new File(ThreadBag.get().getTemplateFilePath());
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line = null;

                int lineCt = 1;

                while ((line = br.readLine()) != null) {
                    if (lineCt >= stackTraceElement.getLineNumber() - 10 && (lineCt <= stackTraceElement.getLineNumber() + 10)) {
                        lineBuffer.append("<div class=\"sourceLine\"><div class=\"sourceLineNumber\">").append(lineCt).append("</div>");
                        lineBuffer.append("<div class=\"sourceCode\">");
                        lineBuffer.append(line.length() == 0 ? "&nbsp;" : StringUtil.htmlEncode(line).replaceAll(" ", "&nbsp;"));
                        lineBuffer.append("</div></div>");
                    }

                    lineCt++;
                }

                br.close();
            } catch (Exception ex) {
                logger.debug("Unable to parse source code to display on error page", ex);
            }
        }

        return lineBuffer.toString();
    }

    private File getErrorFileContents(int errorCode) throws IOException {
        String defaultTemplateExtension = propertiesFile.getString("default.template.extension", "gsp");
        String tmpDir = propertiesFile.getString("easygsp.tmp.dir", System.getProperty("java.io.tmpdir"));

        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("./errors/error" + errorCode + "." + defaultTemplateExtension);
        File errorFile = new File(tmpDir + File.separator + "errors" + File.separator + "error" + errorCode + "." + defaultTemplateExtension);

        // createFile if it doesn't exist
        if (!errorFile.exists()) {
            new File(tmpDir + File.separator + "errors").mkdir();
            FileOutputStream outputStream = new FileOutputStream(errorFile);

            byte[] buff = new byte[1024];

            while (true) {
                int len = inputStream.read(buff, 0, buff.length);
                if (len < 0)
                    break;
                outputStream.write(buff, 0, len);
            }
            outputStream.close();
        }

        return errorFile;
    }

    private void processErrorTemplateRequest(HttpServletResponse httpServletResponse, TemplateInfo templateInfo, Binding binding) {
        try {
            TemplateWriter templateWriter = new TemplateWriter(groovyClassLoader);
            templateWriter.process(httpServletResponse, templateInfo, binding);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static StackTraceElement findErrorInStackTrace(Binding binding, Throwable e) {

        if (ThreadBag.get().isTemplateRequest()) {
            if (e.getStackTrace() != null && ThreadBag.get().getTemplateInfo() != null) {

                for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                    if (stackTraceElement.getFileName() != null) {
                        if (stackTraceElement.getFileName().startsWith(ThreadBag.get().getTemplateInfo().getUniqueTemplateScriptName())) {
                            return stackTraceElement;
                        }
                    }
                }
            }
        } else {
//                        String appPath = RequestThreadInfo.get().getApplication().getAppPath();
//                        String currentFile = ThreadBag.get().getTemplateFilePath().substring(appPath.length() + 1);
//
//                        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
//                                if (stackTraceElement.getFileName() != null) {
//                                        if (stackTraceElement.getFileName().endsWith(currentFile)) {
//                                                return stackTraceElement;
//                                        }
//                                }
//                        }
        }


        return null;
    }


    private void loadPropertiesIntoContext(ServletContext app, PropertiesFile propertiesFile) {
        for (String s : propertiesFile.stringPropertyNames()) {
            app.setAttribute(s, propertiesFile.get(s));

            if (s.matches("ignore\\.url\\.pattern\\.\\d+$")) {
                ignoredUrlPatterns.add(Pattern.compile(propertiesFile.getString(s)));
            }
        }
    }

    private void loadUnannotatedClasses(PropertiesFile propertiesFile) {
        logger.debug("loading unannotated controllers classes");
        String defaultPackageName = propertiesFile.getString("controllers.package");

        Reflections reflections = new Reflections(defaultPackageName, new TypeElementsScanner(), new SubTypesScanner(false));
        Set<String> types = reflections.getAllTypes();

        for (String cls : types) {
            if (classesWithApiAnnotation.contains(cls)) {
                //    continue;
            }

            if (cls.endsWith("Controller")) {
                logger.debug("parsing class: " + cls);
                StringBuffer sb = new StringBuffer();

                String _api = cls.substring(defaultPackageName.length() + 1, cls.length() - "Controller".length());
                for (String s : _api.split("\\.")) {
                    sb.append(s.substring(0, 1).toLowerCase());
                    sb.append(s.substring(1, s.length()));
                    sb.append("/");
                }
                String api = sb.substring(0, sb.length() - 1);
                String pattern = "/" + api;

                Class clazz = parseClassName(cls);

                String[] classMethods = {"list", "index", "get", "put", "post", "delete"};

                if (clazz != null) {
                    String mediaType[] = {propertiesFile.getString("default.response.contentType", MediaType.HTML)};

                    String[] accepts = mediaType;
                    String[] returns = determineDefaultReturnType(clazz);
                    boolean isApiController = isApiController(clazz);

                    Secured securedAnno = (Secured) clazz.getAnnotation(Secured.class);
                    String[] classRoles = securedAnno == null ? new String[]{} : securedAnno.value();

                    for (String classMethod : classMethods) {
                        String _pattern = pattern;
                        String _method = classMethod;

                        if (classMethod.equalsIgnoreCase("list")) {
                            if (isApiController) {
                                _method = "list";
                            } else {
                                _method = "index";
                            }
                        }

                        if (classMethod.equalsIgnoreCase("DELETE") || classMethod.equalsIgnoreCase("GET")) {
                            _pattern = pattern + "/{id}";
                        }

                        String httpMethod = classMethod.equalsIgnoreCase("list") ? "GET" : classMethod.toUpperCase();

                        if (!Routes.contains(_pattern, httpMethod)) {
                            MethodAndRole method = extractMethod(clazz, _method);
                            if (method.getMethod() != null) {
                                String[] roles = combine(method.getRoles(), classRoles);

                                Routes.add(method.getMethod(), _pattern, httpMethod, new Class[]{}, roles, accepts, returns);
                            }
                        }
                    }


//                                Method get = extractMethod(clazz, "get");
//                                addRoute(routes, get, "/" + api + "/{id}", "GET", new Class[]{}, roles, accepts, returns);
//
//                                Method put = extractMethod(clazz, "put");
//                                addRoute(routes, put, "/" + api + "/{id}", "PUT", new Class[]{}, roles, accepts, returns);
//
//                                Method delete = extractMethod(clazz, "delete");
//                                addRoute(routes, delete, "/" + api + "/{id}", "DELETE", new Class[]{}, roles, accepts, returns);
//
//                                Method post = extractMethod(clazz, "post");
//                                addRoute(routes, post, "/" + api + "/{id}", "POST", new Class[]{}, roles, accepts, returns);
                }
            } else {
                logger.warn("class: " + cls + " doesn't end with 'Controller', skipping as controllers");
            }
        }
    }

    private boolean isApiController(Class clazz) {

        String apiControllerPackage = propertiesFile.getString("api.controllers.package", "");

        if (clazz.getPackage().getName().equals(apiControllerPackage)) {
            return true;
        }

        return false;
    }

    private String[] determineDefaultReturnType(Class clazz) {
        Content content = null;
        try {
            content = (Content) clazz.getAnnotation(Content.class);
            if (content != null) {
                String returnType = content.returns();

                if (returnType != null) {
                    return new String[]{returnType};
                }
            }
        } catch (Exception e) {
            logger.debug("unable to get Content annotation form class " + clazz.getName());
        }

        String controllerPackage = propertiesFile.getString("controllers.package", "");
        String apiControllerPackage = propertiesFile.getString("api.controllers.package", "");

        if (clazz.getPackage().getName().equals(controllerPackage)) {
            return new String[]{propertiesFile.getString("default.response.contentType")};
        } else if (clazz.getPackage().getName().equals(apiControllerPackage)) {
            return new String[]{propertiesFile.getString("api.default.response.contentType")};
        }

        return new String[]{};
    }

    private Class parseClassName(String cls) {
        try {
            return Class.forName(cls);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private MethodAndRole extractMethod(Class cls, String methodName) {
        MethodAndRole methodAndRole = new MethodAndRole();

        try {
            Method foundMethod = null;
            for (Method m : cls.getDeclaredMethods()) {
                if (m.getName().equals(methodName)) {
                    if (foundMethod == null) {
                        foundMethod = m;
                    } else {
                        logger.error("method " + methodName + "() found in class: " + cls.getName() + ", not allowed in when using routing by naming convention only.  Use @api annotation instead.");
                        break;
                    }
                }
            }

            if (foundMethod == null) {
                return methodAndRole;
            }

            Method m = foundMethod;
            methodAndRole.setMethod(m);

            Annotation annotation = m.getDeclaredAnnotation(Secured.class);
            if (annotation != null) {
                String[] roles = ((Secured) annotation).value();
                methodAndRole.setRoles(roles);
            }

        } catch (Exception e) {
            logger.debug(methodName.toLowerCase() + "() method not found in class " + cls.getName());
            return methodAndRole;
        }

        return methodAndRole;
    }

    private void loadApiMethods(PropertiesFile propertiesFile) {
        logger.debug("loading annotated controllers methods...");
        Reflections reflections = new Reflections("", new MethodAnnotationsScanner(), new SubTypesScanner());
        Set<Method> methods = reflections.getMethodsAnnotatedWith(Api.class);

        for (Method m : methods) {


            Api classAnno = extractClassAnnotation(m.getDeclaringClass().getDeclaredAnnotations());
            classesWithApiAnnotation.add(m.getDeclaringClass().getName());

            Api anno = m.getDeclaredAnnotation(Api.class);
            if (anno.url().length > 0) {
                logger.debug("loading class: " + m.getDeclaringClass().getName() + ", method: " + m.getName() + "(), url: " + anno.url());

                for (String classPattern : classAnno.url()) {
                    for (String _pattern : anno.url()) {

                        String pattern = classPattern + _pattern;

                        String[] httpMethods = combine(anno.method(), classAnno.method());
                        for (String httpMethod : httpMethods) {
                            if (httpMethod.equals("*")) {
                                httpMethods = sybrix.easygsp2.routing.Route.HTTP_METHODS;
                                break;
                            }
                        }

                        for (String httpMethod : httpMethods) {
                            String[] roles = combine(anno.roles(), classAnno.roles());
                            String[] accepts = anno.accepts().equals("") ? classAnno.accepts() : anno.accepts();
                            String[] returns = anno.contentType().equals("") ? classAnno.contentType() : anno.contentType();

                            Routes.add(m, pattern, httpMethod, null, roles, accepts, returns);
                        }
                    }
                }

            } else {
                logger.info("no patterns found for method " + m);
            }
        }
    }

    private String[] combine(String[] s1, String[] s2) {
        HashSet<String> data = new HashSet<String>();

        for (String s : s1) {
            data.add(s);
        }

        for (String s : s2) {
            data.add(s);
        }

        data.remove("");

        return data.toArray(new String[data.size()]);
    }

    private Api extractClassAnnotation(Annotation[] declaredAnnotations) {

        for (Annotation declaredAnnotation : declaredAnnotations) {
            if (declaredAnnotation instanceof Api) {
                return (Api) declaredAnnotation;
            }
        }

        return _dummy.class.getDeclaredAnnotation(Api.class);
    }


    private Set<String> toList(String[] methods) {
        Set<String> l = new HashSet<String>();
        if (methods != null) {
            for (String m : methods) {
                l.add(m);
            }
        }
        return l;
    }

    private void loadSerializers(PropertiesFile propertiesFile) {
        String jsonSerializer = null;
        String xmlSerializer = null;

        try {
            jsonSerializer = propertiesFile.getString("json.serializer.class", JSON_SERIALIZER_CLASS);
            logger.debug("JsonSerializer: " + jsonSerializer);
            jsonSerializerInstance = (Serializer) Class.forName(jsonSerializer, false, groovyClassLoader).newInstance();
        } catch (Exception e) {
            if (JSON_SERIALIZER_CLASS.equalsIgnoreCase(jsonSerializer)) {
                logger.error("Unable to instantiate default json serializer: " + JSON_SERIALIZER_CLASS);
            } else {
                logger.warn("error occurred instantiating jsonSerializer: " + jsonSerializer + ", attempting to use default: " + JSON_SERIALIZER_CLASS);

                try {
                    jsonSerializerInstance = (Serializer) Class.forName(jsonSerializer, false, groovyClassLoader).newInstance();
                } catch (Exception e1) {
                    logger.error("Unable to instantiate default json serializer: " + JSON_SERIALIZER_CLASS);
                }
            }
        }

        try {
            xmlSerializer = propertiesFile.getString("xml.serializer.class", XML_SERIALIZER_CLASS);
            logger.debug("XmlSerializer: " + xmlSerializer);
            xmlSerializerInstance = (XmlSerializer) Class.forName(xmlSerializer, false, groovyClassLoader).newInstance();
        } catch (Exception e) {
            logger.warn("error occurred instantiating xmlSerializer: " + xmlSerializer + ", attempting to use default: " + XML_SERIALIZER_CLASS);

            try {
                xmlSerializerInstance = (XmlSerializer) Class.forName(xmlSerializer, false, groovyClassLoader).newInstance();
            } catch (Exception e1) {
                logger.error("Unable to instantiate default xml serializer: " + XML_SERIALIZER_CLASS);
            }

        }
    }

    private Object invokeControllerAction(GroovyObject controller, Method m, Object[] params, sybrix.easygsp2.routing.Route route) throws InvokeControllerActionException {
        try {
            return m.invoke(controller, params);
        } catch (Exception e) {
            if (route.isDuplicate()) {
                logger.error("url pattern: " + route.getPath() + " exists on multiple methods.");
            }
            throw new InvokeControllerActionException("unable to invoke method " + m.getDeclaringClass().getName() + "." + m.getName() + extractParameterTypes(m.getParameters()) + ",  attempted: " + m.getName() + extractParameterTypes(params), e);
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
                            value = castToType(val, Class.forName(params[0].getName()));

                            if (obj instanceof GroovyObject) {
                                MetaClass metaClass = InvokerHelper.getMetaClass(obj);
                                metaClass.setProperty(obj, sourceDescriptors[x].getName(), value);
                            } else {
                                sourceDescriptors[x].getWriteMethod().invoke(obj, new Object[]{value});
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.warn("BeanUtil.populate failed. method=" + property + ", value=" + value, e);
                }
            }

        } catch (Exception e) {
            logger.error("Error occurred populating object from request", e);
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


    private void loadRoutes() throws IOException, URISyntaxException, ClassNotFoundException {

        final Class c = groovyClassLoader.parseClass(new File(this.getClass().getClassLoader().getResource("./routes.groovy").toURI()));

        Closure closure = new Closure(groovyClassLoader) {

            public Object call() {
                try {
                    Binding binding = new Binding();
                    binding.setVariable("rroutes", routes);
                    binding.setVariable("groovyClassLoader", groovyClassLoader);

                    Script routesScript = InvokerHelper.createScript(c, binding);
                    routesScript.invokeMethod("run", new Object[]{});
                } catch (Exception e) {
                    logger.error("error in routes.groovy", e);
                }

                return null;
            }
        };
        GroovyCategorySupport.use(RoutingCategory.class, closure);
    }


    public static sybrix.easygsp2.routing.Route findRoute(HttpServletRequest servletRequest) {
        for (sybrix.easygsp2.routing.Route r : routes.values()) {
            if (r.matches(servletRequest)) {
                logger.debug("matching route found! " + r.toString());
                return r;
            }
        }

        logger.debug("no matching route found for " + servletRequest.getRequestURI());

//                String uri = servletRequest.getRequestURI();
//                int i = uri.lastIndexOf('/');

        //String cls = uri.substring(i,i+1) + uri.substring(i,uri.length()) + "Controlller";


        return null;
    }


    private static boolean isJson(String contentType) {

        return contentType == null ? false : contentType.contains(APPLICATION_JSON.getBaseType());
    }

    private static boolean isXML(String contentType) {
        return contentType == null ? false : contentType.contains(APPLICATION_XML.getBaseType());
    }

    private static boolean isFormUrlEncoded(String contentType) {
        return contentType.contains("application/x-www-form-urlencoded");
    }

    private static boolean isMultiPart(String contentType) {
        if (contentType == null)
            return false;

        return contentType.startsWith("multipart/");
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
    public List<String> lookupParameterNames(Method method, ClassLoader classLoader) {
        try {
            List<String> parameters = new ArrayList<String>();

            ClassPool classPool = new ClassPool();
            classPool.appendSystemPath();
            classPool.appendClassPath(new LoaderClassPath(this.getClass().getClassLoader()));
            classPool.appendClassPath("/Users/dsmith/Temp/groovy");

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

    @Api(url = "", method = "", roles = "")
    private static class _dummy {

    }

    private static MimeType parseMimeType(String s) {
        try {
            return new MimeType(s);
        } catch (MimeTypeParseException e) {
            return null;
        }
    }

    public Validator getValidator() {
        return validator;
    }

    public void setValidator(Validator validator) {
        this.validator = validator;
    }
}
