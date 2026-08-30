/**
 * Parse tree node class for a simple interpreter.
 * 
 * (c) 2020 by Ronald Mak
 * Department of Computer Science
 * San Jose State University
 */
package intermediate;

import java.util.ArrayList;

import static intermediate.Node.NodeType.*;

public class Node
{
    public enum NodeType
    {
        PROGRAM, COMPOUND, ASSIGN, LOOP, TEST, WRITE, WRITELN,
        ADD, SUBTRACT, MULTIPLY, DIVIDE, EQ, LT,
        VARIABLE, INTEGER_CONSTANT, REAL_CONSTANT, STRING_CONSTANT
    }

    public NodeType type;
    public int lineNumber;
    public String text;
    public SymtabEntry entry;
    public Object value;
    public ArrayList<Node> children;
    
    public Node(NodeType type)
    {
        this.type = type;
        this.lineNumber = 0;
        this.text = null;
        this.value = null;
        this.children = new ArrayList<>();
    }
    
    public void adopt(Node child) 
    { 
        children.add(child); 
    }
    
    public ArrayList<Node> getChildren() { return children; }
    
    public String getDisplay() 
    {
        String str = type.name() + " ";
        
        if      (type == PROGRAM)          str += text;
        else if (type == VARIABLE)         str += text;
        else if (type == INTEGER_CONSTANT) str += (long) value;
        else if (type == REAL_CONSTANT)    str += value;
        else if (type == STRING_CONSTANT)  str += "'" + (String) value + "'";
        
        if (lineNumber > 0) str = lineNumber + ": " + str;
        
        return str;
    }
}
