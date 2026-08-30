/**
 * Symbol table class for a simple interpreter.
 * 
 * (c) 2026 by Ronald Mak
 * Department of Computer Science
 * San Jose State University
 */
package intermediate;

import java.util.HashMap;

public class Symtab
{
    private HashMap<String, SymtabEntry> contents = new HashMap<>();
    
    /**
     * Make an entry.
     * @param name the entry's name.
     */
    public SymtabEntry enter(String name) 
    { 
        SymtabEntry entry = new SymtabEntry(name);
        contents.put(name, entry);
        
        return entry;
    }
    
    /**
     * Look up an entry.
     * @param name the entry's name.
     * @return the entry or null if it's not in the symbol table.
     */
    public SymtabEntry lookup(String name) { return contents.get(name); }
}
