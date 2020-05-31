package bdcc.kademlia;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;

import bdcc.auction.*;
import bdcc.chain.*;
import bdcc.grpc.NodeInfo;
import bdcc.grpc.NodeResponse;
import bdcc.grpc.NodeSecInfo;
import bdcc.grpc.NodeNotification;
import bdcc.grpc.InfoAuction;
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
                Crypto.convertBytesToString(current_user.getPubKey()),
                (user_auction == null) ? "" : 
                                        Crypto.convertBytesToString(Crypto.encrypt(node.getPubKey(), Crypto.convertStringToBytes(user_auction.getAuctionId()))),
                (user_auction == null) ? "" : 
                                        Crypto.convertBytesToString(Crypto.encrypt(node.getPubKey(), Crypto.convertStringToBytes(user_auction.getItem()))),
                (user_auction == null) ? "" : Crypto.convertBytesToString(Crypto.encrypt(node.getPubKey(), Crypto.convertStringToBytes(Double.toString(user_auction.getValue())))),
                (random_auction == null) ? "" : 
                                        Crypto.convertBytesToString(Crypto.encrypt(node.getPubKey(), Crypto.convertStringToBytes(random_auction.getAuctionId()))),
                (random_auction == null) ? "" : 
                                        Crypto.convertBytesToString(Crypto.encrypt(node.getPubKey(), Crypto.convertStringToBytes(random_auction.getSeller()))),
                (random_auction == null) ? "" : 
                                        Crypto.convertBytesToString(Crypto.encrypt(node.getPubKey(), Crypto.convertStringToBytes(random_auction.getItem()))),
                (random_auction == null) ? "" : Crypto.convertBytesToString(Crypto.encrypt(node.getPubKey(), Crypto.convertStringToBytes(Double.toString(random_auction.getValue()))))
            );
            proccessPingNode(response, userBucket, current_user, auctions);
        }
        catch(UnknownHostException e){
            //if node not found it is removed from the KBucket
            userBucket.removeNode(node);
        }
        catch(Exception e){
            System.out.println(e);
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
            String auction_id = notification.getAuctionId().equals("") ? "" : Crypto.convertBytesToString(Crypto.decrypt(current_user.getPrivateKey(),Crypto.convertStringToBytes(notification.getAuctionId()))),
            item_name = notification.getItem().equals("") ? "" : Crypto.convertBytesToString(Crypto.decrypt(current_user.getPrivateKey(), Crypto.convertStringToBytes(notification.getItem()))),
            random_auction_id = notification.getRandomAuctionId().equals("") ? "" : Crypto.convertBytesToString(Crypto.decrypt(current_user.getPrivateKey(), Crypto.convertStringToBytes(notification.getRandomAuctionId()))),
            random_item_id = notification.getRandomItem().equals("") ? "" : Crypto.convertBytesToString(Crypto.decrypt(current_user.getPrivateKey(), Crypto.convertStringToBytes(notification.getRandomItem()))),
            random_user = notification.getRandomUserId().equals("") ? "" : Crypto.convertBytesToString(Crypto.decrypt(current_user.getPrivateKey(), Crypto.convertStringToBytes(notification.getRandomUserId())));

            Double max_bid = notification.getMaxBid().equals("") ? 0 : Double.parseDouble(Crypto.convertBytesToString(Crypto.decrypt(current_user.getPrivateKey(),Crypto.convertStringToBytes(notification.getMaxBid())))),
                   random_max_bid = notification.getRandomMaxBid().equals("") ? 0 : Double.parseDouble(Crypto.convertBytesToString(Crypto.decrypt(current_user.getPrivateKey(),Crypto.convertStringToBytes(notification.getRandomMaxBid()))));
            //System.out.println("Non encrypted random id is " + notification.getRandomItem());
            if(!auction_id.equals(""))
            {
                Auction sender_auction = new Auction(notification.getUserId(),
                    item_name, max_bid, random_auction_id);
                auctions.addToAuctionList(sender_auction);
            
            }   
            
            if(!random_auction_id.equals("") && !random_user.equals(current_user.getUserId()))
            {
                Auction random_auction = new Auction(random_user, random_item_id,
                                                        random_max_bid, random_auction_id);
                auctions.addToAuctionList(random_auction);
            }
            userBucket.addNode(notification.getUserId(), notification.getUserAddress(),Crypto.convertStringToBytes(notification.getPublicKey()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static KeyNode NodeCompleteSearch(String Key, int server_port, KBucket userBucket, User current_user){
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
                        if(!visited_nodes.contains(info.getUserId()))
                            node_list.add(current_node);

                        if(Key.equals(info.getUserId()))        //found the exact match
                        {
                            try{
                                initial_requester.shutdown();
                            } catch(Exception e){
                                System.out.println("Unable to shutdown client");
                            }
                            return current_node;
                        }
                        
                            
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

    public static Auction getUpdatedAuction(KeyNode seller, Auction auction, User current_user , int port){
        Auction updated_auction = null;
        try 
        {
            NodeOperationsClient initial_requester = new NodeOperationsClient(seller.getValue(), port);

            InfoAuction info_auction = initial_requester.infoAuction(current_user.getUserId(), 
                            InetAddress.getLocalHost().getHostAddress(), Crypto.convertBytesToString(current_user.getPubKey()));

            if(info_auction == null) return null;

            initial_requester.shutdown();
            if(info_auction.getAuctionId().equals(""))
            {
                String seller_id = Crypto.convertBytesToString(Crypto.decrypt(current_user.getPrivateKey(),Crypto.convertStringToBytes(info_auction.getSellerId()))),
                item = Crypto.convertBytesToString(Crypto.decrypt(current_user.getPrivateKey(),Crypto.convertStringToBytes(info_auction.getItem()))),
                auction_id = Crypto.convertBytesToString(Crypto.decrypt(current_user.getPrivateKey(),Crypto.convertStringToBytes(info_auction.getAuctionId())));

                Double auction_base_amount = Double.parseDouble(Crypto.convertBytesToString(Crypto.encrypt(current_user.getPrivateKey(), Crypto.convertStringToBytes(info_auction.getAmount()))));
                if(info_auction.getBuyerId().equals("")) updated_auction = new Auction(seller_id, item, auction_base_amount, auction_id);
                else {
                    updated_auction = new Auction(seller_id, item,auction_base_amount, auction_id,
                    new Bid(auction_id, Double.parseDouble(Crypto.convertBytesToString(Crypto.encrypt(current_user.getPrivateKey(), Crypto.convertStringToBytes(info_auction.getBuyerBid())))),
                    Crypto.convertBytesToString(Crypto.decrypt(current_user.getPrivateKey(),Crypto.convertStringToBytes(info_auction.getBuyerId())))));

                }
            }
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }

        return updated_auction;
    }

    
    public static int makeBid(String id, KBucket bucket, double value, User user, int port, AuctionList list){ // 1 - accepted, 2 - rejected, 3 - auction not live
        Auction temp = list.getAuctionById(id);
        if(temp == null) return 3;

        KeyNode nearest_node = NodeCompleteSearch(temp.getSeller(), port, bucket, user);
        String node_address = null;
        if(temp.getSeller().equals(nearest_node.getKey())){
            node_address = nearest_node.getValue();
        }
        else return 3;

        Bid bid = new Bid(id, value, user.getUserId());
        if(!temp.canUpdateBid(bid)) return 2;

        try 
        {
        NodeOperationsClient initial_requester = new NodeOperationsClient(node_address, port);
        
            NodeResponse response = initial_requester.makeBid(
                Crypto.convertBytesToString(Crypto.encrypt(nearest_node.getPubKey(), Crypto.convertStringToBytes(user.getUserId()))), 
                Crypto.convertBytesToString(Crypto.encrypt(nearest_node.getPubKey(), Crypto.convertStringToBytes(id))), 
                Crypto.convertBytesToString(Crypto.encrypt(nearest_node.getPubKey(), Crypto.convertStringToBytes(Double.toString(value))))
            );
            String response_status = response.getStatus();
            if(response_status.equals("OK"))
            {
                temp.setAuctionBid(bid);
                list.updateList(temp.getAuctionId(), temp,1);
            }

            initial_requester.shutdown();

            if(response_status.equals("ERROR"))
                return 3;
            else if(response_status.equals("REFUSED"))
                return 2;
        } catch(Exception e){
                System.out.println(e.getMessage());
                return 2;
        }
        return 1;
        
    }

    public static void completeAuction(AuctionList list, User current_user, int port){
        NodeOperationsClient initial_requester = new NodeOperationsClient("host", port);
        Auction temp = current_user.getUserAuction();
        current_user.concludeAuction();
        list.updateList(temp.getAuctionId(), temp, 2);
        initial_requester.resultsAuction(temp.getAuctionId(), temp.getHighestBidder(), temp.getHighestBid());
        try{
            initial_requester.shutdown();
        } catch(Exception e){
            System.out.println("Unable to shutdown client");
        }
    }
}