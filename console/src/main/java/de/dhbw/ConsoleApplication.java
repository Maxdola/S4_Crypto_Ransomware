package de.dhbw;

import de.dhbw.bank.Bank;
import de.dhbw.bank.BankAccount;
import de.dhbw.blockchain.BlockchainNetwork;
import de.dhbw.blockchain.StringUtility;
import de.dhbw.blockchain.Wallet;
import de.dhbw.console.GetUserInput;
import de.dhbw.console.PrintUtils;
import de.dhbw.console.StringCallback;
import de.dhbw.report.IReportGenerator;
import de.dhbw.report.ReportManger;

import java.security.PublicKey;

public class ConsoleApplication {

  public static void main(String[] args) {
    new ConsoleApplication();
  }

  private StringCallback handleCommand;

  private PrintUtils printUtils;

  private Bank bank;
  private String iban;
  private Wallet wallet;

  private IReportGenerator reportGenerator;

  private boolean run = true;

  private ConsoleApplication() {
    BlockchainNetwork.getInstance();
    printUtils = new PrintUtils();

    this.wallet = new Wallet();

    this.bank = new Bank();

    this.reportGenerator = new ReportManger().getReportGenerator();

    this.iban = bank.generateAccount("Clue Less", 5000).getIban();

    this.handleCommand = cmd -> {

      switch (cmd.toLowerCase()) {
        case "help":
          printUtils.printHelp();
        return;
        case "show balance":
          BankAccount ba = bank.getBankAccount(this.iban);
          if (ba != null) {
            System.out.printf("You currently have %.2f € in your account (%s). %n", ba.getBalance(), ba.getIban());
          } else {
            System.out.println("Your bank account was not found.");
          }
          System.out.printf("You currenty have %.8f BTC in your wallet (%s)%n", wallet.getBalance(), StringUtility.getStringFromKey(wallet.getPublicKey()));
          return;
        case "launch":
        case "launch http://www.trust-me.mcg/report.jar":
          this.reportGenerator.generate();
          return;
        case "show recipient":
          this.reportGenerator.info();
          return;
        case "check payment":
          this.reportGenerator.check();
          return;
        case "exit":
          System.exit(0);
        return;
      }

      if (cmd.toLowerCase().startsWith("exchange")) {
        String[] parts = cmd.split(" ");
        if (parts.length >= 2) {
          double amount;
          try {
            amount = Double.parseDouble(parts[1]);
            exchange(amount);
          } catch (NumberFormatException e) {
            System.out.printf("'%s' is not a number. Please try again.%s", parts[1]);
          }
          return;
        }
        System.out.println("Format: 'exchange [amount] BTC'");
        return;
      } else if (cmd.toLowerCase().startsWith("pay")) {
        String[] parts = cmd.split(" ");
        if (parts.length != 5) {
          System.out.println("Format: pay [amount] BTC to [address]");
          return;
        }
        double amount;
        try {
          amount = Double.parseDouble(parts[1]);
        } catch (NumberFormatException e) {
          System.out.printf("'%s' is not a number. Please try again.%s", parts[1]);
          return;
        }

        PublicKey pk = StringUtility.getKeyFromString(parts[4]);
        if (pk == null) {
          System.out.println("invalid BTC address.");
          return;
        }
        System.out.println(StringUtility.getStringFromKey(pk));

        BlockchainNetwork.getInstance().addTransaction(wallet.sendFunds(pk, amount));
        return;

      }

      System.out.println("invalid command. type 'help' to see all commands.");

      return;
    };

    this.run();
  }

  private void exchange(double amount) {
    BankAccount ba = bank.getBankAccount(this.iban);
    if (ba == null) {
      System.out.println("Your bank account was not found.");
      return;
    }
    if (amount <= 0) {
      System.out.println("The exchange amount needs to be greater than 0.");
      return;
    }

    double euro = amount / 0.000019;

    if (euro > ba.getBalance()) {
      System.out.println("You dont have enough money in your account.");
      return;
    }

    this.bank.updateBalance(ba.getIban(), euro * -1);

    if (ba.getBalance() < amount) {
      System.out.println("You dont have enough money to exchange '%.2d €' worth of BTC.");
    }

    if (BlockchainNetwork.getInstance().buyBtc(wallet, amount)) {
      System.out.println("You funds were successfully exchanged.");
      System.out.println("Type 'show balance' to see your current balance.");
    } else {
      System.out.println("Sorry, there are no BTC available to buy.");
    }

  }

  private void run() {
    printUtils.printLogo();
    while (run) {
      new GetUserInput(this.handleCommand);
    }
  }

}
