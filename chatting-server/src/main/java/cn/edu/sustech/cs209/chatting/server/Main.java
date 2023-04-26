package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Convert;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

  public static void main(String[] args) {
    String DataLine3="234,345";
    if (DataLine3.split(",")[1].equals("234")) {
      System.out.println(DataLine3.split(",")[0]);
    } else {
      System.out.println(DataLine3.split(",")[1]);
    }
  }

}
