package cn.edu.sustech.cs209.chatting.client;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.stage.WindowEvent;

public class Main extends Application {

  public static Controller FrameController;

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
    stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
      @Override
      public void handle(WindowEvent event) {
        ConnectionC C = Controller.NowController.C;
        if (C != null) {
          try {
            C.CloseClient();
          } catch (Throwable e) {
            throw new RuntimeException(e);
          }
        }
        System.out.println("Quit");
      }
    });
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
    stage.setScene(new Scene(fxmlLoader.load()));
    stage.setTitle("Chatting Client");
    stage.show();
  }
}
