package com.stys.platform.logdrain;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.transfer.model.UploadResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.regex.Pattern;

/** */
public class LogFileUploadService {

    private static final Logger LOG = LoggerFactory.getLogger(LogFileUploadService.class);

    /** Instance of transfer manager */
    private final TransferManager transferManager;

    /** Path to watch */
    private final Path path;

    /** Destination bucket */
    private final String bucket;

    /** Destination folder */
    private final String folder;

    /** Filename pattern */
    private final Pattern pattern;

    /** Cache control string */
    private final String cacheControl;

    /** Access control string */
    private String accessControl;

    public LogFileUploadService(
        TransferManager transferManager,
        Path path,
        String filenamePattern,
        String bucket,
        String folder,
        String cacheControl,
        String accessControl
    ) {
        this.transferManager = transferManager;
        this.path = path;
        this.pattern = Pattern.compile(filenamePattern);
        this.bucket = bucket;
        this.folder = folder;
        this.cacheControl = cacheControl;
        this.accessControl = accessControl;
    }

    public void start() {

        LOG.info("Starting file watcher at: {}", path);

        WatchService watcher;
        WatchKey key;

        try {
            watcher = path.getFileSystem().newWatchService();
            key = path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        while (true) {

            try {
                key = watcher.take();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path filename = ev.context();

                try {
                    Path child = path.resolve(filename);
                    File file = child.toFile();

                    if (!pattern.matcher(file.getName()).matches()) {
                        continue;
                    }

                    FileInputStream fis = new FileInputStream(file);

                    ObjectMetadata meta = new ObjectMetadata();
                    meta.setContentLength(file.length());

                    if (null != cacheControl)
                        meta.setCacheControl(cacheControl);

                    if (null != accessControl)
                        meta.setHeader("x-amz-acl", accessControl);

                    String dst = String.join("/", folder, file.getName());

                    // Prepare upload request
                    PutObjectRequest request = new PutObjectRequest(bucket, dst, fis, meta);

                    // Upload
                    LOG.info("Uploading file {} to {}", file.getName(), dst);
                    Upload upload = transferManager.upload(request);

                    try {
                        UploadResult result = upload.waitForUploadResult();
                        LOG.info("Upload ok: {}", result.getKey());
                    } catch (InterruptedException ex) {

                    }

                } catch (IOException x) {
                    continue;
                }

            }

            boolean valid = key.reset();
            if (!valid) {
                throw new RuntimeException("Watch key is invalid");
            }

        }

    }

}
