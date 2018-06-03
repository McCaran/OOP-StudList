package Model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.HashMap;

public abstract class DatabaseHandler {
    private static String url = "jdbc:sqlite:src/Database/StudRatingDB.db"; //адрес БД  в памяти
    //метод подключения к базеданных. В случае отсутствия БД по адресу, она создается программой
    public static void connectToDatabase() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        createTables(conn);
    }

    //создание таблиц на случай если БД была только что создана
    private static void createTables(Connection conn) {
        //создание необходимых таблиц
        createNewStreamsTable(conn);
        createNewSubjectsTable(conn);
        createNewStudentsTable(conn);
        createNewMarksTable(conn);
    }

    //создание таблицы потоков(если таковой еще нет)
    private static  void createNewStreamsTable(Connection conn) {
        String sql = "CREATE TABLE IF NOT EXISTS streams (\n"
                + "	name text NOT NULL,\n"
                + " scholarship_students_amount integer NOT NULL,\n"
                + " PRIMARY KEY(name)\n"
                + ");";
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //создание таблицы предметов(если таковой еще нет)
    private static void createNewSubjectsTable(Connection conn) {
        String sql = "CREATE TABLE IF NOT EXISTS subjects (\n"
                + "	subject_name text NOT NULL,\n"
                + "	stream_name text NOT NULL,\n"
                + " PRIMARY KEY(subject_name, stream_name)\n"
                + ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //создание таблицы оценок(если таковой еще нет)
    private static void createNewMarksTable(Connection conn) {
        String sql = "CREATE TABLE IF NOT EXISTS marks (\n"
                + "	student_name text NOT NULL,\n"
                + "	student_surname text NOT NULL,\n"
                + " stream_name text NOT NULL,\n"
                + " subject_name text NOT NULL,\n"
                + " mark integer NOT NULL,\n"
                + " PRIMARY KEY(student_name, student_surname, stream_name, subject_name)\n"
                + ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //создание таблицы студентов(если таковой еще нет)
    private static void createNewStudentsTable(Connection conn) {
        String sql = "CREATE TABLE IF NOT EXISTS students (\n"
                + "	name text NOT NULL,\n"
                + "	surname text NOT NULL,\n"
                + " stream_name text NOT NULL,\n"
                + " notation text,\n"
                + " isContractor boolean NOT NULL,\n"
                + " PRIMARY KEY(name, surname, stream_name)\n"
                + ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //Загрузка студентов определенного потока из БД
    public static ObservableList<Student> loadStudentsOfStream(String streamName) {
        ObservableList<Student> students = FXCollections.observableArrayList();
        String sql = "SELECT name, surname, notation, isContractor FROM students " +
                "WHERE stream_name = ?"; //запрос всех данных по студентам, которые учатся на этом потоке
        try {
            //исполнение запроса
            Connection conn = DriverManager.getConnection(url);
            PreparedStatement getStudents = conn.prepareStatement(sql);
            getStudents.setString(1, streamName);
            ResultSet studentSet = getStudents.executeQuery();
            //запрос всех данных из таблицы оценок, где имя, фамилия, и название потока совпадают
            sql = "SELECT subject_name, mark FROM marks WHERE stream_name = ? AND " +
                    "student_name = ? AND student_surname = ?";
            while (studentSet.next()) { //пока не будут загружены все студенты из БД
                //исполнение запроса
                HashMap<String, Integer> marks = new HashMap<>();
                PreparedStatement getMarks = conn.prepareStatement(sql);
                getMarks.setString(1,streamName);
                getMarks.setString(2, studentSet.getString(1));
                getMarks.setString(3, studentSet.getString(2));
                ResultSet marksSet = getMarks.executeQuery();
                while (marksSet.next()) //пока не будут загружены все оценки из БД
                    marks.put(marksSet.getString(1), marksSet.getInt(2)); //заполнение хеш-карты оценок
                Student newStudent = new Student(studentSet.getString(1), studentSet.getString(2),
                        studentSet.getBoolean(4), marks); //создание нового объекта студента
                newStudent.setNotation(studentSet.getString(3)); // загрузка аннотации
                students.add(newStudent); //добавление студента в список студентов потока
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students; //метод возвращает список учащихсяна потоке с оценками
    }

    //внутренний многоразовый метод для удаления данных о потоке из разных таблиц
    private static void deleteStream(String sql, String streamName) {
        Connection conn = null;
        try {
            //исполнение запроса
            conn = DriverManager.getConnection(url);
            PreparedStatement deleteStream = conn.prepareStatement(sql);
            deleteStream.setString(1, streamName);
            deleteStream.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //загрузки потоков из БД
    public static ObservableList<Stream> loadStreamsFromDatabase() {
        ObservableList<Stream> streams = FXCollections.observableArrayList();
        String sql = "SELECT name, scholarship_students_amount FROM streams"; //запрос данных о всех потоках в БД
        try {
            //исполнение запроса
            Connection conn = DriverManager.getConnection(url);
            PreparedStatement getStreams = conn.prepareStatement(sql);
            ResultSet resultStream = getStreams.executeQuery();
            while (resultStream.next()) {
                Stream stream = new Stream(resultStream.getString(1),resultStream.getInt(2));
                sql = "SELECT subject_name FROM subjects WHERE stream_name = ?"; //запрос предметов, изучаемых на потоке
                //исполнение запроса
                PreparedStatement getSubjects = conn.prepareStatement(sql);
                getSubjects.setString(1, stream.getName());
                ResultSet resultSubject = getSubjects.executeQuery();
                while (resultSubject.next()) stream.addSubject(resultSubject.getString(1));
                stream.getStudents().addAll(loadStudentsOfStream(stream.getName())); //загрузка студентов потока из БД
                streams.add(stream); //добовление потока в список потоков
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return streams; //метод возвращает список из всех потоков, присутствующих в БД
    }

    //добавление потока в базу данных
    public static void addStreamToDatabase(Stream stream) {
        //запрос на добавление данных в таблицу потоков
        String sql = "INSERT INTO streams(name, scholarship_students_amount) VALUES(?,?)";
        Connection conn = null;
        try {
            //исполнение запроса
            conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, stream.getName());
            pstmt.setInt(2, stream.scholarshipAmountProperty().get());
            pstmt.execute();
        } catch (SQLException e) {
            e.getMessage();
        }
        //запрос на добавление данных в таблицу предметов
        sql = "INSERT INTO subjects(subject_name, stream_name) VALUES(?,?)";
        for (String subject : stream.getSubjects()) {
            try {
                //исполнение запроса
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, subject);
                pstmt.setString(2, stream.getName());
                pstmt.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    //добавление студента в базу данных
    public static void addStudentToDatabase(Student student, String streamName) {
        //запрос на добавление данных в таблицу студентов
        String sql = "INSERT INTO students(name, surname, stream_name, notation, isContractor) VALUES(?,?,?,?,?)";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, student.getName());
            pstmt.setString(2, student.getSurname());
            pstmt.setString(3, streamName);
            pstmt.setString(4, student.getNotation());
            pstmt.setBoolean(5, student.isContractor());
            pstmt.execute();
        } catch (SQLException e) {
            e.getMessage();
        }
        //запрос на добавление данных в таблицу оценок
        sql = "INSERT INTO marks(student_name, student_surname, stream_name, subject_name, mark) VALUES(?,?,?,?,?)";
        try {
            conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, student.getName());
            pstmt.setString(2, student.getSurname());
            pstmt.setString(3, streamName);
            for (String subject: student.getMarks().keySet()) {
                pstmt.setString(4, subject);
                pstmt.setInt(5, student.getMarks().get(subject));
                pstmt.execute();
            }
        } catch (SQLException e) {
            e.getMessage();
        }
    }

    //удаление потока из БД
    public static void deleteStreamFromDatabase(String streamName) {
        String deleteFromStreamsTable = "DELETE FROM streams WHERE name = ?"; //удаление данных из таблицы потоков
        String deleteStudents = "DELETE FROM students WHERE stream_name = ?"; //удаление данных из таблицы студентов
        String deleteSubjects = "DELETE FROM subjects WHERE stream_name = ?"; //удаление данных из таблицы предметов
        String deleteMarks = "DELETE FROM marks WHERE stream_name = ?";       //удаление данных из таблицы оценок
        //использование того самого внутренного многоразового метода
        deleteStream(deleteFromStreamsTable, streamName);
        deleteStream(deleteStudents, streamName);
        deleteStream(deleteSubjects, streamName);
        deleteStream(deleteMarks, streamName);
    }

    //удаление студента из БД
    public static void deleteStudentFromDatabase(String name, String surname, String streamName) {
        String deleteFromStudents = "DELETE FROM students WHERE name = ? AND surname = ?" +
                " AND stream_name = ?"; //запрос на удаление данных из таблицы студентов
        String deleteFromMarks = "DELETE FROM marks WHERE student_name = ? AND student_surname = ?" +
                " AND stream_name = ?"; //запрос на удаление данных из таблицы оценок
        Connection conn = null;
        try {
            //исполнение запросов
            conn = DriverManager.getConnection(url);
            PreparedStatement deleteStudent = conn.prepareStatement(deleteFromStudents);
            deleteStudent.setString(1,name);
            deleteStudent.setString(2, surname);
            deleteStudent.setString(3, streamName);
            deleteStudent.execute();
            PreparedStatement deleteMarks = conn.prepareStatement(deleteFromMarks);
            deleteMarks.setString(1,name);
            deleteMarks.setString(2, surname);
            deleteMarks.setString(3, streamName);
            deleteMarks.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //сохранение изменений студентов в БД
    public static void saveEditsOfStudent(Student oldStudent, Student newStudent, String streamName) {
        String editStudent = "UPDATE students SET name = ?, " + //запрос на обновление данных в таблице студентов
                "surname = ?, notation = ?, isContractor = ? " +
                "WHERE name = ? AND surname = ? AND stream_name = ?";
        String editMarks = "UPDATE marks SET student_name = ?, " +
                "student_surname = ?, mark = ? " + //запрос на обновление данных в таблицы оценок
                "WHERE student_name = ? AND student_surname = ? AND stream_name = ? AND subject_name = ?";
        Connection conn = null;
        try {
            //исполнение запросов
            conn = DriverManager.getConnection(url);
            PreparedStatement editStudentsTable = conn.prepareStatement(editStudent);
            editStudentsTable.setString(1, newStudent.getName());
            editStudentsTable.setString(2, newStudent.getSurname());
            editStudentsTable.setString(3, newStudent.getNotation());
            editStudentsTable.setBoolean(4, newStudent.isContractor());
            editStudentsTable.setString(5, oldStudent.getName());
            editStudentsTable.setString(6, oldStudent.getSurname());
            editStudentsTable.setString(7, streamName);
            editStudentsTable.execute();
            PreparedStatement editMarksTable = conn.prepareStatement(editMarks);
            editMarksTable.setString(1, newStudent.getName());
            editMarksTable.setString(2, newStudent.getSurname());
            editMarksTable.setString(4, oldStudent.getName());
            editMarksTable.setString(5, oldStudent.getSurname());
            editMarksTable.setString(5, oldStudent.getSurname());
            editMarksTable.setString(6, streamName);
            for(String subject: newStudent.getMarks().keySet()) {
                editMarksTable.setString(7, subject);
                editMarksTable.setInt(3, newStudent.getMarks().get(subject));
                editMarksTable.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //созранение изменений потоков в БД
    public static void saveEditsOfStream(String streamName, int newScholarshipStudentsAmount) {
        String editStream = "UPDATE streams SET scholarship_students_amount = ? where name = ?"; //подготовка запроса
        Connection conn = null;
        try {
            //исполнение запроса
            conn = DriverManager.getConnection(url);
            PreparedStatement edit = conn.prepareStatement(editStream);
            edit.setInt(1, newScholarshipStudentsAmount);
            edit.setString(2, streamName);
            edit.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}