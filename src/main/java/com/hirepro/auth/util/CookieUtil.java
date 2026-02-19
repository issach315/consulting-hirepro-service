package com.hirepro.auth.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieUtil {

    @Value("${app.cookie.access-token-name}")
    private String accessTokenCookieName;

    @Value("${app.cookie.refresh-token-name}")
    private String refreshTokenCookieName;

    @Value("${app.cookie.max-age}")
    private int cookieMaxAge;

    @Value("${app.cookie.secure}")
    private boolean secure;

    @Value("${app.cookie.http-only}")
    private boolean httpOnly;

    @Value("${app.cookie.same-site}")
    private String sameSite;

    @Value("${app.cookie.path}")
    private String cookiePath;

    public void createAccessTokenCookie(HttpServletResponse response, String token, long maxAgeSeconds) {
        Cookie cookie = new Cookie(accessTokenCookieName, token);
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(secure);
        cookie.setPath(cookiePath);
        cookie.setMaxAge((int) maxAgeSeconds);
        cookie.setAttribute("SameSite", sameSite);
        response.addCookie(cookie);
    }

    public void createRefreshTokenCookie(HttpServletResponse response, String token, long maxAgeSeconds) {
        Cookie cookie = new Cookie(refreshTokenCookieName, token);
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(secure);
        cookie.setPath(cookiePath);
        cookie.setMaxAge((int) maxAgeSeconds);
        cookie.setAttribute("SameSite", sameSite);
        response.addCookie(cookie);
    }

    public void clearAccessTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(accessTokenCookieName, null);
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(secure);
        cookie.setPath(cookiePath);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(refreshTokenCookieName, null);
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(secure);
        cookie.setPath(cookiePath);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    public Optional<String> getAccessTokenFromCookie(HttpServletRequest request) {
        return getCookieValue(request, accessTokenCookieName);
    }

    public Optional<String> getRefreshTokenFromCookie(HttpServletRequest request) {
        return getCookieValue(request, refreshTokenCookieName);
    }

    private Optional<String> getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie cookie = WebUtils.getCookie(request, cookieName);
        return Optional.ofNullable(cookie).map(Cookie::getValue);
    }

    public Cookie getAccessTokenCookie(HttpServletRequest request) {
        return WebUtils.getCookie(request, accessTokenCookieName);
    }

    public Cookie getRefreshTokenCookie(HttpServletRequest request) {
        return WebUtils.getCookie(request, refreshTokenCookieName);
    }

    public void setCookieMaxAge(int maxAge) {
        this.cookieMaxAge = maxAge;
    }
}