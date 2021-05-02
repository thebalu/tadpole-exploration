package main;

import model.SimpleTadpoleGraph;

import java.util.ArrayList;
import java.util.List;

public class TadpoleDistributionTest {

    public static void main(String args[]){
        testGauss(100,200,0);
        testUniform(1000, 0);

        testGauss(100,200,1);
        testUniform(1000, 1);

        testGauss(100,200,2);
        testUniform(1000, 2);
    }

    private static void testGauss(int mean, int sd, int largeEdgeCount){
        List<Double> compRatios = new ArrayList<Double>();
        for(int i = 0; i < 100;i++)
        {
            SimpleTadpoleGraph tg = new SimpleTadpoleGraph(8, 7, mean, sd, "Gauss");
            if(largeEdgeCount>0){
                tg.setEdgeWeight("C3-C4", 9999);
            }
            if(largeEdgeCount>1){
                tg.setEdgeWeight("C5-C6", 9999);
            }
            if(largeEdgeCount>2){
                tg.setEdgeWeight("C1-C2", 9999);
            }
            while (!tg.isDone()) {
                tg.step();
            }
            compRatios.add(tg.step());
        }
        System.out.println(compRatios);
    }

    private static void testUniform(int mean, int largeEdgeCount){
        List<Double> compRatios = new ArrayList<Double>();
        for(int i = 0; i < 100;i++)
        {
            SimpleTadpoleGraph tg = new SimpleTadpoleGraph(8, 7, mean, 0, "Uniform");
            if(largeEdgeCount>0){
                tg.setEdgeWeight("C3-C4", 9999);
            }
            if(largeEdgeCount>1){
                tg.setEdgeWeight("C5-C6", 9999);
            }
            if(largeEdgeCount>2){
                tg.setEdgeWeight("C1-C2", 9999);
            }
            while (!tg.isDone()) {
                tg.step();
            }
            compRatios.add(tg.step());
        }
        System.out.println(compRatios);
    }
}
