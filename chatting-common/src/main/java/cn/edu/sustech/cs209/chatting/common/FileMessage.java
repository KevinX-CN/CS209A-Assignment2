package cn.edu.sustech.cs209.chatting.common;

import com.alibaba.fastjson.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.UUID;

public class FileMessage {

  private final Long timestamp;
  private final String senderName;
  private final UUID chatRoomId;
  private String fileName;
  private String fileType;
  byte[] file;

  public FileMessage(String un, UUID cid, String fn, String ft) {
    this.timestamp = System.currentTimeMillis();
    this.senderName = un;
    this.chatRoomId = cid;
    this.fileName = fn;
    this.fileType = ft;
    this.file = null;
  }

  public FileMessage(JSONObject j) {
    this.timestamp = j.getLong("timestamp");
    this.senderName = j.getString("senderName");
    this.chatRoomId = UUID.fromString(j.getString("chatRoomId"));
    this.fileName = j.getString("fileName");
    this.fileType = j.getString("fileType");
    this.file = Base64.getDecoder().decode(j.getString("file"));
  }

  public void readFile(String filePath) {
    try {
      InputStream is = new FileInputStream(filePath);
      this.file = new byte[is.available()];
      is.read(this.file);
      is.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void writeFile(String filePath) throws IOException {
    File f = new File(filePath);
    System.out.println("Write in:" + f.getAbsolutePath());
    OutputStream os = null;
    if (f.exists()) {
      os = new FileOutputStream(f);
      os.write(this.file);
      os.close();
    } else {
      f.createNewFile();
      os = new FileOutputStream(f);
      os.write(this.file);
      os.close();
    }
  }

  public JSONObject ToJson() {
    JSONObject messageJson = new JSONObject();
    messageJson.put("timestamp", this.timestamp);
    messageJson.put("senderName", this.senderName);
    messageJson.put("chatRoomId", this.chatRoomId);
    messageJson.put("fileName", this.fileName);
    messageJson.put("fileType", this.fileType);
    messageJson.put("file", Base64.getEncoder().encodeToString(this.file));
    return messageJson;
  }

  public String getFileName() {
    return this.fileName;
  }

  public UUID getChatRoomId() {
    return this.chatRoomId;
  }

  public byte[] getFile() {
    return this.file;
  }

  public static void main(String[] args) throws IOException {
    FileMessage fm = new FileMessage("A", UUID.randomUUID(), "1.doc", "doc");
    fm.readFile("data/server/1.doc");
    System.out.println(fm.ToJson().toJSONString());
    FileMessage fg = new FileMessage(fm.ToJson());
    System.out.println(fg.ToJson().toJSONString());
    fg.writeFile("data/client/123/1.doc");
  }
}
