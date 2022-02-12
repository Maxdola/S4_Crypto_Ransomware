package de.dhbw.blockchain;

import com.google.gson.GsonBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static de.dhbw.blockchain.StringUtility.blueOutput;

public class BlockchainNetwork {

  private static BlockchainNetwork instance;

  private Transaction genesisTransaction;
  private HashMap<String, TransactionOutput> utx0Map = new HashMap<>();
  private ArrayList<Block> blockchain = new ArrayList<>();
  private int transactionSequence = 0;

  private List<Miner> miners = new ArrayList<>();

  private Block previousBlock;

  public Wallet satoshiNakamoto;

  private BlockchainNetwork() {
    instance = this;
    Security.addProvider(new BouncyCastleProvider());

    miners.add(new Miner("Bob"));
    miners.add(new Miner("Eve"));
    miners.add(new Miner("Sam"));

    this.satoshiNakamoto = new Wallet();

    //TODO send genesis coins to coinbase!
    this.genesisTransaction = new Transaction(satoshiNakamoto.getPublicKey(), satoshiNakamoto.getPublicKey(), 1, null);
    this.genesisTransaction.generateSignature(satoshiNakamoto.getPrivateKey());
    this.genesisTransaction.setId("0");
    this.genesisTransaction.getOutputs().add(
        new TransactionOutput(this.genesisTransaction.getRecipient(),
            this.genesisTransaction.getValue(), this.genesisTransaction.getId())
    );

    this.utx0Map.put(
        this.genesisTransaction.getOutputs().get(0).getID(),
        this.genesisTransaction.getOutputs().get(0)
    );

    System.out.println(blueOutput("creating and mining genesis block"));
    Block genesisBlock = new Block("0");
    genesisBlock.addTransaction(this.genesisTransaction);
    addBlock(genesisBlock);

    //System.out.println("walletA (balance) | " + walletA.getBalance());
    //System.out.println("walletB (balance) | " + walletB.getBalance());

    miners.forEach(m -> {
      System.out.printf(blueOutput("%10s balance | %f%n"), m.getName(), m.getWallet().getBalance());
    });
    System.out.printf(blueOutput("%10s balance | %f%n"), "Satoshi", satoshiNakamoto.getBalance());


    //System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(this));

    isChainValid();
  }

  public boolean isChainValid() {
    Block currentBlock;
    Block previousBlock;
    String hashTarget = StringUtility.getDifficultyString(Configuration.instance.difficulty);
    HashMap<String, TransactionOutput> tempUTXOs = new HashMap<>();
    tempUTXOs.put(this.genesisTransaction.getOutputs().get(0).getID(), this.genesisTransaction.getOutputs().get(0));

    for (int i = 1; i < this.blockchain.size(); i++) {
      currentBlock = this.blockchain.get(i);
      previousBlock = this.blockchain.get(i - 1);

      if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
        System.out.println(blueOutput("#current hashes not equal"));
        return false;
      }

      if (!previousBlock.getHash().equals(currentBlock.getPreviousHash())) {
        System.out.println(blueOutput("#trevious hashes not equal"));
        return false;
      }

      if (!currentBlock.getHash().substring(0, Configuration.instance.difficulty).equals(hashTarget)) {
        System.out.println(blueOutput("#block not mined"));
        return false;
      }

      TransactionOutput tempOutput;
      for (int t = 0; t < currentBlock.getTransactions().size(); t++) {
        Transaction currentTransaction = currentBlock.getTransactions().get(t);

        if (currentTransaction.verifySignature()) {
          System.out.println(blueOutput("#Signature on de.dhbw.blockchain.Transaction(" + t + ") is Invalid"));
          return false;
        }

        if (currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
          System.out.println(blueOutput("#Inputs are not equal to oututs on de.dhbw.blockchain.Transaction(" + t + ")"));
          return false;
        }

        for (TransactionInput input : currentTransaction.getInputs()) {
          tempOutput = tempUTXOs.get(input.getId());

          if (tempOutput == null) {
            System.out.println(blueOutput("#referenced input on transaction(" + t + ") is missing"));
            return false;
          }

          if (input.getUTX0().getValue() != tempOutput.getValue()) {
            System.out.println(blueOutput("#referenced input on transaction(" + t + ") value invalid"));
            return false;
          }

          tempUTXOs.remove(input.getId());
        }

        for (TransactionOutput output : currentTransaction.getOutputs()) {
          tempUTXOs.put(output.getID(), output);
        }

        if (currentTransaction.getOutputs().get(0).getRecipient() != currentTransaction.getRecipient()) {
          System.out.println(blueOutput("#transaction(" + t + ") output recipient is invalid"));
          return false;
        }

        if (currentTransaction.getOutputs().get(1).getRecipient() != currentTransaction.getSender()) {
          System.out.println(blueOutput("#transaction(" + t + ") output 'change' is not sender"));
          return false;
        }
      }
    }
    System.out.println(blueOutput("blockchain valid"));
    return true;
  }

  public boolean buyBtc(Wallet wallet, double amount) {

    if (this.satoshiNakamoto.getBalance() < amount) {
      return false;
    }

    Transaction t = this.satoshiNakamoto.sendFunds(wallet.getPublicKey(), amount);
    this.addTransaction(t);

    return true;
  }

  public void addTransaction(Transaction t) {
    Block b = new Block(this.previousBlock.getHash());
    b.addTransaction(t);
    this.addBlock(b);
  }

  private void addBlock(Block newBlock) {
    Miner m = miners.get(ThreadLocalRandom.current().nextInt(miners.size()) % miners.size());
    newBlock.mineBlock(Configuration.instance.difficulty, m);
    this.blockchain.add(newBlock);
    this.previousBlock = newBlock;

    try {
      File f = Path.of("blockchain.json").toAbsolutePath().toFile();
      f.createNewFile();
      FileWriter fw = new FileWriter(f);
      new GsonBuilder().setPrettyPrinting().create().toJson(this, fw);
      fw.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static BlockchainNetwork getInstance() {
    if (instance == null) instance = new BlockchainNetwork();
    return instance;
  }

  public void incrementTransactionSequence() {
    this.transactionSequence++;
  }

  public int getTransactionSequence() {
    return transactionSequence;
  }

  public HashMap<String, TransactionOutput> getUtx0Map() {
    return this.utx0Map;
  }
}