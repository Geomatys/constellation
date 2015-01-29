package org.constellation.token;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenUtils {

    private static final String TOKEN_SEPARATOR = "_";

    private final static Logger LOGGER = LoggerFactory.getLogger(TokenUtils.class);

    public static final long tokenHalfLife = initTokenHalfLife();

    private static final Pattern TOKEN_PATTERN = Pattern.compile("(\\w+)_(\\d+)_(\\w+)_(\\d+)");

    public static String createToken(String username, String secret) {
        /* Expires in one hour */
        long expires = System.currentTimeMillis() + tokenHalfLife * 2;

        StringBuilder tokenBuilder = new StringBuilder();
        tokenBuilder.append(username);
        tokenBuilder.append(TOKEN_SEPARATOR);
        tokenBuilder.append(expires);
        tokenBuilder.append(TOKEN_SEPARATOR);
        tokenBuilder.append(TokenUtils.computeSignature(username, expires, secret));
        tokenBuilder.append(TOKEN_SEPARATOR);
        tokenBuilder.append(tokenHalfLife);
        return tokenBuilder.toString();
    }

    public static long getTokenLife() {
        String tokenLifeInMinutesAsString = System.getProperty("cstl.token.life", "60");
        long tokenLifeInMinutes;
        try {
            tokenLifeInMinutes = Long.parseLong(tokenLifeInMinutesAsString);
        } catch (NumberFormatException e) {
            LOGGER.warn(e.getMessage(), e);
            tokenLifeInMinutes = 60;
        }
        return tokenLifeInMinutes;
    }

    private static long initTokenHalfLife() {
        long tokenLifeInMinutes = getTokenLife();
        LOGGER.info("Token life set to " + tokenLifeInMinutes + " minutes");
        return 500L * 60 * tokenLifeInMinutes;
    }

    public static boolean isExpired(String token) {
        Matcher matcher = TOKEN_PATTERN.matcher(token);
        if (matcher.matches()) {
            String expireString = matcher.group(2);
            long expire = Long.parseLong(expireString);
            return expire < System.currentTimeMillis();
        }
        return true;
    }

    public static String computeSignature(String username, long expires, String secret) {
        StringBuilder signatureBuilder = new StringBuilder();
        signatureBuilder.append(username);
        signatureBuilder.append(TOKEN_SEPARATOR);
        signatureBuilder.append(expires);
        signatureBuilder.append(TOKEN_SEPARATOR);
        // signatureBuilder.append(userDetails.getPassword());
        signatureBuilder.append(TOKEN_SEPARATOR);
        signatureBuilder.append(secret);

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No MD5 algorithm available!");
        }

        return new String(Hex.encode(digest.digest(signatureBuilder.toString().getBytes())));
    }

    public static String getUserNameFromToken(String access_token) {
        if (null == access_token) {
            return null;
        }

        String[] parts = access_token.split(TOKEN_SEPARATOR);
        return parts[0];
    }

    public static boolean validateToken(String access_token, String secret) {
        String[] parts = access_token.split(TOKEN_SEPARATOR);
        if (parts.length < 4) {
            LOGGER.warn("Token malformed: " + access_token);
            return false;
        }
        String username = parts[0];
        long expires = Long.parseLong(parts[1]);
        String signature = parts[2];

        if (expires < System.currentTimeMillis()) {
            LOGGER.info("Token expired: " + access_token);
            return false;
        }

        if (signature.equals(TokenUtils.computeSignature(username, expires, secret))) {
            return true;
        }
        LOGGER.info("Token missmatch: " + access_token);
        return false;
    }

    public static boolean shouldBeExtended(String access_token) {
        String[] parts = access_token.split(TOKEN_SEPARATOR);
        long expires = Long.parseLong(parts[1]);

        return expires < System.currentTimeMillis() + tokenHalfLife;
    }

    
    /**
     * Extract access_token from header, parameter or cookies.  
     * @param request
     * @return
     */
    public static String extractAccessToken(HttpServletRequest request) {
        return extract(request, "access_token");
    }

    /**
     * Extract value of header, parameter or cookie for a given name.  
     * @param request
     * @param name
     * @return
     */
    public static String extract(HttpServletRequest request, String name) {
        String access_token = request.getHeader(name);
        if (access_token != null)
            return access_token;

        access_token = request.getParameter(name);
        if (access_token != null)
            return access_token;

        Cookie[] cookies = request.getCookies();
        if (cookies == null)
            return null;
       
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName()))
                return cookie.getValue();
        }
        
        return null;
    }

}