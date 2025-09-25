package huffman;

import java.util.ArrayList;

/*
Implementação de uma Fila de Prioridades utilizando um Min-Heap.
Um Min-Heap é uma estrutura de dados de árvore binária onde o valor de cada nó pai
é menor ou igual ao valor de seus filhos. Esta implementação utiliza um ArrayList
para representar a árvore de forma implícita.
 */
public class MinHeap {
    private ArrayList<No> heap;

    /*
    Construtor padrão que inicializa o heap como um ArrayList vazio.
     */
    public MinHeap() {
        this.heap = new ArrayList<>();
    }

    /*
    Calcula o índice do nó pai de um dado nó no índice i.
     */
    public int getParentIndex(int i) {
        return (i - 1) / 2;
    }

    /*
    Calcula o índice do filho esquerdo de um dado nó no índice i.
     */
    public int getLeftChildIndex(int i){
        return 2 * i + 1;
    }
    
    /*
    Calcula o índice do filho direito de um dado nó no índice i.
     */
    public int getRightChildIndex(int i){
        return 2 * i + 2;
    }

    /*
    Troca a posição de dois nós no heap.
     */
    public void swap(int i, int j) {
        No temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }

    /*
    Restaura a propriedade do Min-Heap movendo um nó para cima na árvore.
    O processo continua enquanto o nó for menor que seu pai, trocando-os de lugar.
    É usado após a inserção de um novo elemento.
     */
    private void siftUp(int index) {
        int paiIndex = getParentIndex(index);
        //index > 0 = Enquanto não for raiz || E o nó atual (get(index) for menor que o meu pai(get(paiIndex))
        while (index > 0 && heap.get(index).compareTo(heap.get(paiIndex)) < 0) {
            //Realiza a troca
            swap(index, paiIndex);
            index = paiIndex; // Eu subo para a posição do meu antigo pai
            paiIndex = getParentIndex(index); // E recalculo meu novo pai
        }
    }

    /*
    Adiciona um novo nó ao heap.
    O nó é inserido no final do ArrayList e, em seguida, o método siftUp é
    chamado para garantir que a propriedade do heap seja mantida.
     */
    public void add(No novoNo) {
        // Passo 1: Adiciona o nó no final da lista
        heap.add(novoNo);
        // Passo 2: Pega o índice do último elemento e o "peneira para cima"
        siftUp(heap.size() - 1);
    }

    /*
    Restaura a propriedade do Min-Heap movendo um nó para baixo na árvore.
    O processo compara o nó com seus filhos e o troca pelo menor deles, repetindo
    o processo até que o nó seja menor que ambos os filhos.
    É usado após a remoção do elemento raiz.
     */
    //enquanto eu for maior que algum dos meus filhos, eu troco de lugar com o menor deles e continuo descendo"
    private void siftDown(int index) {
        // O loop continua enquanto o nó atual tiver pelo menos um filho esquerdo.
        // Se não tem filho esquerdo, certamente não tem direito.
        while (getLeftChildIndex(index) < heap.size()) {
            int FilhoEsquerda = getLeftChildIndex(index);
            int FilhoDireita = getRightChildIndex(index);
            // 1. COMECE assumindo que o filho esquerdo é o menor.
            int menorFilho = FilhoEsquerda;
            // 2. VERIFIQUE se o filho direito existe E é ainda menor.
            if (FilhoDireita < heap.size() && heap.get(FilhoDireita).compareTo(heap.get(FilhoEsquerda)) < 0) {
                menorFilho = FilhoDireita;
            }
            // 3. AGORA SIM, compare o pai com o MENOR dos filhos.
            if (heap.get(index).compareTo(heap.get(menorFilho)) > 0) {
                // Se o pai é maior, a ordem está errada. Troque.
                swap(index, menorFilho);
                // Continue o processo de "afundar" a partir da nova posição do nó.
                index = menorFilho;
            } else {
                // Se o pai já é menor ou igual ao seu menor filho, ele está no lugar certo.
                break;
            }
        }
    }

    /*
    Retorna o número de elementos no heap.
     */
    public int size() { 
        return this.heap.size(); 
    }
    
    /*
    Remove e retorna o menor elemento do heap (a raiz).
    O último elemento do heap é movido para a raiz e o método siftDown é
    chamado para restaurar a propriedade do heap.
     */
    public No poll() {
        if (heap.isEmpty()) {
            throw new IllegalStateException("O Heap está vazio! Impossível remover.");
        }

        // 1. Salva o menor elemento (a raiz) para retornar depois.
        No menorElemento = heap.get(0);

        // 2. Pega o último elemento da lista e o remove.
        No ultimoElemento = heap.remove(heap.size() - 1);

        // 3. Se o heap não ficou vazio após a remoção,
        //    coloque o "último" na raiz e conserte a estrutura.
        if (!heap.isEmpty()) {
            heap.set(0, ultimoElemento);
            siftDown(0); // Chama o siftDown para afundar o nó até o lugar certo.
        }

        return menorElemento;
    }
}