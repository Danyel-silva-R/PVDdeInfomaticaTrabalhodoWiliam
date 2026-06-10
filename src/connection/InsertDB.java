package connection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class InsertDB {

    public void inserirCliente(String nome, String telefone) {

        String sql = "INSERT INTO cliente (nome_cliente, telefone_cliente) VALUES (?, ?)";

        try {

            Connection conexao = ConnectionDB.getConnection();

            PreparedStatement stmt = conexao.prepareStatement(sql);

            stmt.setString(1, nome);
            stmt.setString(2, telefone);

            stmt.executeUpdate();

            JOptionPane.showMessageDialog(
                    null,
                    "Cliente cadastrado com sucesso!"
            );

            stmt.close();

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    null,
                    "Erro ao cadastrar cliente!\n" + e.getMessage()
            );
        }
    }

    public void inserirEquipamento(String nome, double preco) {

        String sql = "INSERT INTO equipamento (nome_equi, preco_equi) VALUES (?, ?)";

        try {

            Connection conexao = ConnectionDB.getConnection();

            PreparedStatement stmt = conexao.prepareStatement(sql);

            stmt.setString(1, nome);
            stmt.setDouble(2, preco);

            stmt.executeUpdate();

            JOptionPane.showMessageDialog(
                    null,
                    "Equipamento cadastrado com sucesso!"
            );

            stmt.close();

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    null,
                    "Erro ao cadastrar equipamento!\n" + e.getMessage()
            );
        }
    }

    public void realizarPedido(int idCliente, int idEquipamento, int quantidade) {

        try {

            Connection conexao = ConnectionDB.getConnection();

            // Busca o preço do equipamento
            String sqlPreco = "SELECT preco_equi FROM equipamento WHERE id_equi = ?";

            PreparedStatement stmt = conexao.prepareStatement(sqlPreco);
            stmt.setInt(1, idEquipamento);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                double preco = rs.getDouble("preco_equi");
                double valorTotal = preco * quantidade;

                // Insere o pedido
                String sqlPedido =
                        "INSERT INTO pedido (data_hora_pedido, valor_total, id_cliente) VALUES (?, ?, ?)";

                PreparedStatement stmtPedido =
                        conexao.prepareStatement(sqlPedido);

                stmtPedido.setTimestamp(
                        1,
                        new java.sql.Timestamp(System.currentTimeMillis())
                );
                stmtPedido.setDouble(2, valorTotal);
                stmtPedido.setInt(3, idCliente);

                stmtPedido.executeUpdate();

                JOptionPane.showMessageDialog(
                        null,
                        "Pedido realizado!"
                );

                stmtPedido.close();
            } else {

                JOptionPane.showMessageDialog(
                        null,
                        "Equipamento não encontrado!"
                );
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    null,
                    "Erro ao realizar pedido!\n" + e.getMessage()
            );
        }
    }

}
