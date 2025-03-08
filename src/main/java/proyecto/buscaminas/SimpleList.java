package proyecto.buscaminas;

/**
 * Implementación simple de una lista enlazada para almacenar objetos de tipo Cell.
 */
public class SimpleList {
    private Node head;
    
    private class Node {
        Cell data;
        Node next;
        Node(Cell data) {
            this.data = data;
            this.next = null;
        }
    }
    
    public SimpleList() {
        head = null;
    }
    
    /**
     * Agrega una celda al final de la lista.
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
     * Verifica si la lista contiene la celda (comparando su identificador).
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
     * Retorna un iterador para recorrer la lista.
     */
    public CellIterator iterator() {
        return new CellIterator(head);
    }
    
    public class CellIterator {
        private Node current;
        public CellIterator(Node start) {
            this.current = start;
        }
        public boolean hasNext() {
            return current != null;
        }
        public Cell next() {
            Cell data = current.data;
            current = current.next;
            return data;
        }
    }
}
