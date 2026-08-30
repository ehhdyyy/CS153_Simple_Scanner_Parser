PROGRAM SquareRootTable;

BEGIN
    writeln('Square Root Table':60);
    writeln;
    write('     ');
    
    i := 0;
    REPEAT
        x := i/10;
        write(x:10:1);
        i := i + 1
    UNTIL i = 10;
    writeln;
    
    row := 1;
    REPEAT
        write(row:5:0);
        
        i := 0;
        REPEAT
            number := row + i/10;
            root   := number;
            prev   := root;
            
            REPEAT
                root := (number/root + root)/2;
                diff := prev - root;
                prev := root;
            UNTIL diff < 0.000001;
            
            write(root:10:6);
            i := i + 1
        UNTIL i = 10;
        
        writeln;
        row := row + 1
    UNTIL row = 26;
END.