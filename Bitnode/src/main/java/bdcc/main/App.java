package bdcc.main;

import java.util.Scanner;
import java.util.Iterator;

import io.grpc.stub.StreamObserver;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.ServerSocket;

import bdcc.auction.Auction;
import bdcc.auction.AuctionList;
import bdcc.auction.Bid;
import bdcc.auction.User;
import bdcc.chain.*;
import bdcc.grpc.NodeNotification;
import bdcc.grpc.NodeInfo;
import bdcc.grpc.NodeSecInfo;
import bdcc.grpc.NodeOperationsClient;
import bdcc.grpc.NodeOperationsServer;
import bdcc.kademlia.*;

public class App {
    private static User current_user;
    private static NodeBlockChain block_chain;
    private static int server_port;
    private static NodeOperationsServer user_server;
    private static Scanner scanner;
    private static AuctionList auction_list;
    private static Thread thread_renewal;
    private static ServerSocket server_socket;
    public  static KBucket userBucket;


    private static void auctionMenu(Auction auction, KeyNode seller) {
        


        boolean inProgress = true;
        while(inProgress){

            Auction updated_auction = NodeActions.getUpdatedAuction(seller, auction, current_user ,server_port);

            if(updated_auction == null){
                System.out.println("Auction not found within node. It may already have ended. Exiting...");
                return;
            }

    
            String item = updated_auction.getItem();
            double highestBidVal = updated_auction.getHighestBid();
            String highestBidUser = updated_auction.getHighestBidder();
            boolean noBids = false;
    
            if(highestBidVal == -1){
                highestBidVal = updated_auction.getValue();
                noBids = true;
            }

            if(noBids){
                System.out.println("Auctioning: " + item + " | Starting price: " + highestBidVal);       
            }

            else{
                System.out.println("Auctioning: " + item + " | Current highest bid: " + highestBidVal + " by " + highestBidUser);
            }

            System.out.println("Actions:");         
            System.out.println("1 - Make a bid");
            System.out.println("2 - Back out");         
            System.out.println("Any other input will refresh the current auction");
            int option = scanner.nextInt();
            scanner.skip("\\R");

            if(option == 1){
                System.out.println("How much do you want to bid? (You cannot go back on this action!)");
                double bid_val = scanner.nextDouble();
                scanner.skip("\\R");
                if(bid_val > current_user.getWallet()){
                    System.out.println("You do not have the necessary funds to make this bid...");
                }
                else{
                    int result = NodeActions.makeBid(updated_auction.getAuctionId(), userBucket, bid_val, current_user, server_port, auction_list);
                    switch (result) {
                        case 1:
                            System.out.println("The bid attempt was accepted!");
                            if(noBids){
                                noBids = false;
                            }
                            break;

                        case 2:
                            System.out.println("The bid attempt was rejected...");
                            break;

                        case 3:
                            System.out.println("The auction you are trying to participate in is no longer live.");
                            return;
                    
                        default:
                            System.out.println("Missing 'makeBid' return statement...");
                            break;
                    }
                }
            }
            else if(option == 2){
                //leave the auction
                String conf = "y";
                
                if(auction.getHighestBidder().equals(current_user.getUserId())){
                    System.out.println("You are this auctions current highest bidder, are you sure you want to leave? [y/n] (This action will not invalidate your bid)");
                    conf = scanner.nextLine();
                }
                 
                if(conf.equals("y")){
                    inProgress = false;
                }
            }

        }

        System.out.println("Returning to main menu... \n \n \n");
    }

    private static void setUpAuctionMenu() {
        System.out.println("Select item to sell: ");
        String item = scanner.nextLine();
        System.out.println("Select the starting bid value: ");
        double startingValue = scanner.nextDouble();
        scanner.skip("\\R");
        System.out.println("Place the item '" + item + "' for auction with starting bid value: " + startingValue + "? [y/n]");
        String conf = scanner.nextLine();
        if(conf.equals("y")){
            System.out.println("Setting up the auction...");
            
            if(current_user.setUpAuction(item, startingValue))
                System.out.println("Auction successfully setup!");

            else System.out.println("Unable to setup auction! (This user might already have an auction setup)");
            
        }
        else{
            System.out.println("Auction canceled...");
        }
    }


    private static void makeTranseferMenu() { 
        System.out.println("Select the user that you want to transfer the money to: ");
        String targetUser = scanner.nextLine();
        System.out.println("Select the amount you want to transfer: ");
        int amount = scanner.nextInt();
        System.out.println("Transfer " + amount + " to "+ targetUser + "? [y/n]");
        String conf = scanner.nextLine();
        if(conf.equals("y")){
            System.out.println("Processing transfer...");
            //process transfer
        }
        else{
            System.out.println("Transfer canceled");
            System.out.println("Returning to main menu... \n \n \n");
        }
    }

    private static void joinAuctionMenu(){
        
        if(!auction_list.checkLiveAuctionExists())
        {
            System.out.println("Auction does not exist.");
            return;
        }
        auction_list.getLiveAuctions();

        System.out.println("Enter the index of the auction you want to join: ");
        int targetAuctionIndex = scanner.nextInt() - 1;
        Auction selected_auction = auction_list.getAuctionByIndex(targetAuctionIndex);
        scanner.skip("\\R");
        if(selected_auction == null)
        {
            System.out.println("Non existent auction");
            System.out.println("Returning to main menu... \n \n \n");
            return;
        }
        System.out.println("Join '" + Crypto.toHex(selected_auction.getAuctionId()) + "' for '" + 
            selected_auction.getItem() + " '? [y/n]");
        String conf = "";
        while(!(conf.equals("y") || conf.equals("n")))
        {
            conf = scanner.nextLine();
            if (conf.equals("y")){

                System.out.println("Joining auction room...");
                KeyNode seller_node = NodeActions.NodeCompleteSearch(selected_auction.getSeller(),
                                        server_port, userBucket, current_user);
                if(seller_node != null){
                    System.out.println(seller_node.getKey() + "\n\n\n\n\n");
                    System.out.println(selected_auction.getSeller());
                }
                if(seller_node.getKey().equals(selected_auction.getSeller()))
                    auctionMenu(selected_auction, seller_node);
                else
                {
                    System.out.println("Couldn't find seller. Leaving auction...");
                    auction_list.removeAuction(selected_auction.getAuctionId());
                }
                
            }
            else if (conf.equals("n")) {
                System.out.println("Action canceled");
                System.out.println("Returning to main menu... \n \n \n");
            }
            else System.out.println("Unsupported option");
        }
    }

    private static void integrityCheckMenu() {
        System.out.println("Checking integrity... \n");
        Boolean result = block_chain.verifyValidity();
        if (result) {
            System.out.println("Integrity check successful! The chain has NOT been tampered with.\n");
        } else {
            System.out.println("Integrity check failed... Please check the contents of the chain...\n");
        }
        System.out.println("Returning to main menu... \n \n \n");
    }

    public static void myAuctionStatus(){
        Auction au = current_user.getUserAuction();

        if(au != null){
            System.out.println("=============================================");
            System.out.println("Auction ID: " + Crypto.toHex(au.getAuctionId()));
            System.out.println("    - Item: " + au.getItem());
            if(au.getHighestBid() == -1){
                System.out.println("    - Value: " + au.getValue());
            }else {
                System.out.println("    - Highest Bid: " + au.getHighestBid());
                System.out.println("    - Bidder: " + au.getHighestBidder());
            }
            System.out.println("=============================================");            
        }
        else{
            System.out.println("You do not have an auction set up yet.");
        }
    }

    public static void concludeMyAuction(){
        if(current_user.getUserAuction() == null)
            System.out.println("No auction to conclude.");
        else 
            NodeActions.completeAuction(current_user, userBucket, server_port);
    }


    private static void printStartMenu() {
        System.out.println("Select an action to perform:");
        System.out.println("1 => Choose an item to put up for auction");
        System.out.println("2 => Make a transfer");
        System.out.println("3 => Check chain integrity");
        System.out.println("4 => Join auction");
        System.out.println("5 => View my auction status");
        System.out.println("6 => Conclude my auction");
        System.out.println("0 => Exit");
        System.out.print("Option: ");
    }

    private static void menusCLI() throws UnknownHostException {
    
        String option = "init";
        while(!option.equals("0")){
            try{
                Runtime.getRuntime().exec("clear");
            } catch (Exception e){}
            
            printStartMenu();
            option = scanner.nextLine();

            switch(option){
                case "1": setUpAuctionMenu();
                        /*user_client.notifyNode(current_user.getUserId(), 
                            InetAddress.getLocalHost().getHostAddress() + port);*/
                        break;
                
                case "2": makeTranseferMenu();
                        break;

                case "3": integrityCheckMenu();
                        break;

                case "4": joinAuctionMenu();
                        break;

                case "5": myAuctionStatus();
                        break;

                case "6": concludeMyAuction();
                        break;

                case "0": 
                        System.out.println("Exiting...");
                        break;

                default: System.out.println("Invalid option\n");
                         
            }

        }
    }


    private static boolean initialSetup(String address){
        int numb_nodes_found = 0;
        System.out.println("Initializing connection to bitnode system...");
        NodeOperationsClient initial_requester = new NodeOperationsClient(address, server_port);
        Iterator<NodeSecInfo> response;
        try{  
            response = initial_requester.lookupNode(current_user.getUserId(), 
                InetAddress.getLocalHost().getHostAddress(), Crypto.convertBytesToString(current_user.getPubKey()));
            if(response == null) throw new Exception("Error receiving response from Server");
            while(response.hasNext())
            {   
                NodeSecInfo info = response.next();
                
                if(numb_nodes_found == 0)       //first node is always server
                {
                     userBucket.addNode(info.getUserId(), info.getUserAddress(),Crypto.convertStringToBytes(info.getPublicKey()));
                     System.out.println("Server " + Crypto.toHex(info.getUserId()) + " found with address " + info.getUserAddress());
                }
                else
                {
                    NodeActions.pingNode(new KeyNode(info.getUserId(),info.getUserAddress(), Crypto.convertStringToBytes(info.getPublicKey())), server_port,userBucket, current_user,auction_list);
                }

                numb_nodes_found++;
            }
            
            initial_requester.shutdown();
        }
        catch(Exception e){
            System.out.println("Error: Initial server not found.");
            return false;
        }

        return true;
    }


    public static void main( String[] args )
    {
        RenewalManager renew_manager;
        if(args.length != 1)
        {
            System.out.println("Please specify initial bitnode server address");
            return;
        }
        try
        {
            server_socket = new ServerSocket(4445);
        }
        catch(IOException e){
            System.out.println("Only one instance of bitnode is permited");
            return;
        }
        server_port = 4444;
        scanner = new Scanner(System.in);
        PropertyConfigurator.configure("log4j.properties"); // configure log4js
        current_user = new User();
        userBucket = new KBucket(current_user.getUserId(), 160); //SHA-1 key size
        auction_list = new AuctionList();
        renew_manager = new RenewalManager(userBucket, auction_list, current_user, server_port);

        if(!args[0].equals("Server"))
        {
            if(!initialSetup(args[0]))  return;
        }

        try
        {
            /*byte[] modified_public_key = Crypto.convertStringToBytes(Crypto.convertBytesToString(current_user.getPubKey()));
            byte[] modified_private_key = Crypto.convertStringToBytes(Crypto.convertBytesToString(current_user.getPrivateKey()));
            System.out.println("Result is " + new String(Crypto.decrypt(modified_private_key, Crypto.convertStringToBytes(
                    Crypto.convertBytesToString(Crypto.encrypt(modified_public_key, Crypto.convertStringToBytes("This is a string")))))));*/
            block_chain = NodeBlockChain.getChainManager();
            user_server = new NodeOperationsServer(server_port, current_user.getUserId(),
                            InetAddress.getLocalHost().getHostAddress(), userBucket, current_user, auction_list);
            
            thread_renewal = new Thread(renew_manager);
            thread_renewal.start();
            //inicio de CLI
            
            menusCLI();
            shutdownSystem(renew_manager);
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }
        
    }

    private static void shutdownSystem(RenewalManager renew_manager) throws Exception {
        scanner.close();
        renew_manager.terminate();
        thread_renewal.join();
        user_server.doShutdown();
        server_socket.close();
    }
}
