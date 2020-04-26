package bdcc.main;

import java.util.Scanner;
import java.util.Iterator;

import io.grpc.stub.StreamObserver;
import org.apache.log4j.PropertyConfigurator;
import java.net.InetAddress;
import java.net.UnknownHostException;

import bdcc.auction.User;
import bdcc.chain.*;
import bdcc.grpc.NodeInfo;
import bdcc.grpc.NodeOperationsClient;
import bdcc.grpc.NodeOperationsServer;
import bdcc.kademlia.*;
import bdcc.chain.*;

public class App {
    private static User current_user;
    private static NodeBlockChain block_chain;
    private static int server_port;
    private static NodeOperationsServer user_server;
    private static Scanner scanner;
    private static Thread thread_renewal;
    public  static KBucket userBucket;

    private static User register() {
        System.out.println("Please choose an username");
        String username = scanner.nextLine();
        return new User(username);
    }

    private static void auctionMenu(String auctionID) {
        //auction = getAuctionInfo(auctionID)
        String item = "" /*= auction.getItem*/;
        int highestBidVal = 0 /*= auction.getHighestBidVal() || if there are no bids, get the starting price*/;
        String highestBidUser = "" /*= // auction.getHighestBidUser()*/;
        boolean noBids = false; // auciton.hasBids();
        boolean inProgress = true;
        while(inProgress){

            if(noBids){
                System.out.println("Auctioning: " + item + "    Starting price: " + highestBidVal);       
            }

            else{
                System.out.println("Auctioning: " + item + "    Current highest bid: " + highestBidVal + " by " + highestBidUser);
            }

            System.out.println("Actions:");         
            System.out.println("1 - Make a bid");         
            System.out.println("2 - Back out");
            int option = scanner.nextInt();

            if(option == 1){
                //maybe check if you can make a bid?
                System.out.println("How much do you want to bid?");
                int bid = scanner.nextInt();
                //palceBid(bid,auction)
            }
            else{
                //leave the auction
                String conf = "y";
                /**
                 * if(auction.getHighestBidUser == this.username){
                 *     System.ou.println("You this auctions current highest bidder, are you sure you want to leave? [y/n]");
                 *     conf = scanner.nextLine();
                 * }
                 */
                if(conf.equals("y")){
                    inProgress = false;
                }
                else{
                    inProgress = false;
                }
            }
        }
    }

    private static void setUpAuctionMenu() {
        System.out.println("Select item to sell: ");
        String item = scanner.nextLine();
        //process 'item': check the users items
        System.out.println("Select the starting bid value: ");
        int startingValue = scanner.nextInt();
        System.out.println("Place the item '" + item + "' for auction with starting bid value:" + startingValue + "? [y/n]");
        String conf = scanner.nextLine();
        if(conf.equals("y")){
            System.out.println("Setting up the auction...");
            //setup auction
        }
        else{
            System.out.println("Auction canceled");
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
        }
    }

    private static void joinAuctionMenu(){
        // displayCurrentAuctions | displays the auction item, number of buyers and current bid value | auctions are selected by ID
        System.out.println("Enter the ID of the auction you want to join: ");
        String targetAuction = scanner.nextLine();
        System.out.println("Join '" + targetAuction + "'? [y/n]");
        String conf = scanner.nextLine();
        if (conf.equals("y")){
            System.out.println("Joining auction room...");
            //join auction | make a check first
        }
        else{
            System.out.println("Action canceled");
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


    private static void printStartMenu() {
        System.out.println("Select an action to perform:");
        System.out.println("1 => Choose an item to put up for auction");
        System.out.println("2 => Make a transfer");
        System.out.println("3 => Check chain integrity");
        System.out.println("4 => Join auction");
        System.out.println("5 => Exit");
        System.out.print("Option: ");
    }

    private static void menusCLI() throws UnknownHostException {
    
        String option = "init";
        while(option != "4"){
            printStartMenu();
            scanner.skip("\\R");
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

                case "5": 
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
                System.out.println("Info sent is " + current_user.getUserId());
                
                if(numb_nodes_found == 0)       //first node is always server
                {
                     userBucket.addNode(info.getUserId(), info.getUserAddress());
                     System.out.println("Server " + Crypto.toHex(info.getUserId()) + " found with address " + info.getUserAddress());
                }
                else
                {
                    System.out.println("User " + Crypto.toHex(info.getUserId())  + " delivered with address " + info.getUserAddress());
                    NodeActions.pingNode(new KeyNode(info.getUserId(),info.getUserAddress()), server_port,userBucket, current_user);
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
        server_port = 4444;
        scanner = new Scanner(System.in);
        PropertyConfigurator.configure("log4j.properties"); // configure log4js
        current_user = register();
        userBucket = new KBucket(current_user.getUserId(), 160); //SHA-1 key size
        renew_manager = new RenewalManager(userBucket, current_user, server_port);

        if(!args[0].equals("Server"))
        {
            if(!initialSetup(args[0]))  return;
        }

        try
        {
            block_chain = NodeBlockChain.getChainManager();
            user_server = new NodeOperationsServer(server_port, current_user.getUserId(),
                            InetAddress.getLocalHost().getHostAddress(), userBucket);
            
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
        user_server.blockUntilShutdown();
        thread_renewal.join();
    }
}
