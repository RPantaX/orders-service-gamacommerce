package com.braidsbeautyByAngie.aggregates.constants;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
public class Constants {
    public static String getUserInSession() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String username = request.getHeader("X-Username");
        String userId = request.getHeader("X-User-Id");
        return username + " - " + userId;
    }
    public static Long getCompanyIdInSession() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getHeader("X-companyId") != null ? Long.parseLong(request.getHeader("X-companyId")) : null;
    }
}
