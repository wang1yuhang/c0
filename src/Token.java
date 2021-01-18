/**
 * 
 */


/**
 * @author ftrww
 *
 */
public class Token {
	private StringBuilder value;
	private byte type;
	public static final byte UNKOWN = 0;
	public static final byte FN_KW = 1;
	public static final byte LET_KW  = 2;
	public static final byte CONST_KW = 3;
	public static final byte AS_KW = 4;
	public static final byte WHILE_KW = 5;
	public static final byte IF_KW = 6;
	public static final byte ELSE_KW = 7;
	public static final byte RETURN_KW = 8;
	
	public static final byte PLUS = 9;
	public static final byte MINUS = 10;
	public static final byte MUL = 11;
	public static final byte DIV = 12;
	public static final byte ASSIGN = 13;
	public static final byte EQ = 14;
	public static final byte NEQ = 15;
	public static final byte LT = 16;
	public static final byte GT = 17;
	public static final byte LE = 18;
	public static final byte GE = 19;
	public static final byte L_PAREN = 20;
	public static final byte R_PAREN = 21;
	public static final byte L_BRACE = 22;
	public static final byte R_BRACE = 23;
	public static final byte ARROW = 24;
	public static final byte COMMA = 25;
	public static final byte COLON = 26;
	public static final byte SEMICOLON =27;
	
	public static final byte BREAK_KW = 28;
	public static final byte CONTINUE_KW = 29;
	
	public static final byte UINT_LITERAL = 30;
	public static final byte STRING_LITERAL = 31;
	public static final byte DOUBLE_LITERAL = 32;
	public static final byte CHAR_LITERAL = 33;
	public static final byte IDENT = 34;
	
	public static final byte COMMENT = 35;
	public static final byte DEFAULT = 50;
	
	public Token() {
		this.value = new StringBuilder();
		this.type = UNKOWN;
	}
	
	public Token(StringBuilder value) {
		this.value = value;
		this.type = UNKOWN;
	}
	
	public Token(StringBuilder value,byte type) {
		this.value = value;
		this.type = type;
	}
	
	public StringBuilder getValue() {
		return value;
	}
	
	public void setValue(StringBuilder value) {
		this.value = value;
	}
	
	public byte getType() {
		return type;
	}
	
	public void setType(byte type) {
		this.type = type;
	}
	
	public boolean setKeyWordType() {
		switch(this.value.toString()) {
		case("fn"):
			this.type = Token.FN_KW;
			return true;
		case("let"):
			this.type = Token.LET_KW;
			return true;
		case("const"):
			this.type = Token.CONST_KW;
			return true;
		case("as"):
			this.type = Token.AS_KW;
			return true;
		case("while"):
			this.type = Token.WHILE_KW;
			return true;
		case("if"):
			this.type = Token.IF_KW;
			return true;
		case("else"):
			this.type = Token.ELSE_KW;
			return true;
		case("return"):
			this.type = Token.RETURN_KW;
			return true;
		case("break"):
			this.type = Token.BREAK_KW;
			return true;
		case("continue"):
			this.type = Token.CONTINUE_KW;
			return true;
		default:
			this.type = Token.UNKOWN;
			return false;
		}
	}
	
	public int getPriority(){
		switch(this.type){
			case ASSIGN:
				return 1;
			case GT:
				return 2;
			case LT:
				return 2;
			case GE:
				return 2;
			case LE:
				return 2;
			case EQ:
				return 2;
			case NEQ:
				return 2;
			case PLUS:
				return 3;
			case MINUS:
				return 3;
			case MUL:
				return 4;
			case DIV:
				return 4;
			case AS_KW:
				return 5;
			default:
				return 6;
		}
	}
	
	public boolean isBinaryOp(){
		boolean isBinary1 = (this.type == PLUS);
		boolean isBinary2 = (this.type == MINUS);
		boolean isBinary3 = (this.type == MUL);
		boolean isBinary4 = (this.type == DIV);
		boolean isBinary5 = (this.type == EQ);
		boolean isBinary6 = (this.type == NEQ);
		boolean isBinary7 = (this.type == LT);
		boolean isBinary8 = (this.type == GT);
		boolean isBinary9 = (this.type == LE);
		boolean isBinary10 = (this.type == GE);
		boolean isBinary11 = (this.type == ASSIGN);
		boolean isBinary12 = (this.type == AS_KW);
		boolean isBinaryAll = isBinary1||isBinary2||isBinary3||isBinary4||isBinary5||isBinary6||isBinary7||isBinary8||isBinary9||isBinary10||isBinary11||isBinary12;
		return isBinaryAll;
	}
	
	public boolean isRightAssoc() {
		if(this.type == ASSIGN) {
			return true;
		}
		return false;
	}
}

