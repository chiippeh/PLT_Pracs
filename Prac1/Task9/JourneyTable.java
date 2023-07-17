import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import library.*;

public class JourneyTable {
 
    public static void main(String[] args) {
    //     if (args.length != 2) {
    //         IO.writeLine("missing args - eg: java journeyTable buses.txt results.txt");
    //         System.exit(1);
    //     }
    //   //                                          attempt to open data file
    //       InFile data = new InFile(args[0]);
    //       if (data.openError()) {
    //         IO.writeLine("cannot open " + args[0]);
    //         System.exit(1);
    //     }
    //   //                                          attempt to open results file
    //       OutFile results = new OutFile(args[1]);
    //       if (results.openError()) {
    //         IO.writeLine("cannot open " + args[1]);
    //         System.exit(1);
    //     }

        InFile data = new InFile("buses.txt");
        OutFile results = new OutFile("results.txt");

        // Input data looks as follows: "College  8  Hamilton  5  GreatHal  12  Gino's  25  Mews 9  Union  12  Steers 17  Athies"
        // Add each word and number to an arraylist
        ArrayList<String> busStops = new ArrayList<String>();
        ArrayList<Integer> busTimes = new ArrayList<Integer>();

        // busStops.add(null);
        String busStop = data.readWord();

        while (!data.noMoreData()) {
            busStops.add(busStop);
            busTimes.add(data.readInt());
            busStop = data.readWord();
        }

        System.out.println(busStops);
        System.out.println(busTimes);

        int[][] travelTable = new int[busStops.size()][busStops.size()];
        int travelTime = 0;

        for (int i = 0; i < travelTable.length; i++) {
            for (int j = 0; j < travelTable.length; j++) {
                if (i==j) {
                    travelTable[i][j] = 0;
                } else {

                if (j < i) {
                    travelTime = busTimes.get(j) + travelTable[i][j+1];
                } else {
                    travelTime = busTimes.get(j-1) + travelTable[i][j-1];
                }
                travelTable[i][j] = travelTime;
                travelTable[j][i] = travelTime;
                }   
            }      
        }

        // Print out the travel table
        System.out.println("Travel Table: \n");
        for (int i = 0; i < travelTable.length; i++) {
            for (int j = 0; j < travelTable.length; j++) {
                System.out.print(travelTable[i][j] + " ");
            }
            System.out.println();
        }
    }
    
}
