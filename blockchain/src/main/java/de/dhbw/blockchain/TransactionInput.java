package de.dhbw.blockchain;

public class TransactionInput {
    private final String id;
    private TransactionOutput utx0;

    public TransactionInput(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public TransactionOutput getUTX0() {
        return utx0;
    }

    public void setUtx0(TransactionOutput utx0) {
        this.utx0 = utx0;
    }
}