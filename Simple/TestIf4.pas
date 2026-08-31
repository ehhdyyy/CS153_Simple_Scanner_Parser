PROGRAM TestIf4;
    
BEGIN
    i := 1;
    j := 2;
    
    if i = j then i := 33 
             else if i <= j then i := 44 
                            else if i = j then i := 55 
                                          else i := 6;
END.
