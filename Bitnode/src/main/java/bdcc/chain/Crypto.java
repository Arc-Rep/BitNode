package bdcc.chain;

import org.bouncycastle.util.encoders.Hex;

import bdcc.chain.NodeBlockChain.NodeBlock;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.LinkedList;

public class Crypto {

   public static String hashString(NodeBlock nb) {
        //final String password = "ListenToMyStory";
        int buyer;
        int seller;
        int cash;
        LinkedList<Transaction> transactions = nb.getTransactions();
        StringBuffer sb = new StringBuffer("");

        for(Iterator<Transaction> i = transactions.iterator(); i.hasNext();){
            Transaction temp = i.next();
            buyer = temp.getBuyer();
            seller = temp.getSeller();
            cash = temp.getAmount();
            sb.append(buyer);
            sb.append("&");
            sb.append(seller);
            sb.append("%");
            sb.append(cash);
            sb.append("$");
            sb.append("€");
        }
        String trans = sb.toString();
        String hashed = "";
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest( trans.getBytes(StandardCharsets.UTF_8));
            hashed = new String(Hex.encode(hash));
        }
        catch(NoSuchAlgorithmException e){
            System.out.println("Algorithm not available");
        }

        return hashed;
   }
}