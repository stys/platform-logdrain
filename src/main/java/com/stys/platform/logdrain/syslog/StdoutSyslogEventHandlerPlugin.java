package com.stys.platform.logdrain.syslog;

import com.stys.platform.logdrain.Application;
import com.stys.platform.logdrain.Plugin;
import org.productivity.java.syslog4j.server.SyslogServerEventIF;
import org.productivity.java.syslog4j.server.SyslogServerIF;
import org.productivity.java.syslog4j.server.SyslogServerSessionEventHandlerIF;
import org.productivity.java.syslog4j.util.SyslogUtility;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.net.SocketAddress;
import java.util.Date;

/** Basic implementation of syslog event handler */
public class StdoutSyslogEventHandlerPlugin extends Plugin implements SyslogServerSessionEventHandlerIF {

    private PrintStream stream;
    
    public StdoutSyslogEventHandlerPlugin(Application application) {
        super(application);
        this.logger = LoggerFactory.getLogger(StdoutSyslogEventHandlerPlugin.class);
        this.stream = System.out;
    }
    
    @Override
    public void initialize() { /* EMPTY */  }

    @Override
    public void event(Object o, SyslogServerIF syslogServerIF, SocketAddress socketAddress, SyslogServerEventIF event) {
        String date = (event.getDate() == null ? new Date() : event.getDate()).toString();
        String facility = SyslogUtility.getFacilityString(event.getFacility());
        String level = SyslogUtility.getLevelString(event.getLevel());

        this.stream.println("{" + facility + "} " + date + " " + level + " " + event.getMessage());
    }

    @Override
    public Object sessionOpened(SyslogServerIF syslogServerIF, SocketAddress socketAddress) {
        return null;
    }

    @Override
    public void exception(Object o, SyslogServerIF syslogServerIF, SocketAddress socketAddress, Exception ex) {
        this.logger.error(String.format("Exception callback invoked: %s", ex), ex);
    }

    @Override
    public void sessionClosed(Object o, SyslogServerIF syslogServerIF, SocketAddress socketAddress, boolean b) { /* EMPTY */ }

    @Override
    public void initialize(SyslogServerIF syslogServerIF) { /* EMPTY */  }

    @Override
    public void destroy(SyslogServerIF syslogServerIF) { /* EMPTY */ }
}
