package FormatParva;

/* Driver for Formatting Translators: Coco/R for Java.
   Nov23 exam */

//  ----------------------- you may need to change the "import" clauses:

import java.io.*;
import library.*;

  public class FormatParva {

    public static void main (String[] args) {
      boolean mergeErrors = false;
      String inputName = null;

      // ------------------------- process command line parameters:

      for (int i = 0; i < args.length; i++) {
        if (args[i].toLowerCase().equals("-l")) mergeErrors = true;
        else inputName = args[i];
      }
      if (inputName == null) {
        System.err.println("No input file specified");
        System.exit(1);
      }

      // ------------------------ parser and scanner initialization

      int pos = inputName.lastIndexOf('/');
      if (pos < 0) pos = inputName.lastIndexOf('\\');
      String dir = inputName.substring(0, pos+1);

      String outputName = null;
      pos = inputName.lastIndexOf('.');
      if (pos < 0)
        outputName = inputName + ".ppv";
      else
        outputName = inputName.substring(0, pos) + ".ppv";
      Parser.openOutput(outputName);

      Scanner.Init(inputName);
      Errors.Init(inputName, dir, mergeErrors);
      //  ----------------------- add other initialization if required:
      Parser.Parse();
      Errors.Summarize();
      //  ----------------------- add other finalization if required:
     Parser.closeOutput();
      if (!Parser.Successful()) System.exit(1);

    }

  } // end driver
