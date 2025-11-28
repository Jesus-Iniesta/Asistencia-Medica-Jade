package com.medical.jade.messages;

public class Cita {
    private String pacienteId;
    private String nombre;
    private String sintomas;
    private int numeroTurno;
    private String estado;

    // Getters y Setters
    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String pacienteId) { this.pacienteId = pacienteId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getSintomas() { return sintomas; }
    public void setSintomas(String sintomas) { this.sintomas = sintomas; }
    public int getNumeroTurno() { return numeroTurno; }
    public void setNumeroTurno(int numeroTurno) { this.numeroTurno = numeroTurno; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}