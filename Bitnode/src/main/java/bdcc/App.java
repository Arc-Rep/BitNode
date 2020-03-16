package bdcc;

import java.util.Scanner;
import bdcc.auction.User;
import bdcc.chain.*;

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
    public static void main( String[] args )
    {
        local_address = "localhost:4444";
        current_user = register();
        block_chain = NodeBlockChain.getChainManager();
        //inicio de CLI
    }
}
