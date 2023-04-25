package cn.edu.sustech.cs209.chatting.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

public class DocFile {

  private Message<String> MS;
  private String FileName;
  private String FileType;
  byte[] File;

  public DocFile(String Name) {
    this.FileName = Name;
    this.FileType = "doc";
    this.File = null;
  }

  public DocFile(JSONObject J) {
    this.MS = new Message<>(JSON.parseObject(J.getString("MS")));
    this.FileName = J.getString("FileName");
    this.FileType = J.getString("FileType");
    this.File = J.getBytes("File");
  }

  public void SetMessage(String UN, UUID CID) {
    this.MS = new Message<>(UN, CID, "File <" + this.FileName + ">");
  }

  public void ReadFile(String FileUrl) {
    try {
      InputStream IS = Files.newInputStream(Paths.get(FileUrl));
      File = new byte[IS.available()];
      IS.read(File);
      IS.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void WriteFile(String FileUrl) throws IOException {
    File F = new File(FileUrl);
    OutputStream OS;
    if (F.exists()) {
      OS = Files.newOutputStream(F.toPath());
      OS.write(this.File);
      OS.close();
    }
  }

  public JSONObject ToJson() {
    JSONObject FileJSON = new JSONObject();
    FileJSON.put("MS", this.MS.ToJson().toJSONString());
    FileJSON.put("FileName", this.FileName);
    FileJSON.put("FileType", this.FileType);
    FileJSON.put("File", Arrays.toString(this.File));
    return FileJSON;
  }

  public String toString() {
    return "Saved as " + this.FileName;
  }

  public Message<String> GetMS() {
    return this.MS;
  }

  public String GetFileName() {
    return this.FileName;
  }
}
