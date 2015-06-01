package com.stys.platform.logdrain;

import com.beust.jcommander.Parameter;

/** Command line arguments parser */
public class Args {
    
    /** Configuration file to load */
    @Parameter(names="--conf", description = "Location of config file")
    private String configFilePath = "application.conf";
    
    /** Get path to configuration file */
    public String getConfigFilePath() {
        return configFilePath;
    }
}
