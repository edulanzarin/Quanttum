package org.project.functions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class MoverArquivosExpress {

    private File pastaRaiz;
    private File pastaDestino;

    public MoverArquivosExpress(File pastaRaiz, File pastaDestino) {
        this.pastaRaiz = pastaRaiz;
        this.pastaDestino = pastaDestino;
    }

    public void copiarArquivos(String username) {
        if (pastaRaiz == null || pastaDestino == null || !pastaRaiz.isDirectory() || !pastaDestino.isDirectory()) {
            System.out.println("Pastas inválidas.");
            return;
        }

        try {
            copiarArquivosRecursivo(pastaRaiz);
            System.out.println("Arquivos copiados com sucesso.");
            new RegistrarLog().logAction(username, "mover-arquivos-express");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao copiar arquivos.");
        }
    }

    private void copiarArquivosRecursivo(File pasta) throws IOException {
        File[] arquivos = pasta.listFiles();
        if (arquivos != null) {
            for (File arquivo : arquivos) {
                if (arquivo.isFile()) {
                    copiarArquivo(arquivo, pasta);
                } else if (arquivo.isDirectory()) {
                    copiarArquivosRecursivo(arquivo);
                }
            }
        }
    }

    private void copiarArquivo(File arquivo, File pastaOrigem) throws IOException {
        String nomeArquivo = arquivo.getName();
        String nomePasta = pastaOrigem.getName();

        // Extrair o nome da pasta após o primeiro "-"
        String nomePastaParte = nomePasta;
        int index = nomePasta.indexOf("-");
        if (index != -1 && index + 1 < nomePasta.length()) {
            nomePastaParte = nomePasta.substring(index + 1).trim();
        }

        // Separar o nome e a extensão do arquivo
        String nomeArquivoSemExtensao = nomeArquivo;
        String extensao = "";

        int extIndex = nomeArquivo.lastIndexOf('.');
        if (extIndex != -1) {
            nomeArquivoSemExtensao = nomeArquivo.substring(0, extIndex);
            extensao = nomeArquivo.substring(extIndex);
        }

        // Criar o novo nome do arquivo com sufixo antes da extensão
        String novoNomeArquivo = nomeArquivoSemExtensao + " - " + nomePastaParte + extensao;
        File destino = new File(pastaDestino, novoNomeArquivo);

        // Copiar e renomear o arquivo
        Files.copy(arquivo.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
