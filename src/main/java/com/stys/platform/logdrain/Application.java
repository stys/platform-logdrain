package com.stys.platform.logdrain;

import com.beust.jcommander.JCommander;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 
 * Platform Log Drain
 * ==================
 * Service for collecting and managing syslog messages from other applications.
 */
public class Application {

    /** Singleton application */
    private static Application application;
    
    /** Get current application instance */
    public static Application application() {
        return application;
    }
    
    /** Entry point */
    public static void main(String[] argv) {
        Application.application = new Application(argv);
    }
    
    /** Parsed command line arguments */
    private final Args args;
    
    /** Get current cmd arguments object */
    public Args getArgs() {
        return args;        
    }
    
    /** Configuration object */
    private final Config config;
    
    /** Get current configuration object */
    public Config configuration() {
        return config;        
    }

    /** Internal logger */
    private final static Logger logger = LoggerFactory.getLogger(Application.class);
    
    /** Configuration key for list of plugins */
    private static final String PLUGINS_KEY = "plugins";
    
    /** Internal plugin collection */
    private List<Plugin> plugins = new ArrayList<>();
    
    /** Get plugin instance by class */
    public <T> T plugin(Class<T> pluginClass) {
        for (Plugin p :this.plugins) {
            if (pluginClass.isAssignableFrom(p.getClass())) {
                @SuppressWarnings("unchecked") T result = (T) p;
                return result;
            }
        }
        return null;
    }
    
    /** Application initialization */
    private Application(String[] argv) {
                
        // Parse command line arguments
        this.args = new Args();
        new JCommander(args, argv);

        // Load configuration     
        this.config = ConfigFactory.parseFile(new File(this.args.getConfigFilePath()));
        
        // Load all plugins
        this.config.getStringList(PLUGINS_KEY).stream().forEach(
            plugin -> {
                try {
                    Class class_ = Application.class.getClassLoader().loadClass(plugin);
                    @SuppressWarnings("unchecked") Constructor constructor = class_.getConstructor(Application.class);
                    plugins.add((Plugin) constructor.newInstance(this));
                } catch (ClassNotFoundException ex) {
                    logger.error(String.format("Plugin class %s not found", plugin), ex);
                    throw new RuntimeException(ex);
                } catch (NoSuchMethodException ex) {
                    logger.error(String.format("Plugin class %s must provide single argument constructor", plugin), ex);
                    throw new RuntimeException(ex);
                } catch (Exception ex) {
                    logger.error(String.format("Error creating instance of plugin %s", plugin), ex);
                    throw new RuntimeException(ex);
                }
            }
        );
        
        // Initialize plugins in order of appearance
        this.plugins.stream().forEach(Plugin::initialize);
    }
}
