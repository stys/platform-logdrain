package com.stys.platform.logdrain;

import org.productivity.java.syslog4j.server.SyslogServerIF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;

/** Main class of the application */
public class Main {

    private static final String WORKER_PROPERTIES_KEY = "application.properties";
    private static final String DEFAULT_WORKER_PROPERTIES_FILENAME = "application.properties";

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    /** Entry point */
    public static void main(String[] args) throws Exception {

        // Create Spring application context
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        // Load configuration properties
        ConfigurableEnvironment environment = context.getEnvironment();
        MutablePropertySources propertySources = environment.getPropertySources();
        String propertiesFilename = System.getProperty(WORKER_PROPERTIES_KEY, DEFAULT_WORKER_PROPERTIES_FILENAME);
        LOG.debug("Loading properties from file: {}", propertiesFilename);
        try (FileInputStream fis = new FileInputStream(new File(propertiesFilename))) {
            Properties properties = new Properties();
            properties.load(fis);
            propertySources.addLast(new PropertiesPropertySource(WORKER_PROPERTIES_KEY, properties));
        } catch (FileNotFoundException ex) {
            LOG.warn("File not found {}", propertiesFilename);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        // Register configuration classes
        context.register(ApplicationConfiguration.class);
        context.refresh();

        // Start application
        // SyslogServerIF server = context.getBean(SyslogServerIF.class);
        // server.run();

        LogFileUploadService service = context.getBean(LogFileUploadService.class);
        service.start();

        LOG.info("Hey!");
        Thread.sleep(62000);
        LOG.info("Hop!");
        Thread.sleep(63000);
        LOG.info("What");

    }

}
