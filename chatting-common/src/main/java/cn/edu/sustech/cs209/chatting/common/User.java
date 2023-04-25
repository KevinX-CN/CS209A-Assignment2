package cn.edu.sustech.cs209.chatting.common;

import com.alibaba.fastjson.JSONObject;

public class User {
  private final String UserName;
  private final String PassWord;

  public User(String UN, String PWD) {
    this.UserName = UN;
    this.PassWord = PWD;
  }

  public User(JSONObject J) {
    this.UserName = J.getString("UserName");
    this.PassWord = J.getString("PassWord");
  }

  public boolean Identify(String PWD) {
    return this.PassWord.equals(PWD);
  }

  public String GetUserName() {
    return this.UserName;
  }
  public String GetUserPassWord() {
    return this.PassWord;
  }

  public JSONObject ToJSON()
  {
    JSONObject UserJSON=new JSONObject();
    UserJSON.put("UserName",this.UserName);
    UserJSON.put("PassWord",this.PassWord);
    return UserJSON;
  }
}