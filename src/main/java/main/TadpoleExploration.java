package main;

import model.TadpoleGraph;

/**
  * This is the crazy jumping around version. See {@link Clicks} for a better one (but it is work in progress)
  */
public class TadpoleExploration {
    public static void main(String[] args) {
        System.out.println("Hello world");

        System.setProperty("org.graphstream.ui", "swing");

        TadpoleGraph tg = new TadpoleGraph(8, 7);

        tg.display();

        while(!tg.isDone()) {
            tg.step();
            tg.updateAttributes();
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
