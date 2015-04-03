package org.constellation.auth.transfer;

public class TokenTransfer
{

    private int userId;
    
	private final String token;


	public TokenTransfer(String token, int userId)
	{
		this.token = token;
		this.userId = userId;
	}

	public TokenTransfer() {
	    token=null;
	    userId=0;
    }

    public String getToken()
	{
		return this.token;
	}

	public int getUserId() {
        return userId;
    }
	

}