package com.stys.platform.logdrain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Base plugin class */
public abstract class Plugin {

    /** Singleton instance of current application */
    protected Application application;
    
    /** Instance of logger */
    protected Logger logger = LoggerFactory.getLogger(Plugin.class);
    
    /** All plugin instances should provide a single argument constructor */
    public Plugin(Application application) {
        this.application = application;
        logger.debug(String.format("Picked plugin %s", this.getClass().getSimpleName()));
    }
    
    /** Plugins must implement their own initialization */
    public abstract void initialize();
    
}
