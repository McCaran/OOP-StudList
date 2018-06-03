package Controllers;

import Model.DatabaseHandler;
import Model.Stream;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Optional;

import static Model.Main.alert;


public class StreamAddingWindowController {

    private Stream newStream;
    private Stage streamAddingStage;
    private ObservableList<Stream> streams;
    private final int maximumScholarshipStudents = 60;

    @FXML   private TextField newStreamName;
    @FXML   private TextField scholarshipStudentsAmount;
    @FXML   private TextField subjectNumberField;

    //метод добавления потока в список
    @FXML
    private void addStream() {
        if(newStreamName.getText().isEmpty()) { //если название не введено
            alert("Введите корректное название потока");
            return;
        }
        if(newStreamName.getText().length() > 10) {
            alert("Слишком длинное название потока");
            return;
        }
        for (Stream stream: streams) {
            if (stream.getName().equals(newStreamName.getText())) {
                alert("Такой поток уже существует");
                return;
            }
        }
        int subjectNumber = 0; //количество изучаемых предметов
        int scholarshipAmount = -1; //кол-во стипендиатов на потоке
        try {
            scholarshipAmount = Integer.parseInt(scholarshipStudentsAmount.getText());
        } catch (NumberFormatException e) { //если в поле были введены нечисленные символы
            //вывод сообщения об ошибке
            alert("Введите кол-во стипендиатов корректно");
            return;
        }
        //если было введено некорректное число стипендиатов
        if (scholarshipAmount < 0 || scholarshipAmount > maximumScholarshipStudents) {
           alert("Количество стипендиатов должно быть от 0 до " + maximumScholarshipStudents);
            return;
        }
        try {
            subjectNumber = Integer.parseInt(this.subjectNumberField.getText());
        } catch (NumberFormatException e) {
            alert("Введите кол-во предметов корректно");
            return;
        }
        if (subjectNumber < 4 || subjectNumber > 10) {
            alert("Количество предметов должно быть от 4 до 10");
            return;
        }
        newStream = new Stream(newStreamName.getText(), scholarshipAmount); //создание объекта нового потока
        //Ввод названий изучаемых предметов
        for (int i = 0; i < subjectNumber; i++) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Добавление предмета");
            dialog.setHeaderText("Введите название " + (i + 1) + " предмета");
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) { //если была нажата кнопка подтверждения
                if (result.get().isEmpty()) {
                    alert("Введите название предмета корректно");
                    i--;
                    continue;
                }
                if (result.get().length() > 30) {
                    alert("Слишком длинное название предмета");
                    i--;
                    continue;
                }
                //предмет добавляется в список изучаемых на потоке после проверки
                if (newStream.getSubjects().contains(result.get())) {
                    alert("Такой предмет уже введен");
                    i--;
                } else newStream.addSubject(result.get());
            } else { //если нажата кнопка отмены
                dialog.close();//окна ввода предметов закрывается
                return;
            }
        }
        //если в ходе ввода не будет допущено ошибок, поток будет успешно добавлен
        streams.add(newStream); //поток добавляется в список потоков
        finishAdding(); //окно добавления дакрывается
        DatabaseHandler.addStreamToDatabase(newStream); //поток добавляется в базу данных
    }

    //метод для закрытия окна добавления, вызывается при отмене добавления либо для
    //закрытия окна после успешного выполнения добавления
    @FXML
    private void finishAdding() {
        streamAddingStage.close();
    }

    //установка сцены, передаваемой из главного потока
    public void setStage(Stage stage) {
        streamAddingStage = stage;
    }

    //передача списка потоков из главного окна для пополнения
    public void setStreamList(ObservableList<Stream> streamList) {
        streams = streamList;
    }
}