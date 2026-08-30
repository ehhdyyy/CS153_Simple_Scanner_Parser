/**
 * Source class for a simple interpreter.
 * 
 * (c) 2026 by Ronald Mak
 * Department of Computer Science
 * San Jose State University
 */
package frontend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Source
{
    public static final char EOL = '\n';
    public static final char EOF = '\0';
    
    private BufferedReader source;
    
    private int lineNumber = 1;  // current source line number
    private char currentChar;    // current source character
    
    /**
     * Constructor
     * @param sourceFileName the source file name.
     */
    public Source(String sourceFileName)
    {
        try
        {
            source = new BufferedReader(new FileReader(sourceFileName));
            
            // Read the first character of the file
            currentChar = nextChar();  
        }
        catch(IOException ex)
        {
            System.out.printf("*** ERROR: Failed to open " + sourceFileName);
            System.exit(-1);
        }
    }
    
    /**
     * Getter.
     * @return the current source line number.
     */
    public int lineNumber() { return lineNumber; }
    
    /**
     * Getter.
     * @return the current source character.
     */
    public char currentChar() { return currentChar; }
    
    /**
     * Read and return the next input source character.
     * @return the character, or EOF if at the end of the file.
     */
    public char nextChar()
    {
        try 
        {
            int value = source.read();
            currentChar = value > -1 ? (char) value : EOF;
            if (currentChar == EOL) lineNumber++;
            
            return currentChar;
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            System.exit(-1);
        }
        
        return EOF;
    }
}
