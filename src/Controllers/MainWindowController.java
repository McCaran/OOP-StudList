package Controllers;

import Model.DatabaseHandler;
import Model.Main;
import Model.Stream;
import Model.Student;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static Model.Main.alert;

public class MainWindowController implements Initializable {
    private ObservableList<Stream> streams = FXCollections.observableArrayList();
    private Stream selectedStream = null;
    private Stage primaryStage;
    @FXML   Button openStreamButton;
    @FXML   Button addStreamButton;
    @FXML   Button deleteButton;
    @FXML   ListView<String> studentList;
    @FXML   ListView<String> subjectList;
    @FXML   TableView<Stream> streamTable;
    @FXML   TableColumn<Stream, String> nameColumn;
    @FXML   TableColumn<Stream, String> studentNumberColumn;
    @FXML   TableColumn<Stream, String> budgetAmountColumn;

    //метод перехода к окну потоков
    @FXML
    private void openStream() {
        //если поток выбран, осуществляется переход
        if (selectedStream != null) {
            try {
                Main.goToStreamWindow(selectedStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //иначе выдается сообщение об ошибке
        } else alert("Сначала выберите поток");
}

    //метод удаления потока
    @FXML
    private void deleteStream() {
        if (selectedStream != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Удаление потока " + selectedStream.getName());
            alert.setHeaderText("Вы уверены что хотите удалить этот поток?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                //если поток выбран, он убирается из текущего списка
                if (selectedStream != null) {
                    //удаление из базы данных
                    DatabaseHandler.deleteStreamFromDatabase(selectedStream.getName());
                    streams.remove(selectedStream);
                    selectedStream = null;
                    //если поток не выбран, выдается сообщение об ошибке
                } else alert("Сначала выберите поток");
                streamTable.getSelectionModel().select(null);
            }
        } else alert("Сначала выберите поток");;
    }

    //метод добавления потока
    @FXML
    private void addStream() {
        //создается новое окно
        Stage streamAddingStage = new Stage();
        Parent root = null;
        FXMLLoader loader = new FXMLLoader();
        try {
            //новому окну присваивается вид из соответствующего файла
            loader.setLocation(this.getClass().getResource("../Views/StreamAddingWindow.fxml"));
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        StreamAddingWindowController controller = loader.getController();
        //список потоков передается в контроллер окна для дополнения
        controller.setStreamList(streams);
        streamAddingStage.initModality(Modality.WINDOW_MODAL);
        streamAddingStage.initOwner(primaryStage);
        streamAddingStage.setTitle("Добавление потока");
        streamAddingStage.setScene(new Scene(root));
        streamAddingStage.setMaxWidth(231);
        streamAddingStage.setMaxHeight(186);
        streamAddingStage.setResizable(false);
        controller.setStage(streamAddingStage);
        streamAddingStage.show();
    }

    //метод, выполняемый при инициализации
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureTable(); //настраивается таблица
        streams.addAll(DatabaseHandler.loadStreamsFromDatabase()); //в список потоков загружаются данные из базы
    }
    //отдельный метод для вызова окна ошибки при не выборе потока

    //метод, настраивающий таблицу потоков
    private void configureTable() {
        //установка значений, отражаемых в таблице
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        studentNumberColumn.setCellValueFactory(cellData -> cellData.getValue().studentNumberProperty());
        budgetAmountColumn.setCellValueFactory(cellData -> cellData.getValue().scholarshipStudentsAmount());
        streamTable.setItems(streams); //заполнение таблицы потоков
        //установка реакции на выбор потока из таблицы
        streamTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Stream>() {
            @Override
            public void changed(ObservableValue<? extends Stream> observable, Stream oldValue, Stream newValue) {

                if (newValue != null) { //проверка на случай если выделение было сброшено, например после удаления потока
                    selectedStream = newValue;
                    ObservableList<Student> students = newValue.getStudents();
                    ObservableList<String> studentNames = FXCollections.observableArrayList(); //создание списка имен и фамилий студентов
                    //заполнение этого спикса
                    for (Student s : students) {
                        studentNames.add(s.getSurname() + " " + s.getName());
                    }
                    studentList.setItems(studentNames); //отражение имен и фамилий учащихся в отведенном месте
                    subjectList.setItems(selectedStream.getSubjects()); //отражение изучаемых предметов
                } else { //если выделение было сброщено
                    // списки имен учащихся и изучаемых предметов очищаются
                    studentList.setItems(null);
                    subjectList.setItems(null);
                }
            }
        });
    }

    //метод для передачи сцены из класса Main
    public void setStage(Stage stage) {
        primaryStage = stage;
    }

}
