package de.dhbw.bank;

public class BankAccount {

  private String owner;

  private String iban;

  private double balance;

  BankAccount(String owner, String iban, double balance) {
    this.owner = owner;
    this.iban = iban;
    this.balance = balance;
  }

  public double getBalance() {
    return balance;
  }

  public String getIban() {
    return iban;
  }

  public String getOwner() {
    return owner;
  }

  void setBalance(double balance) {
    this.balance = balance;
  }

  @Override
  public String toString() {
    return "BankAccount{" +
        "owner='" + owner + '\'' +
        ", iban='" + iban + '\'' +
        ", balance=" + balance +
        '}';
  }
}
