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
import bdcc.grpc.TransactionInfo;
import bdcc.grpc.InfoAuction;
import bdcc.grpc.NodeOperationsClient;

public class NodeActions {
    public static void pingNode(KeyNode node, int server_port,KBucket userBucket, User current_user, AuctionList auctions){
        Auction random_auction = auctions.getRandomAuction(), user_auction = current_user.getUserAuction();
        String auction_buyer = "", random_auction_buyer = "";
        NodeNotification response = null;
    
        
        if(random_auction != null){
            if(random_auction.getSeller().equals(node.getKey())) 
                random_auction = null; 
            
        }
        try
        {   

            if(user_auction != null)
                if(user_auction.getHighestBidder() != "")
                    auction_buyer = Crypto.doFullStringEncryption(user_auction.getHighestBidder(), node.getPubKey());

            if(random_auction != null)
                if(random_auction.getHighestBidder() != "")
                    random_auction_buyer = Crypto.doFullStringEncryption(random_auction.getHighestBidder(), node.getPubKey());

            NodeOperationsClient initial_requester = new NodeOperationsClient(node.getValue(), server_port);
        
            response = initial_requester.notifyNode(
                current_user.getUserId(), InetAddress.getLocalHost().getHostAddress(),
                Crypto.convertBytesToString(current_user.getPubKey()),
                (user_auction == null) ? "" : Crypto.doFullStringEncryption(user_auction.getAuctionId(), node.getPubKey()),
                (user_auction == null) ? "" : Crypto.doFullStringEncryption(user_auction.getItem(), node.getPubKey()),
                (user_auction == null) ? "" : Crypto.doFullDoubleEncryption(user_auction.getCurrentHighestAmount(), node.getPubKey()),
                (random_auction == null) ? "" : Crypto.doFullStringEncryption(random_auction.getAuctionId(), node.getPubKey()),
                (random_auction == null) ? "" : Crypto.doFullStringEncryption(random_auction.getSeller(), node.getPubKey()),
                (random_auction == null) ? "" : Crypto.doFullStringEncryption(random_auction.getItem(), node.getPubKey()),
                (random_auction == null) ? "" : Crypto.doFullDoubleEncryption(random_auction.getCurrentHighestAmount(), node.getPubKey()),
                auction_buyer, random_auction_buyer
            );
            proccessPingNode(response, userBucket, current_user, auctions);

            initial_requester.shutdown();
        }
        catch(UnknownHostException e){

        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }

    }

    public static void proccessPingNode(NodeNotification notification, KBucket userBucket, 
                                                    User current_user, AuctionList auctions){
        try { 
            String auction_id = notification.getAuctionId().equals("") ? "" : Crypto.doFullStringDecryption(notification.getAuctionId(), current_user.getPrivateKey()),
            item_name = notification.getItem().equals("") ? "" : Crypto.doFullStringDecryption(notification.getItem(), current_user.getPrivateKey()),
            random_auction_id = notification.getRandomAuctionId().equals("") ? "" : Crypto.doFullStringDecryption(notification.getRandomAuctionId(), current_user.getPrivateKey()),
            random_item_id = notification.getRandomItem().equals("") ? "" : Crypto.doFullStringDecryption(notification.getRandomItem(), current_user.getPrivateKey()),
            random_user = notification.getRandomUserId().equals("") ? "" : Crypto.doFullStringDecryption(notification.getRandomUserId(), current_user.getPrivateKey()),
            auction_buyer = notification.getAuctionBuyer().equals("") ? "" : Crypto.doFullStringDecryption(notification.getAuctionBuyer(), current_user.getPrivateKey()),
            random_auction_buyer = notification.getRandomAuctionBuyer().equals("") ? "" : Crypto.doFullStringDecryption(notification.getRandomAuctionBuyer(), current_user.getPrivateKey());


            Double max_bid = notification.getMaxBid().equals("") ? 0 : Crypto.doFullDoubleDecryption(notification.getMaxBid(), current_user.getPrivateKey()),
                   random_max_bid = notification.getRandomMaxBid().equals("") ? 0 : Crypto.doFullDoubleDecryption(notification.getRandomMaxBid(), current_user.getPrivateKey());
            if(!auction_id.equals(""))
            {
                Auction sender_auction;

                if(auction_buyer.equals("")) 
                    sender_auction = new Auction(notification.getUserId(), item_name, max_bid, auction_id);
                else
                    sender_auction = new Auction(notification.getUserId(), item_name, max_bid, auction_id, 
                        new Bid(auction_id, max_bid, auction_buyer));

                auctions.addToAuctionList(sender_auction);
            
            }   
            
            if(!random_auction_id.equals(""))
            {
                Auction random_auction;

                if(random_auction_buyer.equals("")) 
                    random_auction = new Auction(random_user, random_item_id, random_max_bid, random_auction_id);
                else
                    random_auction = new Auction(random_user, random_item_id, random_max_bid, random_auction_id, 
                        new Bid(random_auction_id, random_max_bid, random_auction_buyer));

                auctions.addToAuctionList(random_auction);
            }
            userBucket.addNode(notification.getUserId(), notification.getUserAddress(),Crypto.convertStringToBytes(notification.getPublicKey()));
        } catch (Exception e) {
            System.out.println("Node notification client error: " + e.getMessage());
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
            if(!info_auction.getAuctionId().equals(""))
            {
                String seller_id = Crypto.doFullStringDecryption(info_auction.getSellerId(), current_user.getPrivateKey()),
                item = Crypto.doFullStringDecryption(info_auction.getItem(), current_user.getPrivateKey()),
                auction_id = Crypto.doFullStringDecryption(info_auction.getAuctionId(), current_user.getPrivateKey());
                Double auction_base_amount = Crypto.doFullDoubleDecryption(info_auction.getAmount(), current_user.getPrivateKey());
                if(info_auction.getBuyerId().equals("")) updated_auction = new Auction(seller_id, item, auction_base_amount, auction_id);
                else {
                    updated_auction = new Auction(seller_id, item,auction_base_amount, auction_id,
                    new Bid(auction_id, Crypto.doFullDoubleDecryption(info_auction.getBuyerBid(), current_user.getPrivateKey()),
                    Crypto.doFullStringDecryption(info_auction.getBuyerId(), current_user.getPrivateKey())));

                }
            }
        } catch(Exception e) {
            System.out.println("Update auction client renewal error: " + e.getMessage());
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
                Crypto.doFullStringEncryption(user.getUserId(), nearest_node.getPubKey()), 
                Crypto.convertBytesToString(Crypto.encrypt(nearest_node.getPubKey(), Crypto.convertStringToBytes(id))), 
                Crypto.convertBytesToString(Crypto.encrypt(nearest_node.getPubKey(), Crypto.convertStringToBytes(Double.toString(value))))
            );
            String response_status = response.getStatus();
            if(response_status.equals("OK"))
            {
                temp.setAuctionBid(bid);
                list.enterAdoptedAuction(temp, user);
            }

            initial_requester.shutdown();

            if(response_status.equals("ERROR"))
                return 3;
            else if(response_status.equals("REFUSED"))
                return 2;
        } catch(Exception e){
                System.out.println("Bid process client error: " + e.getMessage());
                return 2;
        }
        return 1;
        
    }

    public static void completeAuction(User current_user, KBucket user_bucket, TransactionManager t_manager, int port){
        int max_tries = 3;
        Auction my_auction = current_user.getUserAuction();

        if(my_auction.getHighestBidder().equals(""))  {
            System.out.println("Concluding auction with no bidders.");
            current_user.concludeAuction();
            return;
        }

        KeyNode buyer_node = NodeCompleteSearch(my_auction.getHighestBidder(), port, user_bucket, current_user);

        for(int i = 0; i < max_tries && !my_auction.getHighestBidder().equals(buyer_node.getKey()); i++)
            buyer_node = NodeCompleteSearch(my_auction.getHighestBidder(), port, user_bucket, current_user);

        if(my_auction.getHighestBidder().equals(buyer_node.getKey())){

            NodeOperationsClient initial_requester = new NodeOperationsClient(buyer_node.getValue(), port);

            try{
                TransactionInfo return_pay = initial_requester.resultsAuction(
                    Crypto.doFullStringEncryption(my_auction.getAuctionId(), buyer_node.getPubKey()), 
                    Crypto.doFullStringEncryption(my_auction.getHighestBidder(), buyer_node.getPubKey()), 
                    Crypto.doFullDoubleEncryption(my_auction.getHighestBid(), buyer_node.getPubKey()),
                    current_user.getUserId(),
                    InetAddress.getLocalHost().getHostAddress(),
                    Crypto.convertBytesToString(current_user.getPubKey()));

                if(return_pay.getBuyerId().equals("") || return_pay.getSellerId().equals(""))
                    System.out.println("Error on the buyer side");
                else 
                {
                    String transaction_buyer = Crypto.doFullStringDecryption(return_pay.getBuyerId(), current_user.getPrivateKey()),
                    transaction_seller = Crypto.doFullStringDecryption(return_pay.getSellerId(), current_user.getPrivateKey());
                    Double transaction_amount = Crypto.doFullDoubleDecryption(return_pay.getAmount(), current_user.getPrivateKey());

                    if(current_user.proccessAuctionConclusion(transaction_seller, transaction_buyer, transaction_amount)){
                        registerTransaction(new Transaction(transaction_buyer, transaction_seller, transaction_amount), 
                            user_bucket, current_user, t_manager,port);
                        System.out.println("Transaction registration was sent to master node. Awaiting confirmation...");
                    }
                    else {
                        System.out.println("Wrong information came from buyer. Fraud possibility. Cancelling transaction");
                    }
                }
            } catch (Exception e) {
                System.out.println("Error with buyer communication. Cancelling...");
            }

            try{
                initial_requester.shutdown();
            } catch(Exception e){
                System.out.println("Unable to shutdown client");
            }
        }
        else 
            System.out.println("Could not find buyer node");
        current_user.concludeAuction();
    }

    public static void terminateAuction(AuctionList auction_list, User user, String auction_id){
        Auction auction = auction_list.removeAdoptedAuction(auction_id);
        auction_list.removeAuction(auction_id);

        if(auction != null){
            user.returnMoney(auction.getCurrentHighestAmount());
        }
        
    }

    public static void performTransaction(KeyNode target_node, KBucket user_bucket, User current_user, TransactionManager t_manager, double amount, int port){

        try{
            NodeOperationsClient transaction_requester = new NodeOperationsClient(
                target_node.getValue(), port);
            
            NodeResponse response =
                transaction_requester.makeTransaction(
                    Crypto.doFullStringEncryption(current_user.getUserId(), target_node.getPubKey()),
                    Crypto.doFullDoubleEncryption(amount, target_node.getPubKey()), 
                    Crypto.doFullStringEncryption(target_node.getKey(), target_node.getPubKey()));
            transaction_requester.shutdown();

            if(response == null){
                System.out.println("Error during transaction. Cancelling...");
                return;
            }

            if(!response.getStatus().equals("Ok")){
                System.out.println("Error on receiving side. Cancelling...");
                return;
            }
            else System.out.println("Successfully made transferrence request of " + amount + " to " + Crypto.toHex(target_node.getKey()));
            
            registerTransaction(new Transaction(current_user.getUserId(), target_node.getKey(), amount), 
                user_bucket, current_user, t_manager, port);
            //current_user.directPayment(amount);

        } catch(Exception e){
            System.out.println(e.getMessage());
            System.out.println("Error during connection to user. Cancelling...");
        }
    }

    public static Boolean registerTransaction(Transaction transaction, KBucket userBucket, User user, TransactionManager t_manager, int port){

        if(user.getUserId().equals("Server")){
            Transaction completed_transaction = t_manager.addOrCheckTransaction(transaction, user.getUserId());
            if(completed_transaction != null)
            {
                if(completed_transaction.getBuyer().equals("Server")){
                    System.out.println("Successfully transferred " + completed_transaction.getAmount() 
                        + " to user " + Crypto.toHex(completed_transaction.getSeller()));
                    user.directPayment(completed_transaction.getAmount());
                    System.out.println("You now have " + user.getWallet());
                }
                else if(completed_transaction.getSeller().equals("Server")){
                    System.out.println("Successfully received " + completed_transaction.getAmount() 
                    + " from user " + Crypto.toHex(completed_transaction.getBuyer()));
                    user.receiveMoneyTransfer(completed_transaction.getAmount());
                    System.out.println("You now have " + user.getWallet());
                }
            }
        }

        else{
            KeyNode server_node = NodeCompleteSearch("Server", port, userBucket, user);

            if(server_node == null || !server_node.getKey().equals("Server")){
                System.out.println("Couldn't find master node to register transaction");
                return false;
            }

            try{
                NodeOperationsClient transaction_submitter = new NodeOperationsClient(
                    server_node.getValue(), port);
                
                NodeResponse response =
                    transaction_submitter.notifyTransaction(
                        Crypto.doFullStringEncryption(transaction.getBuyer(), server_node.getPubKey()),
                        Crypto.doFullStringEncryption(transaction.getSeller(), server_node.getPubKey()),
                        Crypto.doFullDoubleEncryption(transaction.getAmount(), server_node.getPubKey()), 
                        Crypto.doFullStringEncryption(user.getUserId(), server_node.getPubKey()));

                transaction_submitter.shutdown();

                if(!response.getStatus().equals("Ok")){
                    System.out.println("Error during transaction validation. Cancelling...");
                    return false;
                }

                
            } catch(Exception e){
                System.out.println(e.getMessage());
                System.out.println("Error during connection to user. Cancelling...");
                return false;
            }
        }
        System.out.println("Transaction successfully registered. Waiting for other party to complete");
        return true;
    }
    

    public static void spreadTransactionAccrossNetwork(Transaction to_spread,KBucket node_bucket){
        LinkedList<KeyNode> nodes_list = node_bucket.getNodesList();
        
        for(KeyNode node: nodes_list)
        {
            try{
                NodeOperationsClient transaction_submitter = new NodeOperationsClient(
                    node.getValue(), 4444);

                String buyer_id = Crypto.doFullStringEncryption(to_spread.getBuyer(), node.getPubKey()),
                    seller_id = Crypto.doFullStringEncryption(to_spread.getSeller(), node.getPubKey()),
                    amount = Crypto.doFullDoubleEncryption(to_spread.getAmount(), node.getPubKey());
                
                NodeResponse response = transaction_submitter.addTransactionToBlockChain(buyer_id, seller_id, amount);
                
                if(!response.getStatus().equals("Ok")) System.out.println("Something went wrong with one node");

                transaction_submitter.shutdown();
                
            } catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
        

    }
}


    