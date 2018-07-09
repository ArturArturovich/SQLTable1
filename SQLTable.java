import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;

public class SQLTable {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String LOGIN = "postgres";
    private static final String PASSWORD = "12345678";
    private static Connection connection;
    private static PreparedStatement ps;
    private static Statement statement;

    public static void main(String[] args) {

        try {
            DriverManager.registerDriver(new org.postgresql.Driver());
            connection = DriverManager.getConnection(DB_URL, LOGIN, PASSWORD);
            sqlExecuteUpdate("DELETE FROM dz");
            sqlInsertRandomData();
            sqlSelectAll();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.println("\nИспользуйте команды: ");
            System.out.println("/цена {ТОВАР}");
            System.out.println("/сменитьцену {ТОВАР} {ЦЕНА}");
            System.out.println("/товарыпоцене {ЦЕНА ОТ} {ЦЕНА ДО}");
            System.out.println("/выход");
            while (true) {
                System.out.println("Введите команду: \n");
                String response = br.readLine();
                if (response.startsWith("/цена")) {
                    String arg1 = response.split(" ")[0];
                    sqlSelectPrice(response.split(" ")[0]);
                } else if (response.startsWith("/сменитьцену")) {
                    String arg1 = response.split(" ")[0];
                    Double arg2 = Double.parseDouble(response.split(" ")[1]);
                    sqlUpdatePrice(arg1, arg2);
                } else if (response.startsWith("/товарыпоцене")) {
                    Double arg1 = Double.parseDouble(response.split(" ")[0]);
                    Double arg2 = Double.parseDouble(response.split(" ")[1]);
                    sqlSelectPricesBetween(arg1, arg2);
                } else if (response.startsWith("/выход")) {
                    System.exit(0);
                } else {
                    System.out.println("Команда некорректна!");
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void sqlExecuteUpdate(String SQLQuery) {
        try {
            statement = connection.createStatement();
            statement.executeUpdate(SQLQuery);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void sqlCreateTable() {
        try {
            statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE dz(" +
                    "id serial NOT NULL," +
                    "prodid numeric NOT NULL," +
                    "title text NOT NULL," +
                    "cost numeric(1000,2) NOT NULL," +
                    "CONSTRAINT id PRIMARY KEY (id)," +
                    "CONSTRAINT prodid UNIQUE (prodid))" +
                    "WITH (OIDS = FALSE);" +
                    "ALTER TABLE dz OWNER TO postgres;");
            System.out.println("таблица создана");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void sqlInsertRandomData() {
        try {
            ps = connection.prepareStatement("INSERT INTO dz (prodid, title, cost) VALUES (?, ?, ?)");
            for (int i = 1; i <= 10000; i++) {
                ps.setInt(1, (i * 10));
                ps.setString(2, "Товар" + i);
                ps.setDouble(3, (0 + Math.random() * 10000000) / 100);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void sqlSelectAll(){
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM dz");
            printAllColunms(resultSet);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void sqlSelectPrice(String title) {
        try {
            ps = connection.prepareStatement("SELECT title, cost FROM dz WHERE title = ?");
            ps.setString(1, title);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next() == false) {
                System.out.println("Такого товара нет: " + title);
            } else {
                do {
                    String t = resultSet.getString("title");
                    Double c = resultSet.getDouble("cost");
                    System.out.println("[" + t + "|" + c + "]");
                } while (resultSet.next());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void sqlUpdatePrice(String title, Double cost) {
        try {
            ps = connection.prepareStatement("UPDATE dz SET cost = ? WHERE title = ?");
            ps.setDouble(1, cost);
            ps.setString(2, title);
            ps.executeUpdate();
            sqlSelectPrice(title);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void sqlSelectPricesBetween(Double from, Double to) {
        try {
            ps = connection.prepareStatement("SELECT * FROM dz WHERE cost BETWEEN ? AND ?");
            ps.setDouble(1, from);
            ps.setDouble(2, to);
            ResultSet resultSet = ps.executeQuery();
            printAllColunms(resultSet);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void printAllColunms(ResultSet resultSet) throws SQLException {
        if (resultSet.next() == false) {
            System.out.println("В выборке 0 записей");
        } else {
            do {
                String id = resultSet.getString("id");
                String prodid = resultSet.getString("prodid");
                String title = resultSet.getString("title");
                String cost = resultSet.getString("cost");
                System.out.println("[" + id + "|" + prodid + "|" + title + "|" + cost + "]");
            } while (resultSet.next());
        }
}}
