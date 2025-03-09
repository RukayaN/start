package proyecto.buscaminas;

/**
 * Implementación simple de una lista enlazada para almacenar objetos de tipo {@link Cell}.
 * Esta clase se utiliza, por ejemplo, para mantener la lista de vecinos de una celda.
 */
public class SimpleList {
    private Node head;
    
    /**
     * Clase interna que representa un nodo de la lista enlazada.
     */
    private class Node {
        Cell data;
        Node next;
        
        /**
         * Crea un nodo con la celda especificada.
         * @param data La celda a almacenar en este nodo.
         */
        Node(Cell data) {
            this.data = data;
            this.next = null;
        }
    }
    
    /**
     * Crea una nueva lista vacía.
     */
    public SimpleList() {
        head = null;
    }
    
    /**
     * Agrega una celda al final de la lista.
     * @param cell La celda a agregar.
     */
    public void add(Cell cell) {
        if (head == null) {
            head = new Node(cell);
        } else {
            Node current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = new Node(cell);
        }
    }
    
    /**
     * Verifica si la lista contiene una celda con el mismo identificador.
     * @param cell La celda a buscar.
     * @return true si la celda ya está en la lista, false en caso contrario.
     */
    public boolean contains(Cell cell) {
        Node current = head;
        while (current != null) {
            if (current.data.getId().equals(cell.getId())) {
                return true;
            }
            current = current.next;
        }
        return false;
    }
    
    /**
     * Retorna un iterador para recorrer la lista de celdas.
     * @return Un iterador de tipo {@link CellIterator}.
     */
    public CellIterator iterator() {
        return new CellIterator(head);
    }
    
    /**
     * Iterador para recorrer las celdas almacenadas en la lista.
     */
    public class CellIterator {
        private Node current;
        
        /**
         * Crea un iterador a partir del nodo inicial.
         * @param start El nodo de inicio.
         */
        public CellIterator(Node start) {
            this.current = start;
        }
        
        /**
         * Verifica si hay más celdas en la lista.
         * @return true si existen más elementos, false si se alcanzó el final.
         */
        public boolean hasNext() {
            return current != null;
        }
        
        /**
         * Retorna la siguiente celda en la lista y avanza el iterador.
         * @return La siguiente celda.
         */
        public Cell next() {
            Cell data = current.data;
            current = current.next;
            return data;
        }
    }
}
