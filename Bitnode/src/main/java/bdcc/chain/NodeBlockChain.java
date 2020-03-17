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
        final Date date = new Date();
        try{
            nonce_generator = SecureRandom.getInstance("SHA1PRNG");
            nonce_generator.setSeed(date.getTime());
        }
        catch(final NoSuchAlgorithmException e){
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
        Boolean check = false;
        NodeBlock current = new NodeBlock();
        current = head_block;
        while(current != null){
            if(current.getHash() == current.generateHash()){
                check = true;
            }
            else{
                return false;
            }
            current = current.getNext();
        }
        return check;
    }

    
    public void addTransaction(final Transaction newTransaction){
        if(head_block.getCurrentTransactions() < head_block.getMaxTransactions())
        {
            head_block.transactions.add(newTransaction);
            head_block.currentTransactions++;
            head_block.setHash(Crypto.hashBlock(head_block,head_block.getNonce()));
        }
        else
        {
            final NodeBlock new_head = new NodeBlock();
            new_head.setNext(head_block);
            new_head.transactions.add(newTransaction);
            new_head.hash = new_head.generateHash();
            this.head_block = new_head;
        }
    }

    public class NodeBlock{
        private final int nonce;
        private String hash;
        private NodeBlock next;
        private Boolean hasNext;
        private final int maxTransactions;
        private int currentTransactions;
        private final LinkedList<Transaction> transactions;


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
        
        private int getNonce() {
            return this.nonce;
        }

        public NodeBlock getNext(){
            return this.next;
        }

        public void setNext(final NodeBlock block){
            this.next = block;
            this.hasNext = true;
        }

        public void setHash(String h){
            this.hash = h;
        }

        public int getMaxTransactions(){
            return this.maxTransactions;
        }

        public int getCurrentTransactions(){
            return this.currentTransactions;
        }

        public String getHash(){
            return this.hash;
        }

        public String generateHash(){
            return Crypto.hashBlock(this, this.getNonce());
        }

    }
    
}