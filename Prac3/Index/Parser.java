package Index;


import java.io.*;

public class Parser {
	public static final int _EOF = 0;
	public static final int _word = 1;
	public static final int _number = 2;
	public static final int _combo = 3;
	public static final int _punct = 4;
	// terminals
	public static final int EOF_SYM = 0;
	public static final int word_Sym = 1;
	public static final int number_Sym = 2;
	public static final int combo_Sym = 3;
	public static final int punct_Sym = 4;
	public static final int crlf_Sym = 5;
	public static final int comma_Sym = 6;
	public static final int lparen_Sym = 7;
	public static final int rparen_Sym = 8;
	public static final int minusminus_Sym = 9;
	public static final int Appendix_Sym = 10;
	public static final int NOT_SYM = 11;
	// pragmas

	public static final int maxT = 11;

	static final boolean T = true;
	static final boolean x = false;
	static final int minErrDist = 2;

	public static Token token;    // last recognized token   /* pdt */
	public static Token la;       // lookahead token
	static int errDist = minErrDist;

	

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

	static void Index() {
		while (la.kind == word_Sym || la.kind == crlf_Sym || la.kind == lparen_Sym) {
			Line();
			Expect(crlf_Sym);
		}
		Expect(EOF_SYM);
	}

	static void Line() {
		if (la.kind == word_Sym || la.kind == lparen_Sym) {
			Phrase();
			while (la.kind == comma_Sym) {
				Get();
				Phrase();
			}
			while (la.kind == comma_Sym || la.kind == minusminus_Sym) {
				if (la.kind == comma_Sym) {
					Get();
				} else {
					Dashed();
				}
			}
			while (la.kind == number_Sym) {
				EndIndex();
			}
		} else if (la.kind == crlf_Sym) {
		} else SynErr(12);
	}

	static void Phrase() {
		if (la.kind == word_Sym) {
			Get();
			while (la.kind == punct_Sym) {
				Get();
			}
		} else if (la.kind == lparen_Sym) {
			Get();
			while (la.kind == word_Sym) {
				Get();
			}
			while (la.kind == punct_Sym) {
				Get();
			}
			Expect(rparen_Sym);
		} else SynErr(13);
		while (la.kind == punct_Sym) {
			Get();
		}
		while (la.kind == word_Sym || la.kind == combo_Sym || la.kind == lparen_Sym) {
			if (la.kind == word_Sym) {
				Get();
				while (la.kind == punct_Sym) {
					Get();
				}
			} else if (la.kind == lparen_Sym) {
				Get();
				while (la.kind == word_Sym) {
					Get();
				}
				while (la.kind == punct_Sym) {
					Get();
				}
				Expect(rparen_Sym);
			} else {
				Get();
			}
			while (la.kind == punct_Sym) {
				Get();
			}
		}
	}

	static void Dashed() {
		Expect(minusminus_Sym);
		Phrase();
	}

	static void EndIndex() {
		Expect(number_Sym);
		if (la.kind == comma_Sym) {
			Get();
		}
		while (StartOf(1)) {
			if (la.kind == number_Sym || la.kind == combo_Sym || la.kind == punct_Sym) {
				if (la.kind == number_Sym || la.kind == punct_Sym) {
					while (la.kind == punct_Sym) {
						Get();
					}
					Expect(number_Sym);
				} else {
					Get();
				}
				if (la.kind == comma_Sym) {
					Get();
				}
			} else {
				Appendix();
			}
		}
	}

	static void Appendix() {
		Expect(Appendix_Sym);
		Expect(number_Sym);
	}



	public static void Parse() {
		la = new Token();
		la.val = "";
		Get();
		Index();
		Expect(EOF_SYM);

	}

	private static boolean[][] set = {
		{T,x,x,x, x,x,x,x, x,x,x,x, x},
		{x,x,T,T, T,x,x,x, x,x,T,x, x}

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
			case 1: s = "word expected"; break;
			case 2: s = "number expected"; break;
			case 3: s = "combo expected"; break;
			case 4: s = "punct expected"; break;
			case 5: s = "\"\\r\\n\" expected"; break;
			case 6: s = "\",\" expected"; break;
			case 7: s = "\"(\" expected"; break;
			case 8: s = "\")\" expected"; break;
			case 9: s = "\"--\" expected"; break;
			case 10: s = "\"Appendix\" expected"; break;
			case 11: s = "??? expected"; break;
			case 12: s = "invalid Line"; break;
			case 13: s = "invalid Phrase"; break;
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
