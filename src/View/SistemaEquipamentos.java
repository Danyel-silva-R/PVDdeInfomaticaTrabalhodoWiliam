package View;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

// Importação da sua conexão JDBC
import connection.ConnectionDB;

public class SistemaEquipamentos extends JFrame {

    private static final DecimalFormat df = new DecimalFormat("R$ #,##0.00");

    // Abas de navegação
    private JTabbedPane tabbedPane;

    // --- Componentes da Aba Pedidos ---
    private JComboBox<Cliente> cbClientes;
    private JComboBox<Equipamento> cbEquipamentos;
    private JSpinner spinnerQtd;
    private JTable tabelaCarrinho;
    private DefaultTableModel modelCarrinho;
    private JLabel lblTotalPedido;
    private final List<ItemCarrinho> carrinhoItens = new ArrayList<>();

    // --- Componentes da Aba Clientes ---
    private JTextField txtClienteNome;
    private JTextField txtClienteTelefone;
    private JTable tabelaClientes;
    private DefaultTableModel modelClientes;

    // --- Componentes da Aba Equipamentos ---
    private JTextField txtEquiNome;
    private JTextField txtEquiPreco;
    private JTable tabelaEquipamentos;
    private DefaultTableModel modelEquipamentos;

    public SistemaEquipamentos() {
        setTitle("Sistema de Gestão de Equipamentos ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 600);
        setLocationRelativeTo(null);

        // Define layout principal simples
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();

        // Inicializa as abas de forma limpa
        tabbedPane.addTab("Realizar Pedido", criarAbaPedidos());
        tabbedPane.addTab("Clientes", criarAbaClientes());
        tabbedPane.addTab("Equipamentos", criarAbaEquipamentos());

        add(tabbedPane, BorderLayout.CENTER);

        // Carrega dados iniciais do Banco de Dados
        atualizarTudo();
    }

    private void atualizarTudo() {
        listarClientesNoBanco();
        listarEquipamentosNoBanco();
        carregarDropdowns();
    }


    private JPanel criarAbaPedidos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Formulário Superior de Seleção
        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Novo Pedido"));

        cbClientes = new JComboBox<>();
        cbClientes.setPreferredSize(new Dimension(200, 25));

        cbEquipamentos = new JComboBox<>();
        cbEquipamentos.setPreferredSize(new Dimension(220, 25));

        spinnerQtd = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        spinnerQtd.setPreferredSize(new Dimension(60, 25));

        JButton btnAdd = new JButton("Adicionar Item");
        btnAdd.addActionListener(this::adicionarItemAoCarrinho);

        formPanel.add(new JLabel("Cliente:"));
        formPanel.add(cbClientes);
        formPanel.add(new JLabel("Equipamento:"));
        formPanel.add(cbEquipamentos);
        formPanel.add(new JLabel("Qtd:"));
        formPanel.add(spinnerQtd);
        formPanel.add(btnAdd);

        panel.add(formPanel, BorderLayout.NORTH);

        // Grade Central de Itens do Pedido Atual
        String[] colunas = {"Código", "Equipamento", "Preço Unitário", "Quantidade", "Subtotal"};
        modelCarrinho = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaCarrinho = new JTable(modelCarrinho);
        panel.add(new JScrollPane(tabelaCarrinho), BorderLayout.CENTER);

        // Painel de Fechamento do Pedido
        JPanel rodape = new JPanel(new BorderLayout(10, 10));
        lblTotalPedido = new JLabel("Total: R$ 0,00");
        lblTotalPedido.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JButton btnGravar = new JButton("Gravar Pedido no Banco");
        btnGravar.setPreferredSize(new Dimension(200, 50));
        btnGravar.addActionListener(this::gravarPedidoNoBanco);

        rodape.add(lblTotalPedido, BorderLayout.WEST);
        rodape.add(btnGravar, BorderLayout.EAST);

        panel.add(rodape, BorderLayout.SOUTH);

        return panel;
    }

    private void adicionarItemAoCarrinho(ActionEvent e) {
        Equipamento eq = (Equipamento) cbEquipamentos.getSelectedItem();
        int qtd = (Integer) spinnerQtd.getValue();

        if (eq == null) {
            JOptionPane.showMessageDialog(this, "Selecione um equipamento!");
            return;
        }

        double subtotal = eq.preco * qtd;

        // Se já existe o item no carrinho, apenas atualiza a quantidade
        boolean existe = false;
        for (ItemCarrinho item : carrinhoItens) {
            if (item.idEqui == eq.id) {
                item.quantidade += qtd;
                item.subtotal = item.quantidade * eq.preco;
                existe = true;
                break;
            }
        }

        if (!existe) {
            carrinhoItens.add(new ItemCarrinho(eq.id, eq.nome, eq.preco, qtd, subtotal));
        }

        spinnerQtd.setValue(1);
        atualizarCarrinhoVisual();
    }

    private void atualizarCarrinhoVisual() {
        modelCarrinho.setRowCount(0);
        double totalGeral = 0;
        for (ItemCarrinho item : carrinhoItens) {
            modelCarrinho.addRow(new Object[]{
                    item.idEqui,
                    item.nome,
                    df.format(item.precoUnit),
                    item.quantidade,
                    df.format(item.subtotal)
            });
            totalGeral += item.subtotal;
        }
        lblTotalPedido.setText("Total: " + df.format(totalGeral));
    }

    private void gravarPedidoNoBanco(ActionEvent e) {
        Cliente cli = (Cliente) cbClientes.getSelectedItem();
        if (cli == null) {
            JOptionPane.showMessageDialog(this, "Selecione um cliente para o pedido!");
            return;
        }
        if (carrinhoItens.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Adicione pelo menos um item à tabela!");
            return;
        }

        double totalPedido = 0;
        for (ItemCarrinho item : carrinhoItens) {
            totalPedido += item.subtotal;
        }

        Connection conn = null;
        PreparedStatement stmtPedido = null;
        PreparedStatement stmtItem = null;
        ResultSet rsKeys = null;

        try {
            conn = ConnectionDB.getConnection();
            conn.setAutoCommit(false); // Inicia Transação

            // 1. Insere o Pedido
            String sqlPedido = "INSERT INTO pedido (data_hora_pedido, valor_total, id_cliente) VALUES (?, ?, ?)";
            stmtPedido = conn.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS);
            stmtPedido.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            stmtPedido.setDouble(2, totalPedido);
            stmtPedido.setInt(3, cli.id);
            stmtPedido.executeUpdate();

            // 2. Recupera o id_pedido gerado
            rsKeys = stmtPedido.getGeneratedKeys();
            int idPedidoGerado = -1;
            if (rsKeys.next()) {
                idPedidoGerado = rsKeys.getInt(1);
            }

            // 3. Insere os Itens associados ao Pedido
            String sqlItem = "INSERT INTO item_pedido (id_pedido, id_equi, quantidade) VALUES (?, ?, ?)";
            stmtItem = conn.prepareStatement(sqlItem);

            for (ItemCarrinho item : carrinhoItens) {
                stmtItem.setInt(1, idPedidoGerado);
                stmtItem.setInt(2, item.idEqui);
                stmtItem.setInt(3, item.quantidade);
                stmtItem.addBatch();
            }
            stmtItem.executeBatch();

            conn.commit(); // Salva permanentemente

            JOptionPane.showMessageDialog(this, "Pedido #" + idPedidoGerado + " gravado com sucesso!");
            carrinhoItens.clear();
            atualizarCarrinhoVisual();

        } catch (SQLException ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException rollbackEx) { rollbackEx.printStackTrace(); }
            }
            JOptionPane.showMessageDialog(this, "Erro ao gravar pedido!\n" + ex.getMessage());
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
            try { if (rsKeys != null) rsKeys.close(); } catch (SQLException ignored) {}
            try { if (stmtPedido != null) stmtPedido.close(); } catch (SQLException ignored) {}
            try { if (stmtItem != null) stmtItem.close(); } catch (SQLException ignored) {}
        }
    }

    private JPanel criarAbaClientes() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Formulário Superior
        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Cadastrar Novo Cliente"));

        txtClienteNome = new JTextField(20);
        txtClienteTelefone = new JTextField(15);
        JButton btnSalvar = new JButton("Cadastrar");
        btnSalvar.addActionListener(this::salvarClienteNoBanco);

        formPanel.add(new JLabel("Nome:"));
        formPanel.add(txtClienteNome);
        formPanel.add(new JLabel("Telefone:"));
        formPanel.add(txtClienteTelefone);
        formPanel.add(btnSalvar);

        panel.add(formPanel, BorderLayout.NORTH);

        // Tabela central de Clientes
        String[] colunas = {"ID Cliente", "Nome Cliente", "Telefone"};
        modelClientes = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaClientes = new JTable(modelClientes);
        panel.add(new JScrollPane(tabelaClientes), BorderLayout.CENTER);

        // Rodapé de Ações
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnExcluir = new JButton("Remover Cliente Selecionado");
        btnExcluir.addActionListener(this::deletarClienteNoBanco);
        footer.add(btnExcluir);

        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    private void salvarClienteNoBanco(ActionEvent e) {
        String nome = txtClienteNome.getText().trim();
        String tel = txtClienteTelefone.getText().trim();

        if (nome.isEmpty() || tel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos!");
            return;
        }

        String sql = "INSERT INTO cliente (nome_cliente, telefone_cliente) VALUES (?, ?)";

        try {
            Connection conn = ConnectionDB.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nome);
                stmt.setString(2, tel);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Cliente cadastrado com sucesso!");
                txtClienteNome.setText("");
                txtClienteTelefone.setText("");

                atualizarTudo();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar cliente:\n" + ex.getMessage());
        }
    }

    private void deletarClienteNoBanco(ActionEvent e) {
        int sidebarRow = tabelaClientes.getSelectedRow();
        if (sidebarRow == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um cliente na tabela!");
            return;
        }

        int id = (Integer) modelClientes.getValueAt(sidebarRow, 0);
        String sql = "DELETE FROM cliente WHERE id_cliente = ?";

        try {
            Connection conn = ConnectionDB.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Cliente removido com sucesso!");
                atualizarTudo();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao remover cliente (verifique dependências de pedidos):\n" + ex.getMessage());
        }
    }

    private void listarClientesNoBanco() {
        modelClientes.setRowCount(0);
        String sql = "SELECT * FROM cliente";

        try {
            Connection conn = ConnectionDB.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    modelClientes.addRow(new Object[]{
                            rs.getInt("id_cliente"),
                            rs.getString("nome_cliente"),
                            rs.getString("telefone_cliente")
                    });
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // =========================================================================
    // ABA 3: CADASTRO / EXCLUSÃO DE EQUIPAMENTOS
    // =========================================================================
    private JPanel criarAbaEquipamentos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Formulário Superior
        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Cadastrar Novo Equipamento"));

        txtEquiNome = new JTextField(20);
        txtEquiPreco = new JTextField(10);
        JButton btnSalvar = new JButton("Cadastrar");
        btnSalvar.addActionListener(this::salvarEquipamentoNoBanco);

        formPanel.add(new JLabel("Nome:"));
        formPanel.add(txtEquiNome);
        formPanel.add(new JLabel("Preço Unitario(R$):"));
        formPanel.add(txtEquiPreco);
        formPanel.add(btnSalvar);

        panel.add(formPanel, BorderLayout.NORTH);

        // Tabela central de Equipamentos
        String[] colunas = {"ID Equipamento", "Nome Equipamento", "Preço Unitario"};
        modelEquipamentos = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaEquipamentos = new JTable(modelEquipamentos);
        panel.add(new JScrollPane(tabelaEquipamentos), BorderLayout.CENTER);

        // Rodapé de Ações
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnExcluir = new JButton("Remover Equipamento Selecionado");
        btnExcluir.addActionListener(this::deletarEquipamentoNoBanco);
        footer.add(btnExcluir);

        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    private void salvarEquipamentoNoBanco(ActionEvent e) {
        String nome = txtEquiNome.getText().trim();
        String precoStr = txtEquiPreco.getText().trim().replace(",", ".");

        if (nome.isEmpty() || precoStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos!");
            return;
        }

        try {
            double preco = Double.parseDouble(precoStr);
            String sql = "INSERT INTO equipamento (nome_equi, preco_equi) VALUES (?, ?)";

            try {
                Connection conn = ConnectionDB.getConnection();
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, nome);
                    stmt.setDouble(2, preco);
                    stmt.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Equipamento cadastrado com sucesso!");
                    txtEquiNome.setText("");
                    txtEquiPreco.setText("");

                    atualizarTudo();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar equipamento:\n" + ex.getMessage());
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Insira um valor numérico para o preço!");
        }
    }

    private void deletarEquipamentoNoBanco(ActionEvent e) {
        int sidebarRow = tabelaEquipamentos.getSelectedRow();
        if (sidebarRow == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um equipamento na tabela!");
            return;
        }

        int id = (Integer) modelEquipamentos.getValueAt(sidebarRow, 0);
        String sql = "DELETE FROM equipamento WHERE id_equi = ?";

        try {
            Connection conn = ConnectionDB.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Equipamento removido com sucesso!");
                atualizarTudo();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao remover equipamento (verifique dependências de pedidos):\n" + ex.getMessage());
        }
    }

    private void listarEquipamentosNoBanco() {
        modelEquipamentos.setRowCount(0);
        String sql = "SELECT * FROM equipamento";

        try {
            Connection conn = ConnectionDB.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    modelEquipamentos.addRow(new Object[]{
                            rs.getInt("id_equi"),
                            rs.getString("nome_equi"),
                            df.format(rs.getDouble("preco_equi"))
                    });
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    private void carregarDropdowns() {
        cbClientes.removeAllItems();
        cbEquipamentos.removeAllItems();

        try {
            Connection conn = ConnectionDB.getConnection();

            // Carrega Clientes
            String sqlClientes = "SELECT id_cliente, nome_cliente, telefone_cliente FROM cliente ORDER BY nome_cliente ASC";
            try (PreparedStatement stmt = conn.prepareStatement(sqlClientes);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    cbClientes.addItem(new Cliente(
                            rs.getInt("id_cliente"),
                            rs.getString("nome_cliente"),
                            rs.getString("telefone_cliente")
                    ));
                }
            }

            // Carrega Equipamentos
            String sqlEquipamentos = "SELECT id_equi, nome_equi, preco_equi FROM equipamento ORDER BY nome_equi ASC";
            try (PreparedStatement stmt = conn.prepareStatement(sqlEquipamentos);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    cbEquipamentos.addItem(new Equipamento(
                            rs.getInt("id_equi"),
                            rs.getString("nome_equi"),
                            rs.getDouble("preco_equi")
                    ));
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static class Cliente {
        int id;
        String nome;
        String telefone;

        public Cliente(int id, String nome, String telefone) {
            this.id = id;
            this.nome = nome;
            this.telefone = telefone;
        }

        @Override
        public String toString() {
            return nome + " (ID: " + id + ")";
        }
    }

    public static class Equipamento {
        int id;
        String nome;
        double preco;

        public Equipamento(int id, String nome, double preco) {
            this.id = id;
            this.nome = nome;
            this.preco = preco;
        }

        @Override
        public String toString() {
            return nome + " — " + df.format(preco) + "/diária";
        }
    }

    public static class ItemCarrinho {
        int idEqui;
        String nome;
        double precoUnit;
        int quantidade;
        double subtotal;

        public ItemCarrinho(int idEqui, String nome, double precoUnit, int quantidade, double subtotal) {
            this.idEqui = idEqui;
            this.nome = nome;
            this.precoUnit = precoUnit;
            this.quantidade = quantidade;
            this.subtotal = subtotal;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SistemaEquipamentos().setVisible(true);
        });
    }
}