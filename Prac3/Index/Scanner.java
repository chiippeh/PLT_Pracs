package Index;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.BitSet;

class Token {
	public int kind;    // token kind
	public int pos;     // token position in the source text (starting at 0)
	public int col;     // token column (starting at 0)
	public int line;    // token line (starting at 1)
	public String val;  // token value
	public Token next;  // AW 2003-03-07 Tokens are kept in linked list
}

class Buffer {
	public static final char EOF = (char)256;
	static byte[] buf;
	static int bufLen;
	static int pos;

	public static void Fill (FileInputStream s) {
		try {
			bufLen = s.available();
			buf = new byte[bufLen];
			s.read(buf, 0, bufLen);
			pos = 0;
		} catch (IOException e){
			System.out.println("--- error on filling the buffer ");
			System.exit(1);
		}
	}

	public static int Read () {
		if (pos < bufLen) return buf[pos++] & 0xff;  // mask out sign bits
		else return EOF;                             /* pdt */
	}

	public static int Peek () {
		if (pos < bufLen) return buf[pos] & 0xff;    // mask out sign bits
		else return EOF;                             /* pdt */
	}

	/* AW 2003-03-10 moved this from ParserGen.cs */
	public static String GetString (int beg, int end) {
		StringBuffer s = new StringBuffer(64);
		int oldPos = Buffer.getPos();
		Buffer.setPos(beg);
		while (beg < end) { s.append((char)Buffer.Read()); beg++; }
		Buffer.setPos(oldPos);
		return s.toString();
	}

	public static int getPos() {
		return pos;
	}

	public static void setPos (int value) {
		if (value < 0) pos = 0;
		else if (value >= bufLen) pos = bufLen;
		else pos = value;
	}

} // end Buffer

public class Scanner {
	static final char EOL = '\n';
	static final int  eofSym = 0;
	static final int charSetSize = 256;
	static final int maxT = 11;
	static final int noSym = 11;
	// terminals
	static final int EOF_SYM = 0;
	static final int word_Sym = 1;
	static final int number_Sym = 2;
	static final int combo_Sym = 3;
	static final int punct_Sym = 4;
	static final int crlf_Sym = 5;
	static final int comma_Sym = 6;
	static final int lparen_Sym = 7;
	static final int rparen_Sym = 8;
	static final int minusminus_Sym = 9;
	static final int Appendix_Sym = 10;
	static final int NOT_SYM = 11;
	// pragmas

	static short[] start = {
	  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  6,  0,  0,
	  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
	  0,  0,  0,  0,  0,  0,  0,  0,  9, 10,  0,  4,  8,  5,  0,  0,
	  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  0,  0,  0,  0,  0,  0,
	  0,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,
	  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  0,  0,  0,  0,  0,
	  0,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,
	  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  0,  0,  0,  0,  0,
	  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
	  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
	  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
	  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
	  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
	  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
	  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
	  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
	  -1};


	static Token t;          // current token
	static char ch;          // current input character
	static int pos;          // column number of current character
	static int line;         // line number of current character
	static int lineStart;    // start position of current line
	static int oldEols;      // EOLs that appeared in a comment;
	static BitSet ignore;    // set of characters to be ignored by the scanner

	static Token tokens;     // the complete input token stream
	static Token pt;         // current peek token

	public static void Init (String fileName) {
		FileInputStream s = null;
		try {
			s = new FileInputStream(fileName);
			Init(s);
		} catch (IOException e) {
			System.out.println("--- Cannot open file " + fileName);
			System.exit(1);
		} finally {
			if (s != null) {
				try {
					s.close();
				} catch (IOException e) {
					System.out.println("--- Cannot close file " + fileName);
					System.exit(1);
				}
			}
		}
	}

	public static void Init (FileInputStream s) {
		Buffer.Fill(s);
		pos = -1; line = 1; lineStart = 0;
		oldEols = 0;
		NextCh();
		ignore = new BitSet(charSetSize+1);
		ignore.set(' '); // blanks are always white space
		
		//--- AW: fill token list
		tokens = new Token();  // first token is a dummy
		Token node = tokens;
		do {
			node.next = NextToken();
			node = node.next;
		} while (node.kind != eofSym);
		node.next = node;
		node.val = "EOF";
		t = pt = tokens;
	}

	static void NextCh() {
		if (oldEols > 0) { ch = EOL; oldEols--; }
		else {
			ch = (char)Buffer.Read(); pos++;
			// replace isolated '\r' by '\n' in order to make
			// eol handling uniform across Windows, Unix and Mac
			if (ch == '\r' && Buffer.Peek() != '\n') ch = EOL;
			if (ch == EOL) { line++; lineStart = pos + 1; }
		}

	}



	static void CheckLiteral() {
		String lit = t.val;
		if (lit.compareTo("--") == 0) t.kind = minusminus_Sym;
		else if (lit.compareTo("Appendix") == 0) t.kind = Appendix_Sym;
	}

	/* AW Scan() renamed to NextToken() */
	static Token NextToken() {
		while (ignore.get(ch)) NextCh();

		t = new Token();
		t.pos = pos; t.col = pos - lineStart + 1; t.line = line;
		int state = start[ch];
		StringBuffer buf = new StringBuffer(16);
		buf.append(ch); NextCh();
		boolean done = false;
		while (!done) {
			switch (state) {
				case -1: { t.kind = eofSym; done = true; break; }  // NextCh already done /* pdt */
				case 0: { t.kind = noSym; done = true; break; }    // NextCh already done
				case 1:
					if ((ch >= 'A' && ch <= 'Z'
					  || ch >= 'a' && ch <= 'z')) { buf.append(ch); NextCh(); state = 1; break;}
					else { t.kind = word_Sym; t.val = buf.toString(); CheckLiteral(); return t; }
				case 2:
					if ((ch >= '0' && ch <= '9')) { buf.append(ch); NextCh(); state = 2; break;}
					else { t.kind = number_Sym; done = true; break; }
				case 3:
					{ t.kind = combo_Sym; done = true; break; }
				case 4:
					if ((ch == '+'
					  || ch == '-')) { buf.append(ch); NextCh(); state = 4; break;}
					else { t.kind = punct_Sym; t.val = buf.toString(); CheckLiteral(); return t; }
				case 5:
					if ((ch >= '0' && ch <= '9')) { buf.append(ch); NextCh(); state = 3; break;}
					else if ((ch == '+'
					  || ch == '-')) { buf.append(ch); NextCh(); state = 4; break;}
					else { t.kind = punct_Sym; t.val = buf.toString(); CheckLiteral(); return t; }
				case 6:
					if (ch == 10) { buf.append(ch); NextCh(); state = 7; break;}
					else { t.kind = noSym; done = true; break; }
				case 7:
					{ t.kind = crlf_Sym; done = true; break; }
				case 8:
					{ t.kind = comma_Sym; done = true; break; }
				case 9:
					{ t.kind = lparen_Sym; done = true; break; }
				case 10:
					{ t.kind = rparen_Sym; done = true; break; }

			}
		}
		t.val = buf.toString();
		return t;
	}

	/* AW 2003-03-07 get the next token, move on and synch peek token with current */
	public static Token Scan () {
		t = pt = t.next;
		return t;
	}

	/* AW 2003-03-07 get the next token, ignore pragmas */
	public static Token Peek () {
		do {                      // skip pragmas while peeking
			pt = pt.next;
		} while (pt.kind > maxT);
		return pt;
	}

	/* AW 2003-03-11 to make sure peek start at current scan position */
	public static void ResetPeek () { pt = t; }

} // end Scanner
