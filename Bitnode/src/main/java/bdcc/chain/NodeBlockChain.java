package bdcc.chain;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.LinkedList;

public class NodeBlockChain{
    private static NodeBlockChain chain_instance = null;
    SecureRandom nonce_generator;
    NodeBlock head_block;

    private NodeBlockChain(){
        Date date = new Date();
        try{
            nonce_generator = SecureRandom.getInstance("SHA1PRNG");
            nonce_generator.setSeed(date.getTime());
        }
        catch(NoSuchAlgorithmException e){
            System.out.println("No such algorithm for nonce generation");
        }
        head_block = new NodeBlock();
    }

    public static NodeBlockChain getChainManager(){
        if(chain_instance == null) 
            return new NodeBlockChain();
            
        else return chain_instance;
    }

    private int generateNonce(){
        return nonce_generator.nextInt();
    }

    public Boolean verifyValidity(){
        // compute hash for each block and compare
    }

    
    public void addTransaction(Transaction newTransaction){
        if(head_block.getCurrentTransactions() < head_block.getMaxTransactions())
        {
            head_block.transactions.add(newTransaction);
            head_block.currentTransactions++;
        }
        else
        {

            NodeBlock new_head = new NodeBlock();
            new_head.setNext(head_block);
            //compute hash of previous head to new block
            
            new_head.transactions.add(newTransaction);
            this.head_block = new_head;
        }
    }

    public class NodeBlock{
        private int nonce;
        private String prev_hash;
        private NodeBlock next;
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

        public NodeBlock getNext(){
            return this.next;
        }

        public void setNext(NodeBlock block){
            this.next = block;
            this.hasNext = true;
        }

        public int getMaxTransactions(){
            return this.maxTransactions;
        }

        public int getCurrentTransactions(){
            return this.currentTransactions;
        }

    }
    
}