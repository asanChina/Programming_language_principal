package cop5555sp15;

//import cop5555sp15.TokenStream.Kind;
import cop5555sp15.TokenStream.Token;
import static cop5555sp15.TokenStream.Kind.*;

public class Scanner 
{

	private TokenStream stream;
	
	public Scanner(TokenStream stream) 
	{
		this.stream = stream;
	}


	// Fills in the stream.tokens list with recognized tokens 
     //from the input
	public void scan() 
	{
		int length = stream.inputChars.length;
		char[] tmp = stream.inputChars;

		int begin = -1, end = -1, lineNumber = 1;
		for(int index = 0; index < length; index++)
		{
			
			//skip white spaces including blank, horizontal tab, \r, \n, we should
			//change value "lineNumber" if when encounter single '\r', single '\n' or 
			// "\r\n"
			if(Character.isWhitespace(tmp[index]))
			{
				if(tmp[index] == '\n')
					lineNumber++;
				else if(tmp[index] == '\r' && index +1 < length && tmp[index+1] == '\n')
				{
					lineNumber++;
					index++;
				}
				else if(tmp[index] == '\r')
					lineNumber++;
			}
			//expect to construct a identifier
			else if(Character.isJavaIdentifierStart((tmp[index])))
			{
				Token t = null;
				begin = index;
				end = index+1;
				while(end < length && Character.isJavaIdentifierPart(tmp[end]))
					end++;
				
				//now tmp(begin,...,end-1) are an possible identifier, but we should be
				//careful with keywords since keywords are in same style with normal identifier.
				//currently we don't care whether this keyword is in a syntax valid style or not
				String identifier = new String(tmp, begin, end-begin);
				if(identifier.equals("int"))
					t = stream.new Token(KW_INT, begin, end, lineNumber);
				else if(identifier.equals("string"))
					t = stream.new Token(KW_STRING, begin, end, lineNumber);
				else if(identifier.equals("boolean"))
					t = stream.new Token(KW_BOOLEAN, begin, end, lineNumber);
				else if(identifier.equals("import"))
					t = stream.new Token(KW_IMPORT, begin, end, lineNumber);
				else if(identifier.equals("class"))
					t = stream.new Token(KW_CLASS, begin, end, lineNumber);
				else if(identifier.equals("def"))
					t = stream.new Token(KW_DEF, begin, end, lineNumber);
				else if(identifier.equals("while"))
					t = stream.new Token(KW_WHILE, begin, end, lineNumber);
				else if(identifier.equals("if"))
					t = stream.new Token(KW_IF, begin, end, lineNumber);
				else if(identifier.equals("else"))
					t = stream.new Token(KW_ELSE, begin, end, lineNumber);
				else if(identifier.equals("return"))
					t = stream.new Token(KW_RETURN, begin, end, lineNumber);
				else if(identifier.equals("print"))
					t = stream.new Token(KW_PRINT, begin, end, lineNumber);
				else if(identifier.equals("key"))
					t = stream.new Token(KW_KEY, begin, end, lineNumber);
				else if(identifier.equals("value"))
					t = stream.new Token(KW_VALUE, begin, end, lineNumber);
				else if(identifier.equals("size"))
					t = stream.new Token(KW_SIZE, begin, end, lineNumber);
				else if(identifier.equals("true"))
					t = stream.new Token(BL_TRUE, begin, end, lineNumber);
				else if(identifier.equals("false"))
					t = stream.new Token(BL_FALSE, begin, end, lineNumber);
				else if(identifier.equals("null"))
					t = stream.new Token(NL_NULL, begin, end, lineNumber);
				else//normal identifier
					t = stream.new Token(IDENT, begin, end, lineNumber);
				stream.tokens.add(t);
				
				//don't forget to change "index"
				index = end-1;
			}
			else if(Character.isDigit(tmp[index]))
			{
				Token t = null;
				begin = index;
				end = index + 1;
				if(tmp[index] == '0') //only 0
					t = stream.new Token(INT_LIT, begin, end, lineNumber);
				else //bigger than 0
				{
					while(end < length && Character.isDigit(tmp[end]))
						end++;
					t = stream.new Token(INT_LIT, begin, end, lineNumber);
				}
				index = end - 1;
				stream.tokens.add(t);
			}
			else //otherwise some punctuation 
			{
				Token t = null;
				begin = index;
				end = index + 1;
				switch(tmp[index])
				{	
					case '.': //could be DOT or RANGE
						if(end < length && tmp[end] == '.')//RANGE
						{
							end++;
							t = stream.new Token(RANGE, begin, end, lineNumber);
						}
						else //DOT
							t = stream.new Token(DOT, begin, end, lineNumber);
						index = end - 1;
						stream.tokens.add(t);
						break;
					case ';': //SEMICOLON
						t = stream.new Token(SEMICOLON, begin, end, lineNumber);
						index = end - 1;
						stream.tokens.add(t);
						break;
					case ',': //COMMA
						t = stream.new Token(COMMA, begin, end, lineNumber);
						index = end - 1;
						stream.tokens.add(t);
						break;
					case '(': //LPAREN
						t = stream.new Token(LPAREN, begin, end, lineNumber);
						index = end - 1;
						stream.tokens.add(t);
						break;
					case ')': //RPAREN
						t = stream.new Token(RPAREN, begin, end, lineNumber);
						index = end - 1;
						stream.tokens.add(t);
						break;
					case '[': //LSQUARE
						t = stream.new Token(LSQUARE, begin, end, lineNumber);
						index = end - 1;
						stream.tokens.add(t);
						break;
					case ']': //RSQUARE
						t = stream.new Token(RSQUARE, begin, end, lineNumber);
						index = end - 1;
						stream.tokens.add(t);
						break;
					case '{': //LCURLY
						t = stream.new Token(LCURLY, begin, end, lineNumber);
						index = end - 1;
						stream.tokens.add(t);
						break;
					case '}': //RCURLY
						t = stream.new Token(RCURLY, begin, end, lineNumber);
						index = end - 1;
						stream.tokens.add(t);
						break;
					case ':': //COLON
						t = stream.new Token(COLON, begin, end, lineNumber);
						index = end - 1;
						stream.tokens.add(t);
						break;
					case '?': //QUESTION
						t = stream.new Token(QUESTION, begin, end, lineNumber);
						index = end - 1;
						stream.tokens.add(t);
						break;
					case '=': //could be EQUAL or ASSIGN
						if(end < length && tmp[end] == '=')//EQUAL
						{
							end++;
							t = stream.new Token(EQUAL, begin, end, lineNumber);
						}
						else
							t = stream.new Token(ASSIGN, begin, end, lineNumber);
						index = end - 1;
						stream.tokens.add(t);
						break;
					case '|': //BAR
						t = stream.new Token(BAR, begin, end, lineNumber);
						index = end - 1;
						stream.tokens.add(t);
						break;
					case '&': //AND
						t = stream.new Token(AND, begin, end, lineNumber);
						index = end - 1;
						stream.tokens.add(t);
						break;
					case '!': //could be NOTEQUAL or NOT
						if(end < length && tmp[end] == '=')//NOTEQUAL
						{
							end++;
							t = stream.new Token(NOTEQUAL, begin, end, lineNumber);
						}
						else //NOT
							t = stream.new Token(NOT, begin, end, lineNumber);
						index = end - 1;
						stream.tokens.add(t);
						break;
					case '<': //could be LT or LE or LSHIFT
						if(end < length && tmp[end] == '=')//LE
						{
							end++;
							t = stream.new Token(LE, begin, end, lineNumber);
						}
						else if(end < length && tmp[end] == '<')//LSHIFT
						{
							end++;
							t = stream.new Token(LSHIFT, begin, end, lineNumber);
						}
						else //LT
							t = stream.new Token(LT, begin, end, lineNumber);
						index = end - 1;
						stream.tokens.add(t);
						break;
					case '>': //could be GT or GE or RSHIFT
						if(end < length && tmp[end] == '=')//GE
						{
							end++;
							t = stream.new Token(GE, begin, end, lineNumber);
						}
						else if(end < length && tmp[end] == '>')//RSHIFT
						{
							end++;
							t = stream.new Token(RSHIFT, begin, end, lineNumber);
						}
						else //GT
							t = stream.new Token(GT, begin, end, lineNumber);
						index = end - 1;
						stream.tokens.add(t);
						break;
					case '+': //PLUS
						t = stream.new Token(PLUS, begin, end, lineNumber);
						index = end - 1;
						stream.tokens.add(t);
						break;
					case '-': //could be ARROW or MINUS
						if(end < length && tmp[end] == '>')//ARROW
						{
							end++;
							t = stream.new Token(ARROW, begin, end, lineNumber);
						}
						else //MINUS
							t = stream.new Token(MINUS, begin, end, lineNumber);
						index = end - 1;
						stream.tokens.add(t);
						break;
					case '*': //TIMES
						t = stream.new Token(TIMES, begin, end, lineNumber);
						index = end - 1;
						stream.tokens.add(t);
						break;
					case '/': //could be DIV or Comment
						//comment. we should change "lineNumber" if necessary
						if(end < length && tmp[end] == '*')
						{
							int originalLineNumber = lineNumber;
							//now we try to omit characters till we encounter "*/"
							//the same time we should change lineNumber
							int first = end + 1, second = end + 2;
							while(second < length && !(tmp[first] == '*' && tmp[second] == '/'))
							{
								if(tmp[second] == '\r')
									lineNumber++;
								else if(tmp[second] == '\n' && tmp[first] != '\r')
									lineNumber++;
								first++;
								second++;
							}
							//either a correctly terminated comment or not
							if(second >= length) //UNTERMINATED_COMMENT
							{
								end = second;
								t = stream.new Token(UNTERMINATED_COMMENT, begin, length, originalLineNumber);
								stream.tokens.add(t);
							}
							else //we found a successful multi-line comment
								end = second + 1;
							index = end - 1;
						}
						else//DIV
						{
							t = stream.new Token(DIV, begin, end, lineNumber);
							index = end - 1;
							stream.tokens.add(t);
						}
						break;
					case '%': //MOD
						t = stream.new Token(MOD, begin, end, lineNumber);
						index = end - 1;
						stream.tokens.add(t);
						break;
					case '@': //AT
						t = stream.new Token(AT, begin, end, lineNumber);
						index = end - 1;
						stream.tokens.add(t);
						break;
					case '"': //the beginning of a string literal
						boolean foundRightMark = false;
						while(end < length && !foundRightMark)
						{
							if(tmp[end] == '"') //maybe this is a the matching right double mark
							{
								//we should handle cases when this double mark is a Escape character
								int travel = end - 1;
								while(travel > begin && tmp[travel] == '\\')
									travel--;
								int backSlashCount = end - travel - 1;
								if(backSlashCount%2 == 0) //matched right double quote
								{
									end++;
									t = stream.new Token(STRING_LIT, begin, end, lineNumber);
									index = end - 1;
									stream.tokens.add(t);
									foundRightMark = true;
									break;
								}//otherwise means this is a escape character, we need to go on
							}
							//string literal declaration cannot occupy multi-line
							/*
							else if(tmp[end] == '\r' || tmp[end] == '\n')
								break;
								*/
							end++;
						}
						//if after searching all input chars and found no right double quote
						if(!foundRightMark)
						{
							t = stream.new Token(UNTERMINATED_STRING, begin, end, lineNumber);
							index = end - 1;
							stream.tokens.add(t);
						}
						break;
					default: //illegal char
						t = stream.new Token(ILLEGAL_CHAR, begin, end, lineNumber);
						index = end - 1;
						stream.tokens.add(t);
				}
			}
		}
		//at last we should put in the EOF token
		Token t = stream.new Token(EOF, length, length, lineNumber);
		stream.tokens.add(t);
	}

}

