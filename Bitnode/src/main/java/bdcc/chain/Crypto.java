package bdcc.chain;

import org.bouncycastle.util.encoders.Hex;

import bdcc.chain.NodeBlockChain.NodeBlock;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.LinkedList;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.Date;
import java.security.Security;

public class Crypto {

   public static String hashBlock(NodeBlock nb, int Nonce) {
        //final String password = "ListenToMyStory";
        LinkedList<Transaction> transactions = nb.getTransactions();
        StringBuffer sb = new StringBuffer("");

        for(Iterator<Transaction> i = transactions.iterator(); i.hasNext();){
            Transaction temp = i.next();
            String buyer = temp.getBuyer();
            String seller = temp.getSeller();
            double cash = temp.getAmount();
            sb.append(buyer);
            sb.append("&");
            sb.append(seller);
            sb.append("%");
            sb.append(cash);
            sb.append("$");
            sb.append("€");
        }
        String trans = sb.append(Integer.toString(Nonce)).toString();
        return hashString(trans);
   }

   public static String hashString(String input){
        String hashed = "";
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            hashed = new String(hash,"ASCII");
        }
        catch(Exception e){
            System.out.println("Algorithm not available");
        }
        return hashed;
   }

   public static void generateRSAKeys(Key pubKey, Key privKey){
        try {
 
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");

            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(new Date().getTime());
            generator.initialize(1024, random);

            KeyPair pair = generator.generateKeyPair();
            pubKey = pair.getPublic();
            privKey = pair.getPrivate();
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }
}