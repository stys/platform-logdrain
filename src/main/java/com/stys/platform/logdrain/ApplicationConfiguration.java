package com.stys.platform.logdrain;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.transfer.TransferManager;
import org.productivity.java.syslog4j.SyslogConfigIF;
import org.productivity.java.syslog4j.SyslogRuntimeException;
import org.productivity.java.syslog4j.server.SyslogServer;
import org.productivity.java.syslog4j.server.SyslogServerConfigIF;
import org.productivity.java.syslog4j.server.SyslogServerIF;
import org.productivity.java.syslog4j.server.SyslogServerSessionEventHandlerIF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/** */
@Configuration
public class ApplicationConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationConfiguration.class);

    @Autowired
    private Environment environment;

    @Bean
    public SyslogServerSessionEventHandlerIF syslogServerSessionEventHandler() {
        return new MainSyslogEventHandler();
    }

    @Bean
    public SyslogServerIF syslogServer() {

        String protocol = environment.getProperty("syslog.protocol");
        String host = environment.getProperty("syslog.host");
        Integer port = environment.getProperty("syslog.port", Integer.class);

        SyslogServerIF server = SyslogServer.getInstance(protocol);
        SyslogServerConfigIF config = server.getConfig();
        config.setHost(host);
        config.setPort(port);

        config.addEventHandler(syslogServerSessionEventHandler());

        return new SyslogServerIF() {

            @Override
            public void initialize(String s, SyslogServerConfigIF syslogServerConfigIF) throws SyslogRuntimeException {
                server.initialize(s, syslogServerConfigIF);
            }

            @Override
            public String getProtocol() {
                return server.getProtocol();
            }

            @Override
            public SyslogServerConfigIF getConfig() {
                return server.getConfig();
            }

            @Override
            public void run() {
                LOG.info("Starting syslog server: protocol - {}, port - {}", protocol, port);
                server.run();
            }

            @Override
            public Thread getThread() {
                return server.getThread();
            }

            @Override
            public void setThread(Thread thread) {
                server.setThread(thread);
            }

            @Override
            public void shutdown() {
                server.shutdown();
            }

        };
    }

    @Bean
    public TransferManager transferManager() {
        return new TransferManager(new DefaultAWSCredentialsProviderChain());
    }

    @Bean
    public LogFileUploadService logFileUploadService() {

        String bucket = environment.getProperty("aws.s3.bucket");
        String folder = environment.getProperty("aws.s3.folder");
        String cacheControl = environment.getProperty("aws.s3.cacheControl");
        String accessControl = environment.getProperty("aws.s3.accessControl");

        String watchDir = environment.getProperty("logs.dir");
        String filenamePattern = environment.getProperty("logs.filenamePattern");

        return new LogFileUploadService(
            transferManager(),
            Paths.get(watchDir),
            filenamePattern,
            bucket,
            folder,
            cacheControl,
            accessControl
        );

    }

}
