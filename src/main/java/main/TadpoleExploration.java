package main;

import model.TadpoleGraph;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

public class TadpoleExploration {
    public static void main(String[] args) {
        System.out.println("Hello world");

        System.setProperty("org.graphstream.ui", "swing");

        TadpoleGraph tg = new TadpoleGraph(8, 7);
        tg.display();
    }
}
