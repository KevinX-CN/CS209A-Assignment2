package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Convert;
import cn.edu.sustech.cs209.chatting.common.FileMessage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import javax.swing.JFileChooser;

public class Controller implements Initializable {

  public static Controller nowController;
  protected ConnectionC C;
  @FXML
  ListView<String> chatContentList;
  @FXML
  ListView<String> onlineUserList;
  @FXML
  ListView<String> chatList;
  String username;
  @FXML
  private TextArea inputArea;

  public void ChangeMessageList(List<String> ml) {
    chatContentList.getItems().clear();
    ObservableList<String> observableList = FXCollections.observableArrayList();
    observableList.setAll(ml);
    try {
      chatContentList.setItems(observableList);
    } catch (java.lang.IllegalStateException e) {
      System.out.println("Nothing!");
    }
    chatContentList.refresh();
  }

  public void changeOnlineUserList(List<String> oul) {
    onlineUserList.getItems().clear();
    ObservableList<String> observableList = FXCollections.observableArrayList();
    observableList.setAll(oul);
    onlineUserList.setItems(observableList);
    onlineUserList.refresh();
  }

  public void ChangeChatList(List<String> cl) {
    ObservableList<String> observableList = FXCollections.observableArrayList();
    observableList.setAll(cl);
    chatList.setItems(observableList);
    chatList.refresh();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    nowController = this;
    Dialog<Pair<String, String>> dialog = new Dialog<>();
    dialog.setTitle("SignIn OR SignUp");

    ButtonType loginButtonType = new ButtonType("SignIn/SignUp", ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));
    TextField username = new TextField();
    username.setPromptText("Username");
    PasswordField password = new PasswordField();
    password.setPromptText("Password");
    grid.add(new Label("Username:"), 0, 0);
    grid.add(username, 1, 0);
    grid.add(new Label("Password:"), 0, 1);
    grid.add(password, 1, 1);

    dialog.getDialogPane().setContent(grid);
    Platform.runLater(username::requestFocus);
    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == loginButtonType) {
        return new Pair<>(username.getText(), password.getText());
      }
      return null;
    });

    Optional<Pair<String, String>> input = dialog.showAndWait();
    input.ifPresent(usernamePassword -> {
      try {
        C = new ConnectionC(usernamePassword.getKey(), usernamePassword.getValue());
        if (!C.Connect()) {
          Platform.exit();
        }
      } catch (Throwable e) {
        throw new RuntimeException(e);
      }
    });

    chatList.getOnMouseClicked();
  }

  @FXML
  public void createPrivateChat() throws IOException {
    AtomicReference<String> user = new AtomicReference<>();

    Stage stage = new Stage();
    ComboBox<String> userSel = new ComboBox<>();

    // FIXME: get the user list from server, the current user's name should be filtered out
    List<String> UserList = C.GetUserList();
    userSel.getItems().addAll(
        UserList.stream().filter(u -> !Objects.equals(u, username)).collect(Collectors.toList()));

    Button okBtn = new Button("OK");
    okBtn.setOnAction(e -> {
      user.set(userSel.getSelectionModel().getSelectedItem());
      stage.close();
    });

    HBox box = new HBox(10);
    box.setAlignment(Pos.CENTER);
    box.setPadding(new Insets(20, 20, 20, 20));
    box.getChildren().addAll(userSel, okBtn);
    stage.setScene(new Scene(box));
    stage.showAndWait();

    // TODO: if the current user already chatted with the selected user,
    //  just open the chat with that user

    System.out.println("Creat Private Chat:" + user.get());
    C.CPCR(user.get());
    // TODO: otherwise, create a new chat item in the left panel,
    //  the title should be the selected user's name
  }

  /**
   * A new dialog should contain a multi-select list, showing all user's name. You can select
   * several users that will be joined in the group chat, including yourself.
   * The naming rule for group chats is similar to WeChat: If there are > 3 users: display the first
   * three usernames, sorted in lexicographic order, then use ellipsis with the number of users, for
   * example: UserA, UserB, UserC... (10) If there are <= 3 users: do not display the ellipsis, for
   * example: UserA, UserB (2)
   */
  @FXML
  public void createGroupChat() throws IOException {

    Dialog<String> dialog = new TextInputDialog();
    dialog.setTitle("Create Group Chat");
    dialog.setContentText("Please enter usernames(Divide by ', ':");

    Optional<String> result = dialog.showAndWait();
    List<String> ul = null;
    if (result.isPresent()) {
      ul = Convert.stringToList("[" + result.get() + "]");
    }
    System.out.println(ul);
    // TODO: if the current user already chatted with the selected user,
    //  just open the chat with that user
    assert ul != null;
    C.CGCR(ul);
    // TODO: otherwise, create a new chat item in the left panel,
    //  the title should be the selected user's name

  }

  /**
   * Sends the message to the <b>currently selected</b> chat.
   * Blank messages are not allowed. After sending the message, you should clear the text input
   * field.
   */
  @FXML
  public void doSendMessage() throws IOException {
    // TODO
    String Text = inputArea.getText();
    if (Text.equals("")) {
      return;
    }
    inputArea.setText("");
    C.stm(Text);
  }

  @FXML
  public void changeChat() throws IOException {
    System.out.println(chatList.getSelectionModel().getSelectedItem());
    C.ChangeChat(chatList.getSelectionModel().getSelectedItem());
  }

  public void doUploadFile() throws IOException {
    JFileChooser Chooser = new JFileChooser();
    Chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    Chooser.setCurrentDirectory(new File("data/client"));
    int returnVal = Chooser.showOpenDialog(null);

    File SelectedFile = null;
    if (returnVal == javax.swing.JFileChooser.APPROVE_OPTION) {
      SelectedFile = Chooser.getSelectedFile();
    }

    assert SelectedFile != null;
    FileMessage df = new FileMessage(C.U.getUserName(), C.NowChat, SelectedFile.getName(), "doc");
    df.readFile(SelectedFile.getAbsolutePath());
    C.sdm(df);
  }
}
