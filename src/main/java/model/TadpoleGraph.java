package model;

import org.apache.commons.math3.util.Pair;
import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Element;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.graph.implementations.SingleNode;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.swing_viewer.util.DefaultShortcutManager;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerPipe;

import java.util.*;
import java.util.stream.Collectors;

public class TadpoleGraph {

    private final Graph graph;
    private final UUID uuid = UUID.randomUUID();
    private Random rand;

    private final String startNode;
    private String currentNode;

    private Set<String> visible;
    private Set<String> visited;

    private long totalCost;

    public TadpoleGraph(int cycleVertices, int stemVertices) {
        this(cycleVertices, stemVertices, 100, 100, Optional.empty());
    }

    public TadpoleGraph(int cycleVertices, int stemVertices, String startNode) {
        this(cycleVertices, stemVertices, 100, 200, Optional.of(startNode));
    }

    public TadpoleGraph(int cycleVertices, int stemVertices, int weightMean, int weightSd, Optional<String> startNode) {

        graph = new SingleGraph("Tadpole " + uuid);
        graph.setAttribute("ui.stylesheet",
                "edge{text-background-mode: plain; text-size: 20;}" +
                        "node{text-alignment: at-right; text-size: 20;}");

        int weight;

        for (int i = 0; i < stemVertices; i++) {
            graph.addNode("S" + i).setAttribute("ui.label", "S" + i);
            if (i > 0) {
                weight = randomPositiveWeight(weightMean, weightSd);
                graph
                        .addEdge("S" + (i - 1) + "-S" + i, "S" + (i - 1), "S" + i)
                        .setAttributes(Map.of(
                                "weight", weight,
                                "ui.label", weight
                        ));
            }
        }
        for (int i = 0; i < cycleVertices; i++) {
            graph.addNode("C" + i).setAttribute("ui.label", "C" + i);
            if (i > 0) {
                weight = randomPositiveWeight(weightMean, weightSd);
                graph
                        .addEdge("C" + (i - 1) + "-C" + i, "C" + (i - 1), "C" + i)
                        .setAttributes(Map.of(
                                "weight", weight,
                                "ui.label", weight
                        ));
            }
        }

        weight = randomPositiveWeight(weightMean, weightSd);
        graph.addEdge("S0-C0", "S0", "C0")
                .setAttributes(Map.of(
                        "weight", weight,
                        "ui.label", weight
                ));

        weight = randomPositiveWeight(weightMean, weightSd);
        graph.addEdge("C" + (cycleVertices - 1) + "-C0", "C" + (cycleVertices - 1), "C0")
                .setAttributes(Map.of(
                        "weight", weight,
                        "ui.label", weight
                ));

        // Start at random if no start node given
        this.startNode = startNode.orElse(graph.getNode(rand.nextInt(graph.getNodeCount())).getId());
        if (graph.getNode(this.startNode) == null) {
            throw new ElementNotFoundException("Starting node not found in graph");
        }

        graph.getNode(this.startNode).setAttribute("start", "true");
        graph.getNode(this.startNode).setAttribute("current", "true");
        this.currentNode = this.startNode;


        visible = new HashSet<>();
        visited = new HashSet<>();

        visited.add(this.startNode);

        // Add start node and all neighbors to visible
        visible.add(this.startNode);
        visible.addAll(graph
                .getNode(this.startNode)
                .neighborNodes()
                .map(Element::getId)
                .collect(Collectors.toSet()));

        totalCost = 0;

        updateAttributes();
    }

    public Viewer display() {
        updateAttributes();
        return graph.display();
    }

    public void addViewerPipe(ViewerPipe viewerPipe) {
        viewerPipe.addSink(graph);
    }

    public void updateAttributes() {

        graph.nodes().forEach(node -> {
            if (visible.contains(node.getId())) {
                node.setAttribute("ui.style", "fill-color: green;");
            }
            if (visited.contains(node.getId())) {
                node.setAttribute("ui.style", "fill-color: blue;");
            }
            if (node.getId().equals(currentNode)) {
                node.setAttribute("ui.style", "fill-color: orange;");
            }
            if (node.getId().equals(startNode)) {
                node.setAttribute("ui.style", "fill-color: red;");
            }
        });

    }

    private int randomPositiveWeight(int mean, int sd) {
        if (rand == null) {
            rand = new Random();
        }
        return (int) Math.max(1, Math.round(rand.nextGaussian() * sd + mean));
    }

    public Set<String> getNodes() {
        return graph.nodes().map(Element::getId).collect(Collectors.toSet());
    }

    public Set<String> getVisible() {
        return visible;
    }

    public Set<String> getVisited() {
        return visible;
    }

    public Set<String> getUnvisitedVisible() {
        return visible.stream().filter(n -> !visited.contains(n)).collect(Collectors.toSet());
    }

    // Compute next visitable node greedily
    public Pair<String, Long> nextVisit(String from) {

        Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, "dijkstraResult", "weight");
        dijkstra.init(graph);
        dijkstra.setSource(from);
        dijkstra.compute();

        // Get smallest visitable node, or the start if there are no more unvisited
        String nextVisit = getUnvisitedVisible()
                .stream()
                .min(Comparator.comparingDouble(node -> dijkstra.getPathLength(graph.getNode(node))))
                .orElse(startNode);

        Pair<String, Long> visitWithCost = Pair.create(nextVisit, Math.round(dijkstra.getPathLength(graph.getNode(nextVisit))));
        dijkstra.clear();
        return visitWithCost;
    }

    public long step() {

        Pair<String, Long> nextNodeAndCost = nextVisit(currentNode);
        String nextNode = nextNodeAndCost.getFirst();
        Long nextCost = nextNodeAndCost.getSecond();

        if(visited.contains(nextNode) && !nextNode.equals(startNode)) {
            throw new RuntimeException("Trying to visit an already visited node");
        }

        if (!visible.contains(nextNode)) {
            throw new RuntimeException("Trying to visit an invisible node");
        }

        totalCost += nextCost;
        currentNode = nextNode;
        visited.add(currentNode);
        visible.addAll(graph.getNode(currentNode).neighborNodes().map(node -> node.getId()).collect(Collectors.toSet()));

        System.out.println("Visited node " + currentNode + ", path cost: " + nextCost + ", total cost: " + totalCost);
        return totalCost;
    }

    public boolean isDone() {
        return visited.size() == graph.nodes().count();
    }
}
