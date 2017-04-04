package ru.efive.dms.config;

import com.sun.faces.config.ConfigureListener;
import org.primefaces.webapp.filter.FileUploadFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;
import ru.efive.dms.uifaces.AuthenticationFilter;

import javax.faces.webapp.FacesServlet;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.lang.management.ManagementFactory;

/**
 * Author: Upatov Egor <br>
 * Date: 29.03.2017, 18:52 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
public class WebConfig extends AbstractAnnotationConfigDispatcherServletInitializer {
    private static final Logger log = LoggerFactory.getLogger("CONFIG");

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        final String pid = ManagementFactory.getRuntimeMXBean().getName();
        log.info("####################################################################################################");
        log.info("PID {}: Start application", pid.split("@")[0]);
        //create the root Spring application context
        final AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(ApplicationConfig.class);

        final DispatcherServlet dispatcherServlet = new DispatcherServlet(rootContext);
        // throw NoHandlerFoundException to controller ExceptionHandler.class. Used for <error-page> analogue
        dispatcherServlet.setThrowExceptionIfNoHandlerFound(true);

        //register and map the dispatcher servlet
        //note Dispatcher servlet with constructor arguments
        final ServletRegistration.Dynamic dispatcher = servletContext.addServlet("dispatcher", dispatcherServlet);
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");

        final ServletRegistration.Dynamic facesServlet = servletContext.addServlet("Faces Servlet", new FacesServlet());
        facesServlet.setLoadOnStartup(1);
        facesServlet.addMapping("*.xhtml");

        final FilterRegistration.Dynamic encodingFilter = servletContext.addFilter("encoding-filter", new CharacterEncodingFilter());
        encodingFilter.setInitParameter("encoding", "UTF-8");
        encodingFilter.setInitParameter("forceEncoding", "true");
        encodingFilter.addMappingForUrlPatterns(null, true, "/*");

        final FilterRegistration.Dynamic authorizationFilter = servletContext.addFilter("authentication-filter", new AuthenticationFilter());
        authorizationFilter.addMappingForUrlPatterns(null, true, "/component/*");

        final FilterRegistration.Dynamic fileUploadFilter = servletContext.addFilter("PrimeFaces FileUpload Filter", new FileUploadFilter());
        fileUploadFilter.addMappingForUrlPatterns(null, true, "/component/*");


        servletContext.addListener(new ContextLoaderListener(rootContext));
        servletContext.addListener(new RequestContextListener());
        servletContext.addListener(new ConfigureListener());

        servletContext.setInitParameter("javax.faces.PARTIAL_STATE_SAVING", "false");
        servletContext.setInitParameter("javax.faces.STATE_SAVING_METHOD", "server");
        servletContext.setInitParameter("javax.faces.STATE_SAVING_METHOD", "server");
        servletContext.setInitParameter("primefaces.UPLOADER", "commons");
        //<!--This is basically GET request based. Every GET request creates a new logical view.-->
        servletContext.setInitParameter("com.sun.faces.numberOfViewsInSession", "50");
//         <!--
//                This is basically synchronous (non-ajax!) POST request based.
//                Every synchronous POST request creates a new physical view.
//                They are all stored on basis of a logical view like so Map<LogicalView, Map<PhysicalView, ViewState>>.
//        -->
        servletContext.setInitParameter("com.sun.faces.numberOfLogicalViews", "50");
        servletContext.setInitParameter("javax.faces.DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE", "true");
        //  <!--Тема для элементов из библиотеки primefaces-->
        servletContext.setInitParameter("primefaces.THEME", "bootstrap");
        //  Do not evaluate EL in comments
        servletContext.setInitParameter("javax.faces.FACELETS_SKIP_COMMENTS", "true");

        servletContext.addListener(new HttpSessionListener() {
            @Override
            public void sessionCreated(HttpSessionEvent se) {
                log.info("NEW SESSION [{}]", se.getSession().getId());
                se.getSession().setMaxInactiveInterval(45 * 60);
            }

            @Override
            public void sessionDestroyed(HttpSessionEvent se) {
                log.info("END SESSION [{}]", se.getSession().getId());
            }
        });
    }

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[0];
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[0];
    }

    @Override
    protected String[] getServletMappings() {
        return new String[0];
    }
}
