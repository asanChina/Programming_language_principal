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

import java.util.ArrayList;
import java.util.List;

import cop5555sp15.TokenStream.Kind;
import cop5555sp15.TokenStream.Token;
import cop5555sp15.ast.*;



public class Parser {

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

	Parser(TokenStream tokens) {
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
	
	
	public String getErrors(){
		StringBuilder sb = new StringBuilder();
		for(SyntaxException e : exceptionList)
			sb.append(e.msg).append('\n');
		return sb.toString();
	}
	
	public List<SyntaxException> getExceptionList(){
		return exceptionList;
	}
	
	public Program parse(){
		Program p = null;
		try{
			p = Program();
			if( p != null)
				match(EOF);
		}catch (SyntaxException e){
			exceptionList.add(e);
		}
		if(exceptionList.isEmpty())
			return p;
		else
			return null;
	}

	List<SyntaxException> exceptionList = new ArrayList<SyntaxException>();
	
	private Program Program() throws SyntaxException {

		List<QualifiedName> importList = ImportList();
		match(KW_CLASS);
		String name = new String(t.getText());
		Token firstToken = importList.size()==0?t:importList.get(0).firstToken;
		match(IDENT);
		Block block = Block();
		return new Program(firstToken, importList, name, block);
	}

	private List<QualifiedName> ImportList() throws SyntaxException {
		List<QualifiedName> result = new ArrayList<QualifiedName>();
		
		while(isKind(KW_IMPORT))
		{
			Token firstToken = t;
			consume();
			StringBuilder sb = new StringBuilder();
			sb.append(t.getText());
			match(IDENT);
			while(isKind(DOT))
			{
				sb.append('/');
				consume();
				sb.append(t.getText());
				match(IDENT);
			}
			match(SEMICOLON);
			QualifiedName qn = new QualifiedName(firstToken, sb.toString());
			result.add(qn);
		}
		return result;
	}

	
	private Block Block() throws SyntaxException {
		Token firstToken = t;
		match(LCURLY);
		//first set of <Declaration> union <Statement>
		Kind[] kinds = {KW_DEF, IDENT, KW_PRINT, KW_WHILE, KW_IF, MOD, KW_RETURN, SEMICOLON};
		List<BlockElem> elems = new ArrayList<BlockElem>();
		while(isKind(kinds))
		{
			if(isKind(KW_DEF)) //declaration
				elems.add(Declaration());
			else if(isKind(IDENT, KW_PRINT, KW_WHILE, KW_IF, MOD, KW_RETURN))//otherwise statement
				elems.add(Statement());
			match(SEMICOLON);
		}
		match(RCURLY);
		return new Block(firstToken, elems);
	}

	
	private Declaration Declaration() throws SyntaxException{
		Token firstToken = t;
		Declaration result = null;
		match(KW_DEF);
		//then since first set of <VarDec> and <ClosureDec> are joint
		//we have to do extra matching instead of directly call VarDec() or ClosureDec()
		Token identToken = t;
		match(IDENT);
		if(isKind(ASSIGN))//thought it is <ClosureDec>
		{
			consume();
			result = new ClosureDec(firstToken, identToken, Closure());
		}
		else if(isKind(COLON))//thought it is <VarDec>
		{
			consume();
			result = new VarDec(firstToken, identToken, Type());
		}
		else//still a <VarDec> but its identifier has no type
		{
			System.out.println("here in Declaration: undeclared type");
			result = new VarDec(firstToken, identToken, new UndeclaredType(identToken));
		}
		return result;
	}

	private VarDec VarDec() throws SyntaxException{
		Token firstToken = t;
		Token identToken = t;
		VarDec result = null;
		match(IDENT);
		if(isKind(COLON))
		{
			consume();
			result = new VarDec(firstToken, identToken, Type());
		}
		else
			result = new VarDec(firstToken, identToken, new UndeclaredType(identToken));
		return result;
	}
	
	
	private Type Type() throws SyntaxException{
		//since predict set of <SimpleType>, <KeyValueType>, <ListType> are joint
		//we cannot directly call SimpleType() or KeyValueType() or ListType()
	
		//predict set of <Type>::=<SimpleType> is {KW_INT, KW_STRING, KW_BOOLEAN}
		Type result = null;
		Token firstToken = t;
		if(isKind(KW_INT, KW_STRING, KW_BOOLEAN))
		{
			result = SimpleType();
		}
		else //treat as <KeyValueType> or <ListType>
		{
			match(AT);
			if(isKind(AT))//it is <KeyValueType>
			{
				consume();
				match(LSQUARE);
				SimpleType keyType = SimpleType();
				match(COLON);
				Type valueType = Type();
				match(RSQUARE);
				result = new KeyValueType(firstToken, keyType, valueType);
			}
			else//otherwise we treat it as <ListType>
			{
				match(LSQUARE);
				result = new ListType(firstToken, Type());
				match(RSQUARE);
			}
		}
		return result;
	}
	
	
	private SimpleType SimpleType() throws SyntaxException{
		SimpleType result = new SimpleType(t, t);
		match(KW_INT, KW_STRING, KW_BOOLEAN);
		return result;
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
	
	private Closure Closure() throws SyntaxException{
		Closure result = null;
		Token firstToken = t;
		
		match(LCURLY);
		List<VarDec> formalArgList = FormalArgList();
		match(ARROW);
		List<Statement> statementList = new ArrayList<Statement>();
		while(isKind(FIRST_SET_OF_STATEMENT))
		{
			statementList.add(Statement());
			match(SEMICOLON);
		}
		match(RCURLY);
		result = new Closure(firstToken, formalArgList, statementList);
		return result;
	}
	
	
	private List<VarDec> FormalArgList() throws SyntaxException{
		List<VarDec> result = new ArrayList<VarDec>();
		if(isKind(IDENT))
		{
			result.add(VarDec());
			while(isKind(COMMA))
			{
				consume();
				result.add(VarDec());
			}
		}
		return result;
	}
	
	
	private Statement Statement() throws SyntaxException{
		Token firstToken = t;
		Statement result = null;
		Expression expression = null;
		
		switch(t.kind)
		{
		case IDENT://LValue
			LValue lvalue = LValue();
			match(ASSIGN);
			expression = Expression();
			result = new AssignmentStatement(firstToken, lvalue, expression);
			break;
		case KW_PRINT:
			consume();
			expression = Expression();
			result = new PrintStatement(firstToken, expression);
			break;
		case KW_WHILE: //there have three options
			consume();
			if(isKind(LPAREN))
			{
				consume();
				expression = Expression();
				match(RPAREN);
				result = new WhileStatement(firstToken, expression, Block());
			}
			else//otherwise 
			{
				match(TIMES);
				match(LPAREN);
				expression = Expression();
				if(isKind(RANGE))
				{
					consume();
					Expression expression1 = Expression();
					RangeExpression rangeExpression = new RangeExpression(firstToken, expression, expression1);
					match(RPAREN);
					result = new WhileRangeStatement(firstToken, rangeExpression, Block());
				}
				else
				{
					match(RPAREN);
					result = new WhileStarStatement(firstToken, expression, Block());
				}
			}
			break;
		case KW_IF:
			consume();
			match(LPAREN);
			expression = Expression();
			match(RPAREN);
			Block ifBlock = Block();
			if(isKind(KW_ELSE))
			{
				consume();
				result = new IfElseStatement(firstToken, expression, ifBlock, Block());
			}
			else
			{
				result = new IfStatement(firstToken, expression, ifBlock);
			}
			break;
		case MOD:
			consume();
			result = new ExpressionStatement(firstToken, Expression());
			break;
		case KW_RETURN:
			consume();
			result = new ReturnStatement(firstToken, Expression());
			break;
		default: return null;
		}
		return result;
	}
	/*
	private void ClosureEvalExpression() throws SyntaxException{
		match(IDENT);
		match(LPAREN);
		ExpressionList();
		match(RPAREN);
	}
	*/
	
	private LValue LValue() throws SyntaxException{
		Token firstToken = t;
		Token identToken = t;
		Expression expression;
		match(IDENT);
		if(isKind(LSQUARE))
		{
			consume();
			expression = Expression();
			match(RSQUARE);
			return new ExpressionLValue(firstToken, identToken, expression);
		}
		else
			return new IdentLValue(firstToken, identToken);
	}
	
	/*
	private void List() throws SyntaxException{
		match(AT);
		match(LSQUARE);
		ExpressionList();
		match(RSQUARE);
	}
	*/
	private List<Expression> ExpressionList() throws SyntaxException{
		List<Expression> expressionList = new ArrayList<Expression>();
		
		if(isKind(FIRST_SET_OF_EXPRESSION))
		{
			expressionList.add(Expression());
			while(isKind(COMMA))
			{
				consume();
				expressionList.add(Expression());
			}
		}
		return expressionList;
	}
	
	
	private KeyValueExpression KeyValueExpression() throws SyntaxException{
		Token firstToken = t;
		Expression key = Expression();
		match(COLON);
		Expression value = Expression();
		return new KeyValueExpression(firstToken, key, value);
	}
	
	
	private MapListExpression KeyValueList() throws SyntaxException{
		List<KeyValueExpression> mapList = new ArrayList<KeyValueExpression>();
		Token firstToken = t;
		
		if(isKind(FIRST_SET_OF_EXPRESSION))
		{
			mapList.add(KeyValueExpression());
			while(isKind(COMMA))
			{
				consume();
				mapList.add(KeyValueExpression());
			}
		}
		return new MapListExpression(firstToken, mapList);
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
	private Expression Expression() throws SyntaxException{
		Token firstToken = t;
		Expression expression0 = null;
		Expression expression1 = null;
		expression0 = Term();
		while(isKind(REL_OPS))
		{
			Token op = t;
			RelOp();
			expression1 = Term();
			expression0 = new BinaryExpression(firstToken, expression0, op, expression1);
		}
		return expression0;
	}
	
	
	private Expression Term() throws SyntaxException{
		Token firstToken = t;
		Expression expression0 = null;
		Expression expression1 = null;
		expression0 = Elem();
		while(isKind(WEAK_OPS))
		{
			Token op = t;
			WeakOp();
			expression1 = Elem();
			expression0 = new BinaryExpression(firstToken, expression0, op, expression1);
		}
		return expression0;
	}
	
	
	private Expression Elem() throws SyntaxException{
		Expression expression0 = null;
		Expression expression1 = null;
		
		Token firstToken = t;
		expression0 = Thing();
		while(isKind(STRONG_OPS))
		{
			Token op = t;
			StrongOp();
			expression1 = Thing();
			expression0 = new BinaryExpression(firstToken, expression0, op, expression1);
		}
		return expression0;
	}
	
	
	private Expression Thing() throws SyntaxException{
		Expression expression0 = null;
		Expression expression1 = null;
		Token firstToken = t;
		expression0 = Factor();
		while(isKind(VERY_STRONG_OPS))
		{
			Token op = t;
			VeryStrongOp();
			expression1 = Factor();
			expression0 = new BinaryExpression(firstToken, expression0, op, expression1);
		}
		return expression0;
	}
	
	
	private Expression Factor() throws SyntaxException{
		Expression result = null;
		Token firstToken = t;
		Expression expression;
		
		switch(t.kind)
		{
		case IDENT: //could be any one in "IDENT | IDENT [ <Expression> ] | <ClosureEvalExpression>"
			Token identToken = t;
			consume();
			if(isKind(LSQUARE)) //is IDENT[<Expression>]
			{
				consume();
				expression = Expression();
				match(RSQUARE);
				result = new ListOrMapElemExpression(firstToken, identToken, expression);
			}
			else if(isKind(LPAREN)) // is <ClosureEvalExpression>
			{
				consume();
				List<Expression> expressionList = ExpressionList();
				match(RPAREN);
				result = new ClosureEvalExpression(firstToken, identToken, expressionList);
			}
			else
			{
				result = new IdentExpression(firstToken, identToken);
			}
			break;
		case INT_LIT: result = new IntLitExpression(firstToken, firstToken.getIntVal()); consume(); break;
		case STRING_LIT: result = new StringLitExpression(firstToken, firstToken.getText()); consume(); break;
		case BL_TRUE: result = new BooleanLitExpression(firstToken, firstToken.getBooleanVal()); consume(); break;
		case BL_FALSE: result = new BooleanLitExpression(firstToken, firstToken.getBooleanVal()); consume(); break;
		case KW_KEY: 
			consume();
			match(LPAREN);
			expression = Expression();
			match(RPAREN);
			result = new KeyExpression(firstToken, expression);
			break;
		case KW_VALUE:
			consume();
			match(LPAREN);
			expression = Expression();
			match(RPAREN);
			result = new ValueExpression(firstToken, expression);
			break;
		case KW_SIZE:
			consume();
			match(LPAREN);
			expression = Expression();
			match(RPAREN);
			result = new SizeExpression(firstToken, expression);
			break;
		case LPAREN:
			consume();
			expression = Expression();
			match(RPAREN);
			result = expression;
			break;
		case NOT:
		case MINUS:
			Token op = t;
			consume();
			result = new UnaryExpression(firstToken, op, Factor());
			break;
		case LCURLY:
			result = new ClosureExpression(firstToken, Closure());
			break;
		case AT: //could be <List> or <MapList>
			consume();
			if(isKind(AT)) //<MapList>
			{
				consume();
				match(LSQUARE);
				result = KeyValueList();
				match(RSQUARE);
			}
			else if(isKind(LSQUARE))
			{
				consume();
				result = new ListExpression(firstToken, ExpressionList());
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
		return result;
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
