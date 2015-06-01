package com.stys.platform.logdrain.syslog;

import com.stys.platform.logdrain.Application;
import com.stys.platform.logdrain.Plugin;
import com.typesafe.config.Config;
import org.productivity.java.syslog4j.server.SyslogServer;
import org.productivity.java.syslog4j.server.SyslogServerConfigIF;
import org.productivity.java.syslog4j.server.SyslogServerEventHandlerIF;
import org.productivity.java.syslog4j.server.SyslogServerIF;
import org.slf4j.LoggerFactory;

/** Syslog4j server */
public class SyslogServerPlugin extends Plugin {

    private static final String CONFIG_KEY = "syslog.server";
    private static final String PROTOCOL_KEY = "protocol";
    private static final String HOST_KEY = "host";
    private static final String PORT_KEY = "port";
    
    private final String protocol;
    private final String host;
    private final int port;
    
    private final SyslogServerEventHandlerIF handler;
    
    public SyslogServerPlugin(Application application) {
        super(application);
        this.logger = LoggerFactory.getLogger(SyslogServerPlugin.class);
        
        Config config = this.application.configuration().getConfig(CONFIG_KEY);
        
        this.protocol = config.getString(PROTOCOL_KEY);
        this.host = config.getString(HOST_KEY);
        this.port = config.getInt(PORT_KEY);
                
        this.handler = this.application.plugin(SyslogServerEventHandlerIF.class);
        if (null == handler) {
            Throwable ex = new IllegalStateException("Event handler plugin is not found");
            logger.error(String.format("Error loading syslog server plugin - %s", ex.getMessage()), ex);
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public void initialize() {
        logger.debug(String.format("Server version: %s", SyslogServer.getVersion()));

        if(!SyslogServer.exists(this.protocol)) {
            Throwable ex = new AssertionError(String.format("Protocol %s is not supported by syslog4j server", this.protocol));
            logger.error("Unable to start syslog4j server", ex);
            throw new RuntimeException(ex);
        }
        logger.debug(String.format("Server protocol: %s", this.protocol));
        
        SyslogServerIF server = SyslogServer.getInstance(this.protocol);
        SyslogServerConfigIF config = server.getConfig();
        
        config.setHost(this.host);
        logger.debug(String.format("Listening on host: %s", this.host));
        
        config.setPort(this.port);
        logger.debug(String.format("Listening on port: %d", this.port));
        
        config.addEventHandler(this.handler);
        logger.debug(String.format("Event handler: %s", this.handler.toString()));
        
        server.initialize(this.protocol, config);
        server.run();
    }

}
