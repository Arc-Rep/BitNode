package bdcc.auction;

import java.util.Scanner;
import bdcc.chain.Crypto;
import java.security.Key;
import sun.misc.BASE64Encoder;


class User{
    private String local_address;
    private String user_id;
    private String user_name;
    private double wallet;
    private Key privKey;
    Key pubKey;

    //BASE64Encoder b64 = new BASE64Encoder();

    private User(String username){
        user_name =     username;
        user_id =       Crypto.hashString(user_name);
        wallet =        0;
        local_address = "localhost:4444";
        Crypto.generateRSAKeys(pubKey, privKey);
    }

    private void add_amount(double amount_to_add){
        wallet+= amount_to_add;
    }

    User register(){        
        System.out.println("Please choose an username");
        Scanner scanner = new Scanner(System.in);
        String username = scanner.nextLine();
        scanner.close();
        return new User(username);
    }
}