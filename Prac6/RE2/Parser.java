package RE2;

import library.*;



import java.io.*;

public class Parser {
	public static final int _EOF = 0;
	public static final int _atomic = 1;
	public static final int _escaped = 2;
	public static final int _EOL = 3;
	// terminals
	public static final int EOF_SYM = 0;
	public static final int atomic_Sym = 1;
	public static final int escaped_Sym = 2;
	public static final int EOL_Sym = 3;
	public static final int bar_Sym = 4;
	public static final int point_Sym = 5;
	public static final int star_Sym = 6;
	public static final int query_Sym = 7;
	public static final int plus_Sym = 8;
	public static final int lparen_Sym = 9;
	public static final int rparen_Sym = 10;
	public static final int lbrack_Sym = 11;
	public static final int rbrack_Sym = 12;
	public static final int minus_Sym = 13;
	public static final int NOT_SYM = 14;
	// pragmas

	public static final int maxT = 14;

	static final boolean T = true;
	static final boolean x = false;
	static final int minErrDist = 2;

	public static Token token;    // last recognized token   /* pdt */
	public static Token la;       // lookahead token
	static int errDist = minErrDist;

	public static OutFile output;
public static int lineCounter = 1;



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

	static void RE2() {
		while (StartOf(1)) {
			Expression();
			lineCounter++;
			Expect(EOL_Sym);
		}
		Table.printTable(output);
		Table.printAlphabet(1);
		Table.printAlphabet(2);
		Table.printAlphabet(3);
		Expect(EOF_SYM);
	}

	static void Expression() {
		Term();
		while (la.kind == bar_Sym) {
			Get();
			Term();
		}
	}

	static void Term() {
		Factor();
		while (StartOf(2)) {
			if (la.kind == point_Sym) {
				Get();
			}
			Factor();
		}
	}

	static void Factor() {
		Element();
		if (la.kind == star_Sym || la.kind == query_Sym || la.kind == plus_Sym) {
			if (la.kind == star_Sym) {
				Get();
			} else if (la.kind == query_Sym) {
				Get();
			} else {
				Get();
			}
		}
	}

	static void Element() {
		if (la.kind == atomic_Sym || la.kind == escaped_Sym) {
			Atom();
		} else if (la.kind == lbrack_Sym) {
			Range();
		} else if (la.kind == lparen_Sym) {
			Get();
			Expression();
			Expect(rparen_Sym);
		} else SynErr(15);
	}

	static void Atom() {
		if (la.kind == atomic_Sym) {
			Get();
		} else if (la.kind == escaped_Sym) {
			Get();
		} else SynErr(16);
		if (token.val.length() > 1) { //single quote
		   char c = token.val.charAt(1);
		   Table.addRef((char) c, lineCounter);
		 } else {
		   char c = token.val.charAt(0);
		   Table.addRef(c, lineCounter);
		 }
	}

	static void Range() {
		Expect(lbrack_Sym);
		OneRange();
		while (la.kind == atomic_Sym || la.kind == escaped_Sym) {
			OneRange();
		}
		Expect(rbrack_Sym);
	}

	static void OneRange() {
		char atomVal1 = 'x'; char atomVal2 = 'x';
		Atom();
		if (token.val.length() > 1) {
		  atomVal1 = token.val.charAt(1);
		} else {
		  atomVal1 = token.val.charAt(0);
		}
		if (la.kind == minus_Sym) {
			Get();
			Atom();
			if (token.val.length() > 1) {
			  atomVal2 = token.val.charAt(1);
			} else {
			  atomVal2 = token.val.charAt(0);
			}
		}
		for (int i = (atomVal1)+1; i < atomVal2; i++) {
		    Table.addRef((char) i, lineCounter);
		}
	}



	public static void Parse() {
		la = new Token();
		la.val = "";
		Get();
		RE2();
		Expect(EOF_SYM);

	}

	private static boolean[][] set = {
		{T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x},
		{x,T,T,x, x,x,x,x, x,T,x,T, x,x,x,x},
		{x,T,T,x, x,T,x,x, x,T,x,T, x,x,x,x}

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
			case 1: s = "atomic expected"; break;
			case 2: s = "escaped expected"; break;
			case 3: s = "EOL expected"; break;
			case 4: s = "\"|\" expected"; break;
			case 5: s = "\".\" expected"; break;
			case 6: s = "\"*\" expected"; break;
			case 7: s = "\"?\" expected"; break;
			case 8: s = "\"+\" expected"; break;
			case 9: s = "\"(\" expected"; break;
			case 10: s = "\")\" expected"; break;
			case 11: s = "\"[\" expected"; break;
			case 12: s = "\"]\" expected"; break;
			case 13: s = "\"-\" expected"; break;
			case 14: s = "??? expected"; break;
			case 15: s = "invalid Element"; break;
			case 16: s = "invalid Atom"; break;
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
