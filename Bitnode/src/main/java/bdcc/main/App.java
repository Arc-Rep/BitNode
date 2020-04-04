package bdcc.main;

import java.util.Scanner;


import org.apache.log4j.PropertyConfigurator;
import java.net.InetAddress;
import java.net.UnknownHostException;

import bdcc.auction.User;
import bdcc.chain.*;
import bdcc.grpc.NodeInfo;
import bdcc.grpc.NodeOperationsClient;
import bdcc.grpc.NodeOperationsServer;
import bdcc.kademlia.KBucket;

public class App {
    private static User current_user;
    private static NodeBlockChain block_chain;
    private static int server_port;
    private static NodeOperationsServer user_server;
    private static Scanner scanner;
    public static KBucket userBucket;

    private static User register() {
        System.out.println("Please choose an username");
        String username = scanner.nextLine();
        return new User(username);
    }

    private static void setUpAuctionMenu() {
        System.out.println("To be released");
    }

    private static void makeTranseferMenu() {
        System.out.println("To be released");
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
        System.out.println("4 => Exit");
        System.out.print("Option: ");
    }

    private static void menusCLI() throws UnknownHostException {
    
        String option = "init";
        while(option != "4"){
            printStartMenu();
            scanner.skip("\\R");
            option = scanner.nextLine();

            switch(option){
                case "1": //setUpAuctionMenu();
                        /*user_client.notifyNode(current_user.getUserId(), 
                            InetAddress.getLocalHost().getHostAddress() + port);*/
                        break;
                
                case "2": makeTranseferMenu();
                        break;

                case "3": integrityCheckMenu();
                        break;

                case "4": 
                        System.out.println("Exiting...");
                        break;

                default: System.out.println("Invalid option\n");
                         
            }

        }
    }


    private static boolean initialSetup(String address, String port){
        System.out.println("Initializing connection to bitnode system...");
        NodeOperationsClient initial_requester = new NodeOperationsClient(address, Integer.parseInt(port));
        try{  
            NodeInfo response = initial_requester.notifyNode(current_user.getUserId(), InetAddress.getLocalHost().getHostAddress());
            if(response == null) throw new Exception();
            System.out.println("User " + response.getUserIds() + " found with address " + response.getUserAddresses());
            userBucket.addNode(response.getUserIds(), response.getUserAddresses());
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
        if(args.length != 2)
        {
            System.out.println("Please specify initial bitnode server address and port");
            return;
        }
        server_port = 4444;
        scanner = new Scanner(System.in);
        PropertyConfigurator.configure("log4j.properties"); // configure log4js
        current_user = register();
        userBucket = new KBucket(current_user.getUserId(), 160); //SHA-1 key size

        if(!initialSetup(args[0],args[1]))  return;
        
        try
        {
            block_chain = NodeBlockChain.getChainManager();
            user_server = new NodeOperationsServer(server_port, current_user.getUserId(),
                            InetAddress.getLocalHost().getHostAddress(), userBucket);
            //inicio de CLI
            menusCLI();
            shutdownSystem();
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }
        
    }

    private static void shutdownSystem() throws Exception {
        scanner.close();
        user_server.blockUntilShutdown();
    }
}
