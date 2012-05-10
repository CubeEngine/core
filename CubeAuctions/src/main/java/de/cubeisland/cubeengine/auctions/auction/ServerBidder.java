package de.cubeisland.cubeengine.auctions.auction;

/**
 * Represents the ServerConsole as Bidder
 * 
 * @author Faithcaio
 */
public class ServerBidder extends Bidder
{
    private static ServerBidder instance = null;

    public ServerBidder()
    {
        super(null);
    }

/*
* @return Instance of ServerBidder
*/
    public static Bidder getInstance()
    {
        if (instance == null)
        {
            instance = new ServerBidder();
        }
        return instance;
    }
    
/*
* create new ServerBidder from Database
*/
    public ServerBidder(int id)
    {
        super(id,"*Server");
    }
    
/*
* load in ServerBidder from Database
* @return Instance of ServerBidder
*/
    public static Bidder getInstance(int id)
    {
        if (instance == null)
        {
            instance = new ServerBidder(id);
        }
        return instance;
    }
}
