// Skeleton to handle cross reference table for RE productions
// P.D. Terry, Rhodes University; modified 2023  (Java 1.5)
// Extended (August 2023) by:
// Wynne Edwards g21e2079, Mila Davies g21d6937, Manu Jourdan g21j5408

package RE2;

import java.util.*;
import library.*;

class Entry { // Cross reference table entries
  public char name; // The identifier itself
  public ArrayList<Integer> refs; // Regex numbers where it appears

  public Entry(char name) {
    this.name = name;
    this.refs = new ArrayList<Integer>();
  }
} // Entry

class Table {
  static ArrayList<Entry> list = new ArrayList<Entry>();

  public static void clearTable() {
    // Clears cross-reference table
    list = new ArrayList<Entry>();

  } // clearTable

  public static void addRef(char name, int regnum) {
    // Enters name if not already there, adds another regex reference
    boolean found = false;
    for (Entry entry : list) {
      if (entry.name == name) {
        // check for duplicate references
        if (!entry.refs.contains(regnum)) {
          entry.refs.add(regnum);
        }
        found = true;
        break;
      }
    }

    if (!found) {
      Entry e = new Entry(name);
      e.refs.add(regnum);
      list.add(e);
    }
  } // addRef
  public static void printTable(){
      // Prints out all references in the table (eliminate duplicate regex numbers)
    StringBuilder sb = new StringBuilder();
    for (Entry entry : list) {
      sb.append(entry.name + " ");
      ArrayList<Integer> refList = entry.refs;
      for (Integer ref : refList) {
        sb.append(ref + " ");
      }
      sb.append("\n");
    }

    System.out.println(sb);
  }

  public static void printTable(OutFile output) { // prints to output file
    // Prints out all references in the table (eliminate duplicate regex numbers)
    StringBuilder sb = new StringBuilder();
    for (Entry entry : list) {
      sb.append(entry.name + " ");
      ArrayList<Integer> refList = entry.refs;
      for (Integer ref : refList) {
        sb.append(ref + " ");
      }
      sb.append("\n");
    }

    output.write(sb);
  } // printTable

  public static void printAlphabet(int regnum) {
    // Prints out all terminals in table for particular regex number
    StringBuilder sb = new StringBuilder();
    sb.append("Alphabet " + regnum + " =");
    for (Entry entry : list) {
      ArrayList<Integer> refList = entry.refs;

      if (refList.contains(regnum)) {
        sb.append(" ");
        sb.append(entry.name);
      }
    }
    System.out.println(sb);
  } // printAlphabet
} // Table
