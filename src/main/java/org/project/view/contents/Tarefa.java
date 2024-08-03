package org.project.view.contents;

import java.time.LocalDate;

public class Tarefa {
    private String id;         // Adicione este campo
    private String titulo;
    private String descricao;
    private LocalDate dia;
    private String status;

    public Tarefa(String id, String titulo, String descricao, LocalDate dia, String status) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.dia = dia;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDate getDia() {
        return dia;
    }

    public String getStatus() {
        return status;
    }
}
