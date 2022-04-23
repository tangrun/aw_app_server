package cn.wildfirechat.app.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.web.filter.AbstractRequestLoggingFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;

@Slf4j
//@Component
public class HttpInterceptor extends AbstractRequestLoggingFilter {

    @PostConstruct
    private void init(){
        setIncludePayload(true);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return false;
    }


    protected void doFilterInternal2(ContentCachingRequestWrapper request) throws ServletException, IOException {

        StringBuilder stringBuilder = new StringBuilder();

        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String contentType = request.getContentType();
        long contentLengthLong = request.getContentLengthLong();

        stringBuilder.append("\n").append(method).append(" ").append(requestURI).append("\t").append(contentType).append("\t").append(contentLengthLong);

        Map<String, String[]> parameterMap = request.getParameterMap();

        {
            stringBuilder.append("\nparams:");
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                stringBuilder.append("\n\t").append(entry.getKey()).append(": ").append(Arrays.toString(entry.getValue()));
            }
        }

        if (contentType != null && contentType.startsWith("multipart/")) {
            stringBuilder.append("\nparts:");
            Collection<Part> parts = request.getParts();
            for (Part part : parts) {
                String name = part.getName();
                String partContentType = part.getContentType();
                long size = part.getSize();
                if (partContentType == null || size > Integer.MAX_VALUE) {
                    byte[] bytes = new byte[Math.toIntExact(size)];
                    IOUtils.read(part.getInputStream(), bytes, 0, bytes.length);
                    stringBuilder.append("\n\t").append(name).append(": ").append(new String(bytes));
                } else {
                    stringBuilder.append("\n\t").append(name).append(": ").append(partContentType).append(" ").append(size);
                }
            }
        } else if (method.toLowerCase().equals("post")) {
            while (request.getReader().readLine() !=null){}
            String requestBody = new String(request.getContentAsByteArray(),request.getCharacterEncoding());
            stringBuilder.append("\nrequestBody:\n\t").append(requestBody);
        }
        log.debug(stringBuilder.toString());
    }


    protected void doFilterInternal2(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        StringBuilder stringBuilder = new StringBuilder();

        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String contentType = request.getContentType();
        long contentLengthLong = request.getContentLengthLong();

        stringBuilder.append("\n").append(method).append(" ").append(requestURI).append("\t").append(contentType).append("\t").append(contentLengthLong);

        Map<String, String[]> parameterMap = request.getParameterMap();

        {
            stringBuilder.append("\nparams:");
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                stringBuilder.append("\n\t").append(entry.getKey()).append(": ").append(Arrays.toString(entry.getValue()));
            }
        }

        boolean printRequestBody = false;
        if (contentType != null && contentType.startsWith("multipart/")) {
            stringBuilder.append("\nparts:");
            Collection<Part> parts = request.getParts();
            for (Part part : parts) {
                String name = part.getName();
                String partContentType = part.getContentType();
                long size = part.getSize();
                if (partContentType == null || size > Integer.MAX_VALUE) {
                    byte[] bytes = new byte[Math.toIntExact(size)];
                    IOUtils.read(part.getInputStream(), bytes, 0, bytes.length);
                    stringBuilder.append("\n\t").append(name).append(": ").append(new String(bytes));
                } else {
                    stringBuilder.append("\n\t").append(name).append(": ").append(partContentType).append(" ").append(size);
                }
            }
        } else if (method.toLowerCase().equals("post")) {
            printRequestBody = true;

        }


        try {
            filterChain.doFilter(request, response);
            if (printRequestBody) {
                String requestBody = StringUtils.join(request.getReader().lines(), "\n");
                stringBuilder.append("\nrequestBody:\n\t").append(requestBody);
            }
            stringBuilder.append("\n").append("response ok.");
        } catch (Exception e) {
            stringBuilder.append("\n").append("response error: ").append(e.getMessage());
            throw e;
        } finally {
            log.debug(stringBuilder.toString());
        }
    }


    @Override
    protected void beforeRequest(HttpServletRequest request, String message) {
        if (request instanceof ContentCachingRequestWrapper) {
            try {
                doFilterInternal2((ContentCachingRequestWrapper) request);
            } catch (ServletException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void afterRequest(HttpServletRequest request, String message) {

    }


    protected void doFilterInternal1(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper wrapperRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrapperResponse = new ContentCachingResponseWrapper(response);

        StringBuilder stringBuilder = new StringBuilder();

        String requestURI = wrapperRequest.getRequestURI();
        String method = wrapperRequest.getMethod();
        String contentType = wrapperRequest.getContentType();
        long contentLengthLong = wrapperRequest.getContentLengthLong();

        stringBuilder.append("\n").append(method).append(" ").append(requestURI).append("\t").append(contentType).append("\t").append(contentLengthLong);

        Map<String, String[]> parameterMap = wrapperRequest.getParameterMap();

        {
            stringBuilder.append("\nparams:");
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                stringBuilder.append("\n\t").append(entry.getKey()).append(": ").append(Arrays.toString(entry.getValue()));
            }
        }

        if (contentType != null && contentType.startsWith("multipart/")) {
            stringBuilder.append("\nparts:");
            Collection<Part> parts = wrapperRequest.getParts();
            for (Part part : parts) {
                String name = part.getName();
                String partContentType = part.getContentType();
                long size = part.getSize();
                if (partContentType == null || size > Integer.MAX_VALUE) {
                    byte[] bytes = new byte[Math.toIntExact(size)];
                    IOUtils.read(part.getInputStream(), bytes, 0, bytes.length);
                    stringBuilder.append("\n\t").append(name).append(": ").append(new String(bytes));
                } else {
                    stringBuilder.append("\n\t").append(name).append(": ").append(partContentType).append(" ").append(size);
                }
            }
        } else if (method.toLowerCase().equals("post")) {
            String requestBody = getRequestBody(wrapperRequest);
            stringBuilder.append("\nrequestBody:\n\t").append(requestBody);
        }


        try {
            filterChain.doFilter(wrapperRequest, wrapperResponse);
            ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(wrapperResponse, ContentCachingResponseWrapper.class);
            wrapperResponse.copyBodyToResponse();
            stringBuilder.append("\n").append("response ok.\n").append(new String(wrapper.getContentAsByteArray(), wrapper.getCharacterEncoding()));
        } catch (Exception e) {
            stringBuilder.append("\n").append("response error: ").append(e.getMessage());
            throw e;
        } finally {
            log.debug(stringBuilder.toString());
        }
    }

    public class MyRequestWrapper extends HttpServletRequestWrapper {
        private byte[] body;

        public MyRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);

            StringBuilder sb = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String body = sb.toString();
            this.body = body.getBytes(StandardCharsets.UTF_8);
        }


        public String getBody() {
            return new String(body, StandardCharsets.UTF_8);
        }
    }

    /**
     * 打印请求参数
     *
     * @param request
     */
    private String getRequestBody(ContentCachingRequestWrapper request) {
        try {
            while (request.getReader().readLine() != null) {
            }
        } catch (IOException e) {
        }
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            try {
                return new String(buf, wrapper.getCharacterEncoding());
            } catch (UnsupportedEncodingException e) {
            }
        }
        return "null";
    }

    public String getRequestHeaders(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        Enumeration<String> enu = request.getHeaderNames();

        //获取请求参数
        while (enu.hasMoreElements()) {
            String name = enu.nextElement();
            sb.append("\t").append(name).append(": ").append(request.getHeader(name));
            if (enu.hasMoreElements()) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * 获取请求地址上的参数
     *
     * @param request
     * @return
     */
    public String getRequestParams(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        Enumeration<String> enu = request.getParameterNames();
        //获取请求参数
        while (enu.hasMoreElements()) {
            String name = enu.nextElement();
            sb.append("\t").append(name).append(": ").append(request.getParameter(name));
            if (enu.hasMoreElements()) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
