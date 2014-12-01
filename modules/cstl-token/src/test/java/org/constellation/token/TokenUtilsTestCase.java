package org.constellation.token;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




import org.junit.Assert;
import org.junit.Test;





public class TokenUtilsTestCase
{

    @Test
	public void expire() {
        
	    String token = TokenUtils.createToken("user", "nothing");
	    Assert.assertFalse("Should not be false...", TokenUtils.isExpired(token));
	    
	}
	
}