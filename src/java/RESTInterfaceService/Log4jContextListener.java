package RESTInterfaceService;

import java.io.File;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

@WebListener("application context listener")
public class Log4jContextListener implements ServletContextListener{

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        String log4jConfigFile = context.getInitParameter("log4j-config-location");
        String fullPath = context.getRealPath("") + File.separator + log4jConfigFile;
         
        PropertyConfigurator.configure(fullPath);
        
        BasicConfigurator.configure();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        
    }
    
}
