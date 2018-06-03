package Model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;

public class Student {

    private StringProperty name;
    private StringProperty surname;
    private StringProperty averageMark;
    private StringProperty debt = new SimpleStringProperty("Нет");
    private StringProperty scholarship = new SimpleStringProperty();
    private Boolean isContractor;
    private HashMap<String, Integer> marks = new HashMap<>();
    private String notation;
    private int debtsAmount;

    public Student(String name, String surname, boolean isContractor, HashMap<String, Integer> marks) {
        this.name = new SimpleStringProperty(name);
        this.surname = new SimpleStringProperty(surname);
        this.isContractor = isContractor;
        this.marks = marks;
        setAverageMark();
        countDebts();
    }

    //высчитывание среднего бала на основании оценок
    private void setAverageMark() {
        double average = 0;
        for (int mark: this.marks.values()) {
            average += mark;
        }
        average/=marks.size();
        String averageValue = String.valueOf(average);
        if (averageValue.length() > 6) {
            //укорачивание строки для отсвутствия 50 знаков после запятой
            averageMark = new SimpleStringProperty(averageValue.substring(0, 6));
        } else averageMark = new SimpleStringProperty(averageValue);
        countDebts();
    }

    //подсчет долгов
    private void countDebts() {
        int debts = 0;
        for (int i :marks.values()) {
            //если оценка меньше 60, она считается долгом
            if (i < 60) debts++;
        }
        debtsAmount = debts;
        if (debts == 0) {
            setDebt("Нет");
        } else setDebt("Есть");
    }

    //добавление оценки
    public void addMark(String name, int mark) {
        marks.put(name,mark);
        countDebts();
        setAverageMark();
    }

    public int getDebtsAmount() {
        return debtsAmount;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getSurname() {
        return surname.get();
    }

    public void setSurname(String surname) {
        this.surname.set(surname);
    }

    public HashMap<String, Integer> getMarks() {
        return marks;
    }

    public String getNotation() {
        return notation;
    }

    public boolean isContractor() {
        return isContractor;
    }

    public String getDebt() {
        return debt.get();
    }

    public void setDebt(String debt) {
        this.debt.set(debt);
    }

    public StringProperty getScholarship() {
        return scholarship;
    }

    public void setScholarship(String newValue) {
        scholarship.set(newValue);
    }

    public Double getAverageMark() {
        return Double.parseDouble(averageMark.get());
    }

    public ObservableList<String> getSubjeucts() {
        ObservableList<String> subjects = FXCollections.observableArrayList();
        subjects.addAll(marks.keySet());
        return subjects;
    }

    public void setNotation(String notation) {
        this.notation = notation;
    }

    public void setContractStatus(boolean newStatus) {
        this.isContractor = newStatus;
    }
}
