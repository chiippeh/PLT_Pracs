// Skeleton to handle cross reference table for RE productions
// P.D. Terry, Rhodes University; modified 2023  (Java 1.5)

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

  // public static Entry addRef (Entry i) {
  // Entry entry = new Entry(name);

  // }
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
        entry.refs.add(regnum);
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

  public static void printTable() {
    // Prints out all references in the table (eliminate duplicate regex numbers)
    for (Entry entry : list) {
      System.out.print(entry.name + " ");
      ArrayList<Integer> refList = entry.refs;
      for (Integer ref : refList) {
        System.out.print(ref + " ");
      }
      System.out.println("");
    }
    
  } // printTable

  public static void printAlphabet(int regnum) {
    // Prints out all terminals in table for particular regex number


  } // printAlphabet

  public static void main(String[] args) {
    // Table table = new Table();
    Table.addRef('a', 1);
    Table.addRef('b', 3);
    Table.addRef('c', 1);
    Table.addRef('c', 2);
    Table.printTable();
  }

} // Table
