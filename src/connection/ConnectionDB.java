package connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionDB {
    private static final String url = "jdbc:mysql://127.0.0.1:3306/loja_informatica";
    private static final String user = "root";
    private static final String password = "";

    private static Connection conexao = null;

    public static Connection getConnection() {

        if(conexao == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");

                conexao = DriverManager.getConnection(url, user, password);
                return conexao;

            } catch (ClassNotFoundException e) {
                System.out.println("Driver MySQL não encontrado!");
                e.printStackTrace();

            } catch (SQLException e) {
                System.out.println("Erro ao conectar");
                e.printStackTrace();
            }
        } else {
            return conexao;
        }
        return conexao;

    }
}
