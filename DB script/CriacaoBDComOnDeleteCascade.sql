-- Criação do banco de dados
CREATE DATABASE loja_informatica;

-- Seleciona o banco
USE loja_informatica;

-- Tabela Cliente
CREATE TABLE cliente (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    nome_cliente VARCHAR(100) NOT NULL,
    telefone_cliente VARCHAR(20)
);

-- Tabela Equipamento
CREATE TABLE equipamento (
    id_equi INT AUTO_INCREMENT PRIMARY KEY,
    nome_equi VARCHAR(100) NOT NULL,
    preco_equi DECIMAL(10,2) NOT NULL
);

-- Tabela Pedido
CREATE TABLE pedido (
    id_pedido INT AUTO_INCREMENT PRIMARY KEY,
    data_hora_pedido DATETIME NOT NULL,
    valor_total DECIMAL(10,2) NOT NULL,
    id_cliente INT NOT NULL,

    CONSTRAINT fk_pedido_cliente
        FOREIGN KEY (id_cliente)
        REFERENCES cliente(id_cliente)
        ON DELETE CASCADE
);

-- Tabela Item_Pedido
CREATE TABLE item_pedido (
    id_pedido INT NOT NULL,
    id_equi INT NOT NULL,
    quantidade INT NOT NULL,

    PRIMARY KEY (id_pedido, id_equi),

    CONSTRAINT fk_item_pedido
        FOREIGN KEY (id_pedido)
        REFERENCES pedido(id_pedido)
        ON DELETE CASCADE,

    CONSTRAINT fk_item_equipamento
        FOREIGN KEY (id_equi)
        REFERENCES equipamento(id_equi)
        ON DELETE CASCADE
);