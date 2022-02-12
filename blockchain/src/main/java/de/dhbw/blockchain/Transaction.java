package de.dhbw.blockchain;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

import static de.dhbw.blockchain.StringUtility.blueOutput;

public class Transaction {

  private final PublicKey sender;
  private final PublicKey recipient;
  private final double value;
  private final ArrayList<TransactionOutput> outputs = new ArrayList<>();
  private final ArrayList<TransactionInput> inputs;
  private String id;
  private byte[] signature;

  public Transaction(PublicKey from, PublicKey to, double value, ArrayList<TransactionInput> inputs) {
    this.sender = from;
    this.recipient = to;
    this.value = value;
    this.inputs = inputs;
  }

  private String calculateHash() {
    BlockchainNetwork.getInstance().incrementTransactionSequence();
    return StringUtility.applySha256(StringUtility.getStringFromKey(sender) + StringUtility.getStringFromKey(recipient)
        + value + BlockchainNetwork.getInstance().getTransactionSequence());
  }

  public void generateSignature(PrivateKey privateKey) {
    String data = StringUtility.getStringFromKey(sender) + StringUtility.getStringFromKey(recipient) + value;
    signature = StringUtility.applyECDSASig(privateKey, data);
  }

  public boolean verifySignature() {
    String data = StringUtility.getStringFromKey(sender) + StringUtility.getStringFromKey(recipient) + value;
    return !StringUtility.verifyECDSASig(sender, data, signature);

  }

  public boolean processTransaction() {
    if (verifySignature()) {
      System.out.println(blueOutput("#transaction signature failed to verify"));
      return false;
    }

    for (TransactionInput i : inputs) {
      i.setUtx0(BlockchainNetwork.getInstance().getUtx0Map().get(i.getId()));
    }

    if (getInputsValue() <= 0) {
      System.out.println(blueOutput("#transaction input to small | " + getInputsValue()));
      return false;
    }

    double leftOver = getInputsValue() - value;
    id = calculateHash();
    outputs.add(new TransactionOutput(recipient, value, id));
    outputs.add(new TransactionOutput(sender, leftOver, id));

    for (TransactionOutput o : outputs) {
      BlockchainNetwork.getInstance().getUtx0Map().put(o.getID(), o);
    }

    for (TransactionInput i : inputs) {
      if (i.getUTX0() == null) {
        continue;
      }
      BlockchainNetwork.getInstance().getUtx0Map().remove(i.getUTX0().getID());
    }

    return true;
  }

  public double getInputsValue() {
    double total = 0;

    for (TransactionInput i : inputs) {
      if (i.getUTX0() == null) {
        continue;
      }
      total += i.getUTX0().getValue();
    }

    return total;
  }

  public double getOutputsValue() {
    double total = 0;

    for (TransactionOutput o : outputs) {
      total += o.getValue();
    }

    return total;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public PublicKey getSender() {
    return sender;
  }

  public PublicKey getRecipient() {
    return recipient;
  }

  public double getValue() {
    return value;
  }

  public ArrayList<TransactionInput> getInputs() {
    return inputs;
  }

  public ArrayList<TransactionOutput> getOutputs() {
    return outputs;
  }
}