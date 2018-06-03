package Model;


import Controllers.MainWindowController;
import Controllers.StreamWindowController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        this.primaryStage = primaryStage;
        showMainWindow();
        DatabaseHandler.connectToDatabase();
    }

    //метод, включающий отображение главного окна для работы с потоками
    public static void showMainWindow() throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/Views/MainWindow.fxml"));
        Parent root = loader.load();
        MainWindowController controller = loader.getController();
        primaryStage.setTitle("StudRating");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMaxWidth(600);
        primaryStage.setMaxHeight(428);
        primaryStage.setResizable(false);
        primaryStage.show();
        controller.setStage(primaryStage);
    }

    //метод, переводящий программу в режим работы со студентами
    public static void goToStreamWindow(Stream stream) throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/Views/StreamWindow.fxml"));
        Parent root = loader.load();
        StreamWindowController controller = loader.getController();
        controller.setStream(stream);
        controller.setStage(primaryStage);
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Поток " + stream.getName());
    }

    public static void main(String[] args) {
        launch(args);
    }


    public static void alert(String headerText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(headerText);
        alert.showAndWait();
    }
}
