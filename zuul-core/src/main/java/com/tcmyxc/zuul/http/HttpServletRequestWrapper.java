package com.tcmyxc.zuul.http;


import com.tcmyxc.zuul.constants.ZuulHeaders;
import com.tcmyxc.zuul.context.RequestContext;
import com.tcmyxc.zuul.util.HTTPRequestUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.util.*;

/**
 * done
 */
public class HttpServletRequestWrapper extends javax.servlet.http.HttpServletRequestWrapper {

    private static final Logger logger = LoggerFactory.getLogger(HttpServletRequestWrapper.class);

    private static final HashMap<String, String[]> EMPTY_MAP = new HashMap<>();

    private HttpServletRequest req;

    private byte[] contentData = null;

    private HashMap<String, String[]> parameters = null;

    private long bodyBufferingTimeNs = 0;

    public HttpServletRequestWrapper() {
        super(groovyTrick());
    }

    private static HttpServletRequest groovyTrick() {
        //a trick for Groovy
        throw new IllegalArgumentException("Please use HttpServletRequestWrapper(HttpServletRequest request) constructor!");
    }


    public HttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
        req = request;
    }

    public HttpServletRequestWrapper(HttpServletRequest request, HttpServletRequest req, byte[] contentData, HashMap<String, String[]> parameters) {
        super(request);
        this.req = req;
        this.contentData = contentData;
        this.parameters = parameters;
    }

    public HttpServletRequestWrapper(HttpServletRequest request, HttpServletRequest req, byte[] contentData, HashMap<String, String[]> parameters, long bodyBufferingTimeNs) {
        super(request);
        this.req = req;
        this.contentData = contentData;
        this.parameters = parameters;
        this.bodyBufferingTimeNs = bodyBufferingTimeNs;
    }

    @Override
    public HttpServletRequest getRequest() {
        try {
            parseRequest();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot parse the request!", e);
        }
        return req;
    }

    public byte[] getContentData() {
        return contentData;
    }

    public HashMap<String, String[]> getParameters() {
        if (parameters == null) return EMPTY_MAP;
        HashMap<String, String[]> map = new HashMap<>(parameters.size() * 2);
        for (String key : parameters.keySet()) {
            map.put(key, parameters.get(key).clone());
        }
        return map;
    }

    private void parseRequest() throws IOException {
        if (parameters != null)
            return; // already parsed

        HashMap<String, List<String>> mapA = new HashMap<>();
        List<String> list;

        Map<String, List<String>> query = HTTPRequestUtils.getInstance().getQueryParams();
        if (query != null) {
            for (String key : query.keySet()) {
                list = query.get(key);
                mapA.put(key, list);
            }
        }

        if (shouldBufferBody()) {

            // Read the request body inputstream into a byte array.
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                // Copy all bytes from inputstream to byte array, and record time taken.
                long bufferStartTime = System.nanoTime();
                IOUtils.copy(req.getInputStream(), baos);
                bodyBufferingTimeNs = System.nanoTime() - bufferStartTime;

                contentData = baos.toByteArray();
            } catch (SocketTimeoutException e) {
                // This can happen if the request body is smaller than the size specified in the
                // Content-Length header, and using tomcat APR connector.
                logger.error("SocketTimeoutException reading request body from inputstream. error=" + String.valueOf(e.getMessage()));
                if (contentData == null) {
                    contentData = new byte[0];
                }
            }

            try {
                logger.debug("Length of contentData byte array = " + contentData.length);
                if (req.getContentLength() != contentData.length) {
                    logger.warn("Content-length different from byte array length! cl=" + req.getContentLength() + ", array=" + contentData.length);
                }
            } catch (Exception e) {
                logger.error("Error checking if request body gzipped!", e);
            }

            final boolean isPost = req.getMethod().equals("POST");

            String contentType = req.getContentType();
            final boolean isFormBody = contentType != null && contentType.contains("application/x-www-form-urlencoded");

            // only does magic body param parsing for POST form bodies
            if (isPost && isFormBody) {
                String enc = req.getCharacterEncoding();

                if (enc == null) enc = "UTF-8";
                String s = new String(contentData, enc), name, value;
                StringTokenizer st = new StringTokenizer(s, "&");
                int i;

                boolean decode = req.getContentType() != null;
                while (st.hasMoreTokens()) {
                    s = st.nextToken();
                    i = s.indexOf("=");
                    if (i > 0 && s.length() > i + 1) {
                        name = s.substring(0, i);
                        value = s.substring(i + 1);
                        if (decode) {
                            try {
                                name = URLDecoder.decode(name, "UTF-8");
                            } catch (Exception e) {
                            }
                            try {
                                value = URLDecoder.decode(value, "UTF-8");
                            } catch (Exception e) {
                            }
                        }
                        list = mapA.get(name);
                        if (list == null) {
                            list = new LinkedList<String>();
                            mapA.put(name, list);
                        }
                        list.add(value);
                    }
                }
            }
        }

        HashMap<String, String[]> map = new HashMap<>(mapA.size() * 2);
        for (String key : mapA.keySet()) {
            list = mapA.get(key);
            map.put(key, list.toArray(new String[list.size()]));
        }

        parameters = map;

    }

    private boolean shouldBufferBody() {

        if (logger.isDebugEnabled()) {
            logger.debug("Path = " + req.getPathInfo());
            logger.debug("Transfer-Encoding = " + req.getHeader(ZuulHeaders.TRANSFER_ENCODING));
            logger.debug("Content-Encoding = " + req.getHeader(ZuulHeaders.CONTENT_ENCODING));
            logger.debug("Content-Length header = " + req.getContentLength());
        }

        boolean should = false;
        if (req.getContentLength() > 0) {
            should = true;
        } else if (req.getContentLength() == -1) {
            final String transferEncoding = req.getHeader(ZuulHeaders.TRANSFER_ENCODING);
            if (transferEncoding != null && transferEncoding.equals(ZuulHeaders.CHUNKED)) {
                RequestContext.getCurrentContext().setChunkedRequestBody();
                should = true;
            }
        }

        return should;
    }

    public long getBodyBufferingTimeNs() {
        return bodyBufferingTimeNs;
    }

    @Override
    public String[] getParameterValues(String name) {
        try {
            parseRequest();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot parse the request!", e);
        }
        if (parameters == null) return null;
        String[] arr = parameters.get(name);
        if (arr == null) return null;
        return arr.clone();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        parseRequest();

        return new ServletInputStreamWrapper(contentData);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        parseRequest();

        String enc = req.getCharacterEncoding();
        if (enc == null)
            enc = "UTF-8";
        byte[] data = contentData;
        if (data == null)
            data = new byte[0];
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data), enc));
    }

    @Override
    public String getParameter(String name) {
        try {
            parseRequest();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot parse the request!", e);
        }
        if (parameters == null) return null;
        String[] values = parameters.get(name);
        if (values == null || values.length == 0)
            return null;
        return values[0];
    }

    @Override
    public Map getParameterMap() {
        try {
            parseRequest();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot parse the request!", e);
        }
        return getParameters();
    }

    @Override
    public Enumeration getParameterNames() {
        try {
            parseRequest();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot parse the request!", e);
        }
        return new Enumeration<String>() {
            private String[] arr = getParameters().keySet().toArray(new String[0]);
            private int idx = 0;

            @Override
            public boolean hasMoreElements() {
                return idx < arr.length;
            }

            @Override
            public String nextElement() {
                return arr[idx++];
            }

        };
    }
}
