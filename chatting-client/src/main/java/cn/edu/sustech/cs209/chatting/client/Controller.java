package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Convert;
import cn.edu.sustech.cs209.chatting.common.DocFile;
import cn.edu.sustech.cs209.chatting.common.Message;
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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;
import javax.swing.JFileChooser;

public class Controller implements Initializable {

  public static Controller NowController;

  @FXML
  private TextArea InputArea;

  @FXML
  ListView<String> chatContentList;
  @FXML
  ListView<String> OnlineUserList;
  @FXML
  ListView<String> ChatList;
  protected ConnectionC C;
  String username;

  public void ChangeMessageList(List<String> ML) {
    ObservableList<String> observableList = FXCollections.observableArrayList();
    observableList.setAll(ML);
    chatContentList.setItems(observableList);
    chatContentList.refresh();
  }

  public void ChangeOnlineUserList(List<String> OUL) {
    ObservableList<String> observableList = FXCollections.observableArrayList();
    observableList.setAll(OUL);
    OnlineUserList.setItems(observableList);
    OnlineUserList.refresh();
  }

  public void ChangeChatList(List<String> CL) {
    ObservableList<String> observableList = FXCollections.observableArrayList();
    observableList.setAll(CL);
    ChatList.setItems(observableList);
    ChatList.refresh();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    NowController = this;
    Dialog<Pair<String, String>> dialog = new Dialog<>();
    dialog.setTitle("Signin OR SignUp");

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
    Platform.runLater(() -> username.requestFocus());
    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == loginButtonType) {
        return new Pair<>(username.getText(), password.getText());
      }
      return null;
    });

    Optional<Pair<String, String>> input = dialog.showAndWait();
    input.ifPresent(usernamePassword -> {
      try {
        C = new ConnectionC(usernamePassword.getKey(),usernamePassword.getValue());
        if (!C.Connect()) {
          Platform.exit();
        }
      } catch (Throwable e) {
        throw new RuntimeException(e);
      }
    });

    ChatList.getOnMouseClicked();
  }

  @FXML
  public void createPrivateChat() throws IOException {
    AtomicReference<String> user = new AtomicReference<>();

    Stage stage = new Stage();
    ComboBox<String> userSel = new ComboBox<>();

    // FIXME: get the user list from server, the current user's name should be filtered out
    List<String> UserList = C.GetUserList();
    userSel.getItems().addAll(UserList.stream().filter(u -> {
      return u != username;
    }).collect(Collectors.toList()));

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

    // TODO: if the current user already chatted with the selected user, just open the chat with that user

    System.out.println("Creat Private Chat:" + user.get());
    C.CPCR(user.get());
    // TODO: otherwise, create a new chat item in the left panel, the title should be the selected user's name
  }

  /**
   * A new dialog should contain a multi-select list, showing all user's name. You can select
   * several users that will be joined in the group chat, including yourself.
   * <p>
   * The naming rule for group chats is similar to WeChat: If there are > 3 users: display the first
   * three usernames, sorted in lexicographic order, then use ellipsis with the number of users, for
   * example: UserA, UserB, UserC... (10) If there are <= 3 users: do not display the ellipsis, for
   * example: UserA, UserB (2)
   */
  @FXML
  public void createGroupChat() throws IOException {

    Dialog dialog = new TextInputDialog();
    dialog.setTitle("Create Group Chat");
    dialog.setContentText("Please enter usernames(Divide by ', ':");

    Optional<String> result = dialog.showAndWait();
    List<String> UL = null;
    if (result.isPresent()) {
      UL = Convert.StringToList("[" + result.get() + "]");
    }
    System.out.println(UL);
    // TODO: if the current user already chatted with the selected user, just open the chat with that user
    C.CGCR(UL);
    // TODO: otherwise, create a new chat item in the left panel, the title should be the selected user's name

  }

  /**
   * Sends the message to the <b>currently selected</b> chat.
   * <p>
   * Blank messages are not allowed. After sending the message, you should clear the text input
   * field.
   */
  @FXML
  public void doSendMessage() throws IOException {
    // TODO
    String Text = InputArea.getText();
    if (Text.equals("")) {
      return;
    }
    InputArea.setText("");
    C.STM(Text);
  }

  @FXML
  public void ChangChat(MouseEvent mouseEvent) throws IOException {
    System.out.println(ChatList.getSelectionModel().getSelectedItem());
    C.ChangeChat(ChatList.getSelectionModel().getSelectedItem());
  }

  public void doUploadFile(ActionEvent actionEvent) throws IOException {
    JFileChooser Chooser = new JFileChooser();
    Chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    Chooser.setCurrentDirectory(new File("data/client"));
    int returnVal = Chooser.showOpenDialog(null);

    File SelectedFile = null;
    if (returnVal == javax.swing.JFileChooser.APPROVE_OPTION) {
      SelectedFile = Chooser.getSelectedFile();
    }

    DocFile DF = new DocFile(SelectedFile.getName());
    DF.ReadFile(SelectedFile.getAbsolutePath());
    C.SDM(DF);
  }

  /**
   * You may change the cell factory if you changed the design of {@code Message} model. Hint: you
   * may also define a cell factory for the chats displayed in the left panel, or simply override
   * the toString method.
   */
  private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {

    @Override
    public ListCell<Message> call(ListView<Message> param) {
      return new ListCell<Message>() {

        public void updateItem(Message<String> msg, boolean empty) {
          super.updateItem(msg, empty);
          if (empty || Objects.isNull(msg)) {
            setText(null);
            setGraphic(null);
            return;
          }

          HBox wrapper = new HBox();
          Label nameLabel = new Label(msg.GetSenderName());
          Label msgLabel = new Label(msg.GetMessage());

          nameLabel.setPrefSize(50, 20);
          nameLabel.setWrapText(true);
          nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

          if (username.equals(msg.GetSenderName())) {
            wrapper.setAlignment(Pos.TOP_RIGHT);
            wrapper.getChildren().addAll(msgLabel, nameLabel);
            msgLabel.setPadding(new Insets(0, 20, 0, 0));
          } else {
            wrapper.setAlignment(Pos.TOP_LEFT);
            wrapper.getChildren().addAll(nameLabel, msgLabel);
            msgLabel.setPadding(new Insets(0, 0, 0, 20));
          }

          setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
          setGraphic(wrapper);
        }
      };
    }
  }

  private class StringCellFactory implements Callback<ListView<String>, ListCell<String>> {

    @Override
    public ListCell<String> call(ListView<String> param) {
      return new ListCell<String>() {

        public void updateItem(String msg, boolean empty) {
          super.updateItem(msg, empty);
          if (empty || Objects.isNull(msg)) {
            setText(null);
            setGraphic(null);
            return;
          }

          HBox wrapper = new HBox();
          Label nameLabel = new Label(msg);

          nameLabel.setPrefSize(50, 20);
          nameLabel.setWrapText(true);
          nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

          setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
          setGraphic(wrapper);
        }
      };
    }
  }
}
