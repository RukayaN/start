package proyecto.buscaminas;

/**
 * Representa una casilla (nodo) del Buscaminas.
 * Cada {@code Cell} contiene:
 * <ul>
 *   <li>Un identificador (ej. "A1").</li>
 *   <li>Su posición en el tablero (fila y columna).</li>
 *   <li>Estado interno: si tiene mina, si fue revelada o está marcada.</li>
 *   <li>El número de minas adyacentes.</li>
 *   <li>Una lista de vecinos, implementada con la clase {@link SimpleList}.</li>
 * </ul>
 */
public class Cell {
    private String id;
    private int row;
    private int col;
    private boolean hasMine;
    private boolean revealed;
    private boolean marked;
    private int minesAround;
    private SimpleList neighbors;  // Lista enlazada propia de vecinos

    /**
     * Crea una nueva celda con el identificador y posición especificados.
     * Inicialmente, la celda no tiene mina, no está revelada, no está marcada,
     * y el número de minas alrededor es 0.
     * @param id Identificador de la celda (ej. "A1").
     * @param row Fila en la que se ubica la celda.
     * @param col Columna en la que se ubica la celda.
     */
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

    /**
     * Retorna el identificador de la celda.
     * @return El identificador.
     */
    public String getId() {
        return id;
    }

    /**
     * Retorna la fila en la que se ubica la celda.
     * @return La fila.
     */
    public int getRow() {
        return row;
    }

    /**
     * Retorna la columna en la que se ubica la celda.
     * @return La columna.
     */
    public int getCol() {
        return col;
    }

    /**
     * Indica si la celda contiene una mina.
     * @return true si tiene mina, false en caso contrario.
     */
    public boolean hasMine() {
        return hasMine;
    }

    /**
     * Establece o quita una mina en la celda.
     * @param hasMine true para asignar una mina, false para quitarla.
     */
    public void setMine(boolean hasMine) {
        this.hasMine = hasMine;
    }

    /**
     * Indica si la celda ya ha sido revelada.
     * @return true si está revelada, false en caso contrario.
     */
    public boolean isRevealed() {
        return revealed;
    }

    /**
     * Revela la celda.
     */
    public void reveal() {
        this.revealed = true;
    }

    /**
     * Indica si la celda está marcada con bandera.
     * @return true si está marcada, false en caso contrario.
     */
    public boolean isMarked() {
        return marked;
    }

    /**
     * Marca o desmarca la celda. Solo se permite marcar si la celda no ha sido revelada.
     * @param marked true para marcar, false para desmarcar.
     */
    public void setMarked(boolean marked) {
        if (!revealed) {
            this.marked = marked;
        }
    }

    /**
     * Retorna el número de minas que hay en las celdas adyacentes.
     * @return Número de minas alrededor.
     */
    public int getMinesAround() {
        return minesAround;
    }

    /**
     * Establece el número de minas adyacentes.
     * @param minesAround Número de minas alrededor.
     */
    public void setMinesAround(int minesAround) {
        this.minesAround = minesAround;
    }

    /**
     * Agrega una celda vecina a la lista de vecinos si aún no ha sido agregada.
     * @param neighbor La celda vecina a agregar.
     */
    public void addNeighbor(Cell neighbor) {
        if (neighbor != null && !neighbors.contains(neighbor)) {
            neighbors.add(neighbor);
        }
    }

    /**
     * Retorna un iterador para recorrer los vecinos de la celda.
     * @return Un iterador de vecinos.
     */
    public SimpleList.CellIterator getNeighborsIterator() {
        return neighbors.iterator();
    }
}
