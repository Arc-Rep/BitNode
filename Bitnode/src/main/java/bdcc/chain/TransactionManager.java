package bdcc.chain;

import java.util.LinkedList;

public class TransactionManager {
    LinkedList<TransactionRegistry> halted_transactions;
    
    public TransactionManager(){

        halted_transactions = new LinkedList<TransactionRegistry>();

    }
    public Transaction addOrCheckTransaction(Transaction transaction, String submitter){    //returns completed transactions

        //first verify transactionexists
        for(TransactionRegistry temp: halted_transactions){
            if(temp.checkIfCorresponding(transaction, submitter))
            {
                halted_transactions.remove(temp);

                return temp.getTransaction();
            }
        }

        halted_transactions.add(new TransactionRegistry(transaction, submitter));
        return null;

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