package proyecto.buscaminas;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Buscaminas extends JFrame {
    private Board board;               // Lógica del juego
    private JButton[][] buttons;       // Matriz de botones (UI)
    private JPanel boardPanel;
    private boolean gameInProgress = true;

    // Configuración inicial: filas y columnas entre 3 y 10
    private int filas = 9;
    private int columnas = 9;
    private int minas = 10;
    // Algoritmo de flood fill ("DFS" o "BFS")
    private String searchAlgorithm = "DFS";

    // Objeto para registrar la secuencia de casillas reveladas/marcadas.
    private TraversalGraph traversalGraph;
    
    // Nuevo: contador de banderas colocadas
    private int flagsPlaced = 0;
    
    // Panel de control para cambiar el algoritmo en tiempo real
    private JPanel algorithmPanel;
    private JRadioButton dfsButton;
    private JRadioButton bfsButton;

    public Buscaminas() {
        setTitle("Buscaminas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setJMenuBar(createMenuBar());
        // Crea el panel de control de algoritmo y lo agrega a la ventana
        createAlgorithmPanel();
        initGame();
        setSize(600, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    /**
     * Crea un panel de control que permite cambiar el algoritmo en tiempo real.
     */
    private void createAlgorithmPanel() {
        algorithmPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        dfsButton = new JRadioButton("DFS");
        bfsButton = new JRadioButton("BFS");
        ButtonGroup group = new ButtonGroup();
        group.add(dfsButton);
        group.add(bfsButton);
        dfsButton.setSelected(true);
        algorithmPanel.add(new JLabel("Algoritmo de Búsqueda:"));
        algorithmPanel.add(dfsButton);
        algorithmPanel.add(bfsButton);
        
        // Agrega listeners para actualizar el algoritmo en Board y en la variable local
        dfsButton.addActionListener(e -> {
            searchAlgorithm = "DFS";
            if (board != null) {
                board.setAlgorithm("DFS");
            }
        });
        bfsButton.addActionListener(e -> {
            searchAlgorithm = "BFS";
            if (board != null) {
                board.setAlgorithm("BFS");
            }
        });
        
        // Agrega el panel al norte de la ventana
        add(algorithmPanel, BorderLayout.NORTH);
    }

    /**
     * Inicializa/reinicia el juego:
     * - Crea el Board (lógica) y la instancia de TraversalGraph.
     * - Configura el callback para que cada celda revelada se registre en el grafo.
     * - Construye la UI (panel y botones).
     */
    private void initGame() {
        board = new Board(filas, columnas, minas, searchAlgorithm);
        traversalGraph = new TraversalGraph();
        board.setRevealListener(cell -> {
            String cellId = getCellId(cell.getRow(), cell.getCol());
            traversalGraph.addStep(cellId, false);
        });
        gameInProgress = true;
        flagsPlaced = 0; // Reinicia el contador de banderas
        if (boardPanel != null)
            getContentPane().remove(boardPanel);
        boardPanel = new JPanel(new GridLayout(filas, columnas));
        buttons = new JButton[filas][columnas];
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                JButton btn = new JButton("");
                btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 20));
                btn.putClientProperty("marked", false);
                final int r = i, c = j;
                btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (!gameInProgress)
                            return;
                        if (SwingUtilities.isRightMouseButton(e)) {
                            toggleMark(r, c, btn);
                        } else if (SwingUtilities.isLeftMouseButton(e)) {
                            if (!(Boolean) btn.getClientProperty("marked")) {
                                handleReveal(r, c);
                                checkVictory();
                            }
                        }
                    }
                });
                buttons[i][j] = btn;
                boardPanel.add(btn);
            }
        }
        // Agrega el tablero en el centro
        getContentPane().add(boardPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /**
     * Genera el identificador de la celda (por ejemplo, "A1" para (0,0)).
     */
    private String getCellId(int r, int c) {
        return "" + (char) ('A' + c) + (r + 1);
    }

    /**
     * Maneja la revelación de la celda, registrando el paso en el grafo.
     */
    private void handleReveal(int r, int c) {
        boolean safe = board.revealCell(r, c);
        String cellId = getCellId(r, c);
        traversalGraph.addStep(cellId, false);
        updateButtons();
        if (!safe) {
            gameInProgress = false;
            JOptionPane.showMessageDialog(this, "¡BOOM! Has pisado una mina.");
            revealAll();
        }
    }

    /**
     * Alterna el marcado (bandera) de la celda y registra el paso en el grafo.
     */
    private void toggleMark(int r, int c, JButton btn) {
        if (!btn.isEnabled())
            return;
        boolean marked = (Boolean) btn.getClientProperty("marked");
        if (!marked) {
            if (flagsPlaced >= minas) {
                JOptionPane.showMessageDialog(this, "No puedes colocar más banderas que minas.");
                return;
            }
            flagsPlaced++;
        } else {
            flagsPlaced--;
        }
        board.toggleMark(r, c);
        String cellId = getCellId(r, c);
        traversalGraph.addStep(cellId, !marked);
        if (!marked) {
            btn.putClientProperty("marked", true);
            btn.setText("🚩");
            btn.setForeground(Color.BLUE);
        } else {
            btn.putClientProperty("marked", false);
            btn.setText("");
        }
    }

    /**
     * Actualiza los botones de la UI basándose en el estado actual del Board.
     */
    private void updateButtons() {
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                if (board.cellIsRevealed(i, j)) {
                    buttons[i][j].setEnabled(false);
                    if (board.cellHasMine(i, j)) {
                        buttons[i][j].setText("💣");
                        buttons[i][j].setBackground(Color.RED);
                    } else {
                        int value = board.getCellValue(i, j);
                        buttons[i][j].setText(value > 0 ? String.valueOf(value) : "");
                        buttons[i][j].setBackground(Color.LIGHT_GRAY);
                    }
                }
            }
        }
    }

    /**
     * Verifica la victoria: si todas las celdas sin mina fueron reveladas, se declara victoria.
     */
    private void checkVictory() {
        if (board.checkVictory()) {
            gameInProgress = false;
            JOptionPane.showMessageDialog(this, "¡Felicidades! Has ganado.");
            revealAll();
        }
    }

    /**
     * Revela todas las casillas de la UI, mostrando los valores y las minas.
     */
    private void revealAll() {
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                buttons[i][j].setEnabled(false);
                if (!board.cellIsRevealed(i, j)) {
                    if (board.cellHasMine(i, j)) {
                        buttons[i][j].setText("💣");
                        buttons[i][j].setBackground(Color.RED);
                    } else {
                        int value = board.getCellValue(i, j);
                        buttons[i][j].setText(value > 0 ? String.valueOf(value) : "");
                        buttons[i][j].setBackground(Color.LIGHT_GRAY);
                    }
                }
            }
        }
    }

    /**
     * Crea la barra de menú con las opciones: Nuevo Juego, Guardar Juego, Cargar Juego y Ver Recorrido.
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Juego");

        JMenuItem newGameItem = new JMenuItem("Nuevo Juego");
        newGameItem.addActionListener(e -> showNewGameDialog());
        gameMenu.add(newGameItem);

        JMenuItem saveGameItem = new JMenuItem("Guardar Juego");
        saveGameItem.addActionListener(e -> saveGame());
        gameMenu.add(saveGameItem);

        JMenuItem loadGameItem = new JMenuItem("Cargar Juego");
        loadGameItem.addActionListener(e -> loadGame());
        gameMenu.add(loadGameItem);

        JMenuItem viewGraphItem = new JMenuItem("Ver Recorrido");
        viewGraphItem.addActionListener(e -> {
            if (traversalGraph != null) {
                traversalGraph.display();
            }
        });
        gameMenu.add(viewGraphItem);

        menuBar.add(gameMenu);
        return menuBar;
    }

    /**
     * Muestra un diálogo para configurar un nuevo juego: dimensiones, minas y algoritmo.
     */
    private void showNewGameDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("Filas (3-10):"));
        JTextField filasField = new JTextField(String.valueOf(filas));
        panel.add(filasField);

        panel.add(new JLabel("Columnas (3-10):"));
        JTextField colsField = new JTextField(String.valueOf(columnas));
        panel.add(colsField);

        panel.add(new JLabel("Minas:"));
        JTextField minesField = new JTextField(String.valueOf(minas));
        panel.add(minesField);

        // Los radio buttons aquí son solo para preconfigurar un nuevo juego;
        // el panel de control ya permite cambiarlos durante la partida.
        panel.add(new JLabel("Algoritmo de Búsqueda:"));
        JPanel algoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JRadioButton dfsNew = new JRadioButton("DFS");
        JRadioButton bfsNew = new JRadioButton("BFS");
        ButtonGroup group = new ButtonGroup();
        group.add(dfsNew);
        group.add(bfsNew);
        if (searchAlgorithm.equals("DFS"))
            dfsNew.setSelected(true);
        else
            bfsNew.setSelected(true);
        algoPanel.add(dfsNew);
        algoPanel.add(bfsNew);
        panel.add(algoPanel);

        int result = JOptionPane.showConfirmDialog(this, panel, "Nuevo Juego",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int newRows = Integer.parseInt(filasField.getText());
                int newCols = Integer.parseInt(colsField.getText());
                int newMines = Integer.parseInt(minesField.getText());
                if (newRows < 3 || newRows > 10 || newCols < 3 || newCols > 10 || newMines < 1 || newMines > newRows * newCols) {
                    JOptionPane.showMessageDialog(this, "Valores fuera de rango", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                filas = newRows;
                columnas = newCols;
                minas = newMines;
                searchAlgorithm = dfsNew.isSelected() ? "DFS" : "BFS";
                initGame();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ingrese valores válidos", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Guarda el estado actual del juego en un archivo CSV utilizando JFileChooser.
     */
    private void saveGame() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write(board.toCSV());
                JOptionPane.showMessageDialog(this, "Juego guardado correctamente.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Carga el estado del juego desde un archivo CSV utilizando JFileChooser.
     */
    private void loadGame() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                Board loadedBoard = Board.loadFromCSV(file.getAbsolutePath());
                board = loadedBoard;
                filas = board.getRows();
                columnas = board.getCols();
                if (boardPanel != null)
                    getContentPane().remove(boardPanel);
                boardPanel = new JPanel(new GridLayout(filas, columnas));
                buttons = new JButton[filas][columnas];
                for (int i = 0; i < filas; i++) {
                    for (int j = 0; j < columnas; j++) {
                        JButton btn = new JButton("");
                        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 20));
                        btn.putClientProperty("marked", board.cellIsMarked(i, j));
                        if (board.cellIsRevealed(i, j)) {
                            btn.setEnabled(false);
                            if (board.cellHasMine(i, j)) {
                                btn.setText("💣");
                                btn.setBackground(Color.RED);
                            } else {
                                int value = board.getCellValue(i, j);
                                btn.setText(value > 0 ? String.valueOf(value) : "");
                                btn.setBackground(Color.LIGHT_GRAY);
                            }
                        }
                        final int r = i, c = j;
                        btn.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                if (!gameInProgress)
                                    return;
                                if (SwingUtilities.isRightMouseButton(e)) {
                                    toggleMark(r, c, btn);
                                } else if (SwingUtilities.isLeftMouseButton(e)) {
                                    if (!(Boolean) btn.getClientProperty("marked")) {
                                        handleReveal(r, c);
                                        checkVictory();
                                    }
                                }
                            }
                        });
                        buttons[i][j] = btn;
                        boardPanel.add(btn);
                    }
                }
                getContentPane().add(boardPanel, BorderLayout.CENTER);
                revalidate();
                repaint();
                JOptionPane.showMessageDialog(this, "Juego cargado correctamente.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al cargar: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        System.setProperty("org.graphstream.ui", "swing");
        SwingUtilities.invokeLater(() -> new Buscaminas());
    }
}
