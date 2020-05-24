package bdcc.kademlia;

import java.nio.charset.StandardCharsets;

public class KeyNode{
    public String Key;
    public String Value;
    public byte[] NodePublicKey;
    
    public KeyNode(String new_key, String new_value, byte[] node_pub_key){
        Key = new_key;
        Value = new_value;
        NodePublicKey = node_pub_key;
    }

    public String getKey(){
        return Key;
    }

    public String getValue(){
        return Value;
    }

    public byte[] getPubKey(){
        return NodePublicKey;
    }

    public double compareKeyNodeID(String to_compare){
        byte[] key_b = Key.getBytes(StandardCharsets.ISO_8859_1);
        byte[] compare_b = to_compare.getBytes(StandardCharsets.ISO_8859_1);
        double total = 0, xor;
        int key_rest_size = 20 - key_b.length, compare_rest_size = 20 - compare_b.length, a, b;

        
        for(int i = 0; i < 20; i++){            //size of SHA-1 Key
            if(i < key_rest_size) a = 0;
            else a = key_b[i - key_rest_size];
            if(i < compare_rest_size) b = 0;
            else b = compare_b[i - compare_rest_size];
            xor = (double)(a ^ b); 
            xor = a * (Math.pow(2 ,8 * (key_b.length - i - 1)));
            total += xor;
        }

        return total;
    }
}