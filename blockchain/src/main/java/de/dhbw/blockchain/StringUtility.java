package de.dhbw.blockchain;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;

public class StringUtility {

  public static final String ANSI_BLUE = "\u001B[34m";
  public static final String ANSI_RESET = "\u001B[0m";

  public static String blueOutput(String s) {
    return ANSI_BLUE + s + ANSI_RESET;
  }

  public static String applySha256(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      StringBuilder hexString = new StringBuilder();

      for (byte b : hash) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) {
          hexString.append('0');
        }
        hexString.append(hex);
      }

      return hexString.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
    Signature dsa;
    byte[] output;

    try {
      dsa = Signature.getInstance("ECDSA", "BC");
      dsa.initSign(privateKey);
      byte[] strByte = input.getBytes();
      dsa.update(strByte);
      output = dsa.sign();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return output;
  }

  public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
    try {
      Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
      ecdsaVerify.initVerify(publicKey);
      ecdsaVerify.update(data.getBytes());
      return ecdsaVerify.verify(signature);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static String getStringFromKey(Key key) {
    if (key == null) return "";
    return Base64.getEncoder().encodeToString(key.getEncoded());
  }

  public static PublicKey getKeyFromString(String key){
    try{
      byte[] byteKey = Base64.getDecoder().decode(key.getBytes());
      X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
      KeyFactory kf = KeyFactory.getInstance("ECDSA", "BC");

      return kf.generatePublic(X509publicKey);
    }
    catch(Exception e){
      //e.printStackTrace();
    }

    return null;
  }

  public static String getMerkleRoot(ArrayList<Transaction> transactions) {
    int count = transactions.size();
    ArrayList<String> previousTreeLayer = new ArrayList<>();

    for (Transaction transaction : transactions) {
      previousTreeLayer.add(transaction.getId());
    }

    ArrayList<String> treeLayer = previousTreeLayer;
    while (count > 1) {
      treeLayer = new ArrayList<>();

      for (int i = 1; i < previousTreeLayer.size(); i++) {
        treeLayer.add(applySha256(previousTreeLayer.get(i - 1)) + previousTreeLayer.get(i));
      }

      count = treeLayer.size();
      previousTreeLayer = treeLayer;
    }

    return (treeLayer.size() == 1) ? treeLayer.get(0) : "";
  }

  public static String getDifficultyString(int difficulty) {
    return new String(new char[difficulty]).replace('\0', '0');
  }
}