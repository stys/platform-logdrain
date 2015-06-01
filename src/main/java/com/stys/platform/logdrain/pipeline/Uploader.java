package com.stys.platform.logdrain.pipeline;

/** Interface of file uploader */
public interface Uploader {
    
    /** Upload source file to given destination */
    public void upload(String source, String destination);
    
}
