package uk.gov.companieshouse.ocrapiconsumer.common;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MultipartTiff implements MultipartFile {

    private static final String CONTENT_TYPE = "image/tiff";

    private final String filename;
    private final byte[] tiffContent;

    public MultipartTiff(byte[] tiffContent) {
        this("no-filename-provided", tiffContent);
    }

    public MultipartTiff(String filename, byte[] tiffContent) {
        this.filename = filename;
        this.tiffContent = tiffContent;
    }

    @Override
    public String getName() {
        return filename;
    }

    @Override
    public String getOriginalFilename() {
        return null;
    }

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    @Override
    public boolean isEmpty() {
        return tiffContent == null || tiffContent.length == 0;
    }

    @Override
    public long getSize() {
        return tiffContent.length;
    }

    @Override
    public byte[] getBytes() {
        return tiffContent;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(tiffContent);
    }

    @Override
    public Resource getResource() {
        return null;
    }

    @Override
    public void transferTo(File file) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(tiffContent);
        }
    }

}
