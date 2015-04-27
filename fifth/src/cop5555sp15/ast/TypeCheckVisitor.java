package cop5555sp15.ast;

//import org.objectweb.asm.Label;

import java.util.ArrayList;

import cop5555sp15.TypeConstants;
import cop5555sp15.symbolTable.SymbolTable;

public class TypeCheckVisitor implements ASTVisitor, TypeConstants {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		ASTNode node;

		public TypeCheckException(String message, ASTNode node) {
			super(node.firstToken.lineNumber + ":" + message);
			this.node = node;
		}
	}

	SymbolTable symbolTable;

	public TypeCheckVisitor(SymbolTable symbolTable) {
		this.symbolTable = symbolTable;
	}

	boolean check(boolean condition, String message, ASTNode node)
			throws TypeCheckException {
		if (condition)
			return true;
		throw new TypeCheckException(message, node);
	}

	/**
	 * Ensure that types on left and right hand side are compatible.
	 */
	@Override
	public Object visitAssignmentStatement(
			AssignmentStatement assignmentStatement, Object arg)
			throws Exception {
		//System.out.println("in typecheckvisitor.java: visitAssignmentStatement");
		String leftType = (String)assignmentStatement.lvalue.visit(this, arg);
		String rightType = (String)assignmentStatement.expression.visit(this, arg);
		//System.out.println("in typecheckvisitor.java: visitAssignmentStatement" + ", " + leftType);
		//System.out.println("in typecheckvisitor.java: visitAssignmentStatement" + ", " + rightType);
		if(!leftType.equals(rightType))
		{
			if(leftType.length() < 15 || rightType.length() < 15)
				check(false, "incompatible type", assignmentStatement);
			String s1 = leftType.substring(0, 15);
			String s2 = rightType.substring(0, 15);
			String s3 = rightType.substring(0, 20);
			if(s1.equals(s2) || s1.equals("Ljava/util/List")&&s3.equals("Ljava/util/ArrayList"))
				return leftType;
			check(false, "incompatible type", assignmentStatement);
		}
		return leftType;
	}

	/**
	 * Ensure that both types are the same, save and return the result type
	 */
	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression,
			Object arg) throws Exception {
		//System.out.println("in TypeCheckVisitor.java: visitBinaryExpression");
		String type1 = (String)binaryExpression.expression0.visit(this, arg);
		String type2 = (String)binaryExpression.expression1.visit(this, arg);
		
		String resultType = "";
		check(type1.equals(type2), "the types of these two sub expression are not the same", binaryExpression);
		
		switch(binaryExpression.op.kind)
		{
		case PLUS: 
			if(binaryExpression.expression0.getType().equals(intType))
				resultType = intType;
			else if(binaryExpression.expression0.getType().equals(stringType))
				resultType = stringType;
			break;
		case MINUS: 
		case TIMES: 
		case DIV: resultType = intType; break;
		case AND: 
		case BAR:  
			{
				String type = binaryExpression.expression0.getType();
				check(type.equals(booleanType), "not boolean type", binaryExpression); 
				resultType = booleanType; 
			}
			break;
		case EQUAL: 
		case NOTEQUAL: 
			{
				String type = binaryExpression.expression0.getType();
				check(type.equals(intType) || type.equals(stringType) || type.equals(booleanType), "invalid type", binaryExpression);
				resultType = booleanType;
			}
			break;
		case LT: 
		case LE: 
		case GT: 
		case GE:
		{
			String type = binaryExpression.expression0.getType();
			check(type.equals(intType), "need to be int type", binaryExpression); 
			resultType = booleanType; 
		}
		break;
		default: check(false, "unknown type", binaryExpression);
		}
		binaryExpression.setType(resultType); 
		return resultType;
	}

	/**
	 * Blocks define scopes. Check that the scope nesting level is the same at
	 * the end as at the beginning of block
	 */
	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		//System.out.println("in TypeCheckVisitor.java: visitBlock");
		int numScopes = symbolTable.enterScope();
		// visit children
		for (BlockElem elem : block.elems) {
			elem.visit(this, arg);
		}
		int numScopesExit = symbolTable.leaveScope();
		check(numScopesExit > 0 && numScopesExit == numScopes,
				"unbalanced scopes", block);
		return null;
	}

	/**
	 * Sets the expressionType to booleanType and returns it
	 * 
	 * @param booleanLitExpression
	 * @param arg
	 * @return
	 * @throws Exception
	 */
	@Override
	public Object visitBooleanLitExpression(
			BooleanLitExpression booleanLitExpression, Object arg)
			throws Exception {
		//System.out.println("in TypeCheckVisitor.java: visitBooleanLitExpression");
		booleanLitExpression.setType(booleanType);
		return booleanType;
	}

	/**
	 * A closure defines a new scope Visit all the declarations in the
	 * formalArgList, and all the statements in the statementList construct and
	 * set the JVMType, the argType array, and the result type
	 * 
	 * @param closure
	 * @param arg
	 * @return
	 * @throws Exception
	 */
	@Override
	public Object visitClosure(Closure closure, Object arg) throws Exception {
		throw new UnsupportedOperationException("not yet implemented");
	}

	/**
	 * Make sure that the name has not already been declared and insert in
	 * symbol table. Visit the closure
	 */
	@Override
	public Object visitClosureDec(ClosureDec closureDec, Object arg) {
		throw new UnsupportedOperationException("not yet implemented");
	}

	/**
	 * Check that the given name is declared as a closure Check the argument
	 * types The type is the return type of the closure
	 */
	@Override
	public Object visitClosureEvalExpression(
			ClosureEvalExpression closureExpression, Object arg)
			throws Exception {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitClosureExpression(ClosureExpression closureExpression,
			Object arg) throws Exception {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitExpressionLValue(ExpressionLValue expressionLValue,
			Object arg) throws Exception {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitExpressionStatement(
			ExpressionStatement expressionStatement, Object arg)
			throws Exception {
		throw new UnsupportedOperationException("not yet implemented");
	}

	/**
	 * Check that name has been declared in scope Get its type from the
	 * declaration.
	 * 
	 */
	@Override
	public Object visitIdentExpression(IdentExpression identExpression,
			Object arg) throws Exception {
		//System.out.println("in TypeCheckVisitor.java: visitIdentExpression");
		Declaration dec =  symbolTable.lookup(identExpression.identToken.getText());
		check(dec!=null, "variable is not declared", identExpression);
		identExpression.setType(((VarDec)dec).type.getJVMType());
		return ((VarDec)dec).type.getJVMType();
	}

	@Override
	public Object visitIdentLValue(IdentLValue identLValue, Object arg)
			throws Exception {
		//System.out.println("in TypeCheckVisitor.java: visitIdentLValue");
		Declaration dec =  symbolTable.lookup(identLValue.identToken.getText());
		check(dec!=null, "variable is not declared", identLValue);
		identLValue.setType(((VarDec)dec).type.getJVMType());
		return ((VarDec)dec).type.getJVMType();
	}

	@Override
	public Object visitIfElseStatement(IfElseStatement ifElseStatement,
			Object arg) throws Exception {
		//System.out.println("in TyepCheckVisitor.java: visitIfElseStatement");
		String type = (String)ifElseStatement.expression.visit(this, arg);
		check(type.equals(booleanType), "boolean type needed", ifElseStatement);
		ifElseStatement.ifBlock.visit(this, arg);
		ifElseStatement.elseBlock.visit(this, arg);
		return true;
	}

	/**
	 * expression type is boolean
	 */
	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg)
			throws Exception {
		//System.out.println("in TyepCheckVisitor.java: visitIfStatement");
		String type = (String)ifStatement.expression.visit(this, arg);
		check(type.equals(booleanType), "boolean type needed", ifStatement);
		ifStatement.block.visit(this, arg);
		return true;
	}

	/**
	 * expression type is int
	 */
	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression,
			Object arg) throws Exception {
		//System.out.println("in TypeCheckVisitor.java: visitIntLitExpression");
		intLitExpression.setType(intType);
		return intType;
	}

	@Override
	public Object visitKeyExpression(KeyExpression keyExpression, Object arg)
			throws Exception {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitKeyValueExpression(
			KeyValueExpression keyValueExpression, Object arg) throws Exception {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitKeyValueType(KeyValueType keyValueType, Object arg)
			throws Exception {
		throw new UnsupportedOperationException("not yet implemented");
	}

	// visit the expressions (children) and ensure they are the same type
	// the return type is "Ljava/util/ArrayList<"+type0+">;" where type0 is the
	// type of elements in the list
	// this should handle lists of lists, and empty list. An empty list is
	// indicated by "Ljava/util/ArrayList;".
	@Override
	public Object visitListExpression(ListExpression listExpression, Object arg)
			throws Exception {
		//System.out.println("in typeCheckVisitor.java: visitListExpression");
		String resultType = "";
		ArrayList<String> types = new ArrayList<String>();
		for(Expression e : listExpression.expressionList)
			types.add((String)e.visit(this, arg));
		
		for(int i = 0; i < types.size()-1; ++i)
			check(types.get(i).equals(types.get(i+1)), " incompatible type", listExpression);
	
		if(types.size() == 0)
			resultType = emptyList;
		else
			resultType = "Ljava/util/ArrayList<"+ types.get(0)+">;";
		
		listExpression.setType(resultType);
		return resultType;
	}

	/** gets the type from the enclosed expression */
	@Override
	public Object visitListOrMapElemExpression(
			ListOrMapElemExpression listOrMapElemExpression, Object arg)
			throws Exception {
		//System.out.println("in TypeCheckVisitor.java: visitListOrMapElemExpression");
		
		String ident = listOrMapElemExpression.identToken.getText();
		String type = (String)listOrMapElemExpression.expression.visit(this, arg);
		String resultType = "";
		String wholeType = "";
		Declaration dec =  symbolTable.lookup(ident);
		check(dec!=null, "variable is not declared", listOrMapElemExpression);
		
		if( ((VarDec)dec).type.getClass().getName().contains("ListType")  )
		{
			check(type.equals(intType), "int type needed", listOrMapElemExpression);
			resultType = ((ListType)((VarDec)dec).type).type.getJVMType();
			wholeType = "Ljava/util/List<"+resultType+">;";
			//System.out.println("in typeCheckVisitor.java: visitListOrMapElemExpression, resultType is " + resultType);
		}
		else if( ((VarDec)dec).type.getClass().getName().contains("KeyValueType")  )
		{
			
		}

		listOrMapElemExpression.setType(wholeType);
		return resultType;
	}

	@Override
	public Object visitListType(ListType listType, Object arg) throws Exception {
		//System.out.println("in TypeCheckVisitor.java: visitListType");
		listType.type.visit(this, arg);
		return listType.getJVMType();
	}

	@Override
	public Object visitMapListExpression(MapListExpression mapListExpression,
			Object arg) throws Exception {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitPrintStatement(PrintStatement printStatement, Object arg)
			throws Exception {
		//System.out.println("in TypeCheckVisitor.java: visitPrintStatement");
		printStatement.expression.visit(this, null);
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		//System.out.println("in TypeCheckVisitor.java: visitProgram");
		if (arg == null) {
			program.JVMName = program.name;
		} else {
			program.JVMName = arg + "/" + program.name;
		}
		// ignore the import statement
		if (!symbolTable.insert(program.name, null)) {
			throw new TypeCheckException("name already in symbol table",
					program);
		}
		program.block.visit(this, true);
		return null;
	}

	@Override
	public Object visitQualifiedName(QualifiedName qualifiedName, Object arg) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Checks that both expressions have type int.
	 * 
	 * Note that in spite of the name, this is not in the Expression type
	 * hierarchy.
	 */
	@Override
	public Object visitRangeExpression(RangeExpression rangeExpression,
			Object arg) throws Exception {
		throw new UnsupportedOperationException("not yet implemented");
	}

	// nothing to do here
	@Override
	public Object visitReturnStatement(ReturnStatement returnStatement,
			Object arg) throws Exception {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitSimpleType(SimpleType simpleType, Object arg)
			throws Exception {
		//System.out.println("in TypeCheckVisitor.java: visitSimpleType");
			return simpleType.getJVMType();
	}

	@Override
	public Object visitSizeExpression(SizeExpression sizeExpression, Object arg)
			throws Exception {
		//System.out.println("in typeCheckVisitor.java: visitSizeExpression");
		sizeExpression.expression.visit(this, arg);
		sizeExpression.setType(intType);
		return intType;
	}

	@Override
	public Object visitStringLitExpression(
			StringLitExpression stringLitExpression, Object arg)
			throws Exception {
		//System.out.println("in TypeCheckVisitor.java: visitStringLitExpression");
		stringLitExpression.setType(stringType);
		return stringType;
	}

	/**
	 * if ! and boolean, then boolean else if - and int, then int else error
	 */
	@Override
	public Object visitUnaryExpression(UnaryExpression unaryExpression,
			Object arg) throws Exception {
		//System.out.println("in TypeCheckVisitor.java: visitUnaryExpression");
		String resultType = "";
		switch(unaryExpression.op.kind)
		{
		case NOT: 
		{
			String type = (String)unaryExpression.expression.visit(this, arg);
			if(type.equals(booleanType))
				resultType = booleanType;
			else
				check(false, "boolean type needed", unaryExpression);
		}
			break;
		case MINUS: 
		{
			String type = (String)unaryExpression.expression.visit(this, arg);
			if(type.equals(intType))
				resultType = intType;
			else
				check(false, "int type needed", unaryExpression);
		}
			break;
		default: check(false, "unknow type", unaryExpression);
			break;
		}
		unaryExpression.setType(resultType);
		return resultType;
	}

	@Override
	public Object visitUndeclaredType(UndeclaredType undeclaredType, Object arg)
			throws Exception {
		throw new UnsupportedOperationException(
				"undeclared types not supported");
	}

	@Override
	public Object visitValueExpression(ValueExpression valueExpression,
			Object arg) throws Exception {
		throw new UnsupportedOperationException("not yet implemented");
	}

	/**
	 * check that this variable has not already been declared in the same scope.
	 */
	@Override
	public Object visitVarDec(VarDec varDec, Object arg) throws Exception {
		//System.out.println("in TypeCheckVisitor.java: visitVarDec");
		if (!symbolTable.insert(varDec.identToken.getText(), varDec)) {
			throw new TypeCheckException("name already in symbol table", varDec);
		}
		varDec.type.visit(this, arg);
		return varDec.type.getJVMType();
	}

	/**
	 * All checking will be done in the children since grammar ensures that the
	 * rangeExpression is a rangeExpression.
	 */
	@Override
	public Object visitWhileRangeStatement(
			WhileRangeStatement whileRangeStatement, Object arg)
			throws Exception {
		//System.out.println("in TypeCheckVisitor.java: visitWhileRangeStatement");
		throw new UnsupportedOperationException("not yet implemented");

	}

	@Override
	public Object visitWhileStarStatement(
			WhileStarStatement whileStarStatement, Object arg) throws Exception {
		//System.out.println("in TypeCheckVisitor.java: visitWhileStarStatement");
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg)
			throws Exception {
		//System.out.println("in TypeCheckVisitor.java: visitWhileStatement");
		String type = (String)whileStatement.expression.visit(this, arg);
		check(type.equals(booleanType), "boolean type needed", whileStatement);
		whileStatement.block.visit(this, arg);
		return null;
	}

}
