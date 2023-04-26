package cn.edu.sustech.cs209.chatting.client;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

  public static void main(String[] args) {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        super.run();
        System.out.println("Client End");
      }
    });
    System.out.println("Client Start");
    launch();
  }

  @Override
  public void start(Stage stage) throws IOException {
    stage.setOnCloseRequest(event -> {
      ConnectionC C = Controller.nowController.C;
      if (C != null) {
        try {
          //C.Save();
          C.CloseClient();
        } catch (Throwable e) {
          throw new RuntimeException(e);
        }
      }
      System.out.println("Quit");
    });
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
    stage.setScene(new Scene(fxmlLoader.load()));
    stage.setTitle("Chatting Client");
    stage.show();
  }
}
