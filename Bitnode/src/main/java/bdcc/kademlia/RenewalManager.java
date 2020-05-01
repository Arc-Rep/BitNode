package bdcc.kademlia;

import bdcc.auction.AuctionList;
import bdcc.auction.User;

public class RenewalManager implements Runnable{
    public static KBucket user_bucket;
    public static User current_user;
    public static AuctionList auction_list;
    private int server_port;
    volatile boolean terminate = false;

    public RenewalManager(KBucket bucket, AuctionList auctions, User user, int port){
        user_bucket = bucket;
        auction_list = auctions;
        current_user = user;
        server_port = port;
    }

    public void terminate(){
        terminate = true;
    }

    public void run(){
        while(!terminate)
        {
            try{
                Thread.sleep(5000);
                if(user_bucket.node_number > 0)
                {
                    KeyNode randomNode = user_bucket.getRandomNode();
                    if(randomNode != null) throw new Exception("Could not find node");
                    {
                        NodeActions.pingNode(randomNode,server_port,user_bucket, current_user, auction_list);
                    }
                }
            }
            catch(Exception e){
                
            }
        }
    }


}