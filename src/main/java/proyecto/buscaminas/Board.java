package proyecto.buscaminas;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

public class Board {
    private int rows;
    private int cols;
    private int mines;
    private Cell[][] cells;  // Matriz de celdas (nodos del grafo)
    private boolean gameOver;
    private String algorithm;  // "DFS" o "BFS"
    
    // Listener para notificar cuando una celda se revela.
    public interface CellRevealListener {
        void cellRevealed(Cell cell);
    }
    private CellRevealListener revealListener;
    
    public void setRevealListener(CellRevealListener listener) {
        this.revealListener = listener;
    }
    
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
    
    private void initializeCells() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                String id = "" + (char)('A' + j) + (i + 1);
                cells[i][j] = new Cell(id, i, j);
            }
        }
    }
    
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
    
    // Implementación de lista enlazada propia para almacenar celdas con minas.
    private class Nodo {
        Cell cell;
        Nodo siguiente;
        Nodo(Cell cell) {
            this.cell = cell;
            this.siguiente = null;
        }
    }
    
    private class ListaEnlazada {
        private Nodo cabeza;
        public ListaEnlazada() {
            cabeza = null;
        }
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
    
    private void calculateMinesAround() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (cells[i][j].hasMine())
                    continue;
                int count = 0;
                // Itera sobre los vecinos utilizando el iterador de SimpleList
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
     * Revela la celda en (r, c). Si ya está marcada o revelada, no hace nada.
     * Si se revela una mina, activa gameOver.
     * Si la celda es vacía y tiene 0 minas alrededor, aplica flood fill usando DFS o BFS.
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
    
    // Flood fill recursivo (DFS)
    private void floodFillDFS(int r, int c) {
        boolean[][] visited = new boolean[rows][cols];
        floodFillDFSHelper(r, c, visited);
    }
    
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
    
    // Implementación de una cola simple propia para BFS.
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
        public T dequeue() {
            if (head == null) return null;
            T data = head.data;
            head = head.next;
            if (head == null) tail = null;
            return data;
        }
        public boolean isEmpty() {
            return head == null;
        }
    }
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
    
    
    // Flood fill iterativo (BFS)
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
    
    public void toggleMark(int r, int c) {
        Cell cell = cells[r][c];
        if (!cell.isRevealed()) {
            cell.setMarked(!cell.isMarked());
        }
    }
    
    public boolean checkVictory() {
        for (int i = 0; i < rows; i++){
            for (int j = 0; j < cols; j++){
                if (!cells[i][j].hasMine() && !cells[i][j].isRevealed())
                    return false;
            }
        }
        return true;
    }
    
    public boolean isGameOver() {
        return gameOver;
    }
    
    // Métodos de consulta:
    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getCellValue(int r, int c) { return cells[r][c].getMinesAround(); }
    public boolean cellHasMine(int r, int c) { return cells[r][c].hasMine(); }
    public boolean cellIsRevealed(int r, int c) { return cells[r][c].isRevealed(); }
    public boolean cellIsMarked(int r, int c) { return cells[r][c].isMarked(); }
    
    /**
     * Genera una representación CSV del estado actual del tablero.
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
     * Carga un Board desde un archivo CSV.
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
}
