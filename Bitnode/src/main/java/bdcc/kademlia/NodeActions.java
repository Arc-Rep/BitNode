package bdcc.kademlia;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;

import bdcc.auction.Auction;
import bdcc.auction.AuctionList;
import bdcc.auction.Bid;
import bdcc.auction.User;
import bdcc.chain.*;
import bdcc.grpc.NodeInfo;
import bdcc.grpc.NodeOperationsClient;

public class NodeActions {
    public static void pingNode(KeyNode node, int server_port,KBucket userBucket, User current_user, AuctionList auctions){
        NodeOperationsClient initial_requester = new NodeOperationsClient(node.getValue(), server_port);
        try
        {
            NodeInfo response = initial_requester.notifyNode(
                current_user.getUserId(), InetAddress.getLocalHost().getHostAddress()
            );
            System.out.println("User " + Crypto.toHex(response.getUserId())  + " found with address " + response.getUserAddress());
            userBucket.addNode(response.getUserId(), response.getUserAddress());
        }
        catch(UnknownHostException e){
            //if node not found it is removed from the KBucket
            userBucket.removeNode(node);
        }
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

    public static int makeBid(String id, double value, KeyNode bidder, AuctionList list){ // 1 - accepted, 2 - rejected, 3 - auction not live
        Auction temp = list.auctionIsLive(id);
        if(temp == null){
            return 3;
        }

        Bid bid = new Bid(id, value, bidder);

        if(temp.updateBid(bid)){
            list.updateList(temp,1);
            return 1;
        }

        return 2;
    }

    public static void completeAuction(AuctionList list, User current_user){
        Auction temp = current_user.getUserAuction();
        current_user.concludeAuction();
        list.updateList(temp, 2);
    }
}