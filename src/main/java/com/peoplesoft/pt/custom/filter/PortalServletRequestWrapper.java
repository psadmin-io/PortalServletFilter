package com.peoplesoft.pt.custom.filter;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class PortalServletRequestWrapper extends HttpServletRequestWrapper {

    private byte[] cachedBytes;

    private ServletInputStream inputStream;

    private BufferedReader reader;

    public PortalServletRequestWrapper(HttpServletRequest request) {
        super(request);
        try {
            this.inputStream = request.getInputStream();
            this.cachedBytes = IOUtils.toByteArray(this.inputStream);
        } catch (IOException ex) {
            this.cachedBytes = new byte[0];
        }
        this.inputStream = new CachedInputStream(this.inputStream);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return this.inputStream;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (this.reader == null) {
            return new BufferedReader(new InputStreamReader(this.inputStream));
        }
        return this.reader;
    }

    public byte[] getCachedData() {
        return this.cachedBytes;
    }

    private class CachedInputStream extends ServletInputStream {

        private final ByteArrayInputStream input;

        private final ServletInputStream is;

        public CachedInputStream(ServletInputStream is) {
            this.is = is;
            this.input = new ByteArrayInputStream(cachedBytes);
        }

        @Override
        public int read() throws IOException {
            return this.input.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return this.input.read(b);
        }

        @Override
        public int read(final byte[] b, final int off, final int len) throws IOException {
            return this.input.read(b, off, len);
        }

        @Override
        public int available() throws IOException {
            return this.input.available();
        }

        @Override
        public void close() throws IOException {
            super.close();
            this.input.close();
        }

        @Override
        public void reset() throws IOException {
            this.input.reset();
         }

        @Override
        public long skip(long n) throws IOException {
            return this.input.skip(n);
        }

        @Override
        public boolean isFinished() {
            return this.is.isFinished();
        }

        @Override
        public boolean isReady() {
            return this.is.isReady();
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            this.is.setReadListener(readListener);
        }
    }

}