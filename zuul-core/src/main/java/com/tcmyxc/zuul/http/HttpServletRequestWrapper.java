package com.tcmyxc.zuul.http;


import com.tcmyxc.zuul.constants.ZuulHeaders;
import com.tcmyxc.zuul.context.RequestContext;
import com.tcmyxc.zuul.util.HTTPRequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.IOUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.util.*;

public class HttpServletRequestWrapper extends javax.servlet.http.HttpServletRequestWrapper {

    private static final Logger logger = LoggerFactory.getLogger(HttpServletRequestWrapper.class);

    private static final HashMap<String, String[]> EMPTY_MAP = new HashMap<>();

    private HttpServletRequest req;

    private byte[] contentData = null;

    private HashMap<String, String[]> parameters = null;

    private long bodyBufferingTimeNs = 0;


    public HttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
        req = request;
    }

    public HttpServletRequestWrapper(HttpServletRequest request, HttpServletRequest req, byte[] contentData, HashMap<String, String[]> parameters, long bodyBufferingTimeNs) {
        super(request);
        this.req = req;
        this.contentData = contentData;
        this.parameters = parameters;
        this.bodyBufferingTimeNs = bodyBufferingTimeNs;
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
                LOG.error("SocketTimeoutException reading request body from inputstream. error=" + String.valueOf(e.getMessage()));
                if (contentData == null) {
                    contentData = new byte[0];
                }
            }

            try {
                LOG.debug("Length of contentData byte array = " + contentData.length);
                if (req.getContentLength() != contentData.length) {
                    LOG.warn("Content-length different from byte array length! cl=" + req.getContentLength() + ", array=" + contentData.length);
                }
            } catch (Exception e) {
                LOG.error("Error checking if request body gzipped!", e);
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

        HashMap<String, String[]> map = new HashMap<String, String[]>(mapA.size() * 2);
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
}
