package main.java.bdcc.chain;

import java.security.SecureRandom;
import java.util.*;
import java.util.Random;
import java.util.Date;

public class NodeBlockChain{
    private static NodeBlockChain chain_instance = null;
    Random nonce_generator;
    NodeBlock block;

    private NodeBlockChain(){
        Date date = new Date();
        nonce_generator = new Random(date.getTime());
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
        private int hash;
        private Boolean hasNext;
        private LinkedList<Transaction> transactions;
        private int maxTransactions;
        private int currentTransactions;

        public NodeBlock(){
            nonce = generateNonce();
            transactions = new LinkedList<Transaction>();
            hasNext = false;
            maxTransactions = 10;
            currentTransactions = 0;
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