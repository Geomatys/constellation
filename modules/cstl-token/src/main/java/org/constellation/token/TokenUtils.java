package org.constellation.token;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;





public class TokenUtils
{

	
	public static final long tokenHalfLife = 500L * 60 * 60;


	public static String createToken(String username, String secret)
	{
		/* Expires in one hour */
		long expires = System.currentTimeMillis() + tokenHalfLife * 2;

		StringBuilder tokenBuilder = new StringBuilder();
		tokenBuilder.append(username);
		tokenBuilder.append(":");
		tokenBuilder.append(expires);
		tokenBuilder.append(":");
		tokenBuilder.append(TokenUtils.computeSignature(username, expires, secret));

		return tokenBuilder.toString();
	}


	public static String computeSignature(String username, long expires, String secret)
	{
		StringBuilder signatureBuilder = new StringBuilder();
		signatureBuilder.append(username);
		signatureBuilder.append(":");
		signatureBuilder.append(expires);
		signatureBuilder.append(":");
//		signatureBuilder.append(userDetails.getPassword());
		signatureBuilder.append(":");
		signatureBuilder.append(secret);

		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("No MD5 algorithm available!");
		}

		return new String(Hex.encode(digest.digest(signatureBuilder.toString().getBytes())));
	}


	public static String getUserNameFromToken(String authToken)
	{
		if (null == authToken) {
			return null;
		}

		String[] parts = authToken.split(":");
		return parts[0];
	}


	public static boolean validateToken(String authToken, String username, String secret)
	{
		String[] parts = authToken.split(":");
		long expires = Long.parseLong(parts[1]);
		String signature = parts[2];

		if (expires < System.currentTimeMillis()) {
			return false;
		}

		return signature.equals(TokenUtils.computeSignature(username, expires, secret));
	}


    public static boolean shouldBeExtended(String authToken) {
        String[] parts = authToken.split(":");
        long expires = Long.parseLong(parts[1]);
        
        return expires < System.currentTimeMillis() + tokenHalfLife;
    }
}