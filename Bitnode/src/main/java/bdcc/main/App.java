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


    private static void auctionMenu(String auctionID, String address) {
        Auction auction = auction_list.getAuctionById(auctionID);

        if(auction == null){
            System.out.println("Could not find an auction with id " + auctionID);
            return;
        }

        String item = auction.getItem();
        double highestBidVal = auction.getHighestBid();
        String highestBidUser = auction.getHighestBidder();
        boolean noBids = false;

        if(highestBidVal == -1){
            highestBidVal = auction.getValue();
            noBids = true;
        }

        boolean inProgress = true;
        while(inProgress){

            if(noBids){
                System.out.println("Auctioning: " + item + " | Starting price: " + highestBidVal);       
            }

            else{
                System.out.println("Auctioning: " + item + " | Current highest bid: " + highestBidVal + " by " + highestBidUser);
            }

            System.out.println("Actions:");         
            System.out.println("1 - Make a bid");         
            System.out.println("2 - Back out");
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
                    int result = NodeActions.makeBid(auctionID, bid_val, current_user.getUserId(), auction_list, address, server_port);
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
                            break;
                    
                        default:
                            System.out.println("Missing 'makeBid' return statement...");
                            break;
                    }
                }
            }
            else{
                //leave the auction
                String conf = "y";
                
                if(auction.getHighestBidder() == current_user.getUserId()){
                    System.out.println("You this auctions current highest bidder, are you sure you want to leave? [y/n] (This action will not invalidate your bid)");
                    conf = scanner.nextLine();
                }
                 
                if(conf.equals("y")){
                    inProgress = false;
                }
            }

            //update vars
            auction = auction_list.getAuctionById(auctionID);
            highestBidVal = auction.getHighestBid();
            highestBidUser = auction.getHighestBidder();
        }

        System.out.println("Returning to main menu... \n \n \n");
    }

    private static void setUpAuctionMenu(String address) {
        System.out.println("Select item to sell: ");
        String item = scanner.nextLine();
        System.out.println("Select the starting bid value: ");
        double startingValue = scanner.nextDouble();
        scanner.skip("\\R");
        System.out.println("Place the item '" + item + "' for auction with starting bid value: " + startingValue + "? [y/n]");
        String conf = scanner.nextLine();
        if(conf.equals("y")){
            System.out.println("Setting up the auction...");
            //setup auction
            if(current_user.setUpAuction(item, startingValue)){
                System.out.println("Auction successfully setup!");
                /*
                NodeOperationsClient initial_rquester = new NodeOperationsClient(address, server_port);

                try{
                    initial_rquester.infoAuction(current_user.getUserId(), 
                                             current_user.getUserAuction().getAuctionId(), 
                                             current_user.getUserAuction().getItem(), 
                                             current_user.getUserAuction().getValue(), 
                                             "",
                                             0);
                } catch(Exception e){
                    System.out.println("Error: Initial server not found.");
                }
            */
            } 
            
            else{
                System.out.println("Unable to setup auction! (This user might already have an auction setup)");
            }
        }
        else{
            System.out.println("Auction canceled...");
        }
    }

    //PRECISA DE SER REESTRUTURADA, deve ser feita ao mesmo tempo que uma auction e concluida
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

    private static void joinAuctionMenu(String address){

        auction_list.getLiveAuctions();

        System.out.println("Enter the ID of the auction you want to join: ");
        String targetAuction = scanner.nextLine();
        System.out.println("Join '" + targetAuction + "'? [y/n]");
        String conf = scanner.nextLine();
        if (conf.equals("y")){

            if(!auction_list.auctionIsLive(targetAuction)){
                System.out.println("Target auction is not live");
            }
            else{
                System.out.println("Joining auction room...");
                auctionMenu(targetAuction,address);
            }
            
        }
        else{
            System.out.println("Action canceled");
            System.out.println("Returning to main menu... \n \n \n");
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

    public static void concludeMyAuction(String address){
        NodeActions.completeAuction(auction_list, current_user, address, server_port);
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

    private static void menusCLI(String address) throws UnknownHostException {
    
        String option = "init";
        while(!option.equals("0")){
            try{
                Runtime.getRuntime().exec("clear");
            } catch (Exception e){}
            
            printStartMenu();
            option = scanner.nextLine();

            switch(option){
                case "1": setUpAuctionMenu(address);
                        /*user_client.notifyNode(current_user.getUserId(), 
                            InetAddress.getLocalHost().getHostAddress() + port);*/
                        break;
                
                case "2": makeTranseferMenu();
                        break;

                case "3": integrityCheckMenu();
                        break;

                case "4": joinAuctionMenu(address);
                        break;

                case "5": myAuctionStatus();
                        break;

                case "6": concludeMyAuction(address);
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
        Iterator<NodeInfo> response;
        try{  
            response = initial_requester.lookupNode(current_user.getUserId(), InetAddress.getLocalHost().getHostAddress());
            if(response == null) throw new Exception();
            while(response.hasNext())
            {   
                NodeInfo info = response.next();
                
                if(numb_nodes_found == 0)       //first node is always server
                {
                     userBucket.addNode(info.getUserId(), info.getUserAddress());
                     System.out.println("Server " + Crypto.toHex(info.getUserId()) + " found with address " + info.getUserAddress());
                }
                else
                {
                    NodeActions.pingNode(new KeyNode(info.getUserId(),info.getUserAddress()), server_port,userBucket, current_user,auction_list);
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
            block_chain = NodeBlockChain.getChainManager();
            user_server = new NodeOperationsServer(server_port, current_user.getUserId(),
                            InetAddress.getLocalHost().getHostAddress(), userBucket, current_user, auction_list);
            
            thread_renewal = new Thread(renew_manager);
            thread_renewal.start();
            //inicio de CLI
            menusCLI(args[0]);
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
