package Controllers;

import Model.DatabaseHandler;
import Model.Main;
import Model.Stream;
import Model.Student;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

import static Model.Main.alert;

public class StreamWindowController {
    private Stream currentStream;
    private Student selectedStudent;
    private Boolean isEditModeActive = false;
    private Stage primaryStage;
    private final int maximumStudentsAmount = 120;
    private final int maximumScholarshipStudents = 60;
    private Student backUpStudent;
    @FXML   TableView<Student> studentTable;
    @FXML   TableColumn<Student, String> nameColumn;
    @FXML   TableColumn<Student, String> surnameColumn;
    @FXML   TableColumn<Student, String> avgMarkColumn;
    @FXML   TableColumn<Student, String> debtColumn;
    @FXML   TableColumn<Student, String> scholarshipColumn;
    @FXML   Button saveEditButton;
    @FXML   Button editButton;
    @FXML   Button backButton;
    @FXML   Button addStudentButton;
    @FXML   Button deleteButton;
    @FXML   Button scholarshipButton;
    @FXML   TextField nameField;
    @FXML   TextField surnameField;
    @FXML   TextField debtField;
    @FXML   RadioButton budget;
    @FXML   RadioButton contract;
    @FXML   TextArea notationArea;
    @FXML   ListView<String> subjectList;

    //метод добавления студента
    @FXML
    private void addStudent() {
        if (currentStream.getStudents().size() == maximumStudentsAmount) {//есои достигнут лимит количества студентов
            //выдается соотвествубщее уведомление, и окно добавления студента не вызывается
            alert("Достигнуто максимальное количество студентов");
        } else { //если лимит количества студентов не достигнут
            //вызывается окно добавления студента
            Stage studentAddingStage = new Stage();
            Parent root = null;
            FXMLLoader loader = new FXMLLoader();
            try {
                loader.setLocation(this.getClass().getResource("../Views/StudentAddingWindow.fxml"));
                root = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            StudentAddingWindowController controller = loader.getController();
            controller.setStream(currentStream); //в контроллер окна добавления передается текущий поток для добавления
            studentAddingStage.initModality(Modality.WINDOW_MODAL);
            studentAddingStage.initOwner(primaryStage);
            studentAddingStage.setTitle("Добавление cтудента");
            studentAddingStage.setScene(new Scene(root));
            studentAddingStage.setMaxHeight(160);
            studentAddingStage.setResizable(false);
            controller.setStage(studentAddingStage);
            studentAddingStage.show();
        }
    }

    //метод удаления студента
    @FXML
    private void deleteStudent() {
        //Запрос на подтверждаение удаления через диалоговое окно
        if (selectedStudent != null) { //если студент выбран
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Удаление студента");
        alert.setHeaderText("Вы уверены что хотите удалить этого студента?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) { //если нажата кнопка "ок", студент удаляется
                currentStream.removeStudent(selectedStudent); //затем из текущего потока
                clearStudentDetails(); //поля, отражающие личные данные, очищаются
                studentTable.getSelectionModel().select(null); //выделение в таблице студентов сбрасывается
                currentStream.setScholarship(); // происходит пересчет получения стипендии в потоке
            }
        } else alert("Сначала выберите студента"); //если студент не выбран, выдается уведомление о соответствующей ошибке
    }

    //установка количества стипендиатов
    @FXML
    private void setScholarshipAmount() {
        //вновь контрольные значения, принцип работы тот же
        int resultCorrect = 0; //индикатор правильности ввода
        int newAmount = -1; //новое количество стипендиатов
        while (resultCorrect != 1) { //пока не будет введено корректное значение
            resultCorrect = 0; //возврещеаем значение в изначальное
            //вызов окна для ввода нового значения
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Редактировка значения");
            dialog.setHeaderText("Введите новое количество стипендиатов");
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) { //если нажата кнопка "ок"
                //проверка на корректность ввода
                try {
                    newAmount = Integer.parseInt(result.get());
                } catch (NumberFormatException e) {
                    alert("Введите корректное значение");
                    resultCorrect = -1;//в случае ошибки, установка данного значения в -1
                                       //не даст циклу закончится
                    continue; //проверка на попадание в допустимый диапазон не производится
                              //и цикл начинается заново
                }
                if (newAmount < 0 || newAmount > maximumScholarshipStudents) {
                    alert("Стипендиатов должна быть от 0 до 60");
                    newAmount = -1;
                    resultCorrect = -1; //установка этого значения в случае ошибки ввода не даст
                                        //сохранится некорректному значению
                }
                if (resultCorrect == 0) resultCorrect = 1; //елси ошибок не произошло, цикл оканчивается
                } else break; //если нажата кнопка отмены, ввод отменяется, и окно закрывается
            }
        if (newAmount != -1) { //если новое значение корректно
            currentStream.setScholarshipStudentsAmount(newAmount); //значение в потоке заменяется на введенное
            DatabaseHandler.saveEditsOfStream(currentStream.getName(),newAmount); //изменения сохраняются в базу данных
        }
    }

    //возврат в окно потоков
    @FXML
    private void backToMainMenu() {
        try {
            Main.showMainWindow();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Метод, устанавливабщий реакцию на взаимодейсвтиве с объектами в таблице
    @FXML
    private void selectStudent() {
        if(currentStream.getStudents().size() != 0) { //если список не пуст и студент выбран
            selectedStudent = studentTable.getSelectionModel().getSelectedItem(); //переменная с текущим студентом
                                                                                  // устанавливается на выбранного студента
            if ( selectedStudent != null) {
                backUpStudent = new Student(selectedStudent.getName(), selectedStudent.getSurname(),
                        selectedStudent.isContractor(), (HashMap) selectedStudent.getMarks().clone());
                backUpStudent.setNotation(selectedStudent.getNotation());
                showStudentDetails(); //в полях в павой части отображаются данные выбранного студента
            }

        }
    }

    //метод для редактирования данных
    @FXML
    private void editMark() {
        selectedStudent.setName(nameField.getText()); //сохраняется имя
        selectedStudent.setSurname(surnameField.getText()); //сохраняется фамилия
        if (budget.isSelected()) { //если выбрана форма обучения Б - "бюджет"
            selectedStudent.setContractStatus(false); //студент утанавливается как бюджетник
        } else { //если выбрана форма обучения К - "контракт"
            selectedStudent.setContractStatus(true); //студент устанавливается как контрактник
        }
        selectedStudent.setNotation(notationArea.getText()); //сохраняются заметки
        //контрольные значение для проверки правильности ввода новой оценки
        int resultCorrect = 0; //индикатор правильности ввода
        int newMark = -1; //значение новой оценки
        String subjectName;
        if (subjectList.getSelectionModel().getSelectedItem().endsWith("100")) {
           subjectName = subjectList.getSelectionModel().
                   getSelectedItem().substring(0, subjectList.getSelectionModel().getSelectedItem().length() - 4);
        } else if(subjectList.getSelectionModel().getSelectedItem().
                charAt(subjectList.getSelectionModel().getSelectedItem().length()-2) == ' ') {
            subjectName = subjectList.getSelectionModel().
                    getSelectedItem().substring(0, subjectList.getSelectionModel().getSelectedItem().length() - 2);
        } else {
            subjectName = subjectList.getSelectionModel().
                   getSelectedItem().substring(0, subjectList.getSelectionModel().getSelectedItem().length() - 3);
        }
        while (resultCorrect != 1) { //пока значение не будет введено правильно
            resultCorrect = 0;
            //Создание диалогового окна для ввода новой оценки
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Ввод оценки");
            dialog.setHeaderText("Введите по дисциплине " + subjectName);
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) { //если нажата кнопка "ок"
                try {
                    newMark = Integer.parseInt(result.get()); //значение считывается из строки
                } catch (NumberFormatException e) { //в случае ввода нечисельных символов
                    //вывод сообщения о соответствующей ошибка
                    alert("Введите корректное значение");
                    resultCorrect = -1;
                    continue; //дальнейшая часть тела цикла пропускается
                }
                if (newMark < 0 || newMark > 100) { //если введено некорректное значение
                    //вывод сообщения о соответствующей ошибка
                    alert("Оценка должна быть от 0 до 100");
                    newMark = -1; //для избежания сохранения некорректного значения
                }
                if (resultCorrect == 0) resultCorrect = 1; //если не было ошибок,
            } else break; //если нажата кнопка отмены, ввод значения отменяется
            if (newMark != -1 && resultCorrect == 1) { //введено корректное значение
                selectedStudent.addMark(subjectName, newMark);//установка значения оценки
                showStudentDetails();
            }
        }
    }

    //логика работы кнопки "редактировать"/"отмена"
    @FXML
    private void editButton() {
        selectedStudent = studentTable.getSelectionModel().getSelectedItem();
        //установка отклика на нажатие кнопки редактировки
        if (selectedStudent != null) { //если студент выбран
            setEditMode(); //переключается режим редактировки
            if (!isEditModeActive) { //была использована функция отмены редактирования
                if (backUpStudent != null) {
                    /*этот код активируется при нажатии на кнопку "отмена", при включенном
                    режиме редактирования. При выборе студента в списке автоматически
                    создается копия его записи, и затем, если нажимается отмена,
                    данные из копии возвращают измененную запись к первоначальному виду*/
                    selectedStudent.setName(backUpStudent.getName());
                    selectedStudent.setSurname(backUpStudent.getSurname());
                    selectedStudent.setContractStatus(backUpStudent.isContractor());
                    selectedStudent.setNotation(backUpStudent.getNotation());
                    selectedStudent.setDebt(backUpStudent.getDebt());
                    for (String subject: selectedStudent.getSubjeucts()) {
                        selectedStudent.addMark(subject, backUpStudent.getMarks().get(subject));
                    }
                    currentStream.setScholarship();
                    showStudentDetails();
                }
            } else {
                subjectList.getSelectionModel().selectFirst();
            }
        } else alert("Сначала выберите студента"); //если слудент не выбран, выдается окно с сообщением об ошибке
    }

    //логика работы кнопки сохранения
    @FXML
    private void saveButton() {
        if (nameField.getText().isEmpty() || surnameField.getText().isEmpty()) {
            alert("Введите корректные данные");
            return;
        }
        selectedStudent = studentTable.getSelectionModel().getSelectedItem();
        //установка отклика на кнопку сохранения после редактировки
        selectedStudent.setName(nameField.getText()); //срхраняется имя
        selectedStudent.setSurname(surnameField.getText()); //сохраняется фамилия
        if (budget.isSelected()) { //если выбрана форма обучения Б - "бюджет"
            selectedStudent.setContractStatus(false); //студент утанавливается как бюджетник
        } else { //если выбрана форма обучения К - "контракт"
            selectedStudent.setContractStatus(true); //студент устанавливается как контрактник
        }
        selectedStudent.setNotation(notationArea.getText()); //сохраняются заметки
        setEditMode(); //переключается режим редактировки
        DatabaseHandler.saveEditsOfStudent(backUpStudent, selectedStudent, currentStream.getName()); //новые данные сохраняются
        // в базу данных
        //студенты в таблице перезагружаются из базы данных для отображения изменений
        currentStream.getStudents().clear();
        currentStream.getStudents().addAll(DatabaseHandler.loadStudentsOfStream(currentStream.getName()));
        showStudentDetails();
        currentStream.setScholarship();
    }

    //метод для включения и отключения режима редактировки
    private void setEditMode() {
        //если редактировка включается, поля для ввода изменений активируются, ненужные элементы интерфейса отключаются
        studentTable.setDisable(!studentTable.isDisabled());
        saveEditButton.setDisable(!saveEditButton.isDisabled());
        nameField.setDisable(!nameField.isDisabled());
        surnameField.setDisable(!surnameField.isDisabled());
        debtField.setDisable(!debtField.isDisabled());
        subjectList.setDisable(!subjectList.isDisabled());
        notationArea.setDisable(!notationArea.isDisabled());
        contract.setDisable(!contract.isDisabled());
        budget.setDisable(!budget.isDisabled());
        deleteButton.setDisable(!deleteButton.isDisabled());
        addStudentButton.setDisable(!addStudentButton.isDisabled());
        scholarshipButton.setDisable(!scholarshipButton.isDisabled());
        backButton.setDisable(!backButton.isDisabled());
        if (!isEditModeActive) { //если режим редктировки был выключен
            editButton.setText("Отменить"); //текст кнопки редактировки меняется на "оменить" для соответствия функционалу
            //subjectList.getSelectionModel().select(null);
        } else { //если режим редактировки был включен
            editButton.setText("Редактировать"); //текст кнопки меняется на "редактировать"
        }
        isEditModeActive = !isEditModeActive; //переключается переменная, означающая сам режим
        showStudentDetails(); //данные в полях обновляются
    }

    private void configureTable() {
        //установка значений, отображаемых в таблице
        nameColumn.setCellValueFactory(new PropertyValueFactory<Student, String>("name"));
        surnameColumn.setCellValueFactory(new PropertyValueFactory<Student, String>("surname"));
        avgMarkColumn.setCellValueFactory(new PropertyValueFactory<Student, String>("averageMark"));
        debtColumn.setCellValueFactory(new PropertyValueFactory<Student, String>("debt"));
        scholarshipColumn.setCellValueFactory(cellData -> cellData.getValue().getScholarship());
        studentTable.setItems(currentStream.getStudents()); //таблица студентов заполняется
    }

    //Метод для очистки полей при обнулении выделения
    private void clearStudentDetails() {
        selectedStudent = null;
        nameField.clear();
        surnameField.clear();
        debtField.clear();
        contract.setSelected(false);
        budget.setSelected(false);
        subjectList.setItems(null);
        notationArea.clear();
    }

    //Метод, котоыре заполняет поля в правой части окна
    private void showStudentDetails() {
        nameField.setText(selectedStudent.getName());
        surnameField.setText(selectedStudent.getSurname());
        debtField.setText(String.valueOf(selectedStudent.getDebtsAmount()));
        if (selectedStudent.isContractor()) { //если студент контрактник
            contract.setSelected(true); //устанавливается соответствующее значение окна
        } else { //если студент бюджетник
            budget.setSelected(true); //устанавливается соотвествующее значение
        }
        notationArea.setText(selectedStudent.getNotation());
        ObservableList<String> subjectsAndMarks = FXCollections.observableArrayList();
        for (String subject: selectedStudent.getMarks().keySet()) { //заполнение данных для списка предметов
            //если редактировка отключена, отражаются и предметы и оценки
            /*if (!isEditModeActive) subjectsAndMarks.add(subject + " " + selectedStudent.getMarks().get(subject));
            //в режиме редактировки
            else subjectsAndMarks.add(subject);*/
            subjectsAndMarks.add(subject + " " + selectedStudent.getMarks().get(subject));
        }
        subjectList.setItems(null);
        subjectList.setItems(subjectsAndMarks);
    }

    public void setStage(Stage stage) {
        primaryStage = stage;
    }

    public void setStream(Stream stream) {
        currentStream = stream;
        configureTable();
        currentStream.setScholarship();
    }
}
