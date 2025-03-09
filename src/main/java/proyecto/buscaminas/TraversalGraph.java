package proyecto.buscaminas;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

/**
 * Registra la secuencia de casillas reveladas o marcadas durante el juego y permite visualizar
 * este recorrido como un grafo utilizando GraphStream.
 * Cada nodo representa una casilla (identificada por su id, ej. "A1") y se le asigna una clase CSS
 * según si la casilla fue revelada o marcada. Se conectan los nodos en el orden en que se realizan los pasos.
 */
public class TraversalGraph {
    private Graph graph;
    private int stepCounter;
    private String lastCellId; // Identificador del último nodo agregado.

    /**
     * Crea un nuevo grafo para registrar el recorrido.
     * Configura el stylesheet para definir el estilo de nodos y aristas, y establece que los nodos se creen automáticamente.
     */
    public TraversalGraph() {
        graph = new SingleGraph("Recorrido");
        graph.setAttribute("ui.stylesheet", 
            "node { fill-color: orange; size: 25px; text-size: 16; text-alignment: center; }" +
            "node.marked { fill-color: red; }" +
            "node.revealed { fill-color: green; }" +
            "edge { fill-color: gray; size: 2px; }"
        );
        graph.setAutoCreate(true);
        graph.setStrict(false);
        stepCounter = 0;
        lastCellId = null;
    }

    /**
     * Agrega un paso al grafo, registrando la celda en la que se realizó una acción (revelado o marcado).
     * Se actualiza el nodo correspondiente asignándole una clase CSS según la acción y se conecta con el nodo previo.
     * @param cellId Identificador de la celda (ej. "A1").
     * @param isMarked Indica si la acción fue marcar (true) o revelar (false).
     */
    public void addStep(String cellId, boolean isMarked) {
        Node node = graph.getNode(cellId);
        if (node == null) {
            node = graph.addNode(cellId);
        }
        if (isMarked) {
            node.setAttribute("ui.class", "marked");
        } else {
            node.setAttribute("ui.class", "revealed");
        }
        node.setAttribute("ui.label", cellId + " (" + stepCounter + ")");
        
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
     * Muestra el grafo en una ventana utilizando la UI Swing de GraphStream.
     * Se habilita la auto-organización del grafo y se configura la política de cierre de la ventana.
     */
    public void display() {
        System.setProperty("org.graphstream.ui", "swing");
        Viewer viewer = graph.display();
        viewer.enableAutoLayout();
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.CLOSE_VIEWER);
    }
    
    /**
     * Retorna el grafo subyacente.
     * @return Objeto {@code Graph} utilizado para el recorrido.
     */
    public Graph getGraph() {
        return graph;
    }
}
