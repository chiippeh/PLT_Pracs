  // This is a skeleton program for developing a parser for Regular Expressions
  // P.D. Terry, Rhodes University; Modified by KL Bradshaw 2023
  // Extended by:
  // Wynne Edwards g21e2079, Mila Davies g21d6937, Manu Jourdan g21j5408

import java.util.*;
import library.*;

  class Token {
    public int kind;
    public String val;

    public Token(int kind, String val) {
      this.kind = kind;
      this.val = val;
    }

  } // Token

  class Regex1 {

    // +++++++++++++++++++++++++ File Handling and Error handlers ++++++++++++++++++++

    static InFile input;
    static OutFile output;

    static String newFileName(String oldFileName, String ext) {
    // Creates new file name by changing extension of oldFileName to ext
      int i = oldFileName.lastIndexOf('.');
      if (i < 0) return oldFileName + ext; else return oldFileName.substring(0, i) + ext;
    }

    static void reportError(String errorMessage) {
    // Displays errorMessage on standard output and on reflected output
      System.out.println(errorMessage);
      output.writeLine(errorMessage);
    }

    static void abort(String errorMessage) {
    // Abandons parsing after issuing error message
      reportError(errorMessage);
      output.close();
      System.exit(1);
    }

    // +++++++++++++++++++++++  token kinds enumeration +++++++++++++++++++++++++

    static final int
      noSym        =  0,
      EOFSym       =  1,
      leftBrackSym =  2,
      rightBrackSym = 3,
      rangeSym      = 4,
      semiColonSym  = 5,
      zeroOrMoreSym = 6,
      dotAnyCharSym = 7,
      orSym         = 8,
      oneOrMoreSym  = 9,
      leftParenSym  = 10,
      rightParenSym = 11,
      zeroOrOneSym  = 12,
      atomicSym      = 13,     
      escapedCharSym = 14;

    // +++++++++++++++++++++++++++++ Character Handler ++++++++++++++++++++++++++


    static final char EOF = '\0';
    static boolean atEndOfFile = false;

    // Declaring ch as a global variable is done for expediency - global variables
    // are not always a good thing

    static char ch;    // look ahead character for scanner

    static void getChar() {
    // Obtains next character ch from input, or CHR(0) if EOF reached
    // Reflect ch to output
      if (atEndOfFile) ch = EOF;
      else {
        ch = input.readChar();
        atEndOfFile = ch == EOF;
        if (!atEndOfFile) output.write(ch);
      }
    } // getChar

    // +++++++++++++++++++++++++++++++ Scanner ++++++++++++++++++++++++++++++++++
    ///////////////////////////////// START OF TASK 2 ////////////////////////////////////////

    // Declaring sym as a global variable is done for expediency - global variables
    // are not always a good thing

    static Token sym;

    static void getSym() {
    // Scans for next sym from input
      // while ch > 0 && ch < 32
      while (ch > EOF && ch <= ' ') getChar(); //ignores control chars CHR(1) .. CHR(31)

      ////////////////////////// START OF TASK 3 (within task 2) ////////////////////////////////////////
      if (ch == '\\') { //comment handling taken from textbook
        do getChar(); while(ch != '\\' && ch != EOF);
        if (ch != EOF) {
          getChar(); getSym(); return;
        } else {
          sym = new Token(EOFSym, "EOF"); return;
        }
      }
      ////////////////////////////////// END OF TASK 3 ////////////////////////////////////////

      StringBuilder symLex = new StringBuilder();
      int symKind = noSym;

      // escaped characters
      // ANY the set of all characters in the range 0 .. 255
      symLex.append(ch);
      switch (ch) {
        case EOF:
            symKind = EOFSym; getChar(); break;       
        case '[':
            symKind = leftBrackSym; getChar(); break;
        case ']':
            symKind = rightBrackSym; getChar(); break;
        case '-':
            symKind = rangeSym; getChar(); break;
        case ';':
            symKind = semiColonSym; getChar(); break;
        case '*':
            symKind = zeroOrMoreSym; getChar(); break;                 
        case '.':
            symKind = dotAnyCharSym; getChar(); break;                 
        case '|':
            symKind = orSym; getChar(); break;         
        case '+':
            symKind = oneOrMoreSym; getChar(); break;                
        case '(':
            symKind = leftParenSym; getChar(); break;   
        case ')':
            symKind = rightParenSym; getChar(); break;          
        case '?':
            symKind = zeroOrOneSym; getChar(); break;                     
        case '\'': // single quotes example = 'a'
          getChar();
          if (ch != '\'' && (ch > 32)) {
            symLex.append(ch); 
            getChar();
            if (ch == '\'') {
              symLex.append(ch);
              symKind = escapedCharSym; getChar();
            }
          }          
          break;
        case '"': // double quotes example = "a"
          getChar();
          if (ch != '"' && (ch > 32)) {
            symLex.append(ch); 
            getChar();
            if (ch == '"') {
              symLex.append(ch);
              symKind = escapedCharSym; getChar();
            }
          }          
          break;
        default:
            symKind = atomicSym; getChar();  break;
      }
      sym = new Token(symKind, symLex.toString());
    } // getSym
    ///////////////////////////////// END OF TASK 2 ////////////////////////////////////////


    // +++++++++++++++++++++++++++++++ Parser +++++++++++++++++++++++++++++++++++
    /////////////////////////////// START OF TASK 4 ////////////////////////////////////////

    // ---- First IntSets ----
    // First Element is the first for RE, Expr, Term, Factor
    static IntSet FirstElement = new IntSet(atomicSym, escapedCharSym, leftBrackSym, leftParenSym);
    
    //First(Atom)
    static IntSet FirstAtom = new IntSet(atomicSym, escapedCharSym);

    // IntSet of "*" | "?" | "+" tokens
    static IntSet repetitionSet = new IntSet(zeroOrMoreSym, zeroOrOneSym, oneOrMoreSym);

    static void accept(int wantedSym, String errorMessage) {
    // Checks that lookahead token is wantedSym
      if (sym.kind == wantedSym) getSym(); else abort(errorMessage);
    } // accept

    static void RE() {
      // RE = {Expression ";"} EOF .

      // First(RE) = First(Expression) .... = First(Element)
      while (FirstElement.contains(sym.kind)) { //check if sym is in first set of RE production
        Expression();
        accept(semiColonSym, "; Expected");
      }
      accept(EOFSym, "EOF Expected");
    }

    static void Expression() {
      // Expression = Term { "|" Term } .
      Term();
      while (sym.kind == orSym) { //check if sym is in first set of RE production
        getSym(); Term();
      }
    }

    static void Term() {
      // Term = Factor { [ "." ] Factor } . 
      Factor();
      // First(Factor) = First(Element) :)
      while ((sym.kind == dotAnyCharSym) || (FirstElement.contains(sym.kind))) { //check if sym is in first set of Factor non terminal
        // if dot sym then getSym() + Factor()
        // else just Factor()
        switch (sym.kind) {
          case dotAnyCharSym: getSym(); Factor(); break;
          default: Factor(); break;
        }
      }
    }

    static void Factor() {
      // Factor = Element [ "*" | "?" | "+" ] .
      Element();
      // optional
      if (repetitionSet.contains(sym.kind)) {
        getSym();
      }
    }

    static void Element() {
      // Element = Atom | Range | "(" Expression ")" .
      switch (sym.kind) {
        case atomicSym: Atom(); break; //First(Atom)
        case escapedCharSym: Atom(); break; //First(Atom)
        case leftBrackSym: Range(); break; //First(Range)
        case leftParenSym: getSym(); // "("
                           Expression();
                           accept(rightParenSym, ") Expected");
                           break;
        default: abort("Invalid start to Element"); break;
      }
    }

    static void Range() {
      // Range = "[" OneRange { OneRange } "]" .
      accept(leftBrackSym, "[ Expected");
      OneRange();
      // First(OneRange) = First(Atom) = the while condition
      while (FirstAtom.contains(sym.kind)) {
          OneRange();
      }
      accept(rightBrackSym, "] Expected");
    }

    static void OneRange() {
      //  OneRange = Atom [ "-" Atom ] . 
      Atom();
      if (sym.kind == rangeSym) { 
          getSym(); Atom();
      }
    }

    static void Atom() {
      // Atom = atomic | escaped . 
        switch (sym.kind) {
          case atomicSym: getSym(); break;
          case escapedCharSym: getSym(); break;
          default: abort("Invalid start to Atom"); break;
        }
    }    
    ///////////////////////////END OF TASK 4 ////////////////////////////////////////

    // +++++++++++++++++++++ Main driver function +++++++++++++++++++++++++++++++

    public static void main(String[] args) {
      // Open input and output files from command line arguments
      if (args.length == 0) {
        System.out.println("Usage: RegExp FileName");
        System.exit(1);
      }
      input = new InFile(args[0]);
      output = new OutFile(newFileName(args[0], ".out"));

      getChar();                                  // Lookahead character

  /*  To test the scanner we can use a loop like the following: */
      // do {
      //   getSym();                                 // Lookahead symbol
      //   OutFile.StdOut.write(sym.kind, 3);
      //   OutFile.StdOut.writeLine(" " + sym.val);
      // } while (sym.kind != EOFSym);

  /*  After the scanner is debugged, comment out lines 125 to 129 and uncomment lines 134 to 139. 
      In other words, replace the code immediately above with this code: */

      getSym();                             // Lookahead symbol
      RE();                                 // Start to parse from the goal symbol
      // if we get back here everything must have been satisfactory
      System.out.println("Parsed correctly");

       output.close();
    } // main

  } // Regex1
