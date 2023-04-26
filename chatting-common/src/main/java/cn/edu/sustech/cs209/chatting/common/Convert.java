package cn.edu.sustech.cs209.chatting.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Convert {

  public static List<String> stringToList(String s) {
    s = s.substring(1, s.length() - 1);
    String[] sa = s.split("," + " ");
    return new ArrayList<>(Arrays.asList(sa));
  }
}