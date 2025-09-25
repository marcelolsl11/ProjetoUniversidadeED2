package huffman;

/**
 * Representa um nó na Árvore de Huffman.
 * Esta classe é versátil e pode representar tanto um nó-folha (contendo um caractere
 * e sua frequência) quanto um nó interno/pai (que conecta dois outros nós).
 * Implementa a interface Comparable para ser utilizável diretamente pelo MinHeap.
 */
class No implements Comparable<No> {
    char caractere;
    int frequencia;
    No esquerda, direita;

    /*
    Construtor para criar um NÓ-FOLHA.
    Usado para representar os caracteres individuais e suas contagens de frequência.
     */
    public No(char caractere, int frequencia) {
        this.caractere = caractere;
        this.frequencia = frequencia;
        this.esquerda = null; 
        this.direita = null;
    }

    /*
    Construtor para criar um NÓ-PAI (ou nó interno).
    Usado durante a construção da árvore para combinar dois nós de menor frequência.
    O caractere é nulo ('\0') para indicar que não é uma folha.
     */
    public No(No esquerda, No direita) {
        this.caractere = '\0'; // Caractere "nulo" para indicar que é um nó interno.
        this.frequencia = esquerda.frequencia + direita.frequencia; // A frequência é a soma dos filhos.
        this.esquerda = esquerda;
        this.direita = direita;
    }

    /*
    Compara este nó com outro nó com base na frequência.
    Este método é o "cérebro" da fila de prioridades (MinHeap), permitindo
    que o heap ordene os nós e sempre retorne aquele com a menor frequência.
    Retorna um valor negativo se a frequência deste nó for menor, zero se for igual,
    positivo se for maior.
     */
    @Override
    public int compareTo(No outroNo) {
        return this.frequencia - outroNo.frequencia;
    }

    /*
    Retorna uma representação em String do nó.
    Facilita a depuração e a impressão da árvore, mostrando informações
    diferentes para nós-folha e nós-pai.
     */
    @Override
    public String toString() {
        // Se for um nó interno/pai, não mostramos o caractere nulo.
        if (this.caractere == '\0') {
            return "No-Pai (Freq: " + this.frequencia + ")";
        }
        // Se for um nó-folha, mostramos o caractere e a frequência.
        else {
            return "No('" + this.caractere + "', " + this.frequencia + ")";
        }
    }
}