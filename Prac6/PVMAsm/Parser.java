package PVMAsm;

import library.*;                                                        /* ~~~ Changed ~~~ */



import java.io.*;

public class Parser {
	public static final int _EOF = 0;
	public static final int _Number = 1;
	public static final int _String = 2;
	public static final int _EOL = 3;
	public static final int _Comment = 4;
	// terminals
	public static final int EOF_SYM = 0;
	public static final int Number_Sym = 1;
	public static final int String_Sym = 2;
	public static final int EOL_Sym = 3;
	public static final int Comment_Sym = 4;
	public static final int LDA_Sym = 5;
	public static final int LDC_Sym = 6;
	public static final int DSP_Sym = 7;
	public static final int BRN_Sym = 8;
	public static final int BZE_Sym = 9;
	public static final int ADD_Sym = 10;
	public static final int AND_Sym = 11;
	public static final int ANEW_Sym = 12;
	public static final int CEQ_Sym = 13;
	public static final int CGE_Sym = 14;
	public static final int CGT_Sym = 15;
	public static final int CLE_Sym = 16;
	public static final int CLT_Sym = 17;
	public static final int CNE_Sym = 18;
	public static final int DIV_Sym = 19;
	public static final int HALT_Sym = 20;
	public static final int INPB_Sym = 21;
	public static final int INPI_Sym = 22;
	public static final int LDV_Sym = 23;
	public static final int LDXA_Sym = 24;
	public static final int MUL_Sym = 25;
	public static final int NEG_Sym = 26;
	public static final int NOP_Sym = 27;
	public static final int NOT_Sym = 28;
	public static final int OR_Sym = 29;
	public static final int PRNB_Sym = 30;
	public static final int PRNI_Sym = 31;
	public static final int PRNL_Sym = 32;
	public static final int REM_Sym = 33;
	public static final int STO_Sym = 34;
	public static final int SUB_Sym = 35;
	public static final int PRNS_Sym = 36;
	public static final int NOT_SYM = 37;
	// pragmas

	public static final int maxT = 37;

	static final boolean T = true;
	static final boolean x = false;
	static final int minErrDist = 2;

	public static Token token;    // last recognized token   /* pdt */
	public static Token la;       // lookahead token
	static int errDist = minErrDist;

	public static OutFile output;                                            /* ~~~ Changed ~~~ */
  public static int globalCount = 0;                                       /* ~~~ Changed ~~~ */
  public static int longestString = 0;                                     /* ~~~ Changed ~~~ */



	static void SynErr (int n) {
		if (errDist >= minErrDist) Errors.SynErr(la.line, la.col, n);
		errDist = 0;
	}

	public static void SemErr (String msg) {
		if (errDist >= minErrDist) Errors.Error(token.line, token.col, msg); /* pdt */
		errDist = 0;
	}

	public static void SemError (String msg) {
		if (errDist >= minErrDist) Errors.Error(token.line, token.col, msg); /* pdt */
		errDist = 0;
	}

	public static void Warning (String msg) { /* pdt */
		if (errDist >= minErrDist) Errors.Warn(token.line, token.col, msg);
		errDist = 2; //++ 2009/11/04
	}

	public static boolean Successful() { /* pdt */
		return Errors.count == 0;
	}

	public static String LexString() { /* pdt */
		return token.val;
	}

	public static String LookAheadString() { /* pdt */
		return la.val;
	}

	static void Get () {
		for (;;) {
			token = la; /* pdt */
			la = Scanner.Scan();
			if (la.kind <= maxT) { ++errDist; break; }

			la = token; /* pdt */
		}
	}

	static void Expect (int n) {
		if (la.kind==n) Get(); else { SynErr(n); }
	}

	static boolean StartOf (int s) {
		return set[s][la.kind];
	}

	static void ExpectWeak (int n, int follow) {
		if (la.kind == n) Get();
		else {
			SynErr(n);
			while (!StartOf(follow)) Get();
		}
	}

	static boolean WeakSeparator (int n, int syFol, int repFol) {
		boolean[] s = new boolean[maxT+1];
		if (la.kind == n) { Get(); return true; }
		else if (StartOf(repFol)) return false;
		else {
			for (int i=0; i <= maxT; i++) {
				s[i] = set[syFol][i] || set[repFol][i] || set[0][i];
			}
			SynErr(n);
			while (!s[la.kind]) Get();
			return StartOf(syFol);
		}
	}

	static void PVMAsm() {
		while (StartOf(1)) {
			Statement();
		}
	}

	static void Statement() {
		if (la.kind == Number_Sym) {
			Get();
		}
		output.write(globalCount);
		if (StartOf(2)) {
			Instruction();
		}
		if (la.kind == Comment_Sym) {
			Get();
			output.write(token.val);
		}
		while (!(la.kind == EOF_SYM || la.kind == EOL_Sym)) {SynErr(38); Get();}
		Expect(EOL_Sym);
		output.write("\n");
	}

	static void Instruction() {
		int spaceBeforeLabel = 6 - Integer.toString(globalCount).length();
		output.write(" ".repeat(spaceBeforeLabel));
		if (StartOf(3)) {
			TwoWord();
			globalCount += 2;
		} else if (StartOf(4)) {
			OneWord();
			globalCount += 1;
		} else if (la.kind == PRNS_Sym) {
			PrintString();
			globalCount += 2;
		} else SynErr(39);
	}

	static void TwoWord() {
		if (la.kind == LDA_Sym) {
			Get();
		} else if (la.kind == LDC_Sym) {
			Get();
		} else if (la.kind == DSP_Sym) {
			Get();
		} else if (la.kind == BRN_Sym) {
			Get();
		} else if (la.kind == BZE_Sym) {
			Get();
		} else SynErr(40);
		output.write(token.val);
		Expect(Number_Sym);
		output.write("   ");
		output.write(token.val);
		int spaceAfterLabel = 7 - token.val.length();
		output.write(" ".repeat(spaceAfterLabel));
	}

	static void OneWord() {
		switch (la.kind) {
		case ADD_Sym: {
			Get();
			break;
		}
		case AND_Sym: {
			Get();
			break;
		}
		case ANEW_Sym: {
			Get();
			break;
		}
		case CEQ_Sym: {
			Get();
			break;
		}
		case CGE_Sym: {
			Get();
			break;
		}
		case CGT_Sym: {
			Get();
			break;
		}
		case CLE_Sym: {
			Get();
			break;
		}
		case CLT_Sym: {
			Get();
			break;
		}
		case CNE_Sym: {
			Get();
			break;
		}
		case DIV_Sym: {
			Get();
			break;
		}
		case HALT_Sym: {
			Get();
			break;
		}
		case INPB_Sym: {
			Get();
			break;
		}
		case INPI_Sym: {
			Get();
			break;
		}
		case LDV_Sym: {
			Get();
			break;
		}
		case LDXA_Sym: {
			Get();
			break;
		}
		case MUL_Sym: {
			Get();
			break;
		}
		case NEG_Sym: {
			Get();
			break;
		}
		case NOP_Sym: {
			Get();
			break;
		}
		case NOT_Sym: {
			Get();
			break;
		}
		case OR_Sym: {
			Get();
			break;
		}
		case PRNB_Sym: {
			Get();
			break;
		}
		case PRNI_Sym: {
			Get();
			break;
		}
		case PRNL_Sym: {
			Get();
			break;
		}
		case REM_Sym: {
			Get();
			break;
		}
		case STO_Sym: {
			Get();
			break;
		}
		case SUB_Sym: {
			Get();
			break;
		}
		default: SynErr(41); break;
		}
		output.write(token.val);
		if (token.val.length() < 4) {
		   output.write("          ");
		} else {
		   output.write("         ");
		}
		
	}

	static void PrintString() {
		Expect(PRNS_Sym);
		output.write("PRNS");
		output.write("  ");
		Expect(String_Sym);
		if (token.val.length() > longestString) {
		  longestString = token.val.length();
		}
		
		output.write(token.val);
		output.write(" ".repeat((longestString - token.val.length()) + 1));
	}



	public static void Parse() {
		la = new Token();
		la.val = "";
		Get();
		PVMAsm();
		Expect(EOF_SYM);

	}

	private static boolean[][] set = {
		{T,x,x,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x},
		{x,T,x,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,x,x},
		{x,x,x,x, x,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,x,x},
		{x,x,x,x, x,T,T,T, T,T,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x},
		{x,x,x,x, x,x,x,x, x,x,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, T,T,T,T, x,x,x}

	};

} // end Parser

/* pdt - considerable extension from here on */

class ErrorRec {
	public int line, col, num;
	public String str;
	public ErrorRec next;

	public ErrorRec(int l, int c, String s) {
		line = l; col = c; str = s; next = null;
	}

} // end ErrorRec

class Errors {

	public static int count = 0;                                     // number of errors detected
	public static int warns = 0;                                     // number of warnings detected
	public static String errMsgFormat = "file {0} : ({1}, {2}) {3}"; // 0=file 1=line, 2=column, 3=text
	static String fileName = "";
	static String listName = "";
	static boolean mergeErrors = false;
	static PrintWriter mergedList;

	static ErrorRec first = null, last;
	static boolean eof = false;

	static String getLine() {
		char ch, CR = '\r', LF = '\n';
		int l = 0;
		StringBuffer s = new StringBuffer();
		ch = (char) Buffer.Read();
		while (ch != Buffer.EOF && ch != CR && ch != LF) {
			s.append(ch); l++; ch = (char) Buffer.Read();
		}
		eof = (l == 0 && ch == Buffer.EOF);
		if (ch == CR) {  // check for MS-DOS
			ch = (char) Buffer.Read();
			if (ch != LF && ch != Buffer.EOF) Buffer.pos--;
		}
		return s.toString();
	}

	static private String Int(int n, int len) {
		String s = String.valueOf(n);
		int i = s.length(); if (len < i) len = i;
		int j = 0, d = len - s.length();
		char[] a = new char[len];
		for (i = 0; i < d; i++) a[i] = ' ';
		for (j = 0; i < len; i++) {a[i] = s.charAt(j); j++;}
		return new String(a, 0, len);
	}

	static void display(String s, ErrorRec e) {
		mergedList.print("**** ");
		for (int c = 1; c < e.col; c++)
			if (s.charAt(c-1) == '\t') mergedList.print("\t"); else mergedList.print(" ");
		mergedList.println("^ " + e.str);
	}

	public static void Init (String fn, String dir, boolean merge) {
		fileName = fn;
		listName = dir + "listing.txt";
		mergeErrors = merge;
		if (mergeErrors)
			try {
				mergedList = new PrintWriter(new BufferedWriter(new FileWriter(listName, false)));
			} catch (IOException e) {
				Errors.Exception("-- could not open " + listName);
			}
	}

	public static void Summarize () {
		if (mergeErrors) {
			mergedList.println();
			ErrorRec cur = first;
			Buffer.setPos(0);
			int lnr = 1;
			String s = getLine();
			while (!eof) {
				mergedList.println(Int(lnr, 4) + " " + s);
				while (cur != null && cur.line == lnr) {
					display(s, cur); cur = cur.next;
				}
				lnr++; s = getLine();
			}
			if (cur != null) {
				mergedList.println(Int(lnr, 4));
				while (cur != null) {
					display(s, cur); cur = cur.next;
				}
			}
			mergedList.println();
			mergedList.println(count + " errors detected");
			if (warns > 0) mergedList.println(warns + " warnings detected");
			mergedList.close();
		}
		switch (count) {
			case 0 : System.out.println("Parsed correctly"); break;
			case 1 : System.out.println("1 error detected"); break;
			default: System.out.println(count + " errors detected"); break;
		}
		if (warns > 0) System.out.println(warns + " warnings detected");
		if ((count > 0 || warns > 0) && mergeErrors) System.out.println("see " + listName);
	}

	public static void storeError (int line, int col, String s) {
		if (mergeErrors) {
			ErrorRec latest = new ErrorRec(line, col, s);
			if (first == null) first = latest; else last.next = latest;
			last = latest;
		} else printMsg(fileName, line, col, s);
	}

	public static void SynErr (int line, int col, int n) {
		String s;
		switch (n) {
			case 0: s = "EOF expected"; break;
			case 1: s = "Number expected"; break;
			case 2: s = "String expected"; break;
			case 3: s = "EOL expected"; break;
			case 4: s = "Comment expected"; break;
			case 5: s = "\"LDA\" expected"; break;
			case 6: s = "\"LDC\" expected"; break;
			case 7: s = "\"DSP\" expected"; break;
			case 8: s = "\"BRN\" expected"; break;
			case 9: s = "\"BZE\" expected"; break;
			case 10: s = "\"ADD\" expected"; break;
			case 11: s = "\"AND\" expected"; break;
			case 12: s = "\"ANEW\" expected"; break;
			case 13: s = "\"CEQ\" expected"; break;
			case 14: s = "\"CGE\" expected"; break;
			case 15: s = "\"CGT\" expected"; break;
			case 16: s = "\"CLE\" expected"; break;
			case 17: s = "\"CLT\" expected"; break;
			case 18: s = "\"CNE\" expected"; break;
			case 19: s = "\"DIV\" expected"; break;
			case 20: s = "\"HALT\" expected"; break;
			case 21: s = "\"INPB\" expected"; break;
			case 22: s = "\"INPI\" expected"; break;
			case 23: s = "\"LDV\" expected"; break;
			case 24: s = "\"LDXA\" expected"; break;
			case 25: s = "\"MUL\" expected"; break;
			case 26: s = "\"NEG\" expected"; break;
			case 27: s = "\"NOP\" expected"; break;
			case 28: s = "\"NOT\" expected"; break;
			case 29: s = "\"OR\" expected"; break;
			case 30: s = "\"PRNB\" expected"; break;
			case 31: s = "\"PRNI\" expected"; break;
			case 32: s = "\"PRNL\" expected"; break;
			case 33: s = "\"REM\" expected"; break;
			case 34: s = "\"STO\" expected"; break;
			case 35: s = "\"SUB\" expected"; break;
			case 36: s = "\"PRNS\" expected"; break;
			case 37: s = "??? expected"; break;
			case 38: s = "this symbol not expected in Statement"; break;
			case 39: s = "invalid Instruction"; break;
			case 40: s = "invalid TwoWord"; break;
			case 41: s = "invalid OneWord"; break;
			default: s = "error " + n; break;
		}
		storeError(line, col, s);
		count++;
	}

	public static void SemErr (int line, int col, int n) {
		storeError(line, col, ("error " + n));
		count++;
	}

	public static void Error (int line, int col, String s) {
		storeError(line, col, s);
		count++;
	}

	public static void Error (String s) {
		if (mergeErrors) mergedList.println(s); else System.out.println(s);
		count++;
	}

	public static void Warn (int line, int col, String s) {
		storeError(line, col, s);
		warns++;
	}

	public static void Warn (String s) {
		if (mergeErrors) mergedList.println(s); else System.out.println(s);
		warns++;
	}

	public static void Exception (String s) {
		System.out.println(s);
		System.exit(1);
	}

	private static void printMsg(String fileName, int line, int column, String msg) {
		StringBuffer b = new StringBuffer(errMsgFormat);
		int pos = b.indexOf("{0}");
		if (pos >= 0) { b.replace(pos, pos+3, fileName); }
		pos = b.indexOf("{1}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, line); }
		pos = b.indexOf("{2}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, column); }
		pos = b.indexOf("{3}");
		if (pos >= 0) b.replace(pos, pos+3, msg);
		System.out.println(b.toString());
	}

} // end Errors
