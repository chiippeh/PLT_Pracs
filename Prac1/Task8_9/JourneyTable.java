import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import library.*;

public class JourneyTable {
 
    public static void main(String[] args) {
        if (args.length != 2) {
            IO.writeLine("missing args - eg: java journeyTable buses.txt results.txt");
            System.exit(1);
        }
        
        InFile data = new InFile(args[0]);
        if (data.openError()) { // attempt to open data file
            IO.writeLine("cannot open " + args[0]);
            System.exit(1);
        }
      
        OutFile results = new OutFile(args[1]);
        if (results.openError()) { // attempt to open results file
            IO.writeLine("cannot open " + args[1]);
            System.exit(1);
        }

        // Manual file declaration for testing
        // InFile data = new InFile("buses.txt");
        // OutFile results = new OutFile("results.txt");

        // Add each word and number to an arraylist
        ArrayList<String> busStops = new ArrayList<String>();
        ArrayList<Integer> busTimes = new ArrayList<Integer>();

        String busStop = data.readWord();
        while (!data.noMoreData()) {
            busStops.add(busStop);
            busTimes.add(data.readInt());
            busStop = data.readWord();
        }

        // System.out.println(busStops);
        // System.out.println(busTimes);

        int[][] travelTable = new int[busStops.size()][busStops.size()]; // 2D array
        int travelTime = 0;

        for (int i = 0; i < travelTable.length; i++) {
            for (int j = 0; j < travelTable.length; j++) {
                if (i==j) { // if the bus stop is the same just put zero
                    travelTable[i][j] = 0;
                } else {
                    if (j < i) { // if the bus stop is before the current one
                        travelTime = busTimes.get(j) + travelTable[i][j+1];
                    } else { // if the bus stop is after the current one
                        travelTime = busTimes.get(j-1) + travelTable[i][j-1];
                    }

                    // doing it twice makes it 'symmetrical'
                    travelTable[i][j] = travelTime;
                    // travelTable[j][i] = travelTime;
                }   
            }      
        }

        // print out for testing
        System.out.println("Travel Table: \n");
        for (int i = 0; i < travelTable.length; i++) {
            for (int j = 0; j < travelTable.length; j++) {
                System.out.print(travelTable[i][j] + " ");
            }
            System.out.println();
        }
    }
    
}
