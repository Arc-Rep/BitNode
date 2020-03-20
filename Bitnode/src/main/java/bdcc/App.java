package bdcc;

import java.util.Scanner;
import bdcc.auction.User;
import bdcc.chain.*;
import java.util.concurrent.TimeUnit;

public class App 
{
    private static User current_user;
    private static NodeBlockChain block_chain;
    private static String local_address;

    private static User register(){        
        System.out.println("Please choose an username");
        Scanner scanner = new Scanner(System.in);
        String username = scanner.nextLine();
        scanner.close();
        return new User(username);
    }
    
    private static void setUpAuctionMenu(){
        System.out.println("To be released");
    }

    private static void makeTranseferMenu(){
        System.out.println("To be released");
    }

    private static void integrityCheckMenu(){
        System.out.println("Checking integrity... \n");
        Boolean result = block_chain.verifyValidity();
        if(result){
            System.out.println("Integrity check successful! The chain has NOT been tampered with.\n");
        }
        else{
            System.out.println("Integrity check failed... Please check the contents of the chain...\n");
        }
        System.out.println("Returning to main menu... \n \n \n");
    }

    private static void printStartMenu(){
        System.out.println("Select an action to perform:");
        System.out.println("1 => Choose an item to put up for auction");
        System.out.println("2 => Make a transfer");
        System.out.println("3 => Check chain integrity");
        System.out.println("4 => Exit");
        System.out.print("Option: ");
    }

    private static void menusCLI() {
        //PRECISA DE SER TESTADO
        Scanner in = new Scanner(System.in);
        int option = 0;
        while(option != 4){
            printStartMenu();
            option = in.nextInt();

            switch(option){
                case 1: setUpAuctionMenu();
                        break;
                
                case 2: makeTranseferMenu();
                        break;

                case 3: integrityCheckMenu();
                        break;

                case 4: in.close();
                        System.out.println("Exiting...");
                        break;

                default: System.out.println("Invalid option\n");
                         break;
            }

        }
    }
    public static void main( String[] args )
    {
        local_address = "localhost:4444";
        current_user = register();
        block_chain = NodeBlockChain.getChainManager();
        //inicio de CLI
        menusCLI();
    }
}
