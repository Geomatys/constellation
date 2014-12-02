package org.constellation.token;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenUtils {

    private static final String TOKEN_SEPARATOR = "_";

    private final static Logger LOGGER = LoggerFactory.getLogger(TokenUtils.class);

    public static final long tokenHalfLife = initTokenHalfLife();

    private static final Pattern TOKEN_PATTERN = Pattern.compile("(\\w+)_(\\d+)_(\\w+)");

    public static String createToken(String username, String secret) {
        /* Expires in one hour */
        long expires = System.currentTimeMillis() + tokenHalfLife * 2;

        StringBuilder tokenBuilder = new StringBuilder();
        tokenBuilder.append(username);
        tokenBuilder.append(TOKEN_SEPARATOR);
        tokenBuilder.append(expires);
        tokenBuilder.append(TOKEN_SEPARATOR);
        tokenBuilder.append(TokenUtils.computeSignature(username, expires, secret));

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

    public static String getUserNameFromToken(String authToken) {
        if (null == authToken) {
            return null;
        }

        String[] parts = authToken.split(TOKEN_SEPARATOR);
        return parts[0];
    }

    public static boolean validateToken(String authToken, String username, String secret) {
        String[] parts = authToken.split(TOKEN_SEPARATOR);
        if (parts.length < 3)
            return false;
        long expires = Long.parseLong(parts[1]);
        String signature = parts[2];

        if (expires < System.currentTimeMillis()) {
            return false;
        }

        return signature.equals(TokenUtils.computeSignature(username, expires, secret));
    }

    public static boolean shouldBeExtended(String authToken) {
        String[] parts = authToken.split(TOKEN_SEPARATOR);
        long expires = Long.parseLong(parts[1]);

        return expires < System.currentTimeMillis() + tokenHalfLife;
    }

}