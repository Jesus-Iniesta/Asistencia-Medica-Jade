package com.medical.jade.messages;

public class Diagnostico {
    private String pacienteId;
    private String diagnostico;
    private String tratamiento;
    private String fechaProxima;
    private String doctorNombre;
    private String doctorEspecialidad;
    private String mensajeDoctor;  // 🔥 NUEVO: Mensaje personalizado del doctor
    private String nombrePaciente; // 🔥 NUEVO: Nombre del paciente para el saludo

    // Getters y Setters
    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String pacienteId) { this.pacienteId = pacienteId; }
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }
    public String getFechaProxima() { return fechaProxima; }
    public void setFechaProxima(String fechaProxima) { this.fechaProxima = fechaProxima; }
    public String getDoctorNombre() { return doctorNombre; }
    public void setDoctorNombre(String doctorNombre) { this.doctorNombre = doctorNombre; }
    public String getDoctorEspecialidad() { return doctorEspecialidad; }
    public void setDoctorEspecialidad(String doctorEspecialidad) { this.doctorEspecialidad = doctorEspecialidad; }
    public String getMensajeDoctor() { return mensajeDoctor; }
    public void setMensajeDoctor(String mensajeDoctor) { this.mensajeDoctor = mensajeDoctor; }
    public String getNombrePaciente() { return nombrePaciente; }
    public void setNombrePaciente(String nombrePaciente) { this.nombrePaciente = nombrePaciente; }
}