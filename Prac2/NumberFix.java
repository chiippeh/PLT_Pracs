//Wynne Edwards, Rhodes University, 23/07/2023
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//Welcome to the jankies solution to our problems ever.
public class NumberFix {
    public static void main(String[] args) {
        String fileName = args[0]; // when executing add the name of your pvm file e.g java NumberFix lsmall.pvm
        int counter = 0;
        // use the name provided by the user if they provided one otherwise use default output file name.
        String outputFile = "output.pvm";
        try {
            outputFile = args[1];
        } 
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        List<String> linesToWrite = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = tokenizeLine(line);
                if (tokens != null) {
                    // String increment = tokens[0]; //Can ignore.
                    String instruction = tokens[1];
                    String comment = tokens[2];
                    String instructionNew = instruction.split(" ")[0]; //This is the instruction without the value.
                    if (instructionNew.length() == 4 && !instructionNew.equals("PRNS")){ //Formatting stuff, was trail and error
                        linesToWrite.add(counter + "   " + instruction + "            " + comment);
                    } else if (counter <= 10) {
                        if (valueSwitch(instruction) == 2) {
                            linesToWrite.add(counter + "    " + instruction + "      " + comment);
                        }else if (instruction.substring(instructionNew.length()).trim().isBlank() == true) {
                        linesToWrite.add(counter + "    " + instruction + "             " + comment);
                        }else {
                            linesToWrite.add(counter + "    " + instruction + "            " + comment);
                        }
                    
                    }else if (instruction.substring(instructionNew.length()).trim().isBlank() == true) {
                        linesToWrite.add(counter + "   " + instruction + "             " + comment); 
                        
                    }else {
                        linesToWrite.add(counter + "   " + instruction + "      " + comment);
                    }
                    
                    // System.out.println(counter + " " + instruction + " " + comment);
                    counter += valueSwitch(instruction); //increments a counter based on the switch with instruction set
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Path filePath = Paths.get(outputFile); //Writes to output file.
            Files.write(filePath, linesToWrite);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int valueSwitch(String instruction){
        String instructionNew = instruction.split(" ")[0]; //Splits so it only shows intruction not value.
        switch (instructionNew) { //2 is how many numbers it takes up
            case "LDC":
                return 2;
            case "LDA":
                return 2;
            case "PRNS":
                return 2;
            case "DSP":
                return 2;
            case "BRN":
                return 2;
            case "BZE":
                return 2;
            default:
                return 1;
        }
    }

    private static String[] tokenizeLine(String line) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith(";")) {
            return null; // Ignore empty lines and comments
        }

        String increment = "";
        String instruction = "";
        String comment = "";
        int index = line.indexOf(';'); // Find the index of the comment symbol
        if (index != -1) {
            comment = line.substring(index).trim(); // Extract the comment
            line = line.substring(0, index).trim(); // Remove the comment from the line
        }

        Pattern pattern = Pattern.compile("(\\d+)\\s+(.*)"); //Regex to find the increment and instruction, we dont care for anything after the text so its all just one thing.
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            increment = matcher.group(1);
            instruction = matcher.group(2);
        }

        return new String[]{increment, instruction, comment};
    }
}


