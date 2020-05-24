package bdcc.kademlia;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;

import bdcc.auction.*;
import bdcc.chain.*;
import bdcc.grpc.NodeInfo;
import bdcc.grpc.NodeSecInfo;
import bdcc.grpc.NodeNotification;
import bdcc.grpc.NodeOperationsClient;

public class NodeActions {
    public static void pingNode(KeyNode node, int server_port,KBucket userBucket, User current_user, AuctionList auctions){
        NodeOperationsClient initial_requester = new NodeOperationsClient(node.getValue(), server_port);
        Auction random_auction = auctions.getRandomAuction(), user_auction = current_user.getUserAuction();
        NodeNotification response = null;
        try
        {   //cipher with node public key
            response = initial_requester.notifyNode(
                current_user.getUserId(), InetAddress.getLocalHost().getHostAddress(),
                new String(current_user.getPubKey()),
                (user_auction == null) ? "" : user_auction.getAuctionId(),
                (user_auction == null) ? "" : user_auction.getItem(),
                (user_auction == null) ? 0 : user_auction.getValue(),
                (random_auction == null) ? "" : random_auction.getAuctionId(),
                (random_auction == null) ? "" : random_auction.getSeller(),
                (random_auction == null) ? "" : random_auction.getItem(),
                (random_auction == null) ? 0 : random_auction.getValue(),
                node.getPubKey()
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
        try { 
            String auction_id = new String(Crypto.decrypt(current_user.getPrivateKey(), notification.getAuctionId().getBytes())),
            item_name = new String(Crypto.decrypt(current_user.getPrivateKey(), notification.getItem().getBytes())),
            random_auction_id = new String(Crypto.decrypt(current_user.getPrivateKey(), notification.getRandomAuctionId().getBytes())),
            random_item_id = new String(Crypto.decrypt(current_user.getPrivateKey(), notification.getRandomItem().getBytes())),
            random_user = new String(Crypto.decrypt(current_user.getPrivateKey(), notification.getRandomUserId().getBytes()));
            
            System.out.println("Non encrypted random id is " + notification.getRandomItem());
            System.out.println("Random item id is " + random_item_id);
            if(!auction_id.equals(""))
            {
                Auction sender_auction = new Auction(notification.getUserId(),
                    item_name, notification.getMaxBid(), random_auction_id);
                auctions.addToAuctionList(sender_auction);
            
            }   
            
            if(!random_auction_id.equals("") && !random_user.equals(current_user.getUserId()))
            {
                Auction random_auction = new Auction(random_user, random_item_id,
                                                        notification.getRandomMaxBid(), random_auction_id);
                auctions.addToAuctionList(random_auction);
            }
            userBucket.addNode(notification.getUserId(), notification.getUserAddress(), notification.getPublicKey().getBytes());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static KeyNode findNode(String Key, int server_port, KBucket userBucket, User current_user){
        int alpha = userBucket.getAlpha(), numb_nodes_found, max_iterations = 3;
        double closest_distance = 0;
        NodeOperationsClient initial_requester;
        LinkedList<KeyNode> node_list = userBucket.findNearestNodes(Key, true);
        LinkedList<String> visited_nodes = new LinkedList<String>();
        Iterator<NodeSecInfo> response;
        KeyNode closest_node = null;
        for(KeyNode kn: node_list){
            if(kn.getKey().equals(Key)){
                return kn;
            }
        }

        for(int i=0; i < Math.pow(alpha,max_iterations) && node_list.size() > 0; i++)
        {
            numb_nodes_found = 0;

                initial_requester = new NodeOperationsClient(node_list.get(0).getValue(), server_port);
                try
                {
                response = initial_requester.findNode(current_user.getUserId(), 
                                                        InetAddress.getLocalHost().getHostAddress(),
                                                        Crypto.convertBytesToString(current_user.getPubKey())
                                                    );
                if(response == null) throw new UnknownHostException("Could not find node");
                while(response.hasNext())
                {   
                    NodeSecInfo info = response.next();
                    KeyNode current_node = new KeyNode(info.getUserId(), info.getUserAddress(), 
                        Crypto.convertStringToBytes(info.getPublicKey()));
                    visited_nodes.add(current_node.getKey());
                    double current_distance = current_node.compareKeyNodeID(Key);
                    if(closest_node == null || current_distance < closest_distance){
                        closest_node = current_node;
                        closest_distance = current_distance;
                    }
                    if(numb_nodes_found == 0)       //first node is always the own server
                    {
                        userBucket.addNode(info.getUserId(), info.getUserAddress(), Crypto.convertStringToBytes(info.getPublicKey()));       //update server
                        System.out.println("Server " + Crypto.toHex(info.getUserId()) + " found with address " + info.getUserAddress());
                    }
                    else
                    {
                        if(Key.equals(info.getUserId()))        //found the exact match
                        {
                            try{
                                initial_requester.shutdown();
                            } catch(Exception e){
                                System.out.println("Unable to shutdown client");
                            }
                            return current_node;
                        }
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