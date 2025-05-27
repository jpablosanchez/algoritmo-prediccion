package co.edu.unbosque.model;

import java.util.List;

public class ArchivoModel {

    private List<List<String>> datos;
    private List<String> columnas;
    private int columnaObjetivo;

    public ArchivoModel() {}

    public ArchivoModel(List<List<String>> datos, List<String> columnas, int columnaObjetivo) {
        this.datos = datos;
        this.columnas = columnas;
        this.columnaObjetivo = columnaObjetivo;
    }

    public List<List<String>> getDatos() {
        return datos;
    }

    public void setDatos(List<List<String>> datos) {
        this.datos = datos;
    }

    public List<String> getColumnas() {
        return columnas;
    }

    public void setColumnas(List<String> columnas) {
        this.columnas = columnas;
    }

    public int getColumnaObjetivo() {
        return columnaObjetivo;
    }

    public void setColumnaObjetivo(int columnaObjetivo) {
        this.columnaObjetivo = columnaObjetivo;
    }
}
