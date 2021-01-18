/**
 * 
 */


import java.util.ArrayList;

/**
 * @author ftrww
 *
 */
public class Tokenizer {
	private static ArrayList<Token> tokenList = new ArrayList<Token>();
	private ReadFile readFile;
	private static Tokenizer tokenizer;

	
	private Tokenizer(String str) throws Exception {
		this.readFile = new ReadFile(str);
	}

	public ArrayList<Token> getTokenList(){
		return Tokenizer.tokenList;
	}
	
	public static Tokenizer getTokenizerInstance(String str) throws Exception {
		if(tokenizer == null)
			tokenizer = new Tokenizer(str);
		return tokenizer;
	}

	public void getToken() throws Exception {
		boolean blankFlag = false;
		int charTemp = readFile.readInt();
		char c;
		while (charTemp != -1) {
			c = (char) charTemp;
			Token token = new Token();
			if (this.isBlank(c)) {
				charTemp = readFile.readInt();
				c = (char) charTemp;
				blankFlag = true;
			} else if (Character.isDigit(c)) {
				token.getValue().append(c);
				charTemp = setDigit(token);
				continue;
			} else if (Character.isLowerCase(c) || Character.isUpperCase(c) || c == '_') {
				token.getValue().append(c);
				charTemp = setWord(token);
				continue;
			} else if (c == '"') {
				charTemp = setString(token);
			} else if (c == '\'') {
				charTemp = setChar(token);
			} else {
				switch (c) {
				case ('+'):
					token.getValue().append(c);
					token.setType(Token.PLUS);
					tokenList.add(token);
					break;
				case ('-'):
					token.getValue().append(c);
					charTemp = readFile.readInt();
					c = (char) charTemp;
					if (c == '>') {
						token.getValue().append(c);
						token.setType(Token.ARROW);
						tokenList.add(token);
					} else {
						token.setType(Token.MINUS);
						tokenList.add(token);
						continue;
					}
					break;
				case ('*'):
					token.getValue().append(c);
					token.setType(Token.MUL);
					tokenList.add(token);
					break;
				case ('/'):
					token.getValue().append(c);
					charTemp = readFile.readInt();
					c = (char) charTemp;
					if (c == '/') {
						charTemp = setComment(token);
						continue;
					} else {
						token.setType(Token.DIV);
						tokenList.add(token);
						continue;
					}
				case ('='):
					token.getValue().append(c);
					charTemp = readFile.readInt();
					c = (char) charTemp;
					if (c == '=') {
						token.getValue().append(c);
						token.setType(Token.EQ);
						tokenList.add(token);
					} else {
						token.setType(Token.ASSIGN);
						tokenList.add(token);
						continue;
					}
					break;
				case ('!'):
					token.getValue().append(c);
					charTemp = readFile.readInt();
					c = (char) charTemp;
					if (c == '=') {
						token.getValue().append(c);
						token.setType(Token.NEQ);
						tokenList.add(token);
					} else {
						tokenList.add(token);
						continue;
					}
					break;
				case ('<'):
					token.getValue().append(c);
					charTemp = readFile.readInt();
					c = (char) charTemp;
					if (c == '=') {
						token.getValue().append(c);
						token.setType(Token.LE);
						tokenList.add(token);
					} else {
						token.setType(Token.LT);
						tokenList.add(token);
						continue;
					}
					break;
				case ('>'):
					token.getValue().append(c);
					charTemp = readFile.readInt();
					c = (char) charTemp;
					if (c == '=') {
						token.getValue().append(c);
						token.setType(Token.GE);
						tokenList.add(token);
					} else {
						token.setType(Token.GT);
						tokenList.add(token);
						continue;
					}
					break;
				case ('('):
					token.getValue().append(c);
					token.setType(Token.L_PAREN);
					tokenList.add(token);
					break;
				case (')'):
					token.getValue().append(c);
					token.setType(Token.R_PAREN);
					tokenList.add(token);
					break;
				case ('{'):
					token.getValue().append(c);
					token.setType(Token.L_BRACE);
					tokenList.add(token);
					break;
				case ('}'):
					token.getValue().append(c);
					token.setType(Token.R_BRACE);
					tokenList.add(token);
					break;
				case (','):
					token.getValue().append(c);
					token.setType(Token.COMMA);
					tokenList.add(token);
					break;
				case (':'):
					token.getValue().append(c);
					token.setType(Token.COLON);
					tokenList.add(token);
					break;
				case (';'):
					token.getValue().append(c);
					token.setType(Token.SEMICOLON);
					tokenList.add(token);
					break;
				default:
					throw new TokenizerException("invalid char");
				}
				
			}
			if (!blankFlag) {
				charTemp = readFile.readInt();
				c = (char) charTemp;
			} else {
				blankFlag = false;
			}
		}
	}

	public int setDigit(Token token) throws Exception {
		boolean doubleFlag = false;
		boolean eflag = false;
		int charTemp;
		char c;
		charTemp = readFile.readInt();
		c = (char) charTemp;
		while (Character.isDigit(c) || (c == '.' && doubleFlag == false)||(c == 'e'||c == 'E')) {
			if (c == '.') {
				doubleFlag = true;
				token.getValue().append(c);
				charTemp = readFile.readInt();
				c = (char) charTemp;
				if (!Character.isDigit(c)) {
					throw new TokenizerException("this is an error after point");
				}
			}
			else if (c == 'e'||c == 'E') {
				if(eflag == true) {
					throw new TokenizerException("this is an error after double number with E");
				}
				c = 'E';
				eflag = true;
				token.getValue().append(c);
				charTemp = readFile.readInt();
				c = (char) charTemp;
				if (!Character.isDigit(c)) {
					throw new TokenizerException("this is an error after double number with E");
				}
			}
			token.getValue().append(c);
			charTemp = readFile.readInt();
			c = (char) charTemp;
		}
		if (doubleFlag == true) {
			token.setType(Token.DOUBLE_LITERAL);
		} else {
			token.setType(Token.UINT_LITERAL);
		}
		tokenList.add(token);
		return charTemp;
	}

	public int setString(Token token) throws Exception {
		int charTemp;
		char c;
		charTemp = readFile.readInt();
		c = (char) charTemp;
		while (c != '"') {
			if (c == '\n' || c == '\r' || charTemp == -1) {
				throw new TokenizerException("Uncomplete String");
			}
			if (c == '\\') {
				token.getValue().append(getEscape());
			} else {
				token.getValue().append(c);
			}
			charTemp = readFile.readInt();
			c = (char) charTemp;
		}
		token.setType(Token.STRING_LITERAL);
		tokenList.add(token);
		return charTemp;
	}

	public int setChar(Token token) throws Exception {
		int charTemp;
		char c;
		charTemp = readFile.readInt();
		c = (char) charTemp;
		if (charTemp == -1) {
			throw new TokenizerException("Char error");
		}
		else if(c == '\\') {
			c = getEscape();
		}
		token.getValue().append(c);
		charTemp = readFile.readInt();
		c = (char) charTemp;
		if(c != '\'') {
			throw new TokenizerException("Bad char end");
		}
		tokenList.add(token);
		return charTemp;
	}

	public int setComment(Token token) throws Exception {
		int charTemp;
		char c;
		charTemp = readFile.readInt();
		c = (char) charTemp;
		while (c != '\n' && charTemp != -1) {
			charTemp = readFile.readInt();
			c = (char) charTemp;
		}
		return charTemp;
	}

	public int setWord(Token token) throws Exception {
		int charTemp;
		char c;
		charTemp = readFile.readInt();
		c = (char) charTemp;
		while ((Character.isDigit(c) || Character.isLowerCase(c) || Character.isUpperCase(c)||c=='_')&& charTemp != -1) {
			token.getValue().append(c);
			charTemp = readFile.readInt();
			c = (char) charTemp;
		}
		if (!token.setKeyWordType()) {
			token.setType(Token.IDENT);
		}
		tokenList.add(token);
		return charTemp;
	}

	public boolean isDigit(String str) {
		for (int i = 0; i < str.length(); i++) {
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	public boolean isBlank(char c) {
		return (c == '\n' || c == ' ' || c == '\t' || c == '\r');
	}

	public char getEscape() throws Exception {
		int charTemp = readFile.readInt();
		char c = (char) charTemp;
		switch (c) {
		case ('\\'):
			return '\\';
		case ('t'):
			return '\t';
		case ('n'):
			return '\n';
		case ('r'):
			return '\r';
		case ('"'):
			return '"';
		case ('\''):
			return '\'';
		default:
			throw new TokenizerException("Bad escape word");
		}
	}
}
