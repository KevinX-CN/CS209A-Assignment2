package cn.edu.sustech.cs209.chatting.common;

import java.util.ArrayList;
import java.util.List;

public class Convert {

  public static List<String> StringToList(String S) {
    S = S.substring(1, S.length() - 1);
    String[] SA = S.split("," + " ");
    List<String> SL = new ArrayList<>();
    for (int i = 0; i < SA.length; i++) {
      SL.add(SA[i]);
    }
    return SL;
  }
}
