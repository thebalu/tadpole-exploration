package model;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.graph.implementations.SingleNode;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class TadpoleGraph {

    private Graph graph;
    private UUID uuid = UUID.randomUUID();
    private Random rand;

    private String startNode;

    public TadpoleGraph(int cycleVertices, int stemVertices) {
        this(cycleVertices, stemVertices, 100, 200);
    }

    public TadpoleGraph(int cycleVertices, int stemVertices, int weightMean, int weightSd) {

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
    }

    public void display() {
        graph.display();
    }

    private int randomPositiveWeight(int mean, int sd) {
        if (rand == null) {
            rand = new Random();
        }
        return (int) Math.max(1, Math.round(rand.nextGaussian() * sd + mean));
    }
}
