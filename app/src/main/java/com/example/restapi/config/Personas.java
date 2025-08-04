package com.example.restapi.config;

public class Personas {


    public String id;
    public String nombres;
    public String apellidos;
    public String direccion;
    public String telefono;
    public String fechanac;
    public String foto;

    // Constructor vacío
    public Personas() {
    }

    // Constructor con parámetros (opcional)
    public Personas(String id, String nombres, String apellidos, String direccion, String telefono, String fechanac, String foto) {
        this.id = id;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.direccion = direccion;
        this.telefono = telefono;
        this.fechanac = fechanac;
        this.foto = foto;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getFechanac() {
        return fechanac;
    }

    public void setFechanac(String fechanac) {
        this.fechanac = fechanac;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
}


    @Override
    public String toString() {
        return "PersonaModel{" +
                "id=" + id +
                ", nombres='" + nombres + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", direccion='" + direccion + '\'' +
                ", telefono='" + telefono + '\'' +
                ", fechanac='" + fechanac + '\'' +
                ", foto='" + (foto != null && foto.length() > 20 ? foto.substring(0, 20) + "..." : foto) + '\'' +
                '}';
    }


}
