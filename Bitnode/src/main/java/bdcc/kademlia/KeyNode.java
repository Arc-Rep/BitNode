package bdcc.kademlia;



public class KeyNode{
    public String Key;
    public String Value;
    
    public KeyNode(String new_key, String new_value){
        Key = new_key;
        Value = new_value;
    }

    public String getKey(){
        return Key;
    }

    public String getValue(){
        return Value;
    }

    public double compareKeyNodeID(String to_compare){
        byte[] key_b = Key.getBytes();
        byte[] compare_b = to_compare.getBytes();
        System.out.println("Byte lengths are:" + key_b.length + " and " + compare_b.length);
        double total = 0, xor;
        
        for(int i = 0; i < key_b.length; i++){
            int a = key_b[i];
            int b = compare_b[i];
            xor = (double)(a ^ b); 
            xor = a * (Math.pow(2 ,8 * (key_b.length - i - 1)));
            total += xor;
        }

        return total;
    }
}