package de.dhbw.console;

import java.util.Scanner;

public class GetUserInput {

  public GetUserInput(StringCallback callback) {
    Scanner in = new Scanner(System.in);
    System.out.print("> ");
    try {
      callback.onInput(in.nextLine());
    } catch (Exception ignored) {
    }
  }
}
