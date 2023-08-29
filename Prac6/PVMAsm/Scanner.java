package PVMAsm;

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
	static final int maxT = 37;
	static final int noSym = 37;
	// terminals
	static final int EOF_SYM = 0;
	static final int Number_Sym = 1;
	static final int String_Sym = 2;
	static final int EOL_Sym = 3;
	static final int Comment_Sym = 4;
	static final int LDA_Sym = 5;
	static final int LDC_Sym = 6;
	static final int DSP_Sym = 7;
	static final int BRN_Sym = 8;
	static final int BZE_Sym = 9;
	static final int ADD_Sym = 10;
	static final int AND_Sym = 11;
	static final int ANEW_Sym = 12;
	static final int CEQ_Sym = 13;
	static final int CGE_Sym = 14;
	static final int CGT_Sym = 15;
	static final int CLE_Sym = 16;
	static final int CLT_Sym = 17;
	static final int CNE_Sym = 18;
	static final int DIV_Sym = 19;
	static final int HALT_Sym = 20;
	static final int INPB_Sym = 21;
	static final int INPI_Sym = 22;
	static final int LDV_Sym = 23;
	static final int LDXA_Sym = 24;
	static final int MUL_Sym = 25;
	static final int NEG_Sym = 26;
	static final int NOP_Sym = 27;
	static final int NOT_Sym = 28;
	static final int OR_Sym = 29;
	static final int PRNB_Sym = 30;
	static final int PRNI_Sym = 31;
	static final int PRNL_Sym = 32;
	static final int REM_Sym = 33;
	static final int STO_Sym = 34;
	static final int SUB_Sym = 35;
	static final int PRNS_Sym = 36;
	static final int NOT_SYM = 37;
	// pragmas

	static short[] start = {
	  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  5,  0,  0,  0,  0,  0,
	  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
	  0,  0,  3,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  0,  0,
	  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  0,  6,  0,  0,  0,  0,
	  0, 63, 62, 64, 61,  0,  0,  0, 31, 65,  0,  0, 60, 40, 66, 47,
	 67,  0, 52, 68,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
	  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
	  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
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
		ignore.set(0); ignore.set(1); ignore.set(2); ignore.set(3); 
		ignore.set(4); ignore.set(5); ignore.set(6); ignore.set(7); 
		ignore.set(8); ignore.set(9); ignore.set(11); ignore.set(12); 
		ignore.set(13); ignore.set(14); ignore.set(15); ignore.set(16); 
		ignore.set(17); ignore.set(18); ignore.set(19); ignore.set(20); 
		ignore.set(21); ignore.set(22); ignore.set(23); ignore.set(24); 
		ignore.set(25); ignore.set(26); ignore.set(27); ignore.set(28); 
		ignore.set(29); ignore.set(30); ignore.set(31); 
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
					if ((ch >= '0' && ch <= '9')) { buf.append(ch); NextCh(); state = 2; break;}
					else { t.kind = noSym; done = true; break; }
				case 2:
					if ((ch >= '0' && ch <= '9')) { buf.append(ch); NextCh(); state = 2; break;}
					else { t.kind = Number_Sym; done = true; break; }
				case 3:
					if ((ch >= ' ' && ch <= '!'
					  || ch >= '#' && ch <= 255)) { buf.append(ch); NextCh(); state = 3; break;}
					else if (ch == '"') { buf.append(ch); NextCh(); state = 4; break;}
					else { t.kind = noSym; done = true; break; }
				case 4:
					{ t.kind = String_Sym; done = true; break; }
				case 5:
					{ t.kind = EOL_Sym; done = true; break; }
				case 6:
					if ((ch == 13)) { buf.append(ch); NextCh(); state = 7; break;}
					else if ((ch >= ' ' && ch <= 255)) { buf.append(ch); NextCh(); state = 6; break;}
					else { t.kind = noSym; done = true; break; }
				case 7:
					{ t.kind = Comment_Sym; done = true; break; }
				case 8:
					{ t.kind = LDA_Sym; done = true; break; }
				case 9:
					{ t.kind = LDC_Sym; done = true; break; }
				case 10:
					if (ch == 'P') { buf.append(ch); NextCh(); state = 11; break;}
					else { t.kind = noSym; done = true; break; }
				case 11:
					{ t.kind = DSP_Sym; done = true; break; }
				case 12:
					if (ch == 'N') { buf.append(ch); NextCh(); state = 13; break;}
					else { t.kind = noSym; done = true; break; }
				case 13:
					{ t.kind = BRN_Sym; done = true; break; }
				case 14:
					if (ch == 'E') { buf.append(ch); NextCh(); state = 15; break;}
					else { t.kind = noSym; done = true; break; }
				case 15:
					{ t.kind = BZE_Sym; done = true; break; }
				case 16:
					if (ch == 'D') { buf.append(ch); NextCh(); state = 17; break;}
					else { t.kind = noSym; done = true; break; }
				case 17:
					{ t.kind = ADD_Sym; done = true; break; }
				case 18:
					{ t.kind = AND_Sym; done = true; break; }
				case 19:
					if (ch == 'W') { buf.append(ch); NextCh(); state = 20; break;}
					else { t.kind = noSym; done = true; break; }
				case 20:
					{ t.kind = ANEW_Sym; done = true; break; }
				case 21:
					if (ch == 'Q') { buf.append(ch); NextCh(); state = 22; break;}
					else { t.kind = noSym; done = true; break; }
				case 22:
					{ t.kind = CEQ_Sym; done = true; break; }
				case 23:
					{ t.kind = CGE_Sym; done = true; break; }
				case 24:
					{ t.kind = CGT_Sym; done = true; break; }
				case 25:
					{ t.kind = CLE_Sym; done = true; break; }
				case 26:
					{ t.kind = CLT_Sym; done = true; break; }
				case 27:
					if (ch == 'E') { buf.append(ch); NextCh(); state = 28; break;}
					else { t.kind = noSym; done = true; break; }
				case 28:
					{ t.kind = CNE_Sym; done = true; break; }
				case 29:
					if (ch == 'V') { buf.append(ch); NextCh(); state = 30; break;}
					else { t.kind = noSym; done = true; break; }
				case 30:
					{ t.kind = DIV_Sym; done = true; break; }
				case 31:
					if (ch == 'A') { buf.append(ch); NextCh(); state = 32; break;}
					else { t.kind = noSym; done = true; break; }
				case 32:
					if (ch == 'L') { buf.append(ch); NextCh(); state = 33; break;}
					else { t.kind = noSym; done = true; break; }
				case 33:
					if (ch == 'T') { buf.append(ch); NextCh(); state = 34; break;}
					else { t.kind = noSym; done = true; break; }
				case 34:
					{ t.kind = HALT_Sym; done = true; break; }
				case 35:
					{ t.kind = INPB_Sym; done = true; break; }
				case 36:
					{ t.kind = INPI_Sym; done = true; break; }
				case 37:
					{ t.kind = LDV_Sym; done = true; break; }
				case 38:
					if (ch == 'A') { buf.append(ch); NextCh(); state = 39; break;}
					else { t.kind = noSym; done = true; break; }
				case 39:
					{ t.kind = LDXA_Sym; done = true; break; }
				case 40:
					if (ch == 'U') { buf.append(ch); NextCh(); state = 41; break;}
					else { t.kind = noSym; done = true; break; }
				case 41:
					if (ch == 'L') { buf.append(ch); NextCh(); state = 42; break;}
					else { t.kind = noSym; done = true; break; }
				case 42:
					{ t.kind = MUL_Sym; done = true; break; }
				case 43:
					if (ch == 'G') { buf.append(ch); NextCh(); state = 44; break;}
					else { t.kind = noSym; done = true; break; }
				case 44:
					{ t.kind = NEG_Sym; done = true; break; }
				case 45:
					{ t.kind = NOP_Sym; done = true; break; }
				case 46:
					{ t.kind = NOT_Sym; done = true; break; }
				case 47:
					if (ch == 'R') { buf.append(ch); NextCh(); state = 48; break;}
					else { t.kind = noSym; done = true; break; }
				case 48:
					{ t.kind = OR_Sym; done = true; break; }
				case 49:
					{ t.kind = PRNB_Sym; done = true; break; }
				case 50:
					{ t.kind = PRNI_Sym; done = true; break; }
				case 51:
					{ t.kind = PRNL_Sym; done = true; break; }
				case 52:
					if (ch == 'E') { buf.append(ch); NextCh(); state = 53; break;}
					else { t.kind = noSym; done = true; break; }
				case 53:
					if (ch == 'M') { buf.append(ch); NextCh(); state = 54; break;}
					else { t.kind = noSym; done = true; break; }
				case 54:
					{ t.kind = REM_Sym; done = true; break; }
				case 55:
					if (ch == 'O') { buf.append(ch); NextCh(); state = 56; break;}
					else { t.kind = noSym; done = true; break; }
				case 56:
					{ t.kind = STO_Sym; done = true; break; }
				case 57:
					if (ch == 'B') { buf.append(ch); NextCh(); state = 58; break;}
					else { t.kind = noSym; done = true; break; }
				case 58:
					{ t.kind = SUB_Sym; done = true; break; }
				case 59:
					{ t.kind = PRNS_Sym; done = true; break; }
				case 60:
					if (ch == 'D') { buf.append(ch); NextCh(); state = 69; break;}
					else { t.kind = noSym; done = true; break; }
				case 61:
					if (ch == 'S') { buf.append(ch); NextCh(); state = 10; break;}
					else if (ch == 'I') { buf.append(ch); NextCh(); state = 29; break;}
					else { t.kind = noSym; done = true; break; }
				case 62:
					if (ch == 'R') { buf.append(ch); NextCh(); state = 12; break;}
					else if (ch == 'Z') { buf.append(ch); NextCh(); state = 14; break;}
					else { t.kind = noSym; done = true; break; }
				case 63:
					if (ch == 'D') { buf.append(ch); NextCh(); state = 16; break;}
					else if (ch == 'N') { buf.append(ch); NextCh(); state = 70; break;}
					else { t.kind = noSym; done = true; break; }
				case 64:
					if (ch == 'E') { buf.append(ch); NextCh(); state = 21; break;}
					else if (ch == 'G') { buf.append(ch); NextCh(); state = 71; break;}
					else if (ch == 'L') { buf.append(ch); NextCh(); state = 72; break;}
					else if (ch == 'N') { buf.append(ch); NextCh(); state = 27; break;}
					else { t.kind = noSym; done = true; break; }
				case 65:
					if (ch == 'N') { buf.append(ch); NextCh(); state = 73; break;}
					else { t.kind = noSym; done = true; break; }
				case 66:
					if (ch == 'E') { buf.append(ch); NextCh(); state = 43; break;}
					else if (ch == 'O') { buf.append(ch); NextCh(); state = 74; break;}
					else { t.kind = noSym; done = true; break; }
				case 67:
					if (ch == 'R') { buf.append(ch); NextCh(); state = 75; break;}
					else { t.kind = noSym; done = true; break; }
				case 68:
					if (ch == 'T') { buf.append(ch); NextCh(); state = 55; break;}
					else if (ch == 'U') { buf.append(ch); NextCh(); state = 57; break;}
					else { t.kind = noSym; done = true; break; }
				case 69:
					if (ch == 'A') { buf.append(ch); NextCh(); state = 8; break;}
					else if (ch == 'C') { buf.append(ch); NextCh(); state = 9; break;}
					else if (ch == 'V') { buf.append(ch); NextCh(); state = 37; break;}
					else if (ch == 'X') { buf.append(ch); NextCh(); state = 38; break;}
					else { t.kind = noSym; done = true; break; }
				case 70:
					if (ch == 'D') { buf.append(ch); NextCh(); state = 18; break;}
					else if (ch == 'E') { buf.append(ch); NextCh(); state = 19; break;}
					else { t.kind = noSym; done = true; break; }
				case 71:
					if (ch == 'E') { buf.append(ch); NextCh(); state = 23; break;}
					else if (ch == 'T') { buf.append(ch); NextCh(); state = 24; break;}
					else { t.kind = noSym; done = true; break; }
				case 72:
					if (ch == 'E') { buf.append(ch); NextCh(); state = 25; break;}
					else if (ch == 'T') { buf.append(ch); NextCh(); state = 26; break;}
					else { t.kind = noSym; done = true; break; }
				case 73:
					if (ch == 'P') { buf.append(ch); NextCh(); state = 76; break;}
					else { t.kind = noSym; done = true; break; }
				case 74:
					if (ch == 'P') { buf.append(ch); NextCh(); state = 45; break;}
					else if (ch == 'T') { buf.append(ch); NextCh(); state = 46; break;}
					else { t.kind = noSym; done = true; break; }
				case 75:
					if (ch == 'N') { buf.append(ch); NextCh(); state = 77; break;}
					else { t.kind = noSym; done = true; break; }
				case 76:
					if (ch == 'B') { buf.append(ch); NextCh(); state = 35; break;}
					else if (ch == 'I') { buf.append(ch); NextCh(); state = 36; break;}
					else { t.kind = noSym; done = true; break; }
				case 77:
					if (ch == 'B') { buf.append(ch); NextCh(); state = 49; break;}
					else if (ch == 'I') { buf.append(ch); NextCh(); state = 50; break;}
					else if (ch == 'L') { buf.append(ch); NextCh(); state = 51; break;}
					else if (ch == 'S') { buf.append(ch); NextCh(); state = 59; break;}
					else { t.kind = noSym; done = true; break; }

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
