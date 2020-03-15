package bdcc.chain;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.LinkedList;

public class NodeBlockChain{
    private static NodeBlockChain chain_instance = null;
    SecureRandom nonce_generator;
    NodeBlock block;

    private NodeBlockChain(){
        Date date = new Date();
        try{
            nonce_generator = SecureRandom.getInstance("SHA1PRNG");
            nonce_generator.setSeed(date.getTime());
        }
        catch(NoSuchAlgorithmException e){
            System.out.println("No such algorithm for nonce generation");
        }
        block = new NodeBlock();
    }

    public static NodeBlockChain getChainManager(){
        if(chain_instance == null) 
            return new NodeBlockChain();
            
        else return chain_instance;
    }

    private int generateNonce(){
        return nonce_generator.nextInt();
    }

    public class NodeBlock{
        private int nonce;
        private int prev_hash;
        private Boolean hasNext;
        private int maxTransactions;
        private int currentTransactions;
        private LinkedList<Transaction> transactions;


        public NodeBlock(){
            nonce = generateNonce();
            transactions = new LinkedList<Transaction>();
            hasNext = false;
            maxTransactions = 10;
            currentTransactions = 0;
        }

        public LinkedList<Transaction> getTransactions(){
            return this.transactions;
        }

        public void addTransaction(Transaction newTransaction){
            if(currentTransactions < maxTransactions)
            {
                transactions.add(newTransaction);
                currentTransactions++;
            }
            else
            {
                NodeBlock nextBlock = null;
                if(hasNext == false){
                    nextBlock = new NodeBlock();
                    //compute hash to new block
                    hasNext = true;
                }
                else{
                    //NodeBlock nextBlock = computed Block from hash
                }
                nextBlock.addTransaction(newTransaction);
            }
        }

    }
    
}