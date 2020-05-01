package bdcc.kademlia;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;

import bdcc.auction.*;
import bdcc.chain.*;
import bdcc.grpc.NodeInfo;
import bdcc.grpc.NodeNotification;
import bdcc.grpc.NodeOperationsClient;

public class NodeActions {
    public static void pingNode(KeyNode node, int server_port,KBucket userBucket, User current_user, AuctionList auctions){
        NodeOperationsClient initial_requester = new NodeOperationsClient(node.getValue(), server_port);
        Auction random_auction = auctions.getRandomAuction(), user_auction = current_user.getUserAuction();
        NodeNotification response = null;
        try
        {
            response = initial_requester.notifyNode(
                current_user.getUserId(), InetAddress.getLocalHost().getHostAddress(),
                (user_auction == null) ? "" : user_auction.getAuctionId(),
                (user_auction == null) ? "" : user_auction.getItem(),
                (user_auction == null) ? 0 : user_auction.getValue(),
                (random_auction == null) ? "" : random_auction.getAuctionId(),
                (random_auction == null) ? "" : random_auction.getSeller(),
                (random_auction == null) ? "" : random_auction.getItem(),
                (random_auction == null) ? 0 : random_auction.getValue()
            );
            proccessPingNode(response, userBucket, current_user, auctions);
        }
        catch(UnknownHostException e){
            //if node not found it is removed from the KBucket
            userBucket.removeNode(node);
        }

        try{
            initial_requester.shutdown();
        } catch(Exception e){
            System.out.println("Unable to shutdown client");
        }
    }

    public static void proccessPingNode(NodeNotification notification, KBucket userBucket, 
                                                    User current_user, AuctionList auctions){
        if(!notification.getAuctionId().equals(""))
        {
            Auction sender_auction = new Auction(notification.getUserId(), notification.getItem(), 
                notification.getMaxBid(),notification.getAuctionId())
            auctions.addToAuctionList(sender_auction);
        }   
        
        if(!notification.getRandomAuctionId().equals("") && !notification.getRandomUserId().equals(current_user.getUserId()))
        {
            Auction random_auction = new Auction(notification.getRandomUserId(), notification.getRandomItem(),
                                                    notification.getRandomMaxBid(), notification.getRandomAuctionId())
            auctions.addToAuctionList(random_auction);
        }
        userBucket.addNode(notification.getUserId(), notification.getUserAddress());
    }

    public static KeyNode findNode(String Key, int server_port, KBucket userBucket, User current_user){
        int max_iterations = 5, alpha = userBucket.getAlpha(), numb_nodes_found;
        double closest_distance = 0;
        NodeOperationsClient initial_requester;
        LinkedList<KeyNode> node_list = userBucket.findNearestNodes(Key, true);
        LinkedList<String> visited_nodes = new LinkedList<String>();
        Iterator<NodeInfo> response;
        KeyNode closest_node = null;
        for(int i=0; i < max_iterations; i++)
        {
            numb_nodes_found = 0;
            while(node_list.size() > 0)
            {
                initial_requester = new NodeOperationsClient(node_list.get(0).getValue(), server_port);
                try
                {
                    response = initial_requester.findNode(current_user.getUserId(), InetAddress.getLocalHost().getHostAddress());
                    if(response == null) throw new UnknownHostException("Could not find node");
                    while(response.hasNext())
                    {   
                        NodeInfo info = response.next();
                        KeyNode current_node = new KeyNode(info.getUserId(), info.getUserAddress());
                        visited_nodes.add(current_node.getKey());
                        double current_distance = current_node.compareKeyNodeID(Key);
                        if(closest_node == null || current_distance < closest_distance){
                            closest_node = current_node;
                            closest_distance = current_distance;
                        }
                        if(numb_nodes_found == 0)       //first node is always the own server
                        {
                            userBucket.addNode(info.getUserId(), info.getUserAddress());       //update server
                            System.out.println("Server " + Crypto.toHex(info.getUserId()) + " found with address " + info.getUserAddress());
                        }
                        else
                        {
                            if(Key.equals(info.getUserId()))        //found the exact match
                                return current_node;
                            else if(!visited_nodes.contains(info.getUserId()))
                                node_list.add(current_node);
                            
                        }
        
                        numb_nodes_found++;
                    }

                    try{
                        initial_requester.shutdown();
                    } catch(Exception e){
                        System.out.println("Unable to shutdown client");
                    }

                    node_list.remove(0);
                }
                catch(UnknownHostException e){
                    //if node not found it is removed from the KBucket
                    userBucket.removeNode(node_list.get(0));
                    node_list.remove(0);
                }
            }

        }
        
        return closest_node;
    }


    //needs fine tuning
    public static int makeBid(String id, double value, String bidder, AuctionList list, String host, int port){ // 1 - accepted, 2 - rejected, 3 - auction not live
        NodeOperationsClient initial_requester = new NodeOperationsClient(host, port);
        Auction temp = list.getAuctionById(id);
        if(temp == null){
            try{
                initial_requester.shutdown();
            } catch(Exception e){
                System.out.println("Unable to shutdown client");
            }
            return 3;
        }

        Bid bid = new Bid(id, value, bidder);

        if(temp.updateBid(bid)){
            initial_requester.makeBid(bidder, id, value);
            list.updateList(temp,1);
            try{
                initial_requester.shutdown();
            } catch(Exception e){
                System.out.println("Unable to shutdown client");
            }
            return 1;
        }
        try{
            initial_requester.shutdown();
        } catch(Exception e){
            System.out.println("Unable to shutdown client");
        }
        return 2;
    }

    public static void completeAuction(AuctionList list, User current_user, String host, int port){
        NodeOperationsClient initial_requester = new NodeOperationsClient(host, port);
        Auction temp = current_user.getUserAuction();
        current_user.concludeAuction();
        list.updateList(temp, 2);
        initial_requester.resultsAuction(temp.getAuctionId(), temp.getHighestBidder(), temp.getHighestBid());
        try{
            initial_requester.shutdown();
        } catch(Exception e){
            System.out.println("Unable to shutdown client");
        }
    }
}