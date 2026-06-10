package connection;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class ShowDB {

    public void listarClientes() {

        String sql = "SELECT * FROM cliente";

        try {

            Connection conexao = ConnectionDB.getConnection();

            PreparedStatement stmt = conexao.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                System.out.println(
                        "ID: " + rs.getInt("id_cliente")
                                + " | Nome: " + rs.getString("nome_cliente")
                                + " | Telefone: " + rs.getString("telefone_cliente")
                );
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    null,
                    "Erro ao listar clientes!\n" + e.getMessage()
            );
        }
    }

    public void listarEquipamentos() {

        String sql = "SELECT * FROM equipamento";

        try {

            Connection conexao = ConnectionDB.getConnection();

            PreparedStatement stmt = conexao.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                System.out.println(
                        "ID: " + rs.getInt("id_equi")
                                + " | Nome: " + rs.getString("nome_equi")
                                + " | Preço: R$ " + rs.getDouble("preco_equi")
                );
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    null,
                    "Erro ao listar equipamentos!\n" + e.getMessage()
            );
        }
    }

    public void listarPedidos() {

        String sql = "SELECT * FROM pedido";

        try {

            Connection conexao = ConnectionDB.getConnection();

            PreparedStatement stmt = conexao.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                System.out.println(
                        "ID Pedido: " + rs.getInt("id_pedido")
                                + " | Data: " + rs.getTimestamp("data_hora_pedido")
                                + " | Valor Total: R$ " + rs.getDouble("valor_total")
                                + " | Cliente: " + rs.getInt("id_cliente")
                );
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    null,
                    "Erro ao listar pedidos!\n" + e.getMessage()
            );
        }
    }

    public void listarPedidosComClientes() {

        String sql =
                "SELECT p.id_pedido, p.data_hora_pedido, p.valor_total, " +
                        "c.nome_cliente " +
                        "FROM pedido p " +
                        "INNER JOIN cliente c ON p.id_cliente = c.id_cliente";

        try {

            Connection conexao = ConnectionDB.getConnection();

            PreparedStatement stmt = conexao.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                System.out.println(
                        "Pedido: " + rs.getInt("id_pedido")
                                + " | Cliente: " + rs.getString("nome_cliente")
                                + " | Valor: R$ " + rs.getDouble("valor_total")
                                + " | Data: " + rs.getTimestamp("data_hora_pedido")
                );
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    null,
                    "Erro ao listar pedidos!\n" + e.getMessage()
            );
        }
    }

    public void listarPedidosDetalhados() {

        String sql =
                "SELECT p.id_pedido, c.nome_cliente, " +
                        "p.data_hora_pedido, p.valor_total, " +
                        "e.nome_equi, ip.quantidade, e.preco_equi, " +
                        "(ip.quantidade * e.preco_equi) AS subtotal " +
                        "FROM pedido p " +
                        "INNER JOIN cliente c ON p.id_cliente = c.id_cliente " +
                        "INNER JOIN item_pedido ip ON p.id_pedido = ip.id_pedido " +
                        "INNER JOIN equipamento e ON ip.id_equi = e.id_equi " +
                        "ORDER BY p.id_pedido";

        try {

            Connection conexao = ConnectionDB.getConnection();

            PreparedStatement stmt = conexao.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();

            int pedidoAtual = -1;

            while (rs.next()) {

                int idPedido = rs.getInt("id_pedido");

                if (idPedido != pedidoAtual) {

                    if (pedidoAtual != -1) {
                        System.out.println("-----------------------------------");
                    }

                    pedidoAtual = idPedido;

                    System.out.println(
                            "\nCLIENTE: " + rs.getString("nome_cliente") +
                                    "\nDATA: " + rs.getTimestamp("data_hora_pedido")
                    );
                }

                System.out.println(
                        "\nEQUIPAMENTO: " + rs.getString("nome_equi") +
                                "\nQUANTIDADE: " + rs.getInt("quantidade") +
                                "\nPREÇO UNITÁRIO: R$ " + rs.getDouble("preco_equi") +
                                "\nSUBTOTAL: R$ " + rs.getDouble("subtotal")
                );

                if (rs.isLast() ||
                        rs.getInt("id_pedido") != pedidoAtual) {

                    System.out.println(
                            "\nVALOR TOTAL DO PEDIDO: R$ "
                                    + rs.getDouble("valor_total")
                    );
                }
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    null,
                    "Erro ao listar pedidos!\n" + e.getMessage()
            );
        }
    }
}
