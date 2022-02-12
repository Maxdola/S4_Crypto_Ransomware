package de.dhbw.bank;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Bank {

  private List<BankAccount> accounts = new ArrayList<>();

  public Bank() {}

  public BankAccount generateAccount(String name, int balance) {
    BankAccount ba = new BankAccount(name, generateAccountNumber(), balance);
    this.accounts.add(ba);
    return ba;
  }

  public BankAccount updateBalance(String iban, double amount) {
    BankAccount ba = this.accounts.stream().filter(b -> b.getIban().equals(iban)).findFirst().orElse(null);
    if (ba != null) ba.setBalance(ba.getBalance() + amount);
    return ba;
  }

  public BankAccount getBankAccount(String iban) {
    return this.accounts.stream().filter(b -> b.getIban().equals(iban)).findFirst().orElse(null);
  }

  public static String generateAccountNumber() {
    String countryCode = "DE";
    String gerRecognitionNumber = "131400";
    String blz = "85203647";
    String accountNumber = "";
    String bban;
    String iban;

    for (int i = 0; i < 10; i++) {
      int random = new Random().nextInt(9);
      accountNumber = accountNumber + random;
    }

    bban = blz + accountNumber;

    String checkSumString = bban + gerRecognitionNumber;

    //calculate the checksum
    BigInteger checkSum = new BigInteger(checkSumString).mod(new BigInteger("97"));

    if (checkSum.intValue() < 10) {
      checkSumString = "0" + checkSum.toString();
    } else {
      checkSumString = checkSum.toString();
    }

    return countryCode + checkSumString + blz + accountNumber;
  }

}
