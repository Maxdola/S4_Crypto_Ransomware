package de.dhbw.blockchain;

import com.google.gson.GsonBuilder;

public class BlockchainTestApplication {

  public static void main(String[] args) {
    BlockchainNetwork.getInstance();
    //System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(bn));
  }

}
