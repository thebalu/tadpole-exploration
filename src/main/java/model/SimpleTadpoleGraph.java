package model;

import org.apache.commons.math3.util.Pair;
import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.graphicGraph.stylesheet.StyleConstants;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerPipe;

import java.util.*;
import java.util.stream.Collectors;

//Tadpole Graph for testing different edge weight distributions
public class SimpleTadpoleGraph {

    private final Graph graph;
    private final UUID uuid = UUID.randomUUID();
    private Random rand;

    private final String startNode;
    private String currentNode;

    private Set<String> visible;
    private Set<String> visited;

    private long totalCost;

    private String distribution;

    public SimpleTadpoleGraph(int cycleVertices, int stemVertices, int weightMean, int weightSd, String distr) {
        graph = new SingleGraph("Tadpole " + uuid);

        distribution = distr;

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
        this.startNode = graph.getNode(rand.nextInt(graph.getNodeCount())).getId();
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

    public void setEdgeWeight(String edge, int weight) {
        graph.getEdge(edge).setAttribute("weight", weight);
        graph.getEdge(edge).setAttribute("ui.label", weight);
        updateAttributes();
    }

    public Viewer display() {
        updateAttributes();
        return graph.display(true);
    }

    public void addViewerPipe(ViewerPipe viewerPipe) {
        viewerPipe.addSink(graph);
    }

    public void updateAttributes() {
        calculateDistances(currentNode);
    }

    private int randomPositiveWeight(int mean, int sd) {
        if (rand == null) {
            rand = new Random();
        }
        switch(distribution){
            case "Uniform":
                return (int) Math.max(1, rand.nextInt(mean));
            default:
                return (int) Math.max(1, Math.round(rand.nextGaussian() * sd + mean));
        }
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
        calculateDistances(from);

        // Get smallest visitable node, or the start if there are no more unvisited
        String nextVisit = getUnvisitedVisible()
                .stream()
                .min(Comparator.comparingDouble(node -> (double) graph.getNode(node).getAttribute("dist")))
                .orElse(startNode);

        Pair<String, Long> visitWithCost = Pair.create(nextVisit, Math.round((Double) graph.getNode(nextVisit).getAttribute("dist")));

        return visitWithCost;
    }

    private int getOptimalCost(){
        int optCost, cycleCost;
        int maxCycleEdge = 0;
        optCost = graph
                .edges()
                .filter(edge ->
                        (edge.getSourceNode().getId().startsWith("S") || edge.getTargetNode().getId().startsWith("S")))
                .mapToInt(edge -> 2 * (int) edge.getAttribute("weight"))
                .sum();
        cycleCost = graph
                .edges()
                .filter(edge ->
                        edge.getSourceNode().getId().startsWith("C"))
                .mapToInt(edge -> (int) edge.getAttribute("weight"))
                .sum();
        maxCycleEdge = graph
                .edges()
                .filter(edge ->
                        edge.getSourceNode().getId().startsWith("C"))
                .mapToInt(edge -> (int) edge.getAttribute("weight"))
                .max().orElse(0);
        if(cycleCost > (cycleCost-maxCycleEdge)*2) cycleCost = (cycleCost-maxCycleEdge)*2;
        return optCost+cycleCost;
    }

    public void calculateDistances(String from) {
        Graph knownGraph = new SingleGraph("Known " + uuid);
        graph.nodes()
                .filter(node -> visible.contains(node.getId()))
                .forEach(node ->
                        knownGraph.addNode(String.valueOf(node)));

        graph
                .edges()
                .filter(edge ->
                        (visible.contains(edge.getSourceNode().getId()) && visited.contains(edge.getTargetNode().getId())) ||
                                (visited.contains(edge.getSourceNode().getId()) && visible.contains(edge.getTargetNode().getId())) ||
                                (visited.contains(edge.getSourceNode().getId()) && visited.contains(edge.getTargetNode().getId())))
                .forEach(edge -> {
                            //System.out.println(edge.getId());
                            Edge newEdge = knownGraph
                                    .addEdge(edge.getId(), edge.getSourceNode().getId(), edge.getTargetNode().getId());
                            newEdge.setAttribute("weight", edge.getAttribute("weight", Integer.class));
                        }
                );

        Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, "dijkstraResult", "weight");
        dijkstra.init(knownGraph);
        dijkstra.setSource(from);
        dijkstra.compute();

        //Get unvisited but visible dist
        if (getUnvisitedVisible().size() == 0) {
            graph.getNode(startNode).setAttribute("dist", dijkstra.getPathLength(knownGraph.getNode(startNode)));
        } else {
            getUnvisitedVisible().forEach(node -> graph.getNode(node).setAttribute("dist", dijkstra.getPathLength(knownGraph.getNode(node))));
        }

        dijkstra.clear();
    }

    public double step() {
        Pair<String, Long> nextNodeAndCost = nextVisit(currentNode);
        String nextNode = nextNodeAndCost.getFirst();
        Long nextCost = nextNodeAndCost.getSecond();

        if (visited.contains(nextNode) && !nextNode.equals(startNode)) {
            throw new RuntimeException("Trying to visit an already visited node");
        }

        if (!visible.contains(nextNode)) {
            throw new RuntimeException("Trying to visit an invisible node");
        }

        totalCost += nextCost;
        currentNode = nextNode;
        visited.add(currentNode);
        visible.addAll(graph.getNode(currentNode).neighborNodes().map(node -> node.getId()).collect(Collectors.toSet()));

        //System.out.println("Visited node " + currentNode + ", path cost: " + nextCost + ", total cost: " + totalCost);

        int optCost = getOptimalCost();

        return Math.round((double) totalCost / (double) optCost * 1000.0) / 1000.0;
    }

    public boolean isDone() {
        return visited.size() == graph.nodes().count();
    }
}
