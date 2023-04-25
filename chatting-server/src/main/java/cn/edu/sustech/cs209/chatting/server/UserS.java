package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.User;
import com.alibaba.fastjson.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserS {

  private static final String FilePath = "data/server/user.config";
  private static Map<String, User> AllUserMap = new HashMap<>();
  private static Map<String, User> UserMap = new HashMap<>();

  public static boolean HaveUser(String UN) {
    return UserMap.containsKey(UN);
  }

  public static boolean AddUser(String UN, String PWD) {
    if (!HaveUser(UN)) {
      User U = new User(UN, PWD);
      UserMap.put(U.GetUserName(), U);
      AllUserMap.put(U.GetUserName(), U);
      return true;
    }
    return false;
  }

  public static void RemoveUser(String UN) {
    UserMap.remove(UN);
  }

  public static List<String> GetAllUserName() {
    return new ArrayList<>(UserMap.keySet());
  }

  public static void Save() throws IOException {
    File F = new File(FilePath);
    F.createNewFile();
    BufferedWriter FW = new BufferedWriter(new FileWriter(F));
    for (User i : AllUserMap.values()) {
      FW.write(i.ToJSON() + "\n");
      FW.flush();
    }
  }

  public static boolean Identify(String UN, String PWD) {
    if (AllUserMap.get(UN).Identify(PWD)) {
      UserMap.put(UN, AllUserMap.get(UN));
      return true;
    }
    return false;
  }

  public static void Load() throws IOException {
    AllUserMap = new HashMap<>();
    BufferedReader FR = new BufferedReader(new InputStreamReader(
      Files.newInputStream(Paths.get(FilePath))));
    String FileLine;
    while ((FileLine = FR.readLine()) != null) {
      User U = new User(JSONObject.parseObject(FileLine));
      AllUserMap.put(U.GetUserName(), U);
    }
  }

  public static void main(String[] args) throws IOException {
    UserMap.put("A", new User("A", ""));
    UserMap.put("B", new User("B", ""));
    UserMap.put("C", new User("C", ""));
    Save();
    UserMap = new HashMap<>();
    Load();
  }
}