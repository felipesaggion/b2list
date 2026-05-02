package br.com.b2list.filters;

import br.com.b2list.origin.OriginContext;
import br.com.b2list.tenant.TenantContext;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class HeadersFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String tenant = httpRequest.getHeader("x-tenant");
        String origin = httpRequest.getHeader("x-origin");
        if (tenant != null && !tenant.isBlank()) {
            TenantContext.setTenant(tenant);
        }
        if (origin != null && !origin.isBlank()) {
            OriginContext.setOrigin(tenant);
        }
        try {
            chain.doFilter(request, response);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        } finally {
            TenantContext.clear();
            OriginContext.clear();
        }
    }
}