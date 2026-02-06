package com.ikn.ums.interceptor;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ikn.ums.tenant.client.dto.TenantServiceConfigDTO;
import com.ikn.ums.tenant.core.context.TenantContext;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {

        HttpHeaders headers = request.getHeaders();

        // Mark this as an internal service-to-service call
        headers.set("X-INTERNAL-CALL", "true");

        // 1️ Try TenantContext first
        TenantServiceConfigDTO context = TenantContext.get();
        String orgId = context != null ? context.getOrgId() : null;

        // 2️ Fallback to incoming HTTP request
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs != null) {
            HttpServletRequest incoming = attrs.getRequest();

            if (orgId == null) {
                orgId = incoming.getHeader("X-ORG-ID");
            }

            String auth = incoming.getHeader(HttpHeaders.AUTHORIZATION);
            if (!headers.containsKey(HttpHeaders.AUTHORIZATION) && auth != null) {
                headers.set(HttpHeaders.AUTHORIZATION, auth);
            }
        }

        if (orgId != null) {
            headers.set("X-ORG-ID", orgId);
        }

        return execution.execute(request, body);
    }
}
