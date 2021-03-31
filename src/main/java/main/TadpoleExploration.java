package main;

import model.TadpoleGraph;

public class TadpoleExploration {
    public static void main(String[] args) {
        System.out.println("Hello world");

        System.setProperty("org.graphstream.ui", "swing");

        TadpoleGraph tg = new TadpoleGraph(8, 7);

        tg.display();

        while(!tg.isDone()) {
            tg.step();
            tg.display();
            try
            {
                Thread.sleep(1000);
            }
            catch(InterruptedException ex)
            {
                Thread.currentThread().interrupt();
            }
        }
    }
}
