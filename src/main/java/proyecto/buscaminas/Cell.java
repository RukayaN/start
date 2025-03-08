package proyecto.buscaminas;

/**
 * Representa una casilla (nodo) del Buscaminas.
 * Cada Cell tiene:
 *   - Un identificador (por ejemplo, "A1").
 *   - Su posición (fila y columna) en el tablero.
 *   - Estado interno: si tiene mina, si fue revelada o está marcada.
 *   - El número de minas adyacentes.
 *   - Una lista de vecinos, implementada con una estructura propia.
 */
public class Cell {
    private String id;
    private int row;
    private int col;
    private boolean hasMine;
    private boolean revealed;
    private boolean marked;
    private int minesAround;
    private SimpleList neighbors;  // Implementación propia de lista enlazada

    public Cell(String id, int row, int col) {
        this.id = id;
        this.row = row;
        this.col = col;
        this.hasMine = false;
        this.revealed = false;
        this.marked = false;
        this.minesAround = 0;
        this.neighbors = new SimpleList();
    }

    public String getId() {
        return id;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public boolean hasMine() {
        return hasMine;
    }

    public void setMine(boolean hasMine) {
        this.hasMine = hasMine;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public void reveal() {
        this.revealed = true;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        if (!revealed) {
            this.marked = marked;
        }
    }

    public int getMinesAround() {
        return minesAround;
    }

    public void setMinesAround(int minesAround) {
        this.minesAround = minesAround;
    }

    /**
     * Agrega un vecino a la lista de vecinos de la celda.
     */
    public void addNeighbor(Cell neighbor) {
        if (neighbor != null && !neighbors.contains(neighbor)) {
            neighbors.add(neighbor);
        }
    }

    /**
     * Retorna un iterador para recorrer los vecinos.
     */
    public SimpleList.CellIterator getNeighborsIterator() {
        return neighbors.iterator();
    }
}
