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
        NodeBlock current = head_block;
        
        if(current.getCurrentTransactions() == 0)
            return true;

        while(current.has_previous){
            if(!current.getPrevHash().equals(current.previous.generateHash()))
                return false;
            
            current = current.getPrevious();
        }
        return current.getHash().equals(current.generateHash());
    }

    
    public void addTransaction(final Transaction newTransaction){
        //first check if the one who pays has enough money
        if(checkUserWallet(newTransaction.getBuyer()) < 0){
            System.out.println("Cancelled a transaction since payer doesn't have enough money");
            return;
        }

        if(head_block.getCurrentTransactions() < head_block.getMaxTransactions())
        {
            head_block.transactions.add(newTransaction);
            head_block.currentTransactions++;
            head_block.setHash(Crypto.hashBlock(head_block,head_block.getNonce()));
        }
        else
        {
            final NodeBlock new_head = new NodeBlock();
            new_head.setPrevious(head_block);
            new_head.setPrevHash(this.head_block.generateHash());
            new_head.transactions.add(newTransaction);
            new_head.hash = new_head.generateHash();
            this.head_block = new_head;
        }
    }

    private double checkUserWallet(String user_id){
        double wallet = 10.0;
        NodeBlock block = head_block;
        while(block != null){
            for(Transaction temp: block.transactions){
                if(user_id.equals(temp.getBuyer()))
                    wallet -= temp.getAmount();
                else if(user_id.equals(temp.getSeller()))
                    wallet += temp.getAmount();
            }
            block = block.previous;
        }
        return wallet;
    }

    public void printChainContents(){
        NodeBlock current_block = head_block;

        for(int index = 1; current_block != null; index++){
            System.out.println("Printing contents of Block #" + index + ":");
            current_block.printBlockContents();
            current_block = current_block.previous;
        }
    }

    public class NodeBlock{
        private final int nonce;
        private String hash;
        private String prev_hash;
        private NodeBlock previous;
        private Boolean has_previous;
        private final int maxTransactions;
        private int currentTransactions;
        private final LinkedList<Transaction> transactions;


        public NodeBlock(){
            nonce = generateNonce();
            transactions = new LinkedList<Transaction>();
            has_previous = false;
            prev_hash = null;
            hash = null;
            maxTransactions = 10;
            currentTransactions = 0;
        }

        public LinkedList<Transaction> getTransactions(){
            return this.transactions;
        }
        
        private int getNonce() {
            return this.nonce;
        }

        public NodeBlock getPrevious(){
            return this.previous;
        }

        public void setPrevious(final NodeBlock block){
            this.previous = block;
            this.has_previous = true;
        }

        public void setHash(String h){
            this.hash = h;
        }

        public void setPrevHash(String h){
            this.prev_hash = h;
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

        public String getPrevHash(){
            return this.prev_hash;
        }

        public String generateHash(){
            return Crypto.hashBlock(this, this.getNonce());
        }

        public void printBlockContents(){
            System.out.println("Block has " + currentTransactions + " out of " + maxTransactions + " transactions:");
            int index = 1;
            for(Transaction current_transaction: transactions)
            {
                System.out.println("=============================================");
                System.out.println("Transaction #" + index + ":");
                System.out.println("Spender ID: " + Crypto.toHex(current_transaction.getBuyer()));
                System.out.println("Receiver ID: " + Crypto.toHex(current_transaction.getSeller()));
                System.out.println("Amount: " + current_transaction.getAmount());
                System.out.println("=============================================");
                index++;
            }
        }

    }
    
}