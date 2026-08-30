/**
 * A simple interpreter to demonstrate scanning, parsing, symbol tables,
 * parse trees, and interpreted program execution.
 * 
 * (c) 2026 by Ronald Mak
 * Department of Computer Science
 * San Jose State University
 */
import ajs.printutils.*;

import frontend.*;
import intermediate.*;
import backend.*;

import static frontend.Token.TokenType.*;

public class Simple
{
    public static void main(String args[])
    {
        if (args.length != 2)
        {
            System.out.println("Usage: simple -{scan, parse, execute} " +
                               "sourceFileName");
            System.exit(-1);
        }
        
        String operation      = args[0];  // -scan, -parse, or -execute
        String sourceFileName = args[1];
        
        Source source = new Source(sourceFileName);
        
        if (operation.equalsIgnoreCase("-scan"))
        {
            testScanner(source);
        }
        else if (operation.equalsIgnoreCase("-parse"))
        {
            testParser(new Scanner(source), new Symtab());
        }
        else if (operation.equalsIgnoreCase("-execute"))
        {
            Symtab symtab = new Symtab();
            executeProgram(new Parser(new Scanner(source), symtab), symtab);
        }
    }
    
    /**
     * Test the scanner.
     * @param source the input source.
     */
    private static void testScanner(Source source)
    {
        System.out.println("Tokens:");
        System.out.println();
        
        // Create the scanner.
        Scanner scanner = new Scanner(source);  
        
        // Loop to extract and print each token
        // one at a time from the source.
        for (Token token = scanner.nextToken(); 
             token.type != END_OF_FILE; 
             token = scanner.nextToken())
        {
            System.out.printf("%14s : %s\n", token.type, token.text);
        }
    }
    
    /**
     * Test the parser.
     * @param scanner the scanner.
     * @param symtab the symbol table.
     */
    private static void testParser(Scanner scanner, Symtab symtab)
    {
        // Create the parser and parse the program.
        Parser parser = new Parser(scanner, symtab);
        Node programNode = parser.parseProgram();
        
        // If no errors, print the parse tree.
        int errorCount = parser.errorCount();
        if (errorCount == 0)
        {
            System.out.println("Parse tree:");
            System.out.println();
            
            var ppt = new PrettyPrintTree<Node>(Node::getChildren,
                                                Node::getDisplay);
            ppt.display(programNode);
        }
        else
        {
            System.out.println();
            System.out.println("There were " + errorCount + " syntax errors.");
        }
    }
    
    /**
     * Test the executor.
     * @param parser the parser.
     * @param symtab the symbol table.
     */
    private static void executeProgram(Parser parser, Symtab symtab)
    {
        // Build the parse tree
        Node programNode = parser.parseProgram();  
        
        // If no errors, create the executor
        // and execute the program using the parse tree.
        int errorCount = parser.errorCount();
        if (errorCount == 0)
        {
            Executor executor = new Executor(symtab);
            executor.visit(programNode);
        }
        else
        {
            System.out.println();
            System.out.println("There were " + errorCount + " errors.");
        }
    }
}
