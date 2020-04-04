package bdcc.kademlia;



public class KeyNode{
    public String Key;
    public String Value;
    
    KeyNode(String new_key, String new_value){
        Key = new_key;
        Value = new_value;
    }

    public String getKey(){
        return Key;
    }

    public String getValue(){
        return Value;
    }

    double compareKeyNodeID(String to_compare){
        byte[] key_b = Key.getBytes();
        byte[] compare_b = to_compare.getBytes();
        double total = 0, xor;
        
        for(int i = 0; i < key_b.length; i++){
            int a = key_b[i];
            int b = compare_b[i];
            xor = (double)(a ^ b); 
            xor = a * (Math.pow(2 ,8 * (key_b.length - i)));
            total += xor;
        }

        return total;
    }
}