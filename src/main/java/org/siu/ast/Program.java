package org.siu.ast;

import java.util.Map;

public class Program {
    // TODO: change name of variable
    private final Map<String, ProgramElement> programElement;

    public Program(Map<String, ProgramElement> programElement) {
        this.programElement = programElement;
    }
}
