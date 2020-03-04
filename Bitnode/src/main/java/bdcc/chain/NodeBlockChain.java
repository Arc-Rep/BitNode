package main.java.bdcc.chain;

import java.security.SecureRandom;
import java.util.*;

public class NodeBlockChain{
    NodeBlock block;

    public NodeBlockChain(){
        block = new NodeBlock(0/*nonce*/);
    }

    public class NodeBlock{
        private int nonce;
        private int hash;
        private Bool hasNext;
        private LinkedList<Transaction> transactions;
        private int maxTransactions;
        private int currentTransactions;

        public NodeBlock(int blockNonce){
            nonce = blockNonce;
            transactions = new LinkedList<Transactions>();
            hasNext = false;
            maxTransactions = 5;
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
                if(hasNext == false){
                    NodeBlock nextBlock = new NodeBlock(0/*new nonce*/);
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