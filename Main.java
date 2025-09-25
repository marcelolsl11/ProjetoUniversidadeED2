package huffman;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    /*
    Ponto de entrada do programa. Responsável por validar os argumentos da linha de comando
    e direcionar o fluxo para compressão ou descompressão.
     */
    public static void main(String[] args) {
       // 1. Validação dos Argumentos: Verifica se o número de argumentos é exatamente 3.
        if (args.length != 3) {
            System.err.println("Uso incorreto!");
            System.err.println("Para comprimir: java -jar huffman.jar c <arquivo_original> <arquivo_comprimido>");
            System.err.println("Para descomprimir: java -jar huffman.jar d <arquivo_comprimido> <arquivo_restaurado>");
            return; // Encerra o programa se o uso for incorreto.
        }

        // 2. Extração dos Argumentos: Armazena os argumentos em variáveis.
        String modo = args[0];
        String arquivoEntrada = args[1];
        String arquivoSaida = args[2];

        // 3. Roteamento da Execução: Decide qual método principal chamar com base no modo (c ou d).
        if (modo.equalsIgnoreCase("c")) {
            System.out.println("[Modo de Compressão]");
            long tempoInicio = System.nanoTime();
            comprimeArquivo(arquivoEntrada, arquivoSaida);
            long tempoFim = System.nanoTime();
            long duracaoMs = (tempoFim - tempoInicio) / 1_000_000;
            System.out.println("--------------------------------------------------");
            System.out.println(">> TEMPO DE EXECUÇÃO: " + duracaoMs + " ms");
            System.out.println("--------------------------------------------------");

        } else if (modo.equalsIgnoreCase("d")) {
            System.out.println("[Modo de Descompressão ativado]");
            long tempoInicio = System.nanoTime();
            descomprimeArquivo(arquivoEntrada, arquivoSaida);
            long tempoFim = System.nanoTime();
            long duracaoMs = (tempoFim - tempoInicio) / 1_000_000;
        } else {
            System.err.println("Modo '" + modo + "' inválido. Use 'c' para comprimir ou 'd' para descomprimir.");
        }
    }

    /*
    Direciona o fluxo de compressão de um arquivo.
    Executa todas as etapas: análise de frequência, construção da árvore,
    geração de códigos, escrita do arquivo final e exibição do resumo.
     */
    public static void comprimeArquivo(String caminhoArqOriginal, String caminhoArqSaida) {
        Path caminhoDoArquivo = Paths.get(caminhoArqOriginal);

        // ETAPA 1: Análise de Frequência
        System.out.println("--------------------------------------------------");
        System.out.println("ETAPA 1: Tabela de Frequencia de Caracteres");
        System.out.println("--------------------------------------------------");
        int[] frequencias = calcularFrequenciaDeCaracteres(caminhoDoArquivo);
        if (frequencias == null) {
            return; // Encerra se houver erro na leitura do arquivo.
        }
        for (int i = 0; i < frequencias.length; i++) {
            if (frequencias[i] > 0) {
                System.out.printf("Caractere '%c' (ASCII: %d): %d\n", (char)i, i, frequencias[i]);
            }
        }
        System.out.println("--------------------------------------------------");

        // ETAPA 2: Exibição Conceitual do Min-Heap Inicial
        // A construção real da árvore na ETAPA 3 usa o MinHeap corretamente.

        System.out.println("ETAPA 2: Min-Heap Inicial (Vetor)");
        System.out.println("--------------------------------------------------");
        StringBuilder heapStr = new StringBuilder();
        heapStr.append("[ ");
        boolean first = true;
        for (int i = 0; i < frequencias.length; i++) {
            if (frequencias[i] > 0) {
                if (!first) heapStr.append(", ");
                heapStr.append(String.format("No('%c',%d)", (char)i, frequencias[i]));
                first = false;
            }
        }
        heapStr.append(" ]");
        System.out.println(heapStr.toString());
        System.out.println("--------------------------------------------------");

        // ETAPA 3: Construção da Árvore de Huffman e Impressão
        System.out.println("ETAPA 3: Arvore de Huffman");
        System.out.println("--------------------------------------------------");
        No raizDaArvore = reconstruirArvoreDeHuffman(frequencias);
        imprimirArvoreFormatada(raizDaArvore); // Impressão visual da árvore
        System.out.println("--------------------------------------------------");

        // ETAPA 4: Geração e Exibição da Tabela de Códigos
        System.out.println("ETAPA 4: Tabela de Codigos de Huffman");
        System.out.println("--------------------------------------------------");
        String[] tabelaDeCodigos = gerarTabelaDeCodigos(raizDaArvore);
        for (int i = 0; i < tabelaDeCodigos.length; i++) {
            if (tabelaDeCodigos[i] != null) {
                System.out.printf("Caractere '%c': %s\n", (char)i, tabelaDeCodigos[i]);
            }
        }
        System.out.println("--------------------------------------------------");

        // ETAPA 5: Escrita do Arquivo e Resumo da Compressão

        // Calcula o tamanho original para o resumo
        java.io.File fileOriginal = new java.io.File(caminhoArqOriginal);
        long tamanhoOriginalBytes = fileOriginal.length();
        long tamanhoOriginalBits = tamanhoOriginalBytes * 8;

        // Calcula o tamanho teórico dos dados comprimidos (número exato de bits)
        StringBuilder bufferBits = new StringBuilder();
        try {
            byte[] todosOsBytesDoOriginal = Files.readAllBytes(Paths.get(caminhoArqOriginal));
            for (byte b : todosOsBytesDoOriginal) {
                bufferBits.append(tabelaDeCodigos[b & 0xFF]);
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo para gerar resumo: " + e.getMessage());
        }
        long tamanhoComprimidoBitsTeorico = bufferBits.length();

        // Realiza a escrita do arquivo comprimido no disco
        escreverArquivoComprimido(caminhoArqOriginal, caminhoArqSaida, frequencias, tabelaDeCodigos);

        // Pega o tamanho real (prático) do arquivo salvo em disco
        java.io.File fileComprimido = new java.io.File(caminhoArqSaida);
        long tamanhoComprimidoBytes = fileComprimido.length();

        // Calcula a taxa de compressão com base no tamanho teórico
        double taxa = tamanhoOriginalBits == 0 ? 0 : 100.0 * (1.0 - ((double)tamanhoComprimidoBitsTeorico / tamanhoOriginalBits));

        System.out.println("ETAPA 5: Resumo da Compressao");
        System.out.println("--------------------------------------------------");
        System.out.printf("Tamanho original....: %d bits (%d bytes)\n", tamanhoOriginalBits, tamanhoOriginalBytes);
        System.out.printf("Tamanho comprimido..: %d bits (%d bytes)\n", tamanhoComprimidoBitsTeorico, tamanhoComprimidoBytes);
        System.out.printf("Taxa de compressao..: %.2f%%\n", taxa);
        System.out.println("--------------------------------------------------");
    }
    
    
    //Lê um arquivo, conta a frequência de cada byte (0-255) e retorna um vetor de inteiros.
    public static int[] calcularFrequenciaDeCaracteres(Path caminhoDoArquivo) {
        int[] frequencias = new int[256];
        try {
            // Lê todos os bytes do arquivo de uma vez para a memória.
            byte[] todosOsBytes = Files.readAllBytes(caminhoDoArquivo);
            // Itera por cada byte lido.
            for (byte b : todosOsBytes) {
                // A operação 'b & 0xFF' converte o byte (que em Java é assinado, -128 a 127)
                // para um valor inteiro sem sinal (0 a 255), que pode ser usado como índice.
                frequencias[b & 0xFF]++;
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo '" + caminhoDoArquivo + "': " + e.getMessage());
            return null; // Sinaliza o erro para o método chamador.
        }
        return frequencias;
    }

    /*
    Constrói a Árvore de Huffman a partir da tabela de frequências.
    Utiliza um MinHeap como estrutura auxiliar para garantir que os nós de menor
    frequência sejam combinados primeiro.
     */
    public static No reconstruirArvoreDeHuffman(int[] frequencias) {
        MinHeap filaPrioridade = new MinHeap();

        // Passo 1: Popular o Min-Heap.
        // Cria um nó-folha para cada caractere que aparece no texto (frequência > 0)
        // e o insere na fila de prioridades (Min-Heap).
        for (int i = 0; i < 256; i++) {
            if (frequencias[i] > 0) {
                No noFolha = new No((char) i, frequencias[i]);
                filaPrioridade.add(noFolha);
            }
        }

        // Passo 2: Construir a Árvore.
        // O processo se repete enquanto houver mais de um nó na fila.
        // A cada iteração, os dois nós de menor frequência são removidos,
        // combinados sob um novo nó-pai, e este nó-pai é reinserido na fila.
        while (filaPrioridade.size() > 1) {
            No esquerda = filaPrioridade.poll(); // Remove o menor
            No direita = filaPrioridade.poll();  // Remove o segundo menor
            No pai = new No(esquerda, direita);
            filaPrioridade.add(pai);
        }

        // Ao final, o único nó restante na fila é a raiz da árvore completa.
        return filaPrioridade.poll();
    }

    /*
    Gera a tabela de códigos de Huffman (ex: 'A' -> "01") percorrendo a árvore.
    Este é o método de entrada que inicia o processo recursivo.
     */
    public static String[] gerarTabelaDeCodigos(No raizDaArvore) {
        String[] tabela = new String[256];
        // A chamada recursiva começa na raiz, com um caminho (código) inicial vazio.
        gerarTabelaRecursivo(raizDaArvore, "", tabela);
        return tabela;
    }

    /*
    Método auxiliar recursivo que percorre a árvore para montar os códigos.
    Adiciona 0 ao código ao descer para a esquerda e "1" ao descer para a direita.
     */
    private static void gerarTabelaRecursivo(No noAtual, String codigoAtual, String[] tabela) {
        if (noAtual == null) {
            return;
        }

        // Caso Base: Se o nó é uma folha (não tem filhos), encontramos um caractere.
        // O código acumulado até aqui é o código de Huffman para este caractere.
        if (noAtual.esquerda == null && noAtual.direita == null) {
            tabela[(int) noAtual.caractere] = codigoAtual;
            return;
        }

        // Passo Recursivo: Se não é uma folha, continua a descida.
        // Chama para a esquerda, anexando '0' ao código.
        gerarTabelaRecursivo(noAtual.esquerda, codigoAtual + "0", tabela);
        // Chama para a direita, anexando '1' ao código.
        gerarTabelaRecursivo(noAtual.direita, codigoAtual + "1", tabela);
    }
    
    /*
    Escreve o arquivo comprimido (.huff) no disco. O arquivo consiste em um cabeçalho
    seguido pelos dados comprimidos.
     */
    public static void escreverArquivoComprimido( String caminhoArqOriginal, String caminhoArqSaida, int[] frequencias, String[] tabelaDeCodigos) {
        Path caminhoSaida = Paths.get(caminhoArqSaida);
        StringBuilder bufferBits = new StringBuilder();

        // ETAPA 1: Ler o arquivo original e construir a string gigante de bits
        try {
            byte[] todosOsBytesDoOriginal = Files.readAllBytes(Paths.get(caminhoArqOriginal));
            for (byte b : todosOsBytesDoOriginal) {
                bufferBits.append(tabelaDeCodigos[b & 0xFF]);
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo original para compressão: " + e.getMessage());
            return;
        }

        // ETAPA 2: Escrever o arquivo de saída (Cabeçalho + Dados)
        try (OutputStream fileOut = Files.newOutputStream(caminhoSaida);
             DataOutputStream escritorDeDados = new DataOutputStream(fileOut)) {

            // --- ESCREVENDO O CABEÇALHO ---
            // O cabeçalho é essencial para que a descompressão possa reconstruir a árvore.
            // 1. Tabela de Frequências: 256 inteiros (256 * 4 = 1024 bytes).
            for (int freq : frequencias) {
                escritorDeDados.writeInt(freq);
            }
            // 2. Número de Caracteres: 1 long (8 bytes). Essencial para saber onde parar de ler na descompressão.
            escritorDeDados.writeLong(Files.size(Paths.get(caminhoArqOriginal)));

            // --- ESCREVENDO OS DADOS COMPRIMIDOS ---
            String stringDeBits = bufferBits.toString();
            
            // Percorre a string de bits em fatias de 8 para formar os bytes.
            for (int i = 0; i < stringDeBits.length(); i += 8) {
                String pedacoDe8Bits;
                if (i + 8 > stringDeBits.length()) {
                    pedacoDe8Bits = stringDeBits.substring(i);
                    // Padding: Se o último pedaço for menor que 8, completa com '0's à direita.
                    while (pedacoDe8Bits.length() < 8) {
                        pedacoDe8Bits += "0";
                    }
                } else {
                    pedacoDe8Bits = stringDeBits.substring(i, i + 8);
                }

                int byteVal = Integer.parseInt(pedacoDe8Bits, 2); // Converte a string "01000001" para o inteiro 65.
                escritorDeDados.write(byteVal); // Escreve o byte no arquivo.
            }
            System.out.println("Arquivo comprimido com sucesso!");
        } catch (IOException e) {
            System.err.println("Erro ao escrever o arquivo comprimido: " + e.getMessage());
        }
    }
    
    /**
    Orquestra o processo de descompressão de um arquivo .huff.
     */
    public static void descomprimeArquivo(String caminhoArqComprimido, String caminhoArqSaida) {
        Path caminhoComprimido = Paths.get(caminhoArqComprimido);
        No raizDaArvore = null;
        long numCaracteresOriginais = 0;
        StringBuilder stringDeBits = new StringBuilder();

        // --- ETAPA 1: LER CABEÇALHO E RECONSTRUIR ESTRUTURAS ---
        try (DataInputStream leitor = new DataInputStream(Files.newInputStream(caminhoComprimido))) {
            // 1. Lê a tabela de frequências do cabeçalho.
            int[] frequencias = new int[256];
            for (int i = 0; i < 256; i++) {
                frequencias[i] = leitor.readInt();
            }
            // 2. Lê o número original de caracteres.
            numCaracteresOriginais = leitor.readLong();
            
            // 3. Com as frequências, reconstrói a mesma Árvore de Huffman da compressão.
            raizDaArvore = reconstruirArvoreDeHuffman(frequencias);

            // 4. Lê o restante do arquivo (os dados comprimidos) e converte para uma string de bits.
            int byteLido;
            while ((byteLido = leitor.read()) != -1) {
                // Converte o byte em uma string binária de 8 bits, preenchendo com zeros à esquerda se necessário.
                stringDeBits.append(String.format("%8s", Integer.toBinaryString(byteLido & 0xFF)).replace(' ', '0'));
            }

        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo comprimido: " + e.getMessage());
            return;
        }

        // --- ETAPA 2: DECODIFICAR OS DADOS USANDO A ÁRVORE ---
        // Este é o "percurso guiado pelos dados".
        StringBuilder textoDecodificado = new StringBuilder();
        if (raizDaArvore != null) {
            No noAtual = raizDaArvore;
            for (char bit : stringDeBits.toString().toCharArray()) {
                // Navega na árvore: '0' para esquerda, '1' para direita.
                noAtual = (bit == '0') ? noAtual.esquerda : noAtual.direita;

                // Se chegamos a um nó folha, decodificamos um caractere.
                if (noAtual.esquerda == null && noAtual.direita == null) {
                    textoDecodificado.append(noAtual.caractere);
                    noAtual = raizDaArvore; // Volta para a raiz para o próximo caractere.

                    // Condição de parada: se já decodificamos todos os caracteres, interrompemos o loop.
                    // Isso ignora os bits de padding do final do arquivo.
                    if (textoDecodificado.length() == numCaracteresOriginais) {
                        break;
                    }
                }
            }
        }
        
        // --- ETAPA 3: ESCREVER O ARQUIVO FINAL ---
        try {
            Path caminhoSaida = Paths.get(caminhoArqSaida);
            // Usa ISO_8859_1 para garantir que cada 'char' seja mapeado para um único byte.
            Files.writeString(caminhoSaida, textoDecodificado.toString(), StandardCharsets.ISO_8859_1);
            System.out.println("Arquivo descomprimido com sucesso para: " + caminhoArqSaida);
        } catch (IOException e) {
            System.err.println("Erro ao escrever o arquivo descomprimido: " + e.getMessage());
        }
    }

    /**
    Imprime uma representação visual da Árvore de Huffman no console.
    Utiliza um percurso em pré-ordem para a impressão.
     */
    private static void imprimirArvoreFormatada(No raiz) {
        if (raiz == null) {
            System.out.println("Árvore está vazia ou consiste em um único nó.");
            return;
        }
        imprimirArvoreFormatadaRec(raiz, "", true);
    }
    
    /**
    Método auxiliar recursivo para a impressão formatada da árvore.
     */
    private static void imprimirArvoreFormatadaRec(No no, String prefixo, boolean isRaiz) {
        if (no == null) return;
    
        System.out.print(prefixo);
        if(!isRaiz){
            System.out.print("└──");
        }
    
        if (no.esquerda == null && no.direita == null) {
            // É um nó folha
            System.out.printf(" ('%c', %d)\n", no.caractere, no.frequencia);
        } else {
            // É um nó interno
            System.out.printf(" (Pai, %d)\n", no.frequencia);
            String novoPrefixo = prefixo + (isRaiz ? "    " : "    ");
            imprimirArvoreFormatadaRec(no.direita, novoPrefixo, false);
            imprimirArvoreFormatadaRec(no.esquerda, novoPrefixo, false);
        }
    }
}