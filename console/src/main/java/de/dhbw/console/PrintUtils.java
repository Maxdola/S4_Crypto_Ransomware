package de.dhbw.console;

public class PrintUtils {

  public PrintUtils() {

  }

  public void printLogo() {
    System.out.println(" __          __  _                          \n" +
        " \\ \\        / / | |                         \n" +
        "  \\ \\  /\\  / /__| | ___ ___  _ __ ___   ___ \n" +
        "   \\ \\/  \\/ / _ \\ |/ __/ _ \\| '_ ` _ \\ / _ \\\n" +
        "    \\  /\\  /  __/ | (_| (_) | | | | | |  __/\n" +
        "     \\/  \\/ \\___|_|\\___\\___/|_| |_| |_|\\___|"
    );
    System.out.println("Mr Clue Less.");
    System.out.println("write 'help' to show all possible commands.");
  }

  public void printHelp() {
    System.out.println("-\t show balance");
    System.out.println("-\t show recipient");
    System.out.println("-\t pay [amount] to [address]");
    System.out.println("-\t exchange [amount]");
    System.out.println("-\t check payment");
    System.out.println("-\t launch http://www.trust-me.mcg/report.jar");
    System.out.println("-\t exit");
  }

}
