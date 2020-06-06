package bdcc.chain;


import bdcc.chain.NodeBlockChain.NodeBlock;

import org.bouncycastle.crypto.io.CipherIOException;
import org.bouncycastle.util.encoders.Hex;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.LinkedList;
import java.security.Key;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.KeyPair;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.security.Security;

import javax.crypto.Cipher;

public class Crypto {

    public static String hashBlock(NodeBlock nb, int Nonce) {

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
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.ISO_8859_1));
            hashed = new String(hash, "ISO-8859-1");
        }
        catch(Exception e){
            System.out.println("Algorithm not available");
        }
        return hashed;
    }

   public static String toHex(String str){
       byte[] str_bytes = str.getBytes(StandardCharsets.UTF_8);
       return new String(Hex.encode(str_bytes));
   }

   public static KeyPair generateRSAKeys(){
        KeyPair pair = null;
        try {
 
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");

            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(new Date().getTime());
            generator.initialize(1024, random);

            pair = generator.generateKeyPair();

        }
        catch (Exception e) {
            System.out.println(e);
        }
        return pair;
    }



    public static byte[] encrypt(byte[] publicKey, byte[] inputData)
            throws Exception {


        PublicKey key = KeyFactory.getInstance("RSA", "BC")
            .generatePublic(new X509EncodedKeySpec(publicKey));

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] encryptedBytes = cipher.doFinal(inputData);

        return encryptedBytes;
    }

    public static byte[] decrypt(byte[] privateKey, byte[] inputData)
            throws Exception {

        PrivateKey key = KeyFactory.getInstance("RSA", "BC")
                .generatePrivate(new PKCS8EncodedKeySpec(privateKey));

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] decryptedBytes = cipher.doFinal(inputData);

        return decryptedBytes;
    }

    public static byte[] convertStringToBytes(String str){
        byte[] encoded_str = null;
        try{
            encoded_str = str.getBytes("ISO-8859-1");
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
        return encoded_str;
    }

    public static String convertBytesToString(byte[] encodedbytes){
        String decoded_str = null;
        try{
            decoded_str = new String(encodedbytes, "ISO-8859-1");
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
        return decoded_str;
    }

    public static String doFullStringEncryption(String str_to_cipher, byte[] public_key) throws CipherIOException{
        String ciphered_string = null;
        try{
            ciphered_string = convertBytesToString(encrypt(public_key, convertStringToBytes(str_to_cipher)));
        } catch (Exception e) {
            throw new CipherIOException("Encryption unsuccessful", e);
        }
        return ciphered_string;
    }

    public static String doFullDoubleEncryption(Double double_to_cipher, byte[] public_key) throws CipherIOException{
        String ciphered_string = null;
        try{
            ciphered_string = convertBytesToString(encrypt(public_key, convertStringToBytes(Double.toString(double_to_cipher))));
        } catch (Exception e) {
            throw new CipherIOException("Encryption unsuccessful", e);
        }
        return ciphered_string;
    }

    public static String doFullStringDecryption(String string_to_decipher, byte[] private_key) throws CipherIOException{
        String ciphered_string = null;
        try{
            ciphered_string = convertBytesToString(decrypt(private_key, convertStringToBytes(string_to_decipher)));
        } catch (Exception e) {
            throw new CipherIOException("Decryption unsuccessful", e);
        }
        return ciphered_string;
    }

    public static Double doFullDoubleDecryption(String double_to_decipher, byte[] private_key) throws CipherIOException{
        Double ciphered_string = -1.0;
        try{
            ciphered_string = Double.parseDouble(convertBytesToString(decrypt(private_key, convertStringToBytes(double_to_decipher))));
        } catch (Exception e) {
            throw new CipherIOException("Decryption unsuccessful", e);
        }
        return ciphered_string;
    }
}