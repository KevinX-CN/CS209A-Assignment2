package cn.edu.sustech.cs209.chatting.common;

import com.alibaba.fastjson.JSONObject;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public class User {
  private String UserName;
  private String PassWord;
  private List<UUID> ChatRoomList;

  public User(String UN, String PWD) {
    this.UserName = UN;
    this.PassWord = PWD;
  }

  public User(JSONObject J) {
    this.UserName = J.getString("UserName");
    this.PassWord = J.getString("PassWord");
    //this.ChatRoomList=(Convert.StringToList(J.getString("ChatRoomList"));
  }

  public boolean Identify(String PWD) {
    return this.PassWord.equals(PWD);
  }

  public void AddChatRoom(UUID CID) {
    ChatRoomList.add(CID);
  }

  public void RemoveChatRoom(UUID CID) {
    ChatRoomList.remove(CID);
  }

  public String GetUserName() {
    return this.UserName;
  }
  public String GetUserPassWord() {
    return this.PassWord;
  }

  public List<UUID> GetChatRoomList() {
    return this.ChatRoomList;
  }

  public JSONObject ToJSON()
  {
    JSONObject UserJSON=new JSONObject();
    UserJSON.put("UserName",this.UserName);
    UserJSON.put("PassWord",this.PassWord);
    UserJSON.put("ChatRoomList",this.ChatRoomList);
    return UserJSON;
  }
}