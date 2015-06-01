package com.stys.platform.logdrain.pipeline;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.stys.platform.logdrain.Application;
import com.stys.platform.logdrain.Plugin;
import com.typesafe.config.Config;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.CompletableFuture;

/** Uploader implementation to Amazon AWS S3 */
public class AmazonS3UploaderPlugin extends Plugin implements Uploader {

    private static final String AWS_ACCESS_KEY_KEY = "amazon.aws.s3.access.key";
    private static final String AWS_SECRET_KEY_KEY = "amazon.aws.s3.secret.key";
    private static final String AWS_BUCKET_KEY = "amazon.aws.s3.bucket";
    private static final String AWS_KEY_PREFIX_KEY = "amazon.aws.s3.key.prefix";
    
    private final AmazonS3 client;
    private final String bucket;
    private final String prefix;
    
    public AmazonS3UploaderPlugin(Application application) {
        super(application);
        this.logger = LoggerFactory.getLogger(AmazonS3UploaderPlugin.class);
        
        Config config = this.application.configuration();
        
        String accessKey = config.getString(AWS_ACCESS_KEY_KEY);
        String secretKey = config.getString(AWS_SECRET_KEY_KEY);
        
        this.bucket = config.getString(AWS_BUCKET_KEY);
        this.prefix = config.getString(AWS_KEY_PREFIX_KEY);
        
        this.client = new AmazonS3Client(new BasicAWSCredentials(accessKey, secretKey));
    }
    
    public void initialize() {/* EMPTY */}
    
    public void upload(final String source, final String destination) {
        final String key = prefix + destination;
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            return this.client.putObject(new PutObjectRequest(this.bucket, key, new File(source)));
        }).thenApply(result -> {
            this.logger.debug(String.format("Uploaded %s to %s - %s", source, key, result.getETag()));
            return result.getETag();
        });
    }
}
