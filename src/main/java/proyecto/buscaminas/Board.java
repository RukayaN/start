package proyecto.buscaminas;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

/**
 * Representa el tablero del juego Buscaminas.
 * Esta clase se encarga de:
 * <ul>
 *   <li>Inicializar las celdas del tablero y asignarles identificadores (ej. "A1").</li>
 *   <li>Conectar cada celda con sus vecinos.</li>
 *   <li>Colocar minas aleatoriamente y calcular la cantidad de minas adyacentes.</li>
 *   <li>Manejar el revelado de celdas y el flood fill utilizando DFS o BFS.</li>
 *   <li>Permitir marcar/desmarcar celdas y verificar la condición de victoria.</li>
 *   <li>Guardar y cargar el estado del juego en formato CSV.</li>
 * </ul>
 */
public class Board {
    private int rows;
    private int cols;
    private int mines;
    private Cell[][] cells;  // Matriz de celdas
    private boolean gameOver;
    private String algorithm;  // "DFS" o "BFS"
    
    /**
     * Interface para notificar cuando se revela una celda.
     */
    public interface CellRevealListener {
        /**
         * Método invocado al revelar una celda.
         * @param cell La celda que se ha revelado.
         */
        void cellRevealed(Cell cell);
    }
    private CellRevealListener revealListener;
    
    /**
     * Asigna el listener para la notificación de celdas reveladas.
     * @param listener Objeto que implementa {@code CellRevealListener}.
     */
    public void setRevealListener(CellRevealListener listener) {
        this.revealListener = listener;
    }
    
    /**
     * Crea un nuevo tablero con las dimensiones y cantidad de minas especificadas,
     * usando el algoritmo de flood fill indicado ("DFS" o "BFS").
     * @param rows Número de filas (entre 3 y 10).
     * @param cols Número de columnas (entre 3 y 10).
     * @param mines Número de minas (máximo rows * cols).
     * @param algorithm Algoritmo de flood fill ("DFS" o "BFS").
     * @throws IllegalArgumentException Si las dimensiones o minas están fuera de rango.
     */
    public Board(int rows, int cols, int mines, String algorithm) {
        if (rows < 3 || rows > 10 || cols < 3 || cols > 10 || mines > rows * cols) {
            throw new IllegalArgumentException("Dimensiones o número de minas fuera de rango.");
        }
        this.rows = rows;
        this.cols = cols;
        this.mines = mines;
        this.algorithm = algorithm;
        this.cells = new Cell[rows][cols];
        this.gameOver = false;
        initializeCells();
        assignNeighbors();
        placeMines();
        calculateMinesAround();
    }
    
    /**
     * Inicializa las celdas del tablero asignándoles un identificador basado en su posición.
     * Por ejemplo, la celda en (0,0) se identifica como "A1".
     */
    private void initializeCells() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                String id = "" + (char)('A' + j) + (i + 1);
                cells[i][j] = new Cell(id, i, j);
            }
        }
    }
    
    /**
     * Asigna los vecinos a cada celda del tablero.
     * Cada celda se conecta con las ocho celdas adyacentes (si existen).
     */
    private void assignNeighbors() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                for (int di = -1; di <= 1; di++) {
                    for (int dj = -1; dj <= 1; dj++) {
                        if (di == 0 && dj == 0)
                            continue;
                        int newRow = i + di, newCol = j + dj;
                        if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols) {
                            cells[i][j].addNeighbor(cells[newRow][newCol]);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Clase interna que implementa un nodo para una lista enlazada simple.
     */
    private class Nodo {
        Cell cell;
        Nodo siguiente;
        Nodo(Cell cell) {
            this.cell = cell;
            this.siguiente = null;
        }
    }
    
    /**
     * Lista enlazada simple para almacenar celdas, utilizada para evitar colocar minas duplicadas.
     */
    private class ListaEnlazada {
        private Nodo cabeza;
        public ListaEnlazada() {
            cabeza = null;
        }
        /**
         * Agrega una celda a la lista.
         * @param cell La celda a agregar.
         */
        public void agregar(Cell cell) {
            Nodo nuevo = new Nodo(cell);
            if (cabeza == null) {
                cabeza = nuevo;
            } else {
                Nodo actual = cabeza;
                while (actual.siguiente != null) {
                    actual = actual.siguiente;
                }
                actual.siguiente = nuevo;
            }
        }
        /**
         * Verifica si la lista ya contiene la celda (comparando referencias).
         * @param cell La celda a buscar.
         * @return true si la celda está en la lista, false en caso contrario.
         */
        public boolean contiene(Cell cell) {
            Nodo actual = cabeza;
            while (actual != null) {
                if (actual.cell == cell) {
                    return true;
                }
                actual = actual.siguiente;
            }
            return false;
        }
    }
    
    /**
     * Coloca las minas en posiciones aleatorias del tablero.
     * Se utiliza una lista enlazada propia para asegurar que no se coloque una mina dos veces en la misma celda.
     */
    private void placeMines() {
        Random rand = new Random();
        int placed = 0;
        ListaEnlazada minasColocadas = new ListaEnlazada();
        while (placed < mines) {
            int r = rand.nextInt(rows);
            int c = rand.nextInt(cols);
            if (!minasColocadas.contiene(cells[r][c])) {
                cells[r][c].setMine(true);
                minasColocadas.agregar(cells[r][c]);
                placed++;
            }
        }
    }
    
    /**
     * Calcula y asigna el número de minas adyacentes para cada celda que no contiene mina.
     * Recorre cada celda y utiliza el iterador de vecinos para contar las minas.
     */
    private void calculateMinesAround() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (cells[i][j].hasMine())
                    continue;
                int count = 0;
                SimpleList.CellIterator it = cells[i][j].getNeighborsIterator();
                while(it.hasNext()) {
                    Cell neighbor = it.next();
                    if (neighbor.hasMine())
                        count++;
                }
                cells[i][j].setMinesAround(count);
            }
        }
    }
    
    /**
     * Revela la celda en la posición (r, c).
     * <ul>
     *   <li>Si la celda está marcada o ya revelada, no hace nada.</li>
     *   <li>Si la celda contiene una mina, activa el fin del juego.</li>
     *   <li>Si la celda es vacía (0 minas adyacentes), se inicia un flood fill utilizando DFS o BFS.</li>
     * </ul>
     * @param r Fila de la celda a revelar.
     * @param c Columna de la celda a revelar.
     * @return true si la celda es segura, false si se revela una mina.
     */
    public boolean revealCell(int r, int c) {
        if (r < 0 || r >= rows || c < 0 || c >= cols)
            return true;
        Cell cell = cells[r][c];
        if (cell.isMarked() || cell.isRevealed())
            return true;
        cell.reveal();
        if (revealListener != null) {
            revealListener.cellRevealed(cell);
        }
        if (cell.hasMine()) {
            gameOver = true;
            return false;
        }
        if (cell.getMinesAround() == 0) {
            if (algorithm.equals("DFS"))
                floodFillDFS(r, c);
            else if (algorithm.equals("BFS"))
                floodFillBFS(r, c);
        }
        return true;
    }
    
    /**
     * Inicia el flood fill utilizando búsqueda en profundidad (DFS) a partir de la celda en (r, c).
     * Este método crea una matriz para registrar las celdas visitadas y llama a un método auxiliar.
     * @param r Fila de inicio.
     * @param c Columna de inicio.
     */
    private void floodFillDFS(int r, int c) {
        boolean[][] visited = new boolean[rows][cols];
        floodFillDFSHelper(r, c, visited);
    }
    
    /**
     * Método auxiliar recursivo para el flood fill DFS.
     * Realiza las siguientes acciones:
     * <ul>
     *   <li>Verifica que la celda esté dentro de los límites y no se haya visitado.</li>
     *   <li>Revela la celda si no está marcada.</li>
     *   <li>Si la celda tiene minas adyacentes, detiene la recursión en ese camino.</li>
     *   <li>De lo contrario, continúa recursivamente con cada vecino.</li>
     * </ul>
     * @param r Fila de la celda actual.
     * @param c Columna de la celda actual.
     * @param visited Matriz que registra las celdas ya visitadas.
     */
    private void floodFillDFSHelper(int r, int c, boolean[][] visited) {
        if (r < 0 || r >= rows || c < 0 || c >= cols)
            return;
        if (visited[r][c])
            return;
        visited[r][c] = true;
        Cell cell = cells[r][c];
        if (cell.isMarked())
            return;
        if (!cell.isRevealed()) {
            cell.reveal();
            if (revealListener != null) {
                revealListener.cellRevealed(cell);
            }
        }
        if (cell.getMinesAround() != 0)
            return;
        SimpleList.CellIterator it = cell.getNeighborsIterator();
        while(it.hasNext()) {
            Cell neighbor = it.next();
            floodFillDFSHelper(neighbor.getRow(), neighbor.getCol(), visited);
        }
    }
    
    /**
     * Implementa el flood fill utilizando búsqueda en anchura (BFS) a partir de la celda en (startR, startC).
     * Utiliza una cola propia para recorrer las celdas de manera iterativa.
     * @param startR Fila de inicio.
     * @param startC Columna de inicio.
     */
    private void floodFillBFS(int startR, int startC) {
        SimpleQueue<Cell> queue = new SimpleQueue<>();
        queue.enqueue(cells[startR][startC]);
        while (!queue.isEmpty()) {
            Cell current = queue.dequeue();
            SimpleList.CellIterator it = current.getNeighborsIterator();
            while(it.hasNext()) {
                Cell neighbor = it.next();
                if (!neighbor.isRevealed() && !neighbor.isMarked()) {
                    neighbor.reveal();
                    if (revealListener != null) {
                        revealListener.cellRevealed(neighbor);
                    }
                    if (neighbor.getMinesAround() == 0) {
                        queue.enqueue(neighbor);
                    }
                }
            }
        }
    }
    
    /**
     * Alterna el marcado de la celda en (r, c) si no ha sido revelada.
     * @param r Fila de la celda.
     * @param c Columna de la celda.
     */
    public void toggleMark(int r, int c) {
        Cell cell = cells[r][c];
        if (!cell.isRevealed()) {
            cell.setMarked(!cell.isMarked());
        }
    }
    
    /**
     * Verifica si el jugador ha ganado el juego.
     * El jugador gana si todas las celdas sin mina han sido reveladas.
     * @return true si se cumple la condición de victoria, false en caso contrario.
     */
    public boolean checkVictory() {
        for (int i = 0; i < rows; i++){
            for (int j = 0; j < cols; j++){
                if (!cells[i][j].hasMine() && !cells[i][j].isRevealed())
                    return false;
            }
        }
        return true;
    }
    
    /**
     * Indica si el juego terminó (por haber revelado una mina).
     * @return true si el juego terminó, false en caso contrario.
     */
    public boolean isGameOver() {
        return gameOver;
    }
    
    // Métodos de consulta:
    /**
     * Retorna el número de filas del tablero.
     * @return Número de filas.
     */
    public int getRows() { return rows; }
    
    /**
     * Retorna el número de columnas del tablero.
     * @return Número de columnas.
     */
    public int getCols() { return cols; }
    
    /**
     * Retorna el número de minas adyacentes para la celda en (r, c).
     * @param r Fila de la celda.
     * @param c Columna de la celda.
     * @return Número de minas alrededor.
     */
    public int getCellValue(int r, int c) { return cells[r][c].getMinesAround(); }
    
    /**
     * Indica si la celda en (r, c) contiene una mina.
     * @param r Fila de la celda.
     * @param c Columna de la celda.
     * @return true si la celda tiene mina, false en caso contrario.
     */
    public boolean cellHasMine(int r, int c) { return cells[r][c].hasMine(); }
    
    /**
     * Indica si la celda en (r, c) ya fue revelada.
     * @param r Fila de la celda.
     * @param c Columna de la celda.
     * @return true si la celda está revelada, false en caso contrario.
     */
    public boolean cellIsRevealed(int r, int c) { return cells[r][c].isRevealed(); }
    
    /**
     * Indica si la celda en (r, c) está marcada.
     * @param r Fila de la celda.
     * @param c Columna de la celda.
     * @return true si la celda está marcada, false en caso contrario.
     */
    public boolean cellIsMarked(int r, int c) { return cells[r][c].isMarked(); }
    
    /**
     * Genera una representación en CSV del estado actual del tablero.
     * Incluye las dimensiones, el algoritmo y el estado de cada celda.
     * @return Cadena en formato CSV.
     */
    public String toCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append(rows).append(",").append(cols).append(",").append(mines)
           .append(",").append(algorithm).append("\n");
        for (int i = 0; i < rows; i++){
            for (int j = 0; j < cols; j++){
                Cell cell = cells[i][j];
                sb.append(cell.getId()).append(",");
                sb.append(cell.getRow()).append(",");
                sb.append(cell.getCol()).append(",");
                sb.append(cell.hasMine()).append(",");
                sb.append(cell.isRevealed()).append(",");
                sb.append(cell.isMarked()).append(",");
                sb.append(cell.getMinesAround()).append("\n");
            }
        }
        return sb.toString();
    }
    
    /**
     * Carga un tablero desde un archivo CSV y restaura el estado del juego.
     * El archivo debe haber sido generado previamente por el método {@code toCSV()}.
     * @param filePath Ruta al archivo CSV.
     * @return Objeto {@code Board} con el estado restaurado.
     * @throws IOException Si ocurre un error al leer el archivo o si está vacío.
     */
    public static Board loadFromCSV(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String header = br.readLine();
        if (header == null) {
            br.close();
            throw new IOException("Archivo vacío.");
        }
        String[] parts = header.split(",");
        int rows = Integer.parseInt(parts[0]);
        int cols = Integer.parseInt(parts[1]);
        int mines = Integer.parseInt(parts[2]);
        String algorithm = parts[3];
        Board board = new Board(rows, cols, mines, algorithm);
        String line;
        while ((line = br.readLine()) != null) {
            String[] tokens = line.split(",");
            int r = Integer.parseInt(tokens[1]);
            int c = Integer.parseInt(tokens[2]);
            Cell cell = board.cells[r][c];
            cell.setMine(Boolean.parseBoolean(tokens[3]));
            if (Boolean.parseBoolean(tokens[4])) {
                cell.reveal();
            }
            cell.setMarked(Boolean.parseBoolean(tokens[5]));
            cell.setMinesAround(Integer.parseInt(tokens[6]));
        }
        br.close();
        // Determinar gameOver si alguna mina fue revelada
        for (int i = 0; i < board.rows; i++){
            for (int j = 0; j < board.cols; j++){
                if (board.cells[i][j].hasMine() && board.cells[i][j].isRevealed()){
                    board.gameOver = true;
                    break;
                }
            }
        }
        return board;
    }
    
    /**
     * Establece el algoritmo de flood fill que se utilizará ("DFS" o "BFS").
     * @param algorithm El algoritmo a usar.
     */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
    
    /**
     * Clase interna que implementa una cola simple para el algoritmo BFS.
     * @param <T> Tipo de datos a almacenar.
     */
    private class SimpleQueue<T> {
        private class Node {
            T data;
            Node next;
            Node(T data) {
                this.data = data;
                this.next = null;
            }
        }
        private Node head, tail;
        public SimpleQueue() {
            head = tail = null;
        }
        /**
         * Encola un elemento al final de la cola.
         * @param item El elemento a encolar.
         */
        public void enqueue(T item) {
            Node newNode = new Node(item);
            if (tail != null) {
                tail.next = newNode;
            }
            tail = newNode;
            if (head == null) {
                head = newNode;
            }
        }
        /**
         * Desencola y retorna el elemento al frente de la cola.
         * @return El elemento en el frente, o null si la cola está vacía.
         */
        public T dequeue() {
            if (head == null) return null;
            T data = head.data;
            head = head.next;
            if (head == null) tail = null;
            return data;
        }
        /**
         * Verifica si la cola está vacía.
         * @return true si la cola no tiene elementos, false en caso contrario.
         */
        public boolean isEmpty() {
            return head == null;
        }
    }
}
