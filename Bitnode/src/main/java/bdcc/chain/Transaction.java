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

    public getBuyer(){
        return buyerId;
    }

    public getSeller(){
        return sellerId;
    }

    public getAmount(){
        return cashAmount;
    }
}