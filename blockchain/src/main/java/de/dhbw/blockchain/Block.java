package de.dhbw.blockchain;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import static de.dhbw.blockchain.StringUtility.blueOutput;

public class Block {
    private final String previousHash;
    private final long timeStamp;
    private final ArrayList<Transaction> transactions = new ArrayList<>();
    private PublicKey miner;
    private double minerReward;
    private String merkleRoot;
    private String hash;
    private int nonce;

    public Block(String previousHash) {
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public String getHash() {
        return this.hash;
    }

    public String getPreviousHash() {
        return this.previousHash;
    }

    public String calculateHash() {
        return StringUtility.applySha256(previousHash + timeStamp + nonce + merkleRoot + StringUtility.getStringFromKey(miner) + minerReward);
    }

    public void mineBlock(int difficulty, Miner m) {
        this.miner = m.getWallet().getPublicKey();
        this.minerReward = Configuration.instance.minerReward;

        TransactionOutput reward = new TransactionOutput(m.getWallet().getPublicKey(), minerReward, "BlockReward-" + merkleRoot + "-" + previousHash);

        merkleRoot = StringUtility.getMerkleRoot(transactions);
        String target = StringUtility.getDifficultyString(difficulty);

        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }

        BlockchainNetwork.getInstance().getUtx0Map().put(reward.getID(), reward);

        System.out.printf(blueOutput("new block [%s] mined by %s%n"), hash, m.getName());
    }

    public void addTransaction(Transaction transaction) {
        if (transaction == null) {
            return;
        }

        if (!Objects.equals(previousHash, "0")) {
            if (!transaction.processTransaction()) {
                System.out.println(blueOutput("transaction failed to process"));
                return;
            }
        }

        transactions.add(transaction);
        System.out.println(blueOutput("transaction added to block"));
    }
}