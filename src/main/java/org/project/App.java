package org.project;

import org.project.functions.VerificarAtualizacao;

public class App {

    public static void main(String[] args) {
        // Verifica se há uma atualização antes de iniciar a aplicação
        VerificarAtualizacao.verificarAtualizacao();
    }
}
