package bdcc.chain;

import java.util.LinkedList;

public class TransactionManager {
    LinkedList<TransactionRegistry> halted_transactions;
    
    public TransactionManager(){

        halted_transactions = new LinkedList<TransactionRegistry>();

    }
    public Transaction addOrCheckTransaction(Transaction transaction, String submitter){    //returns completed transactions
        //first verify buyer can buy
        checkUserWallet(transaction.getBuyer());
        
        //then verify transactionexists
        for(TransactionRegistry temp: halted_transactions){
            if(temp.checkIfCorresponding(transaction, submitter))
            {
                halted_transactions.remove(temp);
                if(temp.getTransaction().getBuyer().equals("Server"))
                    System.out.println("Successfully transferred " + temp.getTransaction().getAmount() 
                        + " to user " + Crypto.toHex(temp.getTransaction().getSeller()));
                else if(temp.getTransaction().getSeller().equals("Server"))
                System.out.println("Successfully received " + temp.getTransaction().getAmount() 
                    + " from user " + Crypto.toHex(temp.getTransaction().getBuyer()));
                return temp.getTransaction();
            }
        }

        halted_transactions.add(new TransactionRegistry(transaction, submitter));
        return null;

    }

    private double checkUserWallet(String user_id){
        double wallet = 10.0;
        for(TransactionRegistry temp: halted_transactions){
            if(user_id.equals(temp.getTransaction().getBuyer()))
                wallet -= temp.getTransaction().getAmount();
            else if(user_id.equals(temp.getTransaction().getSeller()))
                wallet += temp.getTransaction().getAmount();
        }
        System.out.println("User has " + wallet + " coins");
        return wallet;
    }

    public class TransactionRegistry{
        Transaction transaction;
        String submitter_id;
        Boolean buyer_submitter;

        TransactionRegistry(Transaction new_t, String submitter){
            transaction = new_t;
            submitter_id = submitter;
            buyer_submitter = new_t.getBuyer().equals(submitter);
        }

        Boolean checkIfCorresponding(Transaction confirmation, String new_submitter){
            if(transaction.getBuyer().equals(confirmation.getBuyer()) &&
                transaction.getSeller().equals(confirmation.getSeller()) &&
                transaction.getAmount() == confirmation.getAmount()){

                if((buyer_submitter && new_submitter.equals(transaction.getSeller())) ||
                    ((!buyer_submitter) && new_submitter.equals(transaction.getBuyer())))
                        return true;
            }
            return false;
        }

        Transaction getTransaction(){
            return transaction;
        }

        String getSubmitter(){
            return submitter_id;
        }

        Boolean getIfSubmitterBuyer(){
            return buyer_submitter;
        }

    }
}