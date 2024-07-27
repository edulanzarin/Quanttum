package org.project;

import javafx.application.Application;
import org.project.model.AuthWindow;

/**
 * A classe principal da aplicação JavaFX.
 * Esta classe serve como o ponto de entrada para a aplicação.
 * Ela inicializa e lança a aplicação JavaFX utilizando a classe LicenseWindow.
 */
public class App {

    /**
     * O método main é o ponto de entrada para a aplicação JavaFX.
     * Ele utiliza o método Application.launch para iniciar a aplicação.
     *
     * @param args Argumentos da linha de comando passados para a aplicação.
     */
    public static void main(String[] args) {
        Application.launch(AuthWindow.class, args);
    }
}
