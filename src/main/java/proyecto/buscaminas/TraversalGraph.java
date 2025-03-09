package proyecto.buscaminas;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

/**
 * Clase TraversalGraph:
 * Registra la secuencia en la que el jugador revela o marca casillas.
 * Cada nodo corresponde a una casilla (por su identificador, p.ej., "A1")
 * y se le asigna una clase CSS diferente según si la casilla fue marcada.
 * Se crean aristas conectando los nodos en el orden de los pasos.
 */
public class TraversalGraph {
    private Graph graph;
    private int stepCounter;
    private String lastCellId; // Almacena el identificador del último nodo agregado.

    public TraversalGraph() {
        graph = new SingleGraph("Recorrido");
        // Configuramos el stylesheet para usar colores que contrasten:
        graph.setAttribute("ui.stylesheet", 
            "node { fill-color: orange; size: 25px; text-size: 16; text-alignment: center; }" +
            "node.marked { fill-color: red; }" +
            "node.revealed { fill-color: green; }" +
            "edge { fill-color: gray; size: 2px; }"
        );
        // Configuramos el grafo para auto creación de nodos y sin estricto modo (nos aseguramos que no haya duplicados)
        graph.setAutoCreate(true);
        graph.setStrict(false);
        stepCounter = 0;
        lastCellId = null;
    }

    /**
     * Agrega un paso al grafo de recorrido.
     *
     * @param cellId   Identificador de la casilla (por ejemplo, "A1").
     * @param isMarked Indica si la celda fue marcada (true) o simplemente revelada (false).
     */
    public void addStep(String cellId, boolean isMarked) {
        // Obtiene o crea el nodo correspondiente a la celda.
        Node node = graph.getNode(cellId);
        if (node == null) {
            node = graph.addNode(cellId);
        }
        // Establece la clase CSS según el estado:
        if (isMarked) {
            node.setAttribute("ui.class", "marked");
        } else {
            node.setAttribute("ui.class", "revealed");
        }
        // Actualiza la etiqueta para mostrar el identificador y el número de paso.
        node.setAttribute("ui.label", cellId + " (" + stepCounter + ")");
        
        // Si existe una celda previa, crea una arista de conexión.
        if (lastCellId != null) {
            String edgeId = lastCellId + "-" + cellId + "-" + stepCounter;
            if (graph.getEdge(edgeId) == null) {
                graph.addEdge(edgeId, lastCellId, cellId, true);
            }
        }
        lastCellId = cellId;
        stepCounter++;
    }

    /**
     * Muestra el grafo en una ventana utilizando GraphStream y la UI Swing.
     */
    public void display() {
        // Asegura que se use Swing para la visualización.
        System.setProperty("org.graphstream.ui", "swing");
        graph.display();
    }
    
    public Graph getGraph() {
        return graph;
    }
}
