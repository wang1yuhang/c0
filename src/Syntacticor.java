/**
 * 
 */

import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @author ftrww
 *
 */
public class Syntacticor {
	private static Syntacticor syntacticor;
	private ArrayList<Token> tokenList = new ArrayList<Token>();
	private int index = 0;
	private Token curToken;
	private SymbolTable localTable = new SymbolTable();
	private SymbolTable globalTable = new SymbolTable();
	public SaveCode allSave = new SaveCode();
	private int tokenDepth = 0;
	private LinkedList<Byte> paramTypes;
	private int paramLoc;
	private int paramOffset;
	private boolean asFlag = false;
	private boolean returnFlag = false;
	
	private Syntacticor() {
		
	}
	
	public SymbolTable getLocalTable() {
		return this.localTable;
	}
	
	public SymbolTable getGlobalTable() {
		return this.globalTable;
	}
	
	public static Syntacticor getInstance(String filename) throws Exception{
		Tokenizer tokenizer = Tokenizer.getTokenizerInstance(filename);
		tokenizer.getToken();
		if(syntacticor == null) {
			syntacticor = new Syntacticor();
		}
		syntacticor.tokenList = tokenizer.getTokenList();
		return syntacticor;
	}
	
	public ArrayList<Token>  getTokenlist(){
		return tokenList;
	}
	
	public Token getToken() {
		if(index == tokenList.size()) {
			return null;
		}
		else {
			return tokenList.get(index++);
		}
	}
	
	public int unreadToken() throws Exception{
		if(index <= 0) {
			throw new SyntacticorException("unread token error");
		}
		else {
			index --;
			curToken = tokenList.get(index-1);
			return index;
		}
	}
	
	public SymbolTable getCurrentTable() {
		if(this.tokenDepth == 0) {
			return this.globalTable;
		}
		else {
			return this.localTable;
		}
	}
	
	public void analyzeProgram() throws Exception{
		TableItem _start = new TableItem("_start",TableItem.FUNTION,0);
		SaveCode startSave = new SaveCode();
		SaveCode globalSave = new SaveCode();
		_start.setConst();
		this.globalTable.addItem(_start);
		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		while(curToken!=null) {
			if(curToken.getType() == Token.FN_KW){
				analyzeFunc();
			}
			else {
				analyzeDeclStmt(startSave,false);
			}
		}
		TableItem item = this.globalTable.find("main");
		byte mainReturnType;
		if(item == null || item.type != TableItem.FUNTION) {
			throw new SemanticException("the main function is not exist");
		}
		mainReturnType = item.returnType;
		if(mainReturnType != TableItem.VOID) {
			 startSave.addCode(CodeConversion.stackalloc(1));
		}
		else {
			startSave.addCode(CodeConversion.stackalloc(0));
		}
		startSave.addCode(CodeConversion.call(this.getFunctionNum("main")));
		this.allSave.addCodeFirst(startSave);
		this.allSave.addCodeFirst(startSave.codeList.size(),4);
		this.allSave.addCodeFirst(0,4);
		this.allSave.addCodeFirst(0,4);
		this.allSave.addCodeFirst(0,4);
		this.allSave.addCodeFirst(0,4);
		int num = 0;
		for(int i = 0;i<this.globalTable.getTableList().size();i++) {
			if(this.globalTable.getTableList().get(i).type == TableItem.FUNTION) {
				num++;
			}
		}
		this.globalTable.print();
		this.allSave.addCodeFirst(num,4);
		for(int i=0;i<this.globalTable.getTableList().size();i++) {
			TableItem sti = this.globalTable.getTableList().get(i);
			int isConst = sti.isConst ? 1 : 0;
			globalSave.addCode(isConst,1);
			if(sti.type == TableItem.INT || sti.type == TableItem.DOUBLE){
                globalSave.addCode(8, 4);
                globalSave.addCode(0, 8);
            }
            else if(sti.type == TableItem.FUNTION){
                globalSave.addCode(sti.getName().length(), 4);
                StringBuilder value = new StringBuilder();
                for(int j=0; j<sti.getName().length(); j++){
                    value.append(String.format("%02x", (int)sti.getName().charAt(j)));
                }
                globalSave.addCode(value.toString());
            }
            else if(sti.type == TableItem.STRING){
                globalSave.addCode(sti.StringValue.length(), 4);
                StringBuilder value = new StringBuilder();
                for(int j=0; j<sti.StringValue.length(); j++){
                    value.append(String.format("%02x", (int)sti.StringValue.charAt(j)));
                }
                globalSave.addCode(value.toString());
            }
            else{
                throw new SemanticException("Global error.");
            }
		}
		this.allSave.addCodeFirst(globalSave);

        // global.count
        this.allSave.addCodeFirst(this.globalTable.getTableList().size(), 4);

        // version
        this.allSave.addCodeFirst("00000001");

        // magic
        this.allSave.addCodeFirst("72303b3e");

        this.allSave.replaceLabel();
	}
	
	public void analyzeFunc() throws Exception{
		String funcName;
		byte returnType;
		int instructionNum = 0;
		TableItem funcItem;
		SaveCode funcSave = new SaveCode();
		paramLoc = 0;
		paramTypes = new LinkedList<Byte>();
		int returnSlot = 0;
		int paramSlot = 0;
		int locSlot = 0;
		expr exprTemp;
		if(curToken.getType() != Token.FN_KW) {
			throw new SyntacticorException("no fn keyword for a function");
		}
		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		if(curToken == null || curToken.getType()!=Token.IDENT) {
			throw new SyntacticorException("no ident for a function");
		}
		funcName = curToken.getValue().toString();
		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		if(curToken == null || curToken.getType()!=Token.L_PAREN) {
			throw new SyntacticorException("no left paren for a function");
		}
		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		if(curToken.getType() == Token.CONST_KW || curToken.getType() == Token.IDENT) {
			analyzeParamList();
		}
		if(curToken == null || curToken.getType()!=Token.R_PAREN) {
			throw new SyntacticorException("no right paren for a function");
		}
		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		if(curToken == null || curToken.getType()!= Token.ARROW) {
			throw new SyntacticorException("no arrow for a function");
		}
		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		if(curToken == null || curToken.getType()!=Token.IDENT) {
			throw new SyntacticorException("no ty for a function");
		}
		returnType = SemanticFunc.checkTy(curToken.getValue().toString());
		if(returnType == TableItem.VOID) {
			paramOffset = 0;
		}
		else {
			paramOffset = 1;
		}
		returnSlot = this.typeToSlot(returnType);

		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		
		funcItem = new TableItem(funcName,TableItem.FUNTION,0,returnType);
		funcItem.setConst();
		funcItem.location = this.globalTable.getTableList().size();
		
		
		for(byte t:paramTypes) {
			funcItem.addParam(t);
			paramSlot += this.typeToSlot(t);
		}
		
		if(this.globalTable.hasSameName(funcItem)) {
			throw new SemanticException("duplicate function name " + funcName);
		}
		else {
			this.globalTable.addItem(funcItem);
		}
		exprTemp = analyzeBlockStmt(funcSave,true);
		
		if(exprTemp != null && exprTemp.type != returnType){
			throw new SemanticException("the type of the block return can not match the required return type");
		}
		//添加指令数量
		funcSave.addCodeFirst(funcSave.codeList.size()+1,4);
		funcSave.addCodeFirst(funcSave.variableSlot, 4);
		funcSave.addCodeFirst(paramSlot,4);
		funcSave.addCodeFirst(returnSlot,4);
		funcSave.addCodeFirst(funcItem.location, 4);
		funcSave.addCode(CodeConversion.ret);
		this.allSave.addCode(funcSave);
	}
	
	public void analyzeParamList() throws Exception{
		analyzeParam();
		while(curToken != null && curToken.getType() == Token.COMMA) {
			curToken = getToken();
			//System.out.println(curToken.getValue()+"---"+curToken.getType());
			//System.out.println("this is analyzeParamList");
			analyzeParam();
		}
	}
	
	public void analyzeParam() throws Exception{
		TableItem paramItem;
		String paramName;
		boolean isConst = false;
		if(curToken == null) {
			throw new SyntacticorException("function param error");
		}
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		if(curToken.getType() == Token.CONST_KW) {
			curToken = getToken();
			isConst = true;
			//System.out.println(curToken.getValue()+"---"+curToken.getType());
		}
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		if(curToken == null || curToken.getType() != Token.IDENT) {
			throw new SyntacticorException("function param need ident");
		}
		paramName = curToken.getValue().toString();
		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		if(curToken == null || curToken.getType() != Token.COLON) {
			throw new SyntacticorException("funciton param need colon");
		}
		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		if(curToken == null || curToken.getType() != Token.IDENT) {
			throw new SyntacticorException("function param need ty");
		}
		byte type = SemanticFunc.checkTy(curToken.getValue().toString());
		paramTypes.add(type);
		paramItem = new TableItem(paramName,type,1);
		paramItem.setParam();
		paramItem.location =  paramLoc++;
		if(isConst) {
			paramItem.setConst();
		}
		if(this.localTable.hasSameName(paramItem)) {
			throw new SemanticException("duplicate param name: " + paramName);
		}
		this.localTable.addItem(paramItem);
		curToken = getToken();
	}
	
	public expr analyzeDeclStmt(SaveCode saveCode,boolean isInFuncBlock) throws Exception{
		if(curToken == null) {
			throw new SyntacticorException("analyzeDectStmt error");
		}
		if(curToken.getType() == Token.LET_KW) {
			analyzeLetDeclStmt(saveCode,isInFuncBlock);
		}
		else if(curToken.getType() == Token.CONST_KW) {
			analyzeConstDeclStmt(saveCode,isInFuncBlock);
		}
		else {
			throw new SyntacticorException("no type when analyze declStmt");
		}
		return null;
	}
	
	public void analyzeLetDeclStmt(SaveCode saveCode,boolean isInFuncBlock) throws Exception{
		expr returnExpr;
		SymbolTable currentTable = this.getCurrentTable();
		TableItem declItem;
		String variableName;
		SaveCode exprSave = new SaveCode();
		byte type;
		if(curToken == null || curToken.getType() != Token.LET_KW) {
			throw new SyntacticorException("analyzeLetDeclStmt error");
		}
		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		if(curToken == null || curToken.getType() != Token.IDENT) {
			throw new SyntacticorException("letDeclStmt need ident");
		}
		variableName = curToken.getValue().toString();
		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		if(curToken == null || curToken.getType() != Token.COLON) {
			throw new SyntacticorException("letDeclStmt need colon");
		}
		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		if(curToken == null || curToken.getType() != Token.IDENT) {
			throw new SyntacticorException("letDeclStmt need ty");
		}
		type = SemanticFunc.checkTy(curToken.getValue().toString());
		declItem = new TableItem(variableName,type,this.tokenDepth);
		if(currentTable.hasSameName(declItem)) {
			this.globalTable.print();
			this.localTable.print();
			throw new SemanticException("duplicate decl for variable "+ variableName);
		}
		if(this.tokenDepth == 0) {
			declItem.location = currentTable.getTableList().size();
		}
		else {
			declItem.location = saveCode.variableNum;
		}
		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		if(curToken.getType() == Token.ASSIGN) {
			curToken  = getToken();
			returnExpr = analyzeExpr(exprSave);
			if(returnExpr == null || type != returnExpr.type) {
				//System.out.println("type is "+type);
				throw new SemanticException("the type of variable "+variableName+" is not the same as the expr");
			}
			if(this.tokenDepth == 0) {
				//System.out.println("here");
				exprSave.addCodeFirst(CodeConversion.globa(currentTable.getTableList().size()));
			}
			else {
				//System.out.println("loc2");
				exprSave.addCodeFirst(CodeConversion.loca(saveCode.variableNum));
			}
			saveCode.addCode(exprSave);
			saveCode.addCode(CodeConversion.store64);
		}
		if(curToken == null || curToken.getType() != Token.SEMICOLON) {
			throw new SyntacticorException("letDeclStmt need semicolon");
		}
		saveCode.variableSlot += this.typeToSlot(type);
		saveCode.variableNum += 1;
		currentTable.addItem(declItem);
		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
	}

	public void analyzeConstDeclStmt(SaveCode saveCode,boolean isInFuncBlock) throws Exception{
		expr returnExpr = null;
		SymbolTable currentTable = this.getCurrentTable();
		TableItem declItem;
		SaveCode exprSave = new SaveCode();
		String variableName;
		byte type;
		if(curToken == null || curToken.getType() != Token.CONST_KW) {
			throw new SyntacticorException("ConstDeclStmt error");
		}
		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		if(curToken == null || curToken.getType() != Token.IDENT) {
			throw new SyntacticorException("ConstDeclStmt need ident");
		}
		variableName = curToken.getValue().toString();
		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		if(curToken == null || curToken.getType() != Token.COLON) {
			throw new SyntacticorException("ConstDeclStmt need colon");
		}
		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		if(curToken == null || curToken.getType() != Token.IDENT) {
			throw new SyntacticorException("CosntDeclStmt need ty");
		}
		type = SemanticFunc.checkTy(curToken.getValue().toString());
		declItem = new TableItem(variableName,type,this.tokenDepth);
		if(currentTable.hasSameName(declItem)) {
			throw new SemanticException("duplicate decl for variable "+ variableName);
		}
		if(this.tokenDepth == 0) {
			declItem.location = currentTable.getTableList().size();
		}
		else {
			declItem.location = saveCode.variableNum;
		}
		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		if(curToken == null || curToken.getType() != Token.ASSIGN) {
			throw new SyntacticorException("CosntDeclStmt need assign");
		}
		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		
		returnExpr = analyzeExpr(exprSave);
		if(returnExpr == null ||type != returnExpr.type) {
			throw new SemanticException("the type of variable "+variableName+" is not the same as the expr");
		}
		if(curToken == null || curToken.getType() != Token.SEMICOLON) {
			throw new SyntacticorException("CosntDeclStmt need semicolon");
		}
		if(this.tokenDepth == 0) {
			exprSave.addCodeFirst(CodeConversion.globa(currentTable.getTableList().size()));
		}
		else {
			//System.out.println("loc3");
			exprSave.addCodeFirst(CodeConversion.loca(saveCode.variableNum));
		}
		saveCode.addCode(exprSave);
		saveCode.addCode(CodeConversion.store64);
		declItem.setConst();
		saveCode.variableSlot += this.typeToSlot(type);
		saveCode.variableNum += 1;
		currentTable.addItem(declItem);
		saveCode.variableNum = saveCode.variableNum + 1;
		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
	}
	
	public expr analyzeBlockStmt(SaveCode saveCode, boolean isInFunction) throws Exception{
		expr returnExpr = null;
		this.tokenDepth = this.tokenDepth + 1;
		//System.out.println("the depth is "+ this.tokenDepth);
//		if(isInFunction== true) {
//			saveCode.variableNum = 0;
//			saveCode.variableSlot = 0;
//		}
		SymbolTable currentTable = this.getCurrentTable();
		if(curToken == null || curToken.getType() != Token.L_BRACE) {
			throw new SyntacticorException("BlockStmt need {");
		}
		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		if(curToken == null) {
			throw new SyntacticorException("BlockStmt need }");
		}
		while(curToken != null && curToken.getType() != Token.R_BRACE) {
			if(returnExpr == null)
				returnExpr = analyzeStmt(saveCode,isInFunction);
			else {
				analyzeStmt(saveCode,isInFunction);
			}
		}
		if(curToken == null || curToken.getType() != Token.R_BRACE) {
			throw new SyntacticorException("BlockStmt need }");
		}
		//this.localTable.print();
		currentTable.popTable(this.tokenDepth);
		this.tokenDepth = this.tokenDepth - 1;
		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		this.returnFlag = false;
		return returnExpr;
	}
	
	
	public expr analyzeStmt(SaveCode saveCode,boolean isInFuncBlock) throws Exception{
		expr returnExpr = null;
		switch(curToken.getType()) {
		case(Token.LET_KW):
			analyzeDeclStmt(saveCode,isInFuncBlock);
			break;
		case(Token.CONST_KW):
			analyzeDeclStmt(saveCode,isInFuncBlock);
			break;
		case(Token.IF_KW):
			analyzeIfStmt(saveCode);
			break;
		case(Token.WHILE_KW):
			//System.out.println("??");
			analyzeWhileStmt(saveCode);
			//System.out.println("???");
			break;
		case(Token.BREAK_KW):
			analyzeBreakStmt(saveCode);
			break;
		case(Token.CONTINUE_KW):
			analyzeContinueStmt(saveCode);
			break;
		case(Token.RETURN_KW):
			returnExpr = analyzeReturnStmt(saveCode);
			break;
		case(Token.L_BRACE):
			analyzeBlockStmt(saveCode,false);
			break;
		case(Token.SEMICOLON):
			curToken = getToken();
			//System.out.println(curToken.getValue()+"---"+curToken.getType());
			break;
		default:
			analyzeExprStmt(saveCode);
		}
		return returnExpr;
	}
	
	public expr analyzeIfStmt(SaveCode saveCode) throws Exception{
		ArrayList<SaveCode> compareSaveCodeList = new ArrayList<SaveCode>();
		ArrayList<SaveCode> blockSaveCodeList = new ArrayList<SaveCode>();
		SaveCode compareSaveCode = new SaveCode();
		SaveCode blockSaveCode = new SaveCode();
		SaveCode elseSaveCode = new SaveCode();
		
		if(curToken == null || curToken.getType() != Token.IF_KW) {
			throw new SyntacticorException("if stmt need if");
		}
		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		analyzeExpr(compareSaveCode);
		compareSaveCodeList.add(compareSaveCode);
		compareSaveCode = new SaveCode();
		analyzeBlockStmt(blockSaveCode,false);
		blockSaveCodeList.add(blockSaveCode);
		blockSaveCode = new SaveCode();
		
		
		while(curToken != null && curToken.getType() == Token.ELSE_KW) {
			curToken = getToken();
			//System.out.println(curToken.getValue()+"---"+curToken.getType());
			if(curToken != null ) {
				if(curToken.getType() == Token.IF_KW) {
					curToken = getToken();
					//System.out.println(curToken.getValue()+"---"+curToken.getType());
					analyzeExpr(compareSaveCode);
					compareSaveCodeList.add(compareSaveCode);
					compareSaveCode = new SaveCode();
					analyzeBlockStmt(blockSaveCode,false);
					blockSaveCodeList.add(blockSaveCode);
					blockSaveCode = new SaveCode();
				}
				else if(curToken.getType() == Token.L_BRACE){
					analyzeBlockStmt(elseSaveCode,false);
					break;
				}
				else {
					throw new SyntacticorException("wrong after else") ;
				}
			}
		}
		
		for(int i = 0; i < compareSaveCodeList.size();i++) {
			saveCode.addCode(compareSaveCodeList.get(i));
			saveCode.addCode(CodeConversion.brtrue(1));
			saveCode.addCode(CodeConversion.br(blockSaveCodeList.get(i).codeList.size()+1));
			saveCode.addCode(blockSaveCodeList.get(i));
			int count = 0;
			for(int j = i+1;j<compareSaveCodeList.size();j++) {
				count += compareSaveCodeList.get(j).codeList.size();
				count += 3;
				count += blockSaveCodeList.get(j).codeList.size();
			}
			count += elseSaveCode.codeList.size();
			saveCode.addCode(CodeConversion.br(count));
		}
		saveCode.addCode(elseSaveCode);
		return null;
	}
	
	public expr analyzeWhileStmt(SaveCode saveCode) throws Exception{
		if(curToken == null || curToken.getType() != Token.WHILE_KW) {
			throw new SyntacticorException("while stmt need while");
		}
		SaveCode compareSaveCode = new SaveCode();
		SaveCode blockSaveCode = new SaveCode();
		blockSaveCode.variableNum = saveCode.variableNum;
		blockSaveCode.variableSlot = saveCode.variableSlot;
		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		analyzeExpr(compareSaveCode);
		analyzeBlockStmt(blockSaveCode,false);
		saveCode.addCode(CodeConversion.whileStartFlag);
		saveCode.addCode(CodeConversion.nop);
		saveCode.addCode(compareSaveCode);
		saveCode.addCode(CodeConversion.brtrue(1));
		saveCode.addCode(CodeConversion.br(blockSaveCode.codeList.size()+1));
		saveCode.addCode(blockSaveCode);
		saveCode.addCode(CodeConversion.br(-(compareSaveCode.codeList.size()+3+blockSaveCode.codeList.size())));
		saveCode.addCode(CodeConversion.whileEndFlag);
		saveCode.variableNum = blockSaveCode.variableNum;
		saveCode.variableSlot = blockSaveCode.variableSlot;
		
		return null;
	}
	
	
	public expr analyzeBreakStmt(SaveCode saveCode) throws Exception{
		if(curToken == null || curToken.getType() != Token.BREAK_KW) {
			throw new SyntacticorException("break stmt need break");
		}
		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		if(curToken == null || curToken.getType() != Token.SEMICOLON) {
			throw new SyntacticorException("break stmt need ;");
		}
		curToken = getToken();
		saveCode.addCode(CodeConversion.breakFlag);
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		return null;
	}
	
	public expr analyzeContinueStmt(SaveCode saveCode) throws Exception{
		if(curToken == null || curToken.getType() != Token.CONTINUE_KW) {
			throw new SyntacticorException("continue stmt need continue");
		}
		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		if(curToken == null || curToken.getType() != Token.SEMICOLON) {
			throw new SyntacticorException("continue stmt need ;");
		}
		curToken = getToken();
		saveCode.addCode(CodeConversion.contninueFlag);
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		return null;
	}
	
	public expr analyzeReturnStmt(SaveCode saveCode) throws Exception{
		this.returnFlag = true;
		expr returnExpr = null;
		SaveCode exprSaveCode = new SaveCode();
		if(curToken == null || curToken.getType() != Token.RETURN_KW) {
			throw new SyntacticorException("return stmt need return");
		}
		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		if(curToken == null) {
			throw new SyntacticorException("return stmt need ;");
		}
		if(curToken.getType() != Token.SEMICOLON) {
			returnExpr = analyzeExpr(exprSaveCode);
			if(returnExpr.type == TableItem.INT || returnExpr.type == TableItem.DOUBLE) {
				saveCode.addCode(CodeConversion.arga(0));
				saveCode.addCode(exprSaveCode);
				saveCode.addCode(CodeConversion.store64);
			}
		}
		if(curToken == null || curToken.getType() != Token.SEMICOLON) {
			throw new SyntacticorException("return stmt need ;");
		}
		curToken = getToken();
		saveCode.addCode(CodeConversion.ret);
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		return returnExpr;
	}
	
	public void analyzeExprStmt(SaveCode saveCode) throws Exception{
		analyzeExpr(saveCode);
		if(curToken == null || curToken.getType() != Token.SEMICOLON) {
			throw new SyntacticorException("expr stmt need ;");
		}
		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
	}
	
	public expr analyzeExpr(SaveCode saveCode) throws Exception{
		expr returnExpr;
		expr lhs = analyzePrimaryExpr(saveCode);
		returnExpr = analyzeOPG(lhs,0,saveCode);
		return returnExpr;
	}
	
	public expr analyzePrimaryExpr(SaveCode saveCode) throws Exception{
		expr returnExpr;
		byte exprType;
		SymbolTable currentTable = this.getCurrentTable();
		Token tokenTemp;
		TableItem item;
		boolean constFlag = false;
		if(curToken.getType() == Token.IDENT) {
			//System.out.println(curToken.getValue()+"---"+curToken.getType());
			tokenTemp = curToken;
			curToken = getToken();
			//System.out.println(curToken.getValue()+"---"+curToken.getType());
			if(curToken != null && curToken.getType() == Token.L_PAREN) {
				unreadToken();
				//System.out.println(curToken.getValue()+"---"+curToken.getType());
				returnExpr = analyzeCallExpr(saveCode);
				exprType = returnExpr.type;
			}
			else {
				if(this.asFlag == true) {
					if(tokenTemp.getValue().toString().equals("int")) {
						exprType = TableItem.INT;
						returnExpr = new expr(tokenTemp.getValue().toString(),exprType,this.tokenDepth);
					}
					else if(tokenTemp.getValue().toString().equals("double")) { 
						exprType = TableItem.DOUBLE;
						returnExpr = new expr(tokenTemp.getValue().toString(),exprType,this.tokenDepth);
					}
					else {
						throw new SemanticException("not int or double after as");
					}
				}
				else {
					currentTable.print();
					item = currentTable.find(tokenTemp.getValue().toString());
					if(item == null) {
						if(currentTable == this.localTable) {
							item = this.globalTable.find(tokenTemp.getValue().toString());
							if(item == null) {
								throw new SemanticException("the variable is not defined");
							}
							else {
								saveCode.addCode(CodeConversion.globa(item.location));
							}
						}
					}
					else {
						if(curToken != null && curToken.getType() != Token.ASSIGN) {
							if(item.isParam) {
								saveCode.addCode(CodeConversion.arga(item.location+this.paramOffset));
							}
							else if(currentTable == this.localTable){
								//System.out.println("loc4");
								saveCode.addCode(CodeConversion.loca(item.location));
							}
							else {
								saveCode.addCode(CodeConversion.globa(item.location));
							}
						}
					}
					if(item.isConst) {
						constFlag = true;
					}
					exprType = item.type;
					returnExpr = new expr(item.getName(),exprType,this.tokenDepth);
					returnExpr.isIdent = true;
					if(constFlag == true) {
						returnExpr.setConst();
					}
					if(curToken != null && curToken.getType() != Token.ASSIGN) {
						saveCode.addCode(CodeConversion.load64);
					}
				}
			}
			
			return returnExpr;
		}
		else if(curToken.getType() == Token.L_PAREN) {
			curToken = getToken();
			//System.out.println(curToken.getValue()+"---"+curToken.getType());
			returnExpr = analyzeExpr(saveCode);
			if(curToken == null || curToken.getType() != Token.R_PAREN) {
				throw new SyntacticorException("group expr need )");
			}
			curToken = getToken();
			//System.out.println(curToken.getValue()+"---"+curToken.getType());
			return returnExpr;
		}
		else if(curToken.getType() == Token.MINUS) {
			curToken = getToken();
			//System.out.println(curToken.getValue()+"---"+curToken.getType());
			returnExpr = analyzeExpr(saveCode);
			if(returnExpr.type == TableItem.INT) {
				saveCode.addCode(CodeConversion.negi);
			}
			else {
				saveCode.addCode(CodeConversion.negf);
			}
			return returnExpr;
		}
		else if(curToken.getType() == Token.UINT_LITERAL
				||curToken.getType() == Token.DOUBLE_LITERAL
				||curToken.getType() == Token.CHAR_LITERAL
				||curToken.getType() == Token.STRING_LITERAL) {
			if(curToken.getType() == Token.UINT_LITERAL) {
				long temp = (long)Integer.valueOf(curToken.getValue().toString());
				saveCode.addCode(CodeConversion.push(temp));
			}
			else if(curToken.getType() == Token.CHAR_LITERAL) {
				long temp = (long)curToken.getValue().charAt(0);
				saveCode.addCode(CodeConversion.push(temp));
			}
			else if(curToken.getType() == Token.DOUBLE_LITERAL) {
				double temp = Double.valueOf(curToken.getValue().toString());
				saveCode.addCode(CodeConversion.push(this.transDouble(temp)));
			}
			else if(curToken.getType() == Token.STRING_LITERAL) {
				TableItem StringItem = new TableItem("STRING"+String.valueOf(this.globalTable.getTableList().size()),TableItem.STRING,0);
				StringItem.location = this.globalTable.getTableList().size();
				StringItem.StringValue = curToken.getValue().toString();
				StringItem.setConst();
				this.globalTable.addItem(StringItem);
				saveCode.addCode(CodeConversion.push(StringItem.location));
			}
			exprType =this.changeTokenTypeToTableType(curToken.getType());
			returnExpr = new expr(exprType,this.tokenDepth);
			curToken = getToken();
			//System.out.println(curToken.getValue()+"---"+curToken.getType());
			return returnExpr;
		}
		else {
			throw new SyntacticorException("left expr error");
		}
	}
	

	public expr analyzeCallExpr(SaveCode saveCode) throws Exception{
		byte exprType;
		TableItem funcItem;
		String funcName;
		expr returnExpr;
		SaveCode funcSave = new SaveCode();
		boolean stdFlag = false;
		if(curToken == null || curToken.getType() != Token.IDENT) {
			throw new SyntacticorException("call expr error");
		}
		funcItem = this.globalTable.find(curToken.getValue().toString());
		if(funcItem == null) {
			funcItem = this.getStdFuncItem(curToken.getValue().toString(),funcSave);
			if(funcItem == null || funcItem.returnType == TableItem.FAULT||funcItem.type != TableItem.FUNTION ) {
				throw new SemanticException("no func name "+curToken.getValue().toString()+" exits");
			}
			funcName = funcItem.getName();
			exprType = funcItem.returnType;
//			if(exprType == TableItem.INT || exprType == TableItem.DOUBLE) {
//				saveCode.addCode(CodeConversion.stackalloc(1));
//			}
//			else {
//				saveCode.addCode(CodeConversion.stackalloc(0));
//			}
			returnExpr = new expr(funcItem.getName(),funcItem.returnType,0);
			curToken = getToken();
			//System.out.println(curToken.getValue()+"---"+curToken.getType());
			if(curToken == null || curToken.getType() != Token.L_PAREN) {
				throw new SyntacticorException("left expr need (");
			}
			curToken = getToken();
			//System.out.println(curToken.getValue()+"---"+curToken.getType());

			if(curToken.getType() != Token.R_PAREN) {
				AnalyzeCallParamList(returnExpr,funcItem,saveCode);
			}
			if(curToken == null || curToken.getType() != Token.R_PAREN) {
				throw new SyntacticorException("left expr need )");
			}
			curToken = getToken();
			saveCode.addCode(funcSave);
			//System.out.println(curToken.getValue()+"---"+curToken.getType());
			return returnExpr;
		}
		if(funcItem == null || funcItem.returnType == TableItem.FAULT||funcItem.type != TableItem.FUNTION ) {
			throw new SemanticException("no func name "+curToken.getValue().toString()+" exits");
		}
		funcName = funcItem.getName();
		exprType = funcItem.returnType;
		if(exprType == TableItem.INT || exprType == TableItem.DOUBLE) {
			saveCode.addCode(CodeConversion.stackalloc(1));
		}
		else {
			saveCode.addCode(CodeConversion.stackalloc(0));
		}
		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		if(curToken == null || curToken.getType() != Token.L_PAREN) {
			throw new SyntacticorException("left expr need (");
		}
		returnExpr = new expr(funcName,exprType,this.tokenDepth);
		returnExpr.setConst();
		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());

		if(curToken.getType() != Token.R_PAREN) {
			AnalyzeCallParamList(returnExpr,funcItem,saveCode);
		}
		if(curToken == null || curToken.getType() != Token.R_PAREN) {
			throw new SyntacticorException("left expr need )");
		}
		saveCode.addCode(CodeConversion.call(getFunctionNum(funcName)));
		curToken = getToken();
		//System.out.println(curToken.getValue()+"---"+curToken.getType());
		return returnExpr;
	}
	
	private int getFunctionNum(String name) throws Exception {
        int i = 0;
        for(int j=0; j<this.globalTable.getTableList().size(); j++){
            if(this.globalTable.getTableList().get(j).type == TableItem.FUNTION && name.equals(this.globalTable.getTableList().get(j).getName())){
                return i;
            }
            else if(this.globalTable.getTableList().get(j).type == TableItem.FUNTION){
                i++;
            }
        }
        throw new SemanticException("Can not find the function.");
    }
	
	public void AnalyzeCallParamList(expr e,TableItem funcItem,SaveCode saveCode) throws Exception{
		expr exprTemp;
		exprTemp = analyzeExpr(saveCode);
		int i = 0;
		if(funcItem.paramList.size() <= i || funcItem.paramList.get(i) != exprTemp.type) {
			throw new SemanticException("the param of "+funcItem.getName()+" is error");
		}
		e.addParam(exprTemp.type);
		i++;
		while(curToken != null && curToken.getType() == Token.COMMA) {
			curToken = getToken();
			//System.out.println(curToken.getValue()+"---"+curToken.getType());
			exprTemp = analyzeExpr(saveCode);
			if(funcItem.paramList.size() < i || funcItem.paramList.get(i) != exprTemp.type) {
				throw new SemanticException("the param of "+funcItem.getName()+" is error");
			}
			
			e.addParam(exprTemp.type);
			i++;
		}
	}
	
	public expr analyzeOPG(expr lhs,int priority,SaveCode saveCode) throws Exception{
		expr returnExpr = lhs;
		if(curToken == null) {
			return lhs;
		}
		Token op;
		expr rhs; 
		while(curToken.isBinaryOp() && curToken.getPriority() >= priority) {
			op = curToken;
			//System.out.println(op.getType());
			if(op.getType() == Token.AS_KW) {
				this.asFlag = true;
			}
			else if(op.getType() == Token.ASSIGN) {
				//System.out.println("+++"+lhs.getName());
				TableItem itemTemp = this.localTable.find(lhs.getName());
				if(itemTemp != null) {
					if(itemTemp.isParam) {
						saveCode.addCode(CodeConversion.arga(itemTemp.location+this.paramOffset));
					}
					else {
						//System.out.println("loc1");
						saveCode.addCode(CodeConversion.loca(itemTemp.location));
					}
				}
				else {
					itemTemp = this.globalTable.find(lhs.getName());
					if(itemTemp != null) {
						saveCode.addCode(CodeConversion.globa(itemTemp.location));
					}
					else {
						throw new SemanticException("the variable is not defined");
					}
				}
			}
			curToken = getToken();
			//System.out.println(curToken.getValue()+"---"+curToken.getType());
			rhs = analyzePrimaryExpr(saveCode);
			this.asFlag = false;
			while(curToken.isBinaryOp()&&
				(curToken.getPriority()>op.getPriority()
				||curToken.isRightAssoc() && curToken.getPriority() == op.getPriority()
 				)
			) {
				//System.out.println("hjh");
				rhs = analyzeOPG(rhs,curToken.getPriority(),saveCode);
			}
			if(op.getType() == Token.AS_KW) {
				if(rhs.type != TableItem.DOUBLE && rhs.type != TableItem.INT) {
					throw new SyntacticorException("not ident after as");
				}
				if(lhs.type==TableItem.DOUBLE && rhs.type == TableItem.INT) {
					saveCode.addCode(CodeConversion.ftoi);
				}
				else if(lhs.type == TableItem.INT && rhs.type == TableItem.DOUBLE) {
					saveCode.addCode(CodeConversion.itof);
				}
				lhs.type = rhs.type;
				returnExpr = new expr(lhs.type,this.tokenDepth);
				returnExpr.isIdent = false;
			}
			else if(op.getType() == Token.ASSIGN) {
				if(lhs.isConst == true) {
					throw new SemanticException("const variable "+lhs.getName()+" can not be changed");
				}
				else if(lhs.type != rhs.type) {
					throw new SemanticException("not the same type before = and after =");
				}
				else if(lhs.isIdent == false) {
					throw new SemanticException("not the ident type before =");
				}
				returnExpr = new expr(TableItem.VOID,this.tokenDepth);
				returnExpr.isIdent = false;
				saveCode.addCode(CodeConversion.store64);
			}
			else {
				if(op.getType() == Token.PLUS) {
					if(lhs.type != rhs.type) {
						throw new SemanticException("not the same type before + and after +");
					}
					else if(lhs.type == TableItem.INT) {
						saveCode.addCode(CodeConversion.addi);
					}
				else {
						saveCode.addCode(CodeConversion.addf);
					}
					returnExpr = new expr(lhs.type,this.tokenDepth);
					returnExpr.isIdent = false;
				}
				else if(op.getType() == Token.MINUS) {
					if(lhs.type != rhs.type) {
						throw new SemanticException("not the same type before - and after -");
					}
					else if(lhs.type == TableItem.INT) {
						saveCode.addCode(CodeConversion.subi);
					}
					else {
						saveCode.addCode(CodeConversion.subf);
					}
					returnExpr = new expr(lhs.type,this.tokenDepth);
					returnExpr.isIdent = false;
				}
				else if(op.getType() == Token.MUL) {
					if(lhs.type != rhs.type) {
						throw new SemanticException("not the same type before * and after *");
					}
					else if(lhs.type == TableItem.INT) {
						saveCode.addCode(CodeConversion.muli);
					}
					else {
						saveCode.addCode(CodeConversion.mulf);
					}
					returnExpr = new expr(lhs.type,this.tokenDepth);
					returnExpr.isIdent = false;
				}
				else if(op.getType() == Token.DIV) {
					if(lhs.type != rhs.type) {
						throw new SemanticException("not the same type before / and after /");
					}
					else if(lhs.type == TableItem.INT) {
						saveCode.addCode(CodeConversion.divi);
					}
					else {
						saveCode.addCode(CodeConversion.divf);
					}
					returnExpr = new expr(lhs.type,this.tokenDepth);
					returnExpr.isIdent = false;
				}
				else if(op.getType() == Token.EQ) {
					if(lhs.type != rhs.type) {
						throw new SemanticException("not the same type before == and after ==");
					}
					else if(lhs.type == TableItem.INT) {
						saveCode.addCode(CodeConversion.cmpi);
					}
					else {
						saveCode.addCode(CodeConversion.cmpf);
					}
					saveCode.addCode(CodeConversion.not);
					returnExpr = new expr(TableItem.INT,this.tokenDepth);
					returnExpr.isIdent = false;
				}
				else if(op.getType() == Token.NEQ) {
					if(lhs.type != rhs.type) {
						throw new SemanticException("not the same type before != and after !=");
					}
					else if(lhs.type == TableItem.INT) {
						saveCode.addCode(CodeConversion.cmpi);
					}
					else {
						saveCode.addCode(CodeConversion.cmpf);
					}
					returnExpr = new expr(TableItem.INT,this.tokenDepth);
					returnExpr.isIdent = false;
				}
				else if(op.getType() == Token.LT) {
					if(lhs.type != rhs.type) {
						throw new SemanticException("not the same type before < and after <");
					}
					else if(lhs.type == TableItem.INT) {
						saveCode.addCode(CodeConversion.cmpi);
					}
					else {
						saveCode.addCode(CodeConversion.cmpf);
					}
					saveCode.addCode(CodeConversion.setlt);
					returnExpr = new expr(TableItem.INT,this.tokenDepth);
					returnExpr.isIdent = false;
				}
				else if(op.getType() == Token.GT) {
					if(lhs.type != rhs.type) {
						throw new SemanticException("not the same type before > and after >");
					}
					else if(lhs.type == TableItem.INT) {
						saveCode.addCode(CodeConversion.cmpi);
					}
					else {
						saveCode.addCode(CodeConversion.cmpf);
					}
					saveCode.addCode(CodeConversion.setgt);
					returnExpr = new expr(TableItem.INT,this.tokenDepth);
					returnExpr.isIdent = false;
				}
				else if(op.getType() == Token.LE) {
					if(lhs.type != rhs.type) {
						throw new SemanticException("not the same type before <= and after <=");
					}
					else if(lhs.type == TableItem.INT) {
						saveCode.addCode(CodeConversion.cmpi);
					}
					else {
						saveCode.addCode(CodeConversion.cmpf);
					}
					saveCode.addCode(CodeConversion.setgt);
					saveCode.addCode(CodeConversion.not);
					returnExpr = new expr(TableItem.INT,this.tokenDepth);
					returnExpr.isIdent = false;

				}
				else if(op.getType() == Token.GE) {
					if(lhs.type != rhs.type) {
						throw new SemanticException("not the same type before >= and after >=");
					}
					else if(lhs.type == TableItem.INT) {
						saveCode.addCode(CodeConversion.cmpi);
					}
					else {
						saveCode.addCode(CodeConversion.cmpf);
					}
					saveCode.addCode(CodeConversion.setlt);
					saveCode.addCode(CodeConversion.not);
					returnExpr = new expr(TableItem.INT,this.tokenDepth);
					returnExpr.isIdent = false;
				}
				else {
					throw new SemanticException("this is not a valid oparator");
				}
				
			}
			lhs = returnExpr;
		}
		//System.out.println("the op is "+op.getType())
		return returnExpr;
	}
	
	
	public boolean compareValidType(byte lhs,byte rhs) throws Exception {
		byte lhsTemp;
		byte rhsTemp;
		if(lhs == Token.UINT_LITERAL || lhs == Token.CHAR_LITERAL) {
			lhsTemp = TableItem.INT;
		}
		else if(lhs == Token.DOUBLE_LITERAL) {
			lhsTemp = TableItem.DOUBLE;
		}
		else {
			throw new SemanticException("not int or double type for a expr left");
		}
		
		if(rhs == Token.UINT_LITERAL || rhs == Token.CHAR_LITERAL) {
			rhsTemp = TableItem.INT;
		}
		else if(rhs == Token.DOUBLE_LITERAL) {
			rhsTemp = TableItem.DOUBLE;
		}
		else {
			throw new SemanticException("not int or double type for a expr right");
		}
		if(lhsTemp == rhsTemp) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public byte changeTokenTypeToTableType(byte type) {
		if(type == Token.UINT_LITERAL||type == Token.CHAR_LITERAL||type == Token.STRING_LITERAL ) {
			return TableItem.INT;
		}
		else if(type == Token.DOUBLE_LITERAL) {
			return TableItem.DOUBLE;
		}
		return type;
	}
	
	public TableItem getStdFuncItem(String funcName,SaveCode saveCode) throws Exception{
		TableItem returnItem;
		byte returnType;
		byte type = TableItem.FUNTION;
		byte funcType;
		switch(funcName){
		case"getint":
			returnType = TableItem.INT;
			returnItem = new TableItem(funcName,type,0,returnType);
			saveCode.addCode(CodeConversion.scani);
			break;
		case"getdouble":
			returnType = TableItem.DOUBLE;
			returnItem = new TableItem(funcName,type,0,returnType);
			saveCode.addCode(CodeConversion.scanf);
			break;
		case"getchar":
			returnType = TableItem.INT;
			returnItem = new TableItem(funcName,type,0,returnType);
			saveCode.addCode(CodeConversion.scanc);
			break;
		case"putint":
			returnType = TableItem.VOID;
			returnItem = new TableItem(funcName,type,0,returnType);
			saveCode.addCode(CodeConversion.printi);
			funcType = TableItem.INT;
			returnItem.addParam(funcType);
			break;
		case"putdouble":
			returnType = TableItem.VOID;
			returnItem = new TableItem(funcName,type,0,returnType);
			saveCode.addCode(CodeConversion.printf);
			funcType = TableItem.DOUBLE;
			returnItem.addParam(funcType);
			break;
		case"putchar":
			returnType = TableItem.VOID;
			returnItem = new TableItem(funcName,type,0,returnType);
			funcType = TableItem.INT;
			returnItem.addParam(funcType);
			saveCode.addCode(CodeConversion.printc);
			break;
		case"putstr":
			returnType = TableItem.VOID;
			returnItem = new TableItem(funcName,type,0,returnType);
			funcType = TableItem.INT;
			returnItem.addParam(funcType);
			saveCode.addCode(CodeConversion.prints);
			break;
		case"putln":
			returnType = TableItem.VOID;
			returnItem = new TableItem(funcName,type,0,returnType);
			saveCode.addCode(CodeConversion.println);
			break;
		default:
			returnItem = null;
		}
		return returnItem;
	}
	
	public int typeToSlot(byte type) throws Exception{
		switch(type) {
		case TableItem.DOUBLE:
			return 1;
		case TableItem.INT:
			return 1;
		case TableItem.VOID:
			return 0;
		default:
			throw new SemanticException("the type is not double or int");
		}
	}
	
	private long transDouble(double num){
        return Double.doubleToLongBits(num);
    }

}

class expr {
	private String name;
	public byte type;
	public int depth;
	public boolean isConst = false;
	private ArrayList<Byte> paramList = new ArrayList<Byte>();
	public boolean isParam = false;
	public int location = 0;
	public boolean isIdent = false;
	
	public String getName() {
		return this.name;
	}
	
	public expr(byte type,int depth) {
		this.name = "DEFAULT_NAME";
		this.type = type;
		this.depth = depth;
	}
	
	public expr(String name ,byte type,int depth) {
		this.name = name;
		this.type = type;
		this.depth = depth;
	}
	
	
	public void setConst() {
		isConst = true;
	}
	
	public void setParam() {
		isParam = true;
	}
	
	public void addParam(byte type) throws Exception{
		switch(type){
		case TableItem.DOUBLE:
		case TableItem.INT:
		case TableItem.VOID:
			this.paramList.add(type);
			break;
		default:
			throw new SemanticException("bad type for function param");
		}
	}	
}