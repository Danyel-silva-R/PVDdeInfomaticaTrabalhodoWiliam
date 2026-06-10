package connection;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DeleteDB {

    public void deletarCliente(int idCliente) {

        String sql = "DELETE FROM cliente WHERE id_cliente = ?";

        try {

            Connection conexao = ConnectionDB.getConnection();

            PreparedStatement stmt = conexao.prepareStatement(sql);

            stmt.setInt(1, idCliente);

            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                JOptionPane.showMessageDialog(
                        null,
                        "Cliente removido com sucesso!"
                );
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "Cliente não encontrado!"
                );
            }

            stmt.close();

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    null,
                    "Erro ao remover cliente!\n" + e.getMessage()
            );
        }
    }

    public void deletarEquipamento(int idEquipamento) {

        String sql = "DELETE FROM equipamento WHERE id_equi = ?";

        try {

            Connection conexao = ConnectionDB.getConnection();

            PreparedStatement stmt = conexao.prepareStatement(sql);

            stmt.setInt(1, idEquipamento);

            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                JOptionPane.showMessageDialog(
                        null,
                        "Equipamento removido com sucesso!"
                );
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "Equipamento não encontrado!"
                );
            }

            stmt.close();

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    null,
                    "Erro ao remover equipamento!\n" + e.getMessage()
            );
        }
    }
}
