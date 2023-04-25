package cn.edu.sustech.cs209.chatting.common;

import com.alibaba.fastjson.annotation.JSONType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Convert {

  public static List<String> stringToList(String s) {
    s = s.substring(1, s.length() - 1);
    String[] sa = s.split("," + " ");
    List<String> sl = new ArrayList<>();
    Collections.addAll(sl, sa);
    return sl;
  }
}
