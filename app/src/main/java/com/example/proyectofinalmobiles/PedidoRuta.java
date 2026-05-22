package com.example.proyectofinalmobiles;

public class PedidoRuta {
    private String cliente;
    private String direccion;
    private String estado;

    public PedidoRuta(String cliente, String direccion, String estado) {
        this.cliente = cliente;
        this.direccion = direccion;
        this.estado = estado;
    }

    public String getCliente() {
        return cliente;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getEstado() {
        return estado;
    }
}
