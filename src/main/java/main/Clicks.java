package main;

import model.TadpoleGraph;
import org.graphstream.ui.swing_viewer.util.DefaultMouseManager;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;

import java.awt.event.MouseEvent;

public class Clicks implements ViewerListener{
    protected boolean loop = true;
    protected volatile boolean stop = true;

    public static void main(String args[]) {
        new Clicks();
    }
    public Clicks() {
        System.setProperty("org.graphstream.ui", "swing");

        TadpoleGraph tg = new TadpoleGraph(8, 7);
        tg.setEdgeWeight("C3-C4", 999);
        Viewer viewer = tg.display();
//        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
        ViewerPipe fromViewer = viewer.newViewerPipe();
        fromViewer.addViewerListener(this);
        // Dependency injection so we don't have to expose graph
        tg.addViewerPipe(fromViewer);
        viewer.getDefaultView().enableMouseOptions();
        //To override mouse clicked event
        viewer.getDefaultView().setMouseManager(new InternalMouseManager());
        while (stop) {
            Thread.onSpinWait();
        }
        stop = true;

        while(loop && !tg.isDone()) {
            //fromViewer.pump(); // or fromViewer.blockingPump(); in the nightly builds

            tg.step();
            tg.updateAttributes();
            //Wait for mouse clicked event
            while (stop) {
                Thread.onSpinWait();
            }
            stop = true;
        }
        tg.step();
        tg.updateAttributes();
        //fromViewer.pump();
    }

    public void viewClosed(String id) {
        loop = false;
    }

    @Override
    public void buttonPushed(String id) {
        System.out.println("Button pushed on node "+id);
    }

    public void buttonReleased(String id) {
        System.out.println("Button released on node "+id);
    }

    @Override
    public void mouseOver(String id) {}

    @Override
    public void mouseLeft(String id) {}

    // On click do a step in the graph
    class InternalMouseManager extends DefaultMouseManager {
        @Override
        public void mouseClicked(MouseEvent e) {
            stop = false;
        }
    }
}