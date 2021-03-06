package cop5555sp15.ast;

import org.objectweb.asm.*;

//import cop5555sp15.TokenStream.Kind;
import cop5555sp15.TypeConstants;

public class CodeGenVisitor implements ASTVisitor, Opcodes, TypeConstants {

	ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
	// Because we used the COMPUTE_FRAMES flag, we do not need to
	// insert the mv.visitFrame calls that you will see in some of the
	// asmifier examples. ASM will insert those for us.
	// FYI, the purpose of those instructions is to provide information
	// about what is on the stack just before each branch target in order
	// to speed up class verification.
	FieldVisitor fv;
	String className;
	String classDescriptor;

	// This class holds all attributes that need to be passed downwards as the
	// AST is traversed. Initially, it only holds the current MethodVisitor.
	// Later, we may add more attributes.
	static class InheritedAttributes {
		public InheritedAttributes(MethodVisitor mv) {
			super();
			this.mv = mv;
		}

		MethodVisitor mv;
	}

	@Override
	public Object visitAssignmentStatement(
			AssignmentStatement assignmentStatement, Object arg)
			throws Exception {
		//System.out.println("in CodeGenVisitor.java: visitAssignmentStatement");
		MethodVisitor mv = ((InheritedAttributes) arg).mv;
		IdentLValue identLValue = ((IdentLValue)assignmentStatement.lvalue);
		String lvaluetype = identLValue.getType();
		String ident = identLValue.identToken.getText();
		String expressionType = assignmentStatement.expression.getType();
		if(lvaluetype.equals(intType) || lvaluetype.equals(booleanType) || lvaluetype.equals(stringType))
		{
			mv.visitVarInsn(ALOAD, 0);
			assignmentStatement.expression.visit(this, arg);
			mv.visitFieldInsn(PUTFIELD, className, ident, lvaluetype);
		}
		else
		{
			if(lvaluetype.contains("I"))
			{
				
				char l[] = lvaluetype.toCharArray();
				int i;
				int angleCount = 0;
				for(i = l.length-1; i >= 0; --i)
				{
					if(l[i] == 'I')
						break;
					else
						++angleCount;
				}
				lvaluetype = new String(l, 0, i-0-angleCount+1);
				
				l = expressionType.toCharArray();
				angleCount = 0;
				for(i = l.length-1; i >= 0; --i)
				{
					if(l[i] == 'I')
						break;
					else
						++angleCount;
				}
				if(i != -1)
					expressionType = new String(l, 0, i-angleCount+1);
				String type = expressionType.substring(1);
				//System.out.println("in CodeGenVisitor.java: visitAssignmentStatement, " + ident + ", " + lvaluetype + ", " + expressionType + ", " + type);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitTypeInsn(NEW, type);
				mv.visitInsn(DUP);
				mv.visitMethodInsn(INVOKESPECIAL, type, "<init>", "()V", false);
				mv.visitFieldInsn(PUTFIELD, className, ident, lvaluetype+";");
				assignmentStatement.expression.visit(this, arg);
				int number = ((ListExpression)assignmentStatement.expression).expressionList.size();
				if(number == 0)
				{
					//System.out.println("number is 0");
				}
				else
				{
					for(i = 0; i < number; ++i)
					{
						mv.visitVarInsn(ALOAD, 0);
						mv.visitFieldInsn(GETFIELD, className, ident, lvaluetype+";");
						mv.visitInsn(SWAP);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
						mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true);
						mv.visitInsn(POP);
					}
					
				}
			}
			else if(lvaluetype.contains("Z"))
			{
				lvaluetype = lvaluetype.replace("Z", "Ljava/lang/Boolean;");
			}
			else//contains Ljava/lang/String
			{
				
			}
		}
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression,
			Object arg) throws Exception {
		
		//System.out.println("in CodeGenVisitor.java: visitBinaryExpression");
		MethodVisitor mv = ((InheritedAttributes) arg).mv; // this should be the
															// first statement
															// of all visit
															// methods that
															// generate
															// instructions

		switch(binaryExpression.op.kind)
		{
		case PLUS: 
			binaryExpression.expression0.visit(this, arg);
			binaryExpression.expression1.visit(this, arg);
			if(binaryExpression.getType().equals(intType))
				mv.visitInsn(IADD);
			else if(binaryExpression.getType().equals(stringType))
				mv.visitMethodInsn(INVOKESTATIC, className, "stringPlus",
						"(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", false);
			break;
		case MINUS: 
			binaryExpression.expression0.visit(this, arg);
			binaryExpression.expression1.visit(this, arg);
			if(binaryExpression.getType().equals(intType))
				mv.visitInsn(ISUB);
			break;
		case TIMES: 
			binaryExpression.expression0.visit(this, arg);
			binaryExpression.expression1.visit(this, arg);
			if(binaryExpression.getType().equals(intType))
				mv.visitInsn(IMUL);
			break;
		case DIV: 
			binaryExpression.expression0.visit(this, arg);
			binaryExpression.expression1.visit(this, arg);
			if(binaryExpression.getType().equals(intType))
				mv.visitInsn(IDIV);
			break;
		case AND: 
			{
				binaryExpression.expression0.visit(this, arg);
				Label l1 = new Label();
				mv.visitJumpInsn(IFEQ, l1);
				binaryExpression.expression1.visit(this,arg);
				mv.visitJumpInsn(IFEQ, l1);
				mv.visitInsn(ICONST_1);
				Label l2 = new Label();
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(l2);
			}
			break;
		case BAR: 
			{
				binaryExpression.expression0.visit(this, arg);
				Label l1 = new Label();
				mv.visitJumpInsn(IFNE, l1);
				binaryExpression.expression1.visit(this,arg);
				mv.visitJumpInsn(IFNE, l1);
				mv.visitInsn(ICONST_0);
				Label l2 = new Label();
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(l2);
			}
			break;
		case EQUAL: 
		{
			if(binaryExpression.expression0.getType().equals(intType) || binaryExpression.expression0.getType().equals(booleanType))
			{
				binaryExpression.expression0.visit(this, arg);
				Label l1 = new Label();
				binaryExpression.expression1.visit(this,arg);
				mv.visitJumpInsn(IF_ICMPEQ, l1);
				mv.visitInsn(ICONST_0);
				Label l2 = new Label();
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(l2);
			}
			else if(binaryExpression.expression0.getType().equals(stringType))
			{
				binaryExpression.expression0.visit(this, arg);
				binaryExpression.expression1.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, className, "stringEquals",
						"(Ljava/lang/String;Ljava/lang/String;)Z", false);
			}
		}
			break;
		case NOTEQUAL: 
		{
			if(binaryExpression.expression0.getType().equals(intType) || binaryExpression.expression0.getType().equals(booleanType))
			{
				binaryExpression.expression0.visit(this, arg);
				Label l1 = new Label();
				binaryExpression.expression1.visit(this,arg);
				mv.visitJumpInsn(IF_ICMPNE, l1);
				mv.visitInsn(ICONST_0);
				Label l2 = new Label();
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(l2);
			}
			else if(binaryExpression.expression0.getType().equals(stringType))
			{
				binaryExpression.expression0.visit(this, arg);
				binaryExpression.expression1.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, className, "stringNotEquals",
						"(Ljava/lang/String;Ljava/lang/String;)Z", false);
			}
		}
			break;
		case LT: 
		{
			binaryExpression.expression0.visit(this, arg);
			binaryExpression.expression1.visit(this, arg);
			Label l1 = new Label();
			mv.visitJumpInsn(IF_ICMPLT , l1);
			mv.visitInsn(ICONST_0);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_1);
			
			mv.visitLabel(l2);
		}
			break;
		case LE: 
		{
			binaryExpression.expression0.visit(this, arg);
			binaryExpression.expression1.visit(this, arg);
			Label l1 = new Label();
			mv.visitJumpInsn(IF_ICMPLE  , l1);
			mv.visitInsn(ICONST_0);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_1);
			
			mv.visitLabel(l2);
		}
			break;
		case GT: 
		{
			binaryExpression.expression0.visit(this, arg);
			binaryExpression.expression1.visit(this, arg);
			Label l1 = new Label();
			mv.visitJumpInsn(IF_ICMPGT  , l1);
			mv.visitInsn(ICONST_0);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_1);
			
			mv.visitLabel(l2);
		}
			break;
		case GE: 
		{
			binaryExpression.expression0.visit(this, arg);
			binaryExpression.expression1.visit(this, arg);
			Label l1 = new Label();
			mv.visitJumpInsn(IF_ICMPGE  , l1);
			mv.visitInsn(ICONST_0);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_1);
			
			mv.visitLabel(l2);
		}
			break;
		default: throw new Exception("wrong type and/or opcode in " + binaryExpression);
		}
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		//System.out.println("in CodeGenVisitor.java: visitBlock");
		for (BlockElem elem : block.elems) {
			elem.visit(this, arg);
		}
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(
			BooleanLitExpression booleanLitExpression, Object arg)
			throws Exception {
		//System.out.println("in CodeGenVisitor.java: visitBooleanLitExpression");
		MethodVisitor mv = ((InheritedAttributes) arg).mv; // this should be the
															// first statement
															// of all visit
															// methods that
															// generate
															// instructions
		mv.visitLdcInsn(booleanLitExpression.value);
		return null;
	}

	@Override
	public Object visitClosure(Closure closure, Object arg) throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitClosureDec(ClosureDec closureDeclaration, Object arg)
			throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitClosureEvalExpression(
			ClosureEvalExpression closureExpression, Object arg)
			throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitClosureExpression(ClosureExpression closureExpression,
			Object arg) throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitExpressionLValue(ExpressionLValue expressionLValue,
			Object arg) throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitExpressionStatement(
			ExpressionStatement expressionStatement, Object arg)
			throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression,
			Object arg) throws Exception {
		//System.out.println("in CodeGenVisitor.java: visitIdentExpression");
		MethodVisitor mv = ((InheritedAttributes) arg).mv;
		mv.visitVarInsn(ALOAD, 0);
		String ident = identExpression.identToken.getText();
		String type = identExpression.getType();
		if(type.equals("I") || type.equals("Z"))
			mv.visitFieldInsn(GETFIELD, className, ident, type);
		else
		{
			if(type.contains("I"))
			{
				
				char l[] = type.toCharArray();
				int i;
				int angleCount = 0;
				for(i = l.length-1; i >= 0; --i)
				{
					if(l[i] == 'I')
						break;
					else
						++angleCount;
				}
				type = new String(l, 0, i-0-angleCount+1);
				//System.out.println("in CodeGenVisitor.java: visitIdentExpression, " + ident + ", " + type);
				mv.visitFieldInsn(GETFIELD, className, ident, type+";");
			}
			else if(type.contains("Z"))
			{
				type = type.replace("Z", "Ljava/lang/Boolean;");
			}
		}
		return null;
		
	}

	@Override
	public Object visitIdentLValue(IdentLValue identLValue, Object arg)
			throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitIfElseStatement(IfElseStatement ifElseStatement,
			Object arg) throws Exception {
		//System.out.println("in CodeGenVisitor.java: visitIfElseStatement");
		MethodVisitor mv = ((InheritedAttributes) arg).mv; 
		ifElseStatement.expression.visit(this, arg);
		Label l1 = new Label();
		mv.visitJumpInsn(IFEQ, l1);
		ifElseStatement.ifBlock.visit(this, arg);
		Label l2 = new Label();
		mv.visitJumpInsn(GOTO, l2);
		mv.visitLabel(l1);
		ifElseStatement.elseBlock.visit(this, arg);
		mv.visitLabel(l2);
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg)
			throws Exception {
		//System.out.println("in CodeGenVisitor.java: visitIfStatement");
		MethodVisitor mv = ((InheritedAttributes) arg).mv; 
		ifStatement.expression.visit(this, arg);
		Label l1 = new Label();
		mv.visitJumpInsn(IFEQ, l1);
		ifStatement.block.visit(this, arg);
		mv.visitLabel(l1);
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression,
			Object arg) throws Exception {
		//System.out.println("in CodeGenVisitor.java: visitIntLitExpression");
		MethodVisitor mv = ((InheritedAttributes) arg).mv; // this should be the
															// first statement
															// of all visit
															// methods that
															// generate
															// instructions
		mv.visitLdcInsn(intLitExpression.value);
		return null;
	}

	@Override
	public Object visitKeyExpression(KeyExpression keyExpression, Object arg)
			throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitKeyValueExpression(
			KeyValueExpression keyValueExpression, Object arg) throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitKeyValueType(KeyValueType keyValueType, Object arg)
			throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitListExpression(ListExpression listExpression, Object arg)
			throws Exception {
		//System.out.println("in CodeGenVisitor.java: visitListExpression");
		for(Expression e : listExpression.expressionList)
			e.visit(this, arg);
		return null;
	}

	@Override
	public Object visitListOrMapElemExpression(
			ListOrMapElemExpression listOrMapElemExpression, Object arg)
			throws Exception {
		//System.out.println("in CodeGenVisitor.java: visitListOrMapElemExpression");
		
		MethodVisitor mv = ((InheritedAttributes) arg).mv;
		String ident = listOrMapElemExpression.identToken.getText();
		
		String type = "";
		String signature = "";
		if(listOrMapElemExpression.getType().contains("List"))
		{
			String wholeDesc = listOrMapElemExpression.getType();

			if(wholeDesc.contains("I"))
			{
				char[] l = wholeDesc.toCharArray();
				int i;
				int angleCount = 0;
				for(i = l.length-1;  i >= 0; --i)
				{
					if(l[i] == 'I')
						break;
					else
						++angleCount;
				}
				type = new String(l, 1, i-angleCount);
				signature = wholeDesc.replace("I", "Ljava/lang/Integer;");
			}
			else if(wholeDesc.contains("Z"))
			{
				char[] l = wholeDesc.toCharArray();
				int i;
				int angleCount = 0;
				for(i = l.length-1; i >= 0; --i)
				{
					if(l[i] == 'I')
						break;
					else
						++angleCount;
				}
				type = new String(l, 1, i-angleCount);
				signature = wholeDesc.replace("Z", "Ljava/lang/Boolean;");
			}
			else
			{
				//to be implemented
			}
			//System.out.println("in codeGenVisitor.java: visitListOrMapElemExpression, " + ident + ", " + wholeDesc +", " + type + ", "+signature);
		}
		else if(listOrMapElemExpression.getType().contains("Hash"))
		{}
		
		
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, ident, "L"+type+";");
		listOrMapElemExpression.expression.visit(this, arg);
		mv.visitMethodInsn(INVOKEINTERFACE, type, "get", "(I)Ljava/lang/Object;", true);
		mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
		return null;
	}

	@Override
	public Object visitListType(ListType listType, Object arg) throws Exception {
		//System.out.println("in CodeGenVisitor.java: visitListType");
		return listType.getJVMType();
	}

	@Override
	public Object visitMapListExpression(MapListExpression mapListExpression,
			Object arg) throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitPrintStatement(PrintStatement printStatement, Object arg)
			throws Exception {
		//System.out.println("in CodeGenVisitor.java: visitPrintStatement");
		MethodVisitor mv = ((InheritedAttributes) arg).mv;
		
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(printStatement.firstToken.getLineNumber(), l0);
		mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
				"Ljava/io/PrintStream;");
		printStatement.expression.visit(this, arg); // adds code to leave value
													// of expression on top of
													// stack.
													// Unless there is a good
													// reason to do otherwise,
													// pass arg down the tree
		String etype = printStatement.expression.getType();
		if (etype.equals("I") || etype.equals("Z")
				|| etype.equals("Ljava/lang/String;")) {
			String desc = "(" + etype + ")V";
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", desc, false);
		} else
			throw new UnsupportedOperationException(
					"printing list or map not yet implemented");
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		//System.out.println("in CodeGenVisitor.java: visitProgram");
		className = program.JVMName;
		classDescriptor = 'L' + className + ';';
		cw.visit(52, // version
				ACC_PUBLIC + ACC_SUPER, // access codes
				className, // fully qualified classname
				null, // signature
				"java/lang/Object", // superclass
				new String[] { "cop5555sp15/Codelet" } // implemented interfaces
		);
		cw.visitSource(null, null); // maybe replace first argument with source
									// file name

		// create init method
		{
			MethodVisitor mv;
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(3, l0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>",
					"()V", false);
			mv.visitInsn(RETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", classDescriptor, null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}

		//create static method "String stringPlus(String a, String b)"
		/*
		 * public static String StringPlus(String a, String b)
		 * {
		 * 		StringBuilder c = new StringBuilder(a);
		 * 		c.append(b);
		 * 		return c.toString();
		 * }
		 */
		{
			String methodName = "stringPlus";
			String methodDesc = "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;";
			MethodVisitor mv = cw.visitMethod(ACC_STATIC+ACC_PUBLIC, methodName, methodDesc, null, null);
			Label start;
			Label end;
			int a_slot = 0; //the first argument
			int b_slot = 1; //the second argument
			int c_slot = 2; //local variable which is type StringBuilder
			mv.visitCode();
			start = new Label();
			mv.visitLabel(start);
			mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, a_slot);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
			mv.visitVarInsn(ASTORE, c_slot);
			mv.visitVarInsn(ALOAD, c_slot);
			mv.visitVarInsn(ALOAD, b_slot);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
			mv.visitInsn(POP);
			mv.visitVarInsn(ALOAD, c_slot);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
			mv.visitInsn(ARETURN);
			end = new Label();
			mv.visitLabel(end);
			mv.visitLocalVariable("a", "Ljava/lang/String;", null, start, end, a_slot);
			mv.visitLocalVariable("b", "Ljava/lang/String;", null, start, end, b_slot);
			mv.visitLocalVariable("c", "Ljava/lang/StringBuilder;", null, start, end, c_slot);
			mv.visitMaxs(3, 3);
			mv.visitEnd();
		}
		
		//create static method "boolean stringEquals(String a, String b)"
		/*
		* public static boolean stringEquals(String a, String b)
		* {
		* 		return a.equals(b);
		* }
		*/
		{
			String methodName = "stringEquals";
			String methodDesc = "(Ljava/lang/String;Ljava/lang/String;)Z";
			MethodVisitor mv = cw.visitMethod(ACC_STATIC+ACC_PUBLIC, methodName, methodDesc, null, null);
			Label start;
			Label end;
			int a_slot = 0; //the first argument
			int b_slot = 1; //the second argument
			mv.visitCode();
			start = new Label();
			mv.visitLabel(start);
			mv.visitVarInsn(ALOAD, a_slot);
			mv.visitVarInsn(ALOAD, b_slot);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
			mv.visitInsn(IRETURN);
			end = new Label();
			mv.visitLabel(end);
			mv.visitLocalVariable("a", "Ljava/lang/String;", null, start, end, a_slot);
			mv.visitLocalVariable("b", "Ljava/lang/String;", null, start, end, b_slot);
			mv.visitMaxs(3, 3);
			mv.visitEnd();
		}
		
		//create static method "boolean stringNotEquals(String a, String b)"
		/*
		* public static boolean stringNotEquals(String a, String b)
		* {
		* 		return !a.equals(b);
		* }
		*/
		{
			String methodName = "stringNotEquals";
			String methodDesc = "(Ljava/lang/String;Ljava/lang/String;)Z";
			MethodVisitor mv = cw.visitMethod(ACC_STATIC+ACC_PUBLIC, methodName, methodDesc, null, null);
			Label start;
			Label end;
			int a_slot = 0; //the first argument
			int b_slot = 1; //the second argument
			mv.visitCode();
			start = new Label();
			mv.visitLabel(start);
			mv.visitVarInsn(ALOAD, a_slot);
			mv.visitVarInsn(ALOAD, b_slot);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
			
			Label l1 = new Label();
			mv.visitJumpInsn(IFNE, l1);
			mv.visitInsn(ICONST_1);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_0);
			
			mv.visitLabel(l2);
			mv.visitInsn(IRETURN);
			
			end = new Label();
			mv.visitLabel(end);
			mv.visitLocalVariable("a", "Ljava/lang/String;", null, start, end, a_slot);
			mv.visitLocalVariable("b", "Ljava/lang/String;", null, start, end, b_slot);
			mv.visitMaxs(3, 3);
			mv.visitEnd();
		}
				
		// generate the execute method
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "execute", // name of top
																	// level
																	// method
				"()V", // descriptor: this method is parameterless with no
						// return value
				null, // signature.  This is null for us, it has to do with generic types
				null // array of strings containing exceptions
				);
		mv.visitCode();
		Label lbeg = new Label();
		mv.visitLabel(lbeg);
		mv.visitLineNumber(program.firstToken.lineNumber, lbeg);
		program.block.visit(this, new InheritedAttributes(mv));
		mv.visitInsn(RETURN);
		Label lend = new Label();
		mv.visitLabel(lend);
		mv.visitLocalVariable("this", classDescriptor, null, lbeg, lend, 0);
		mv.visitMaxs(0, 0);  //this is required just before the end of a method. 
		                     //It causes asm to calculate information about the
		                     //stack usage of this method.
		mv.visitEnd();

		
		cw.visitEnd();
		return cw.toByteArray();
	}

	@Override
	public Object visitQualifiedName(QualifiedName qualifiedName, Object arg) {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitRangeExpression(RangeExpression rangeExpression,
			Object arg) throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitReturnStatement(ReturnStatement returnStatement,
			Object arg) throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitSimpleType(SimpleType simpleType, Object arg)
			throws Exception {
		//System.out.println("in CodeGenVisitor.java: visitSimpleType");
		return simpleType.getJVMType();
	}

	@Override
	public Object visitSizeExpression(SizeExpression sizeExpression, Object arg)
			throws Exception {
		//System.out.println("in CodeGenVisitor.java: visitSizeExpression");
		MethodVisitor mv = ((InheritedAttributes) arg).mv;
		String owner = "";
		if(sizeExpression.expression.getType().contains("List"))
			owner = "java/util/List";
		sizeExpression.expression.visit(this, arg);
		
		mv.visitMethodInsn(INVOKEINTERFACE, owner, "size", "()I", true);
		sizeExpression.setType(intType);
		return intType;
	}

	@Override
	public Object visitStringLitExpression(
			StringLitExpression stringLitExpression, Object arg)
			throws Exception {
		//System.out.println("in CodeGenVisitor.java: visitStringLitExpression, stringLitExpression.value = " + stringLitExpression.value);
		MethodVisitor mv = ((InheritedAttributes) arg).mv; // this should be the
															// first statement
															// of all visit
															// methods that
															// generate
															// instructions
		mv.visitLdcInsn(stringLitExpression.value);
		return null;
	}

	@Override
	public Object visitUnaryExpression(UnaryExpression unaryExpression,
			Object arg) throws Exception {
		//System.out.println("in CodeGenVisitor.java: visitIntLitExpression");
		MethodVisitor mv = ((InheritedAttributes) arg).mv; // this should be the
															// first statement
															// of all visit
															// methods that
															// generate
															// instructions
		if(unaryExpression.getType().equals(intType))
		{
			unaryExpression.expression.visit(this, arg);
			mv.visitInsn(INEG);
		}
		else if(unaryExpression.getType().equals(booleanType))
		{
			unaryExpression.expression.visit(this, arg);
			Label l1 = new Label();
			mv.visitJumpInsn(IFEQ, l1);
			mv.visitInsn(ICONST_0);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(l2);
		}
		else
			throw new Exception("unknow type");
		return null;
	}

	@Override
	public Object visitValueExpression(ValueExpression valueExpression,
			Object arg) throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitVarDec(VarDec varDec, Object arg) throws Exception {
		//System.out.println("in CodeGenVisitor.java:  visitVarDec");
		String varName = varDec.identToken.getText();
		
		String type = (String)varDec.type.visit(this, arg);
		
		//System.out.println("here");
		if(type.equals("I") || type.equals("Z") || type.equals("Ljava/lang/String;"))
		{
			fv = cw.visitField(0, varName, type, null, null);
			fv.visitEnd();
		}
		else
		{
			type = type.replace("I", "Ljava/lang/Integer;");
			type = type.replace("Z", "Ljava/lang/Boolean;");
			String fieldType = "Ljava/util/List;";
			//System.out.println("in CodeGenVisitor.java:  visitVarDec, " + type + ", " + varName);
			fv = cw.visitField(0, varName, fieldType, type, null);
			fv.visitEnd();
		}
	    return null;
	}

	@Override
	public Object visitWhileRangeStatement(
			WhileRangeStatement whileRangeStatement, Object arg)
			throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitWhileStarStatement(WhileStarStatement whileStarStatment,
			Object arg) throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg)
			throws Exception {
		//System.out.println("in CodeGenVisitor.java: visitWhileStatement");
		MethodVisitor mv = ((InheritedAttributes) arg).mv; 
		
		Label l1 = new Label();
		mv.visitJumpInsn(GOTO, l1);
		Label l2 = new Label();
		
		mv.visitLabel(l2);
		whileStatement.block.visit(this, arg);
		
		mv.visitLabel(l1);
		whileStatement.expression.visit(this, arg);
		mv.visitJumpInsn(IFNE, l2);
		return null;
	}

	@Override
	public Object visitUndeclaredType(UndeclaredType undeclaredType, Object arg)
			throws Exception {
		throw new UnsupportedOperationException(
				"code generation not yet implemented");
	}

}
