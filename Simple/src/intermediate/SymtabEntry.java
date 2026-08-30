/**
 * Symbol table entry class for a simple interpreter.
 * 
 * (c) 2020 by Ronald Mak
 * Department of Computer Science
 * San Jose State University
 */
package intermediate;

public class SymtabEntry
{
    private String name;
    private Double value;
    
    /**
     * Constructor.
     * @param name the entry's name.
     */
    public SymtabEntry(String name)
    {
        this.name  = name;
        this.value = 0.0;
    }
    
    /**
     * Getter.
     * @return the entry's name.
     */
    public String getName()  { return name;  }

    /**
     * Getter.
     * @return the entry's valuel
     */
    public Double getValue() { return value; }
    
    /**
     * Set the entry's value.
     * @param value the value to set.
     */
    public void setValue(Double value) { this.value = value; }
}
