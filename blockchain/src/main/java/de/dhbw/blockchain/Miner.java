package de.dhbw.blockchain;

public class Miner {

  private Wallet wallet;

  private String name;

  public Miner(String name) {
    this.name = name;
    this.wallet = new Wallet();
  }

  public String getName() {
    return name;
  }

  public Wallet getWallet() {
    return wallet;
  }
}
