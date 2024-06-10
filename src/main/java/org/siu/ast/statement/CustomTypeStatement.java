package org.siu.ast.statement;

import org.siu.ast.Parameter;
import org.siu.ast.Statement;

import java.util.List;

public interface CustomTypeStatement extends Statement {
    List<Parameter> getParameters();
    default boolean isVariant() {
        return false;
    }
    default boolean isStruct() {
        return false;
    }
}
