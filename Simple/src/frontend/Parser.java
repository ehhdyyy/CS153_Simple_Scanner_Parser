/**
 * Parser class for a simple interpreter.
 * 
 * (c) 2026 by Ronald Mak
 * Department of Computer Science
 * San Jose State University
 */
package frontend;

import java.util.HashSet;

import intermediate.*;
import static frontend.Token.TokenType.*;
import static intermediate.Node.NodeType.*;

public class Parser
{
    private Scanner scanner;
    private Symtab symtab;
    private Token currentToken;
    private int lineNumber;
    private int errorCount;
    
    public Parser(Scanner scanner, Symtab symtab)
    {
        this.scanner = scanner;
        this.symtab  = symtab;
        this.currentToken = null;
        this.lineNumber = 1;
        this.errorCount = 0;
    }
    
    public int errorCount() { return errorCount; }
    
    public Node parseProgram()
    {
        Node programNode = new Node(Node.NodeType.PROGRAM);
        
        // First token!
        currentToken = scanner.nextToken();
        
        if (currentToken.type == Token.TokenType.PROGRAM) 
        {
            // Consume PROGRAM.
            currentToken = scanner.nextToken();  
        }
        else syntaxError("Expecting PROGRAM");
        
        if (currentToken.type == IDENTIFIER) 
        {
            String programName = currentToken.text;
            symtab.enter(programName);
            programNode.text = programName;
            
            // Consume program name.
            currentToken = scanner.nextToken();
        }
        else syntaxError("Expecting program name");
        
        if (currentToken.type == SEMICOLON) 
        {
            // Consume ;
            currentToken = scanner.nextToken();
        }
        else syntaxError("Missing ;");
        
        if (currentToken.type != BEGIN) syntaxError("Expecting BEGIN");
        
        // The PROGRAM node adopts the COMPOUND tree.
        programNode.adopt(parseCompoundStatement());
        
        if (currentToken.type != PERIOD) syntaxError("Expecting .");
        return programNode;
    }
    
    private static HashSet<Token.TokenType> statementStarters;
    private static HashSet<Token.TokenType> statementFollowers;
    private static HashSet<Token.TokenType> relationalOperators;
    private static HashSet<Token.TokenType> simpleExpressionOperators;
    private static HashSet<Token.TokenType> termOperators;

    static
    {
        statementStarters = new HashSet<Token.TokenType>();
        statementFollowers = new HashSet<Token.TokenType>();
        relationalOperators = new HashSet<Token.TokenType>();
        simpleExpressionOperators = new HashSet<Token.TokenType>();
        termOperators = new HashSet<Token.TokenType>();
        
        // Tokens that can start a statement.
        statementStarters.add(BEGIN);
        statementStarters.add(IDENTIFIER);
        statementStarters.add(REPEAT);
        statementStarters.add(Token.TokenType.WRITE);
        statementStarters.add(Token.TokenType.WRITELN);
        
        // Tokens that can immediately follow a statement.
        statementFollowers.add(SEMICOLON);
        statementFollowers.add(END);
        statementFollowers.add(UNTIL);
        statementFollowers.add(END_OF_FILE);
        
        relationalOperators.add(EQUALS);
        relationalOperators.add(LESS_THAN);
        
        simpleExpressionOperators.add(PLUS);
        simpleExpressionOperators.add(MINUS);
        
        termOperators.add(STAR);
        termOperators.add(SLASH);
    }
    
    private Node parseStatement()
    {
        Node stmtNode = null;
        int savedLineNumber = currentToken.lineNumber;
        lineNumber = savedLineNumber;
        
        switch (currentToken.type)
        {
            case IDENTIFIER : stmtNode = parseAssignmentStatement(); break;
            case BEGIN :      stmtNode = parseCompoundStatement();   break;
            case REPEAT :     stmtNode = parseRepeatStatement();     break;
            case WRITE :      stmtNode = parseWriteStatement();      break;
            case WRITELN :    stmtNode = parseWritelnStatement();    break;
            case SEMICOLON :  stmtNode = null; break;  // empty statement
            
            default : syntaxError("Unexpected token");
        }
        
        if (stmtNode != null) stmtNode.lineNumber = savedLineNumber;
        return stmtNode;
    }
    
    private Node parseAssignmentStatement()
    {
        // The current token should now be the left-hand-side variable name.
        
        Node assignmentNode = new Node(ASSIGN);
        
        // The assignment node adopts the variable node as its first child.
        Node lhsNode = new Node(VARIABLE);
        String variableName = currentToken.text;
        SymtabEntry variableEntry = symtab.enter(variableName.toLowerCase());
        
        lhsNode.text  = variableName;
        lhsNode.entry = variableEntry;
        assignmentNode.adopt(lhsNode);
        
        // Consume the LHS variable.
        currentToken = scanner.nextToken();  
        
        if (currentToken.type == COLON_EQUALS) 
        {
            // Consume :=
            currentToken = scanner.nextToken();  
        }
        else syntaxError("Missing :=");
        
        // The assignment node adopts the expression node 
        // as its second child.
        Node rhsNode = parseExpression();
        assignmentNode.adopt(rhsNode);
        
        return assignmentNode;
    }
    
    private Node parseCompoundStatement()
    {
        Node compoundNode = new Node(COMPOUND);
        compoundNode.lineNumber = currentToken.lineNumber;
        
        // Consume BEGIN
        currentToken = scanner.nextToken();
        
        parseStatementList(compoundNode, END);    
        
        if (currentToken.type == END) 
        {
            // Consume END
            currentToken = scanner.nextToken();  
        }
        else syntaxError("Expecting END");
        
        return compoundNode;
    }
    
    private void parseStatementList(Node parentNode, 
                                    Token.TokenType terminalType)
    {
        while (   (currentToken.type != terminalType) 
               && (currentToken.type != END_OF_FILE))
        {
            Node stmtNode = parseStatement();
            if (stmtNode != null) parentNode.adopt(stmtNode);
            
            // A semicolon separates statements.
            if (currentToken.type == SEMICOLON)
            {
                while (currentToken.type == SEMICOLON)
                {
                    // Consume ;
                    currentToken = scanner.nextToken();  
                }
            }
            else if (statementStarters.contains(currentToken.type))
            {
                syntaxError("Missing ;");
            }
        }
    }

    private Node parseRepeatStatement()
    {
        // The current token should now be REPEAT.
        
        // Create a LOOP node.
        Node loopNode = new Node(LOOP);
        
        // Consume REPEAT
        currentToken = scanner.nextToken();  
        
        parseStatementList(loopNode, UNTIL);    
        
        if (currentToken.type == UNTIL) 
        {
            // Create a TEST node.
            // It adopts the test expression node.
            Node testNode = new Node(TEST);
            lineNumber = currentToken.lineNumber;
            testNode.lineNumber = lineNumber;
            
            // Consume UNTIL.
            currentToken = scanner.nextToken(); 
            
            testNode.adopt(parseExpression());
            
            // The LOOP node adopts the TEST node
            // as its final child.
            loopNode.adopt(testNode);
        }
        else syntaxError("Expecting UNTIL");
        
        return loopNode;
    }
    
    private Node parseWriteStatement()
    {
        // The current token should now be WRITE.
        
        // Create a WRITE node.
        // It adopts the variable or string node.
        Node writeNode = new Node(Node.NodeType.WRITE);
        
        // Consume WRITE.
        currentToken = scanner.nextToken();  
        
        parseWriteArguments(writeNode);
        if (writeNode.children.size() == 0)
        {
            syntaxError("Invalid WRITE statement");
        }
        
        return writeNode;
    }
    
    private Node parseWritelnStatement()
    {
        // The current token should now be WRITELN.
        
        // Create a WRITELN node.
        // It adopts the variable or string node.
        Node writelnNode = new Node(Node.NodeType.WRITELN);
        
        // Consume WRITELN.
        currentToken = scanner.nextToken();  
        
        if (currentToken.type == LPAREN) parseWriteArguments(writelnNode);
        return writelnNode;
    }
    
    private void parseWriteArguments(Node node)
    {
        // The current token should now be (
        
        boolean hasArgument = false;
        
        if (currentToken.type == LPAREN) 
        {
            // Consume (
            currentToken = scanner.nextToken();
        }
        else syntaxError("Missing left parenthesis");
        
        if (currentToken.type == IDENTIFIER)
        {
            node.adopt(parseVariable());
            hasArgument = true;
        }
        else if (currentToken.type == STRING)
        {
            node.adopt(parseStringConstant());
            hasArgument = true;
        }
        else syntaxError("Invalid WRITE or WRITELN statement");
        
        // Look for a field width and a count of decimal places.
        if (hasArgument)
        {
            if (currentToken.type == COLON) 
            {
			    // Consume :
                currentToken = scanner.nextToken();
                
                if (currentToken.type == INTEGER)
                {
                    // Field width
                    node.adopt(parseIntegerConstant());
                    
                    if (currentToken.type == COLON) 
                    {
                        // Consume :
                        currentToken = scanner.nextToken();
                        
                        if (currentToken.type == INTEGER)
                        {
                            // Count of decimal places.
                            node.adopt(parseIntegerConstant());
                        }
                        else syntaxError("Invalid count of decimal places");
                    }
                }
                else syntaxError("Invalid field width");
            }
        }
        
        if (currentToken.type == RPAREN) 
        {
            // Consume )
            currentToken = scanner.nextToken();
        }
        else syntaxError("Missing right parenthesis");
    }

    private Node parseExpression()
    {
        // The current token should now be an identifier or a number.
        
        // The expression's root node.
        Node exprNode = parseSimpleExpression();
        
        // The current token might now be a relational operator.
        if (relationalOperators.contains(currentToken.type))
        {
            Token.TokenType tokenType = currentToken.type;
            Node opNode = tokenType == EQUALS    ? new Node(EQ)
                        : tokenType == LESS_THAN ? new Node(LT)
                        :                          null;
            
            // Consume relational operator.
            currentToken = scanner.nextToken();  
            
            // The relational operator node adopts the first 
            // simple expression node as its first child and the
            // second simple expression node as its second child. 
            // Then it becomes the expression's root node.
            if (opNode != null)
            {
                opNode.adopt(exprNode);
                opNode.adopt(parseSimpleExpression());
                exprNode = opNode;
            }
        }
        
        return exprNode;
    }
    
    private Node parseSimpleExpression()
    {
        // The current token should now be an identifier or a number.
        
        // The simple expression's root node.
        Node simpExprNode = parseTerm();
        
        // Keep parsing more terms as long as the current token
        // is a + or - operator.
        while (simpleExpressionOperators.contains(currentToken.type))
        {
            Node opNode = currentToken.type == PLUS ? new Node(ADD)
                                                    : new Node(SUBTRACT);
            // Consume the operator.
            currentToken = scanner.nextToken();  

            // The add or subtract node adopts the first term node as its
            // first child and the next term node as its second child. 
            // Then it becomes the simple expression's root node.
            opNode.adopt(simpExprNode);
            opNode.adopt(parseTerm());
            simpExprNode = opNode;
        }
        
        return simpExprNode;
    }
    
    private Node parseTerm()
    {
        // The current token should now be an identifier or a number.
        
        // The term's root node.
        Node termNode = parseFactor();
        
        // Keep parsing more factor as long as the current token
        // is a * or / operator.
        while (termOperators.contains(currentToken.type))
        {
            Node opNode = currentToken.type == STAR ? new Node(MULTIPLY)
                                                    : new Node(DIVIDE);
            // Consume the operator.
            currentToken = scanner.nextToken();  

            // The multiply or dive node adopts the first factor node as its
            // as its first child and the next factor node as its second child. 
            // Then it becomes the term's root node.
            opNode.adopt(termNode);
            opNode.adopt(parseFactor());
            termNode = opNode;
        }
        
        return termNode;
    }
    
    private Node parseFactor()
    {   
        // The current token should now be an identifier or a number or (
        
        if      (currentToken.type == IDENTIFIER) return parseVariable();
        else if (currentToken.type == INTEGER)    return parseIntegerConstant();
        else if (currentToken.type == REAL)       return parseRealConstant();
        
        else if (currentToken.type == LPAREN)
        {
            // Consume (
            currentToken = scanner.nextToken(); 
            
            Node exprNode = parseExpression();
            
            if (currentToken.type == RPAREN)
            {
                // Consume )
                currentToken = scanner.nextToken();  
            }
            else syntaxError("Expecting )");
            
            return exprNode;
        }
        
        else syntaxError("Unexpected token");
        return null;
    }
    
    private Node parseVariable()
    {
        // The current token should now be an identifier.
        
        // Has the variable been "declared"?
        String variableName = currentToken.text;
        SymtabEntry variableEntry = symtab.lookup(variableName.toLowerCase());
        if (variableEntry == null) semanticError("Undeclared identifier");
        
        Node node  = new Node(VARIABLE);
        node.text  = variableName;
        node.entry = variableEntry;
        
        // Consume the identifier.  
        currentToken = scanner.nextToken();  
        
        return node;
    }

    private Node parseIntegerConstant()
    {
        // The current token should now be a number.
        
        Node integerNode = new Node(INTEGER_CONSTANT);
        integerNode.value = currentToken.value;
        
        // Consume the number.
        currentToken = scanner.nextToken();     
        
        return integerNode;
    }

    private Node parseRealConstant()
    {
        // The current token should now be a number.
        
        Node realNode = new Node(REAL_CONSTANT);
        realNode.value = currentToken.value;
        
        // Consume the number.
        currentToken = scanner.nextToken(); 
        
        return realNode;
    }
    
    private Node parseStringConstant()
    {
        // The current token should now be STRING.
        
        Node stringNode = new Node(STRING_CONSTANT);
        stringNode.value = currentToken.value;
        
        // Consume the string.
        currentToken = scanner.nextToken();         
        return stringNode;
    }

    private void syntaxError(String message)
    {
        System.out.println("SYNTAX ERROR at line " + lineNumber 
                           + ": " + message + " at '" + currentToken.text + "'");
        errorCount++;
        
        // Recover by skipping the rest of the statement.
        // Skip to a statement follower token.
        while (! statementFollowers.contains(currentToken.type))
        {
            currentToken = scanner.nextToken();
        }
    }
    
    private void semanticError(String message)
    {
        System.out.println("SEMANTIC ERROR at line " + lineNumber 
                           + ": " + message + " at '" + currentToken.text + "'");
        errorCount++;
    }
}
