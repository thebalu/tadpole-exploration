package model;

import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;

public class Clicks implements ViewerListener {
    protected boolean loop = true;

    int x = 0;

    public static void main(String args[]) {
        new Clicks();
    }
    public Clicks() {
        System.setProperty("org.graphstream.ui", "swing");

        TadpoleGraph tg = new TadpoleGraph(8, 7);

        Viewer viewer = tg.display();
//        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
        ViewerPipe fromViewer = viewer.newViewerPipe();
        fromViewer.addViewerListener(this);
        // Dependency injection so we don't have to expose graph
        tg.addViewerPipe(fromViewer);


        System.out.println("hi");
        while(loop && !tg.isDone()) {
            fromViewer.pump(); // or fromViewer.blockingPump(); in the nightly builds
            tg.step();
            tg.updateAttributes();
//            System.out.println(x);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        tg.step();
        tg.updateAttributes();
        fromViewer.pump();
    }

    public void viewClosed(String id) {
        loop = false;
    }

    @Override
    public void buttonPushed(String id) {
        System.out.println("Button pushed on node "+id);
        x++;
    }

    public void buttonReleased(String id) {
        System.out.println("Button released on node "+id);
    }

    @Override
    public void mouseOver(String id) {
        System.out.println("mouse");
    }

    @Override
    public void mouseLeft(String id) {
    }
}