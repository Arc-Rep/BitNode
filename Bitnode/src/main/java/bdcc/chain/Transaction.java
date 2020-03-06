package main.java.bdcc.chain;

public class Transaction{
    private int buyerId;
    private int sellerId;
    private int cashAmount;

    public Transaction(int buyer, int seller, int cash){
        buyerId = buyer;
        sellerId = seller;
        cashAmount = cash;
    }

    public int getBuyer(){
        return buyerId;
    }

    public int getSeller(){
        return sellerId;
    }

    public int getAmount(){
        return cashAmount;
    }
}