package cn.edu.sustech.cs209.chatting.common;

import com.alibaba.fastjson.JSONObject;

public class User {

  private final String userName;
  private final String passWord;

  public User(String un, String pwd) {
    this.userName = un;
    this.passWord = pwd;
  }

  public User(JSONObject j) {
    this.userName = j.getString("userName");
    this.passWord = j.getString("passWord");
  }

  public boolean identify(String pwd) {
    return this.passWord.equals(pwd);
  }

  public String getUserName() {
    return this.userName;
  }

  public String getPassWord() {
    return this.passWord;
  }


  public JSONObject toJson() {
    JSONObject userJson = new JSONObject();
    userJson.put("userName", this.userName);
    userJson.put("passWord", this.passWord);
    return userJson;
  }
}