package cop5555sp15;

import static cop5555sp15.TokenStream.Kind.AND;
import static cop5555sp15.TokenStream.Kind.ARROW;
import static cop5555sp15.TokenStream.Kind.ASSIGN;
import static cop5555sp15.TokenStream.Kind.AT;
import static cop5555sp15.TokenStream.Kind.BAR;
import static cop5555sp15.TokenStream.Kind.BL_FALSE;
import static cop5555sp15.TokenStream.Kind.BL_TRUE;
import static cop5555sp15.TokenStream.Kind.COLON;
import static cop5555sp15.TokenStream.Kind.COMMA;
import static cop5555sp15.TokenStream.Kind.DIV;
import static cop5555sp15.TokenStream.Kind.DOT;
import static cop5555sp15.TokenStream.Kind.EOF;
import static cop5555sp15.TokenStream.Kind.EQUAL;
import static cop5555sp15.TokenStream.Kind.GE;
import static cop5555sp15.TokenStream.Kind.GT;
import static cop5555sp15.TokenStream.Kind.IDENT;
import static cop5555sp15.TokenStream.Kind.INT_LIT;
import static cop5555sp15.TokenStream.Kind.KW_BOOLEAN;
import static cop5555sp15.TokenStream.Kind.KW_CLASS;
import static cop5555sp15.TokenStream.Kind.KW_DEF;
import static cop5555sp15.TokenStream.Kind.KW_ELSE;
import static cop5555sp15.TokenStream.Kind.KW_IF;
import static cop5555sp15.TokenStream.Kind.KW_IMPORT;
import static cop5555sp15.TokenStream.Kind.KW_INT;
import static cop5555sp15.TokenStream.Kind.KW_PRINT;
import static cop5555sp15.TokenStream.Kind.KW_KEY;
import static cop5555sp15.TokenStream.Kind.KW_VALUE;
import static cop5555sp15.TokenStream.Kind.KW_SIZE;
import static cop5555sp15.TokenStream.Kind.KW_RETURN;
import static cop5555sp15.TokenStream.Kind.KW_STRING;
import static cop5555sp15.TokenStream.Kind.KW_WHILE;
import static cop5555sp15.TokenStream.Kind.LCURLY;
import static cop5555sp15.TokenStream.Kind.LE;
import static cop5555sp15.TokenStream.Kind.LPAREN;
import static cop5555sp15.TokenStream.Kind.LSHIFT;
import static cop5555sp15.TokenStream.Kind.LSQUARE;
import static cop5555sp15.TokenStream.Kind.LT;
import static cop5555sp15.TokenStream.Kind.MINUS;
import static cop5555sp15.TokenStream.Kind.MOD;
import static cop5555sp15.TokenStream.Kind.NOT;
import static cop5555sp15.TokenStream.Kind.NOTEQUAL;
import static cop5555sp15.TokenStream.Kind.PLUS;
import static cop5555sp15.TokenStream.Kind.RANGE;
import static cop5555sp15.TokenStream.Kind.RCURLY;
import static cop5555sp15.TokenStream.Kind.RPAREN;
import static cop5555sp15.TokenStream.Kind.RSHIFT;
import static cop5555sp15.TokenStream.Kind.RSQUARE;
import static cop5555sp15.TokenStream.Kind.SEMICOLON;
import static cop5555sp15.TokenStream.Kind.STRING_LIT;
import static cop5555sp15.TokenStream.Kind.TIMES;
import cop5555sp15.TokenStream.Kind;
import cop5555sp15.TokenStream.Token;


public class SimpleParser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;
		Kind[] expected;
		String msg;

		SyntaxException(Token t, Kind expected) {
			this.t = t;
			msg = "";
			this.expected = new Kind[1];
			this.expected[0] = expected;

		}

		public SyntaxException(Token t, String msg) {
			this.t = t;
			this.msg = msg;
		}

		public SyntaxException(Token t, Kind[] expected) {
			this.t = t;
			msg = "";
			this.expected = expected;
		}

		public String getMessage() {
			StringBuilder sb = new StringBuilder();
			sb.append(" error at token ").append(t.toString()).append(" ")
					.append(msg);
			sb.append(". Expected: ");
			for (Kind kind : expected) {
				sb.append(kind).append(" ");
			}
			return sb.toString();
		}
	}

	TokenStream tokens;
	Token t;

	SimpleParser(TokenStream tokens) {
		this.tokens = tokens;
		t = tokens.nextToken();
	}

	private Kind match(Kind kind) throws SyntaxException {
		if (isKind(kind)) {
			consume();
			return kind;
		}
		throw new SyntaxException(t, kind);
	}

	private Kind match(Kind... kinds) throws SyntaxException {
		Kind kind = t.kind;
		if (isKind(kinds)) {
			consume();
			return kind;
		}
		StringBuilder sb = new StringBuilder();
		for (Kind kind1 : kinds) {
			sb.append(kind1).append(kind1).append(" ");
		}
		throw new SyntaxException(t, "expected one of " + sb.toString());
	}

	private boolean isKind(Kind kind) {
		return (t.kind == kind);
	}

	private void consume() {
		if (t.kind != EOF)
			t = tokens.nextToken();
	}

	private boolean isKind(Kind... kinds) {
		for (Kind kind : kinds) {
			if (t.kind == kind)
				return true;
		}
		return false;
	}

	//This is a convenient way to represent fixed sets of
	//token kinds.  You can pass these to isKind.
	static final Kind[] REL_OPS = { BAR, AND, EQUAL, NOTEQUAL, LT, GT, LE, GE };
	static final Kind[] WEAK_OPS = { PLUS, MINUS };
	static final Kind[] STRONG_OPS = { TIMES, DIV };
	static final Kind[] VERY_STRONG_OPS = { LSHIFT, RSHIFT };
	static final Kind[] FIRST_SET_OF_STATEMENT = {IDENT, KW_PRINT, KW_WHILE, KW_IF, MOD, KW_RETURN};
	
	//<Expression>, <Term>, <Elem>, <Thing>, <Factor> all have the same first set as below
	//<RangeExpression> also have the same first set as <Expression>
	// predict set of "<ExpressionList> ::= <Expression> ( , <Expression> )* " is the same with below
	static final Kind[] FIRST_SET_OF_EXPRESSION = { IDENT, INT_LIT, BL_TRUE, BL_FALSE, STRING_LIT, LPAREN, NOT, MINUS, KW_SIZE, KW_KEY, KW_VALUE, LCURLY, AT};
	
	
	public void parse() throws SyntaxException {
		Program();
		match(EOF);
	}

	private void Program() throws SyntaxException {
		ImportList();
		match(KW_CLASS);
		match(IDENT);
		Block();
	}

	private void ImportList() throws SyntaxException {
		while(isKind(KW_IMPORT))
		{
			consume();
			match(IDENT);
			while(isKind(DOT))
			{
				consume();
				match(IDENT);
			}
			match(SEMICOLON);
		}
	}

	private void Block() throws SyntaxException {
		match(LCURLY);
		//first set of <Declaration> union <Statement>
		Kind[] kinds = {KW_DEF, IDENT, KW_PRINT, KW_WHILE, KW_IF, MOD, KW_RETURN, SEMICOLON};
		while(isKind(kinds))
		{
			if(isKind(KW_DEF)) //declaration
				Declaration();
			else if(isKind(IDENT, KW_PRINT, KW_WHILE, KW_IF, MOD, KW_RETURN))//otherwise statement
				Statement();
			match(SEMICOLON);
		}
		match(RCURLY);
	}

	private void Declaration() throws SyntaxException{
		match(KW_DEF);
		//then since first set of <VarDec> and <ClosureDec> are joint
		//we have to do extra matching instead of directly call VarDec() or ClosureDec()
		match(IDENT);
		if(isKind(ASSIGN))//thought it is <ClosureDec>
		{
			consume();
			Closure();
		}
		else if(isKind(COLON))//thought it is <VarDec>
		{
			consume();
			Type();
		}
		else  //still a <VarDec> but its identifier has no type
			return;
	}

	private void VarDec() throws SyntaxException{
		match(IDENT);
		if(isKind(COLON))
		{
			consume();
			Type();
		}
	}
	
	private void Type() throws SyntaxException{
		//since predict set of <SimpleType>, <KeyValueType>, <ListType> are joint
		//we cannot directly call SimpleType() or KeyValueType() or ListType()
		
		//predict set of <Type>::=<SimpleType> is {KW_INT, KW_STRING, KW_BOOLEAN}
		if(isKind(KW_INT, KW_STRING, KW_BOOLEAN))
			SimpleType();
		else //treat as <KeyValueType> or <ListType>
		{
			match(AT);
			if(isKind(AT))//it is <KeyValueType>
			{
				consume();
				match(LSQUARE);
				SimpleType();
				match(COLON);
				Type();
				match(RSQUARE);
			}
			else//otherwise we treat it as <ListType>
			{
				match(LSQUARE);
				Type();
				match(RSQUARE);
			}
		}
	}
	
	private void SimpleType() throws SyntaxException{
		match(KW_INT, KW_STRING, KW_BOOLEAN);
	}
	/*
	private void KeyValueType() throws SyntaxException{
		match(AT);
		match(AT);
		match(LSQUARE);
		SimpleType();
		match(COLON);
		Type();
		match(RSQUARE);
	}
	
	private void ListType() throws SyntaxException{
		match(AT);
		match(LSQUARE);
		Type();
		match(RSQUARE);
	}
	
	private void ClosureDec() throws SyntaxException{
		match(IDENT);
		match(ASSIGN);
		Closure();
	}*/
	
	private void Closure() throws SyntaxException{
		match(LCURLY);
		FormalArgList();
		match(ARROW);
		while(isKind(FIRST_SET_OF_STATEMENT))
		{
			Statement();
			match(SEMICOLON);
		}
		match(RCURLY);
	}
	
	private void FormalArgList() throws SyntaxException{
		if(isKind(IDENT))
		{
			VarDec();
			while(isKind(COMMA))
			{
				consume();
				VarDec();
			}
		}
	}
	
	private void Statement() throws SyntaxException{
		switch(t.kind)
		{
		case IDENT://LValue
			LValue();
			match(ASSIGN);
			Expression();
			break;
		case KW_PRINT:
			consume();
			Expression();
			break;
		case KW_WHILE: //there have three options
			consume();
			if(isKind(LPAREN))
			{
				consume();
				Expression();
			}
			else//otherwise 
			{
				match(TIMES);
				match(LPAREN);
				Expression();
				if(isKind(RANGE))
				{
					consume();
					Expression();
				}
			}
			match(RPAREN);
			Block();
			break;
		case KW_IF:
			consume();
			match(LPAREN);
			Expression();
			match(RPAREN);
			Block();
			if(isKind(KW_ELSE))
			{
				consume();
				Block();
			}
			break;
		case MOD:
			consume();
			Expression();
			break;
		case KW_RETURN:
			consume();
			Expression();
			break;
		default: return;
		}
	}
	/*
	private void ClosureEvalExpression() throws SyntaxException{
		match(IDENT);
		match(LPAREN);
		ExpressionList();
		match(RPAREN);
	}
	*/
	private void LValue() throws SyntaxException{
		match(IDENT);
		if(isKind(LSQUARE))
		{
			consume();
			Expression();
			match(RSQUARE);
		}
	}
	/*
	private void List() throws SyntaxException{
		match(AT);
		match(LSQUARE);
		ExpressionList();
		match(RSQUARE);
	}
	*/
	private void ExpressionList() throws SyntaxException{
		if(isKind(FIRST_SET_OF_EXPRESSION))
		{
			Expression();
			while(isKind(COMMA))
			{
				consume();
				Expression();
			}
		}
	}
	
	private void KeyValueExpression() throws SyntaxException{
		Expression();
		match(COLON);
		Expression();
	}
	
	private void KeyValueList() throws SyntaxException{
		if(isKind(FIRST_SET_OF_EXPRESSION))
		{
			KeyValueExpression();
			while(isKind(COMMA))
			{
				consume();
				KeyValueExpression();
			}
		}
	}
	/*
	private void MapList() throws SyntaxException{
		match(AT);
		match(AT);
		match(LSQUARE);
		KeyValueList();
		match(RSQUARE);
	}
	
	private void RangeExpression() throws SyntaxException{
		Expression();
		match(RANGE);
		Expression();
	}
	*/
	private void Expression() throws SyntaxException{
		Term();
		while(isKind(REL_OPS))
		{
			RelOp();
			Term();
		}
	}
	
	private void Term() throws SyntaxException{
		Elem();
		while(isKind(WEAK_OPS))
		{
			WeakOp();
			Elem();
		}
	}
	
	private void Elem() throws SyntaxException{
		Thing();
		while(isKind(STRONG_OPS))
		{
			StrongOp();
			Thing();
		}
	}
	
	private void Thing() throws SyntaxException{
		Factor();
		while(isKind(VERY_STRONG_OPS))
		{
			VeryStrongOp();
			Factor();
		}
	}
	
	private void Factor() throws SyntaxException{
		switch(t.kind)
		{
		case IDENT: //could be any one in "IDENT | IDENT [ <Expression> ] | <ClosureEvalExpression>"
			consume();
			if(isKind(LSQUARE)) //is IDENT[<Expression>]
			{
				consume();
				Expression();
				match(RSQUARE);
			}
			else if(isKind(LPAREN)) // is <ClosureEvalExpression>
			{
				consume();
				ExpressionList();
				match(RPAREN);
			}
			break;
		case INT_LIT:
		case STRING_LIT:
		case BL_TRUE:
		case BL_FALSE:
			consume();
			break;
		case KW_KEY:
		case KW_VALUE:
		case KW_SIZE:
			consume();
			match(LPAREN);
			Expression();
			match(RPAREN);
			break;
		case LPAREN:
			consume();
			Expression();
			match(RPAREN);
			break;
		case NOT:
		case MINUS:
			consume();
			Factor();
			break;
		case LCURLY:
			Closure();
			break;
		case AT: //could be <List> or <MapList>
			consume();
			if(isKind(AT)) //<MapList>
			{
				consume();
				match(LSQUARE);
				KeyValueList();
				match(RSQUARE);
			}
			else if(isKind(LSQUARE))
			{
				consume();
				ExpressionList();
				match(RSQUARE);
			}
			else
				throw new SyntaxException(t, "expected @ or [");
			break;
		default:
				StringBuilder sb = new StringBuilder();
				for (Kind kind1 : FIRST_SET_OF_EXPRESSION) {
					sb.append(kind1).append(kind1).append(" ");
				}
				throw new SyntaxException(t, "expected one of " + sb.toString());
		}
	}
	
	private void RelOp() throws SyntaxException{
		match(REL_OPS);
	}
	
	private void WeakOp() throws SyntaxException{
		match(WEAK_OPS);
	}
	
	private void StrongOp() throws SyntaxException{
		match(STRONG_OPS);
	}
	
	private void VeryStrongOp() throws SyntaxException{
		match(VERY_STRONG_OPS);
	}
}
