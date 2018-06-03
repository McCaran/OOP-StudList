package Controllers;

import Model.DatabaseHandler;
import javafx.scene.control.RadioButton;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import Model.Stream;
import Model.Student;

import java.util.HashMap;
import java.util.Optional;

import static Model.Main.alert;

public class StudentAddingWindowController{

    private Student newStudent;
    private Stream currentStream;
    private Stage studentAddingStage;
    @FXML   private TextField nameField;
    @FXML   private TextField surnameField;
    @FXML   private RadioButton budget;

    //Метод создания и добавления нового студента
    @FXML
    private void addStudent() {
        if (currentStream.containsStudent(nameField.getText(), surnameField.getText())) {
            alert("Такой студент уже существует в этом потоке");
            return;
        }
        if (nameField.getText().length() > 30) {
            alert("Слишком длинное имя");
            return;
        }
        if (surnameField.getText().length() > 30) {
            alert("Слишком длинная фамилия");
            return;
        }
        if (nameField.getText().isEmpty() || surnameField.getText().isEmpty()) {
            alert("Введите корректные имя и фамилию");
            return;
        }
        HashMap<String, Integer> marks = new HashMap<>(); //будущие оценки с предметами
        for (String subject : currentStream.getSubjects()) { //проходим циклом по всем предметам
            //Тот же принцип что и в других классах - контрольные значения
            int resultCorrect = 0;
            int mark = -1;
                while (resultCorrect != 1) {
                   resultCorrect = 0;
                   TextInputDialog dialog = new TextInputDialog();
                   dialog.setTitle("Ввод оценки");
                   dialog.setHeaderText("Введите оценку по дисциплине " + subject);
                   Optional<String> result = dialog.showAndWait();
                   if (result.isPresent()) { //Если была нажата кнопка "ок"
                       try {
                           if (result.get().isEmpty()) {
                               alert("Введите корректное значение");
                               continue;
                           }
                           mark = Integer.parseInt(result.get());
                       } catch (NumberFormatException e) {
                           //Сообщение об ошибке в случае ввода нечисленных символов
                           alert("Введите корректное значение");
                           continue;
                       }
                       if (resultCorrect == 0)
                           if (mark < 0 || mark > 100) {
                               //сообщение об ошибке при неправильном вводе оценки
                               alert("Недопустимое значение. Повторите ввод");
                               continue;
                           }
                       if (resultCorrect == 0) resultCorrect = 1;
                   } else {
                       marks.clear();
                       return;
                   }
                   marks.put(subject, mark);
            }
        }
        if (!marks.isEmpty()) {
            if (budget.isSelected()) {
                newStudent = new Student(nameField.getText(), surnameField.getText(), false, marks);
            } else newStudent = new Student(nameField.getText(), surnameField.getText(), true, marks);
            currentStream.addStudent(newStudent);
            currentStream.setScholarship();
            studentAddingStage.close();
            DatabaseHandler.addStudentToDatabase(newStudent, currentStream.getName());
        }
    }

    @FXML
    private void cancelAdding() {
        studentAddingStage.close();
    }

    public void setStream(Stream stream) {
        currentStream = stream;
    }

    public void setStage(Stage stage) {
        studentAddingStage = stage;
    }
}