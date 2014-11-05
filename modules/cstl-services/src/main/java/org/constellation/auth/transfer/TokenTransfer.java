package org.constellation.auth.transfer;

public class TokenTransfer
{

    private int userId;
    
    private int domainId; 
    
	private final String token;


	public TokenTransfer(String token, int userId, int domainId)
	{
		this.token = token;
		this.userId = userId;
		this.domainId = domainId;
	}

	public TokenTransfer() {
	    token=null;
	    userId=0;
	    domainId=0;
    }

    public String getToken()
	{
		return this.token;
	}

	public int getUserId() {
        return userId;
    }
	
	public int getDomainId() {
        return domainId;
    }
}