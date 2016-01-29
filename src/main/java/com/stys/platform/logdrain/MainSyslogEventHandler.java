package com.stys.platform.logdrain;

import org.productivity.java.syslog4j.server.SyslogServerEventIF;
import org.productivity.java.syslog4j.server.SyslogServerIF;
import org.productivity.java.syslog4j.server.SyslogServerSessionEventHandlerIF;
import org.productivity.java.syslog4j.util.SyslogUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;


import java.net.SocketAddress;
import java.util.Date;

/** Basic implementation of syslog event handler */
public class MainSyslogEventHandler implements SyslogServerSessionEventHandlerIF {

    private static final Logger LOG = LoggerFactory.getLogger(MainSyslogEventHandler.class);

    @Override
    public void event(Object session, SyslogServerIF syslogServerIF, SocketAddress socketAddress, SyslogServerEventIF event) {
        String date = (event.getDate() == null ? new Date() : event.getDate()).toString();
        String facility = SyslogUtility.getFacilityString(event.getFacility());
        String level = SyslogUtility.getLevelString(event.getLevel());
        String message = event.getMessage();

        LOG.info(MarkerFactory.getMarker(level), "{}; {}; {}", date, facility, message);
    }

    @Override
    public Object sessionOpened(SyslogServerIF syslogServerIF, SocketAddress socketAddress) {
        return null;
    }

    @Override
    public void exception(Object session, SyslogServerIF syslogServerIF, SocketAddress socketAddress, Exception ex) {
        /* EMPTY */
    }

    @Override
    public void sessionClosed(Object o, SyslogServerIF syslogServerIF, SocketAddress socketAddress, boolean b) {
        /* EMPTY */
    }

    @Override
    public void initialize(SyslogServerIF syslogServerIF) {
        /* EMPTY */
    }

    @Override
    public void destroy(SyslogServerIF syslogServerIF) {
        /* EMPTY */
    }
}
