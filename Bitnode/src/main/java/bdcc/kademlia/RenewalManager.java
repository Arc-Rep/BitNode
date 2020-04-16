package bdcc.kademlia;

import bdcc.auction.User;

public class RenewalManager implements Runnable{
    public static KBucket user_bucket;
    public static User current_user;
    private int server_port;
    volatile boolean terminate = false;

    public RenewalManager(KBucket bucket, User current_user, int port){
        user_bucket = bucket;
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
                    if(randomNode == null) throw new Exception("Could not find node");
                    else
                    {
                        NodeActions.pingNode(randomNode.Value, server_port,user_bucket, current_user);
                    }
                }
            }
            catch(Exception e){
                System.out.println("Error within thread");
            }
        }
    }


}