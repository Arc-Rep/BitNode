package bdcc.chain;

public class Transaction{
    private String buyerId;
    private String sellerId;
    private double cashAmount;

    public Transaction(String buyer, String seller, double cash){
        buyerId = buyer;
        sellerId = seller;
        cashAmount = cash;
    }

    public String getBuyer(){
        return buyerId;
    }

    public String getSeller(){
        return sellerId;
    }

    public double getAmount(){
        return cashAmount;
    }
}