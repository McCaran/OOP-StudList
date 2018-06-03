package Model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Comparator;

public class Stream {
    private StringProperty name;
    private IntegerProperty scholarshipStudentsAmount;
    private IntegerProperty studentsAmount = new SimpleIntegerProperty();
    private ObservableList<Student> students = FXCollections.observableArrayList();
    private ObservableList<String> subjects = FXCollections.observableArrayList();

    public Stream(String name, int scholarshipStudentsAmount) {
        this.name = new SimpleStringProperty(name);
        this.scholarshipStudentsAmount = new SimpleIntegerProperty(scholarshipStudentsAmount);
    }

    //определение стипенлиатов на потоке
    public void setScholarship() {
        if(students.size() > 1) { //если кол-во студентов больше 1
            //студенты потока сортируются по убыванию среднего балла
            students.sort(new Comparator<Student>() {
                @Override
                public int compare(Student o1, Student o2) {
                    if (o2 == null) {
                        return -1;
                    }
                    double avg1 = o1.getAverageMark();
                    double avg2 = o2.getAverageMark();
                    if (avg1 < avg2) return 1;
                    else if (avg1 == avg2) return 0;
                    else return -1;
                }
            });
        }
        int ctr = 1; //счетчик
        for (Student s: students) {
            if (!s.isContractor() && s.getDebt().equals("Нет")) { //если у человека нет долгов и он на бюджете
                                                                  //он является претендентом на получение стипендии
                if (ctr > scholarshipStudentsAmount.get()) { //если студент не попадает в N кол-во первых
                    s.setScholarship("Нет"); //стипендия отсутствует
                } else s.setScholarship("Есть"); //если студент попал в N кол-во первых
                ctr++;
            } else s.setScholarship("Нет"); //если человек контрактник, она стипендию не претендует
        }
    }

    //добавление студента
    public void addStudent(Student student) {
        students.add(student);
        studentsAmount.set(students.size());
    }

    //удаление студента
    public void removeStudent(Student student) {
        students.remove(student);
        //студент удаляется из базы данных
        DatabaseHandler.deleteStudentFromDatabase(student.getName(),
                student.getSurname(), this.getName());
    }

    //получение доступа к списку студентов
    public ObservableList<Student> getStudents() {
        return students;
    }

    //получение количества стипендиатов для таблицы
    public IntegerProperty scholarshipAmountProperty() {
        return scholarshipStudentsAmount;
    }

    //установка количества стипендиатов
    public void setScholarshipStudentsAmount(int amount) {
        scholarshipStudentsAmount.set(amount);
        setScholarship();
    }

    //получение доступа к названию потока
    public String getName() {
        return name.get();
    }

    //получение количества учащихся для таблицы
    public StringProperty studentNumberProperty() {
        return new SimpleStringProperty(String.valueOf(students.size()));
    }

    //получение количества стипендиатов для таблицы
    public StringProperty scholarshipStudentsAmount() {
        return new SimpleStringProperty((String.valueOf(scholarshipStudentsAmount.get())));
    }

    //добавление предмета на изучение
    public void addSubject(String subjectName) {
        subjects.add(subjectName);
    }

    //получение списка предметов
    public ObservableList<String> getSubjects() {
        return subjects;
    }

    public boolean containsStudent(String name, String surname) {
        for (Student student: students) {
            if (student.getName().equals(name) && student.getSurname().equals(surname))
                return true;
        }
        return false;
    }
}
