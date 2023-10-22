package FormatParva;

import library.*;
import java.util.*;



import java.io.*;

public class Parser {
	public static final int _EOF = 0;
	public static final int _identifier = 1;
	public static final int _number = 2;
	public static final int _stringLit = 3;
	public static final int _charLit = 4;
	// terminals
	public static final int EOF_SYM = 0;
	public static final int identifier_Sym = 1;
	public static final int number_Sym = 2;
	public static final int stringLit_Sym = 3;
	public static final int charLit_Sym = 4;
	public static final int void_Sym = 5;
	public static final int lparen_Sym = 6;
	public static final int rparen_Sym = 7;
	public static final int lbrace_Sym = 8;
	public static final int rbrace_Sym = 9;
	public static final int semicolon_Sym = 10;
	public static final int const_Sym = 11;
	public static final int comma_Sym = 12;
	public static final int equal_Sym = 13;
	public static final int true_Sym = 14;
	public static final int false_Sym = 15;
	public static final int null_Sym = 16;
	public static final int lbrackrbrack_Sym = 17;
	public static final int int_Sym = 18;
	public static final int bool_Sym = 19;
	public static final int char_Sym = 20;
	public static final int plusplus_Sym = 21;
	public static final int minusminus_Sym = 22;
	public static final int lbrack_Sym = 23;
	public static final int rbrack_Sym = 24;
	public static final int if_Sym = 25;
	public static final int elsif_Sym = 26;
	public static final int else_Sym = 27;
	public static final int while_Sym = 28;
	public static final int do_Sym = 29;
	public static final int for_Sym = 30;
	public static final int to_Sym = 31;
	public static final int downto_Sym = 32;
	public static final int break_Sym = 33;
	public static final int halt_Sym = 34;
	public static final int read_Sym = 35;
	public static final int readLine_Sym = 36;
	public static final int write_Sym = 37;
	public static final int writeLine_Sym = 38;
	public static final int barbar_Sym = 39;
	public static final int andand_Sym = 40;
	public static final int plus_Sym = 41;
	public static final int minus_Sym = 42;
	public static final int bang_Sym = 43;
	public static final int new_Sym = 44;
	public static final int star_Sym = 45;
	public static final int slash_Sym = 46;
	public static final int percent_Sym = 47;
	public static final int equalequal_Sym = 48;
	public static final int bangequal_Sym = 49;
	public static final int less_Sym = 50;
	public static final int lessequal_Sym = 51;
	public static final int greater_Sym = 52;
	public static final int greaterequal_Sym = 53;
	public static final int NOT_SYM = 54;
	// pragmas

	public static final int maxT = 54;

	static final boolean T = true;
	static final boolean x = false;
	static final int minErrDist = 2;

	public static Token token;    // last recognized token   /* pdt */
	public static Token la;       // lookahead token
	static int errDist = minErrDist;

	public static OutFile output = null;

    static int indentation = 0;

    public static void openOutput(String fileName) {
    // Opens output file from specified fileName
      output = new OutFile(fileName);
      if (output.openError()) {
        System.out.println("cannot open " + fileName);
        System.exit(1);
      }
    }

    public static void closeOutput() {
    // Closes output file
      output.close();
    }
	public static void append(String str) {
    // Appends str to output file
      output.write(str);
    }

    public static void newLine() {
    // Writes line mark to output file but leaves indentation as before
      output.writeLine();
      appendLeadingSpaces();
    }

    static void appendLeadingSpaces() {
    // Appends the spaces needed at the start of each line of output before a statement
      for (int j = 1; j <= indentation; j++) output.write(' ');
    }

    public static void indentNewLine() {
    // Writes line mark to output file and prepares to indent further lines
    // by two spaces more than before
      indentation += 2;
      newLine();
    }

    public static void decIndentNewLine() {
    // Writes line mark to output file and prepares to indent further lines
    // by two spaces less
      indentation -= 2;
      newLine();
    }

    public static void indent() {
    // Increments indentation level by 2
      indentation += 2;
    }

    public static void decIndent() {
    // Decrements indentation level by 2
      indentation -= 2;
    }


  public static boolean
    indented = true;



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

	static void FormatParva() {
		String name;
		Expect(void_Sym);
		append("void ");
		name = Ident();
		append(name);
		Expect(lparen_Sym);
		Expect(rparen_Sym);
		append("()");
		Block();
	}

	static String Ident() {
		String name;
		Expect(identifier_Sym);
		name = token.val;
		return name;
	}

	static void Block() {
		Expect(lbrace_Sym);
		append(" {"); indent();
		while (StartOf(1)) {
			Statement(!indented);
		}
		Expect(rbrace_Sym);
		decIndentNewLine();
		append("}");
	}

	static void Statement(boolean indented) {
		if (la.kind == lbrace_Sym) {
			Block();
		} else if (la.kind == semicolon_Sym) {
			Get();
			append(";");
		} else if (StartOf(2)) {
			if (indented) indent();
			newLine();
			switch (la.kind) {
			case const_Sym: {
				ConstDeclarations();
				break;
			}
			case int_Sym: case bool_Sym: case char_Sym: {
				VarDeclarations();
				break;
			}
			case identifier_Sym: case plusplus_Sym: case minusminus_Sym: {
				AssignmentStatement();
				break;
			}
			case if_Sym: {
				IfStatement();
				break;
			}
			case while_Sym: {
				WhileStatement();
				break;
			}
			case do_Sym: {
				DoWhileStatement();
				break;
			}
			case for_Sym: {
				ForStatement();
				break;
			}
			case break_Sym: {
				BreakStatement();
				break;
			}
			case halt_Sym: {
				HaltStatement();
				break;
			}
			case read_Sym: {
				ReadStatement();
				break;
			}
			case readLine_Sym: {
				ReadLineStatement();
				break;
			}
			case write_Sym: {
				WriteStatement();
				break;
			}
			case writeLine_Sym: {
				WriteLineStatement();
				break;
			}
			}
			if (indented) decIndent();
		} else SynErr(55);
	}

	static void ConstDeclarations() {
		Expect(const_Sym);
		append("const ");
		OneConst();
		while (WeakSeparator(comma_Sym, 3, 4)) {
			append(", ");
			OneConst();
		}
		ExpectWeak(semicolon_Sym, 5);
		append(";");
	}

	static void VarDeclarations() {
		String name = Type();
		append(name + " ");
		OneVar();
		while (WeakSeparator(comma_Sym, 3, 4)) {
			append(", ");
			OneVar();
		}
		ExpectWeak(semicolon_Sym, 5);
		append(";");
	}

	static void AssignmentStatement() {
		Assignment();
		ExpectWeak(semicolon_Sym, 5);
		append(";");
	}

	static void IfStatement() {
		Expect(if_Sym);
		append("if ");
		Expect(lparen_Sym);
		append("(");
		Condition();
		Expect(rparen_Sym);
		append(")");
		Statement(indented);
		while (la.kind == elsif_Sym) {
			Get();
			newLine(); append("else if ");
			Expect(lparen_Sym);
			append("(");
			Condition();
			Expect(rparen_Sym);
			append(")");
			Statement(indented);
		}
		if (la.kind == else_Sym) {
			Get();
			newLine(); append("else ");
			Statement(indented);
		}
	}

	static void WhileStatement() {
		Expect(while_Sym);
		append("while ");
		Expect(lparen_Sym);
		append("(");
		Condition();
		Expect(rparen_Sym);
		append(")");
		Statement(indented);
	}

	static void DoWhileStatement() {
		Expect(do_Sym);
		append("do ");
		Statement(indented);
		ExpectWeak(while_Sym, 6);
		append(" while ");
		Expect(lparen_Sym);
		append("(");
		Condition();
		Expect(rparen_Sym);
		append(")");
		ExpectWeak(semicolon_Sym, 5);
		append(";");
	}

	static void ForStatement() {
		Expect(for_Sym);
		append("for ");
		ForControl();
		Statement(indented);
	}

	static void BreakStatement() {
		Expect(break_Sym);
		append("break");
		ExpectWeak(semicolon_Sym, 5);
		append(";");
	}

	static void HaltStatement() {
		Expect(halt_Sym);
		append("halt");
		ExpectWeak(semicolon_Sym, 5);
		append(";");
	}

	static void ReadStatement() {
		Expect(read_Sym);
		append("read");
		Expect(lparen_Sym);
		append("(");
		ReadElement();
		while (WeakSeparator(comma_Sym, 7, 8)) {
			append(", ");
			ReadElement();
		}
		Expect(rparen_Sym);
		append(")");
		ExpectWeak(semicolon_Sym, 5);
		append(";");
	}

	static void ReadLineStatement() {
		Expect(readLine_Sym);
		append("readLine");
		Expect(lparen_Sym);
		append("(");
		if (la.kind == identifier_Sym || la.kind == stringLit_Sym) {
			ReadElement();
			while (WeakSeparator(comma_Sym, 7, 8)) {
				append(", ");
				ReadElement();
			}
		}
		Expect(rparen_Sym);
		append(")");
		ExpectWeak(semicolon_Sym, 5);
		append(";");
	}

	static void WriteStatement() {
		Expect(write_Sym);
		append("write");
		Expect(lparen_Sym);
		append("(");
		WriteElement();
		while (WeakSeparator(comma_Sym, 9, 8)) {
			append(", ");
			WriteElement();
		}
		Expect(rparen_Sym);
		append(")");
		ExpectWeak(semicolon_Sym, 5);
		append(";");
	}

	static void WriteLineStatement() {
		Expect(writeLine_Sym);
		append("writeLine");
		Expect(lparen_Sym);
		append("(");
		if (StartOf(9)) {
			WriteElement();
			while (WeakSeparator(comma_Sym, 9, 8)) {
				append(", ");
				WriteElement();
			}
		}
		Expect(rparen_Sym);
		append(")");
		ExpectWeak(semicolon_Sym, 5);
		append(";");
	}

	static void OneConst() {
		String name;
		name = Ident();
		append(name);
		Expect(equal_Sym);
		append(" = ");
		Constant();
	}

	static void Constant() {
		String name;
		if (la.kind == number_Sym) {
			name = IntConst();
			append(name);
		} else if (la.kind == charLit_Sym) {
			name = CharConst();
			append(name);
		} else if (la.kind == true_Sym) {
			Get();
			append("true");
		} else if (la.kind == false_Sym) {
			Get();
			append("false");
		} else if (la.kind == null_Sym) {
			Get();
			append("null");
		} else SynErr(56);
	}

	static String IntConst() {
		String name;
		Expect(number_Sym);
		name = token.val;
		return name;
	}

	static String CharConst() {
		String name;
		Expect(charLit_Sym);
		name = token.val;
		return name;
	}

	static String Type() {
		String name;
		name = "";
		name = BasicType();
		if (la.kind == lbrackrbrack_Sym) {
			Get();
			append(name + "[] ");
		}
		return name;
	}

	static void OneVar() {
		String name;
		name = Ident();
		append(name);
		if (la.kind == equal_Sym) {
			Get();
			append(" = ");
			Expression();
		}
	}

	static String BasicType() {
		String name;
		name = "";
		if (la.kind == int_Sym) {
			Get();
			name = "int";
		} else if (la.kind == bool_Sym) {
			Get();
			name = "bool";
		} else if (la.kind == char_Sym) {
			Get();
			name = "char";
		} else SynErr(57);
		return name;
	}

	static void Expression() {
		AndExp();
		while (la.kind == barbar_Sym) {
			Get();
			append(" || ");
			AndExp();
		}
	}

	static void Assignment() {
		if (la.kind == identifier_Sym) {
			Designator();
			if (la.kind == equal_Sym) {
				AssignOp();
				Expression();
			} else if (la.kind == plusplus_Sym) {
				Get();
				append("++");
			} else if (la.kind == minusminus_Sym) {
				Get();
				append("--");
			} else SynErr(58);
		} else if (la.kind == plusplus_Sym) {
			Get();
			Designator();
		} else if (la.kind == minusminus_Sym) {
			Get();
			Designator();
		} else SynErr(59);
	}

	static void Designator() {
		String name;
		name = Ident();
		append(name);
		if (la.kind == lbrack_Sym) {
			Get();
			append("[");
			Expression();
			Expect(rbrack_Sym);
			append("]");
		}
	}

	static void AssignOp() {
		Expect(equal_Sym);
		append(" = ");
	}

	static void Condition() {
		Expression();
	}

	static void ForControl() {
		String name;
		if (la.kind == lparen_Sym) {
			Get();
			append("(");
			if (StartOf(10)) {
				if (la.kind == int_Sym || la.kind == bool_Sym || la.kind == char_Sym) {
					name = BasicType();
					append(name);
				}
				name = Ident();
				append(name);
				Expect(equal_Sym);
				append("=");
				Expression();
			}
			Expect(semicolon_Sym);
			append(";");
			if (StartOf(11)) {
				Condition();
			}
			Expect(semicolon_Sym);
			append(";");
			if (la.kind == identifier_Sym || la.kind == plusplus_Sym || la.kind == minusminus_Sym) {
				Assignment();
			}
			Expect(rparen_Sym);
			append(")");
		} else if (la.kind == identifier_Sym) {
			name = Ident();
			append(name);
			Expect(equal_Sym);
			append(" = ");
			Expression();
			if (la.kind == to_Sym) {
				Get();
				append("to ");
			} else if (la.kind == downto_Sym) {
				Get();
				append("downto ");
			} else SynErr(60);
			Expression();
		} else SynErr(61);
	}

	static void ReadElement() {
		String name;
		if (la.kind == stringLit_Sym) {
			name = StringConst();
			append(name);
		} else if (la.kind == identifier_Sym) {
			Designator();
		} else SynErr(62);
	}

	static String StringConst() {
		String name;
		Expect(stringLit_Sym);
		name = token.val;
		return name;
	}

	static void WriteElement() {
		String name;
		if (la.kind == stringLit_Sym) {
			name = StringConst();
			append(name);
		} else if (StartOf(11)) {
			Expression();
		} else SynErr(63);
	}

	static void AndExp() {
		EqlExp();
		while (la.kind == andand_Sym) {
			Get();
			append(" && ");
			EqlExp();
		}
	}

	static void EqlExp() {
		RelExp();
		while (la.kind == equalequal_Sym || la.kind == bangequal_Sym) {
			EqualOp();
			RelExp();
		}
	}

	static void RelExp() {
		AddExp();
		if (StartOf(12)) {
			RelOp();
			AddExp();
		}
	}

	static void EqualOp() {
		if (la.kind == equalequal_Sym) {
			Get();
			append(" == ");
		} else if (la.kind == bangequal_Sym) {
			Get();
			append(" != ");
		} else SynErr(64);
	}

	static void AddExp() {
		MultExp();
		while (la.kind == plus_Sym || la.kind == minus_Sym) {
			AddOp();
			MultExp();
		}
	}

	static void RelOp() {
		if (la.kind == less_Sym) {
			Get();
			append(" < ");
		} else if (la.kind == lessequal_Sym) {
			Get();
			append(" <= ");
		} else if (la.kind == greater_Sym) {
			Get();
			append(" > ");
		} else if (la.kind == greaterequal_Sym) {
			Get();
			append(" >= ");
		} else SynErr(65);
	}

	static void MultExp() {
		Factor();
		while (la.kind == star_Sym || la.kind == slash_Sym || la.kind == percent_Sym) {
			MulOp();
			Factor();
		}
	}

	static void AddOp() {
		if (la.kind == plus_Sym) {
			Get();
			append(" + ");
		} else if (la.kind == minus_Sym) {
			Get();
			append(" - ");
		} else SynErr(66);
	}

	static void Factor() {
		if (StartOf(13)) {
			Primary();
		} else if (la.kind == plus_Sym) {
			Get();
			append(" + ");
			Factor();
		} else if (la.kind == minus_Sym) {
			Get();
			append(" - ");
			Factor();
		} else if (la.kind == bang_Sym) {
			Get();
			append("!");
			Factor();
		} else SynErr(67);
	}

	static void MulOp() {
		if (la.kind == star_Sym) {
			Get();
			append(" * ");
		} else if (la.kind == slash_Sym) {
			Get();
			append(" / ");
		} else if (la.kind == percent_Sym) {
			Get();
			append(" % ");
		} else SynErr(68);
	}

	static void Primary() {
		String name;
		if (la.kind == identifier_Sym) {
			Designator();
		} else if (StartOf(14)) {
			Constant();
		} else if (la.kind == new_Sym) {
			Get();
			append(" = new ");
			name = BasicType();
			append(name);
			Expect(lbrack_Sym);
			append("[");
			Expression();
			Expect(rbrack_Sym);
			append("]");
		} else if (la.kind == lparen_Sym) {
			Get();
			append("(");
			if (la.kind == char_Sym) {
				Get();
				Expect(rparen_Sym);
				append("char )");
				Factor();
			} else if (la.kind == int_Sym) {
				Get();
				Expect(rparen_Sym);
				append("int )");
				Factor();
			} else if (StartOf(11)) {
				Expression();
				Expect(rparen_Sym);
				append(")");
			} else SynErr(69);
		} else SynErr(70);
	}



	public static void Parse() {
		la = new Token();
		la.val = "";
		Get();
		FormatParva();
		Expect(EOF_SYM);

	}

	private static boolean[][] set = {
		{T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x},
		{x,T,x,x, x,x,x,x, T,x,T,T, x,x,x,x, x,x,T,T, T,T,T,x, x,T,x,x, T,T,T,x, x,T,T,T, T,T,T,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x},
		{x,T,x,x, x,x,x,x, x,x,x,T, x,x,x,x, x,x,T,T, T,T,T,x, x,T,x,x, T,T,T,x, x,T,T,T, T,T,T,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x},
		{x,T,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x},
		{x,x,x,x, x,x,x,x, x,x,T,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x},
		{T,T,x,x, x,x,x,x, T,T,T,T, x,x,x,x, x,x,T,T, T,T,T,x, x,T,T,T, T,T,T,x, x,T,T,T, T,T,T,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x},
		{T,x,x,x, x,x,T,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x},
		{x,T,x,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x},
		{x,x,x,x, x,x,x,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x},
		{x,T,T,T, T,x,T,x, x,x,x,x, x,x,T,T, T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,T,T,T, T,x,x,x, x,x,x,x, x,x,x,x},
		{x,T,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,T, T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x},
		{x,T,T,x, T,x,T,x, x,x,x,x, x,x,T,T, T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,T,T,T, T,x,x,x, x,x,x,x, x,x,x,x},
		{x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,T, T,T,x,x},
		{x,T,T,x, T,x,T,x, x,x,x,x, x,x,T,T, T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, T,x,x,x, x,x,x,x, x,x,x,x},
		{x,x,T,x, T,x,x,x, x,x,x,x, x,x,T,T, T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x}

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
			case 1: s = "identifier expected"; break;
			case 2: s = "number expected"; break;
			case 3: s = "stringLit expected"; break;
			case 4: s = "charLit expected"; break;
			case 5: s = "\"void\" expected"; break;
			case 6: s = "\"(\" expected"; break;
			case 7: s = "\")\" expected"; break;
			case 8: s = "\"{\" expected"; break;
			case 9: s = "\"}\" expected"; break;
			case 10: s = "\";\" expected"; break;
			case 11: s = "\"const\" expected"; break;
			case 12: s = "\",\" expected"; break;
			case 13: s = "\"=\" expected"; break;
			case 14: s = "\"true\" expected"; break;
			case 15: s = "\"false\" expected"; break;
			case 16: s = "\"null\" expected"; break;
			case 17: s = "\"[]\" expected"; break;
			case 18: s = "\"int\" expected"; break;
			case 19: s = "\"bool\" expected"; break;
			case 20: s = "\"char\" expected"; break;
			case 21: s = "\"++\" expected"; break;
			case 22: s = "\"--\" expected"; break;
			case 23: s = "\"[\" expected"; break;
			case 24: s = "\"]\" expected"; break;
			case 25: s = "\"if\" expected"; break;
			case 26: s = "\"elsif\" expected"; break;
			case 27: s = "\"else\" expected"; break;
			case 28: s = "\"while\" expected"; break;
			case 29: s = "\"do\" expected"; break;
			case 30: s = "\"for\" expected"; break;
			case 31: s = "\"to\" expected"; break;
			case 32: s = "\"downto\" expected"; break;
			case 33: s = "\"break\" expected"; break;
			case 34: s = "\"halt\" expected"; break;
			case 35: s = "\"read\" expected"; break;
			case 36: s = "\"readLine\" expected"; break;
			case 37: s = "\"write\" expected"; break;
			case 38: s = "\"writeLine\" expected"; break;
			case 39: s = "\"||\" expected"; break;
			case 40: s = "\"&&\" expected"; break;
			case 41: s = "\"+\" expected"; break;
			case 42: s = "\"-\" expected"; break;
			case 43: s = "\"!\" expected"; break;
			case 44: s = "\"new\" expected"; break;
			case 45: s = "\"*\" expected"; break;
			case 46: s = "\"/\" expected"; break;
			case 47: s = "\"%\" expected"; break;
			case 48: s = "\"==\" expected"; break;
			case 49: s = "\"!=\" expected"; break;
			case 50: s = "\"<\" expected"; break;
			case 51: s = "\"<=\" expected"; break;
			case 52: s = "\">\" expected"; break;
			case 53: s = "\">=\" expected"; break;
			case 54: s = "??? expected"; break;
			case 55: s = "invalid Statement"; break;
			case 56: s = "invalid Constant"; break;
			case 57: s = "invalid BasicType"; break;
			case 58: s = "invalid Assignment"; break;
			case 59: s = "invalid Assignment"; break;
			case 60: s = "invalid ForControl"; break;
			case 61: s = "invalid ForControl"; break;
			case 62: s = "invalid ReadElement"; break;
			case 63: s = "invalid WriteElement"; break;
			case 64: s = "invalid EqualOp"; break;
			case 65: s = "invalid RelOp"; break;
			case 66: s = "invalid AddOp"; break;
			case 67: s = "invalid Factor"; break;
			case 68: s = "invalid MulOp"; break;
			case 69: s = "invalid Primary"; break;
			case 70: s = "invalid Primary"; break;
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
