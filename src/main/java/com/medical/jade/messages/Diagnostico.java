package com.medical.jade.messages;

public class Diagnostico {
    private String pacienteId;
    private String diagnostico;
    private String tratamiento;
    private String fechaProxima;

    // Getters y Setters
    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String pacienteId) { this.pacienteId = pacienteId; }
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }
    public String getFechaProxima() { return fechaProxima; }
    public void setFechaProxima(String fechaProxima) { this.fechaProxima = fechaProxima; }
}