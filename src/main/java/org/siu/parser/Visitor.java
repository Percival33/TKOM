package org.siu.parser;

import org.siu.parser.statement.IfStatement;
import org.siu.parser.statement.WhileStatement;

/**
 * STATEMENT               = IF_STATEMENT
 *                         | WHILE_STATEMENT
 *                         | DECLARATION
 *                         | RETURN_STATEMENT
 *                         | ASSINGMENT
 *                         | MATCH
 *                         | FN_CALL;
 */
public interface Visitor {
    // TODO: implement rest of statements
    // TODO: FN_CALL - is it really a statement??
    void visit(WhileStatement statement);
    void visit(IfStatement statement);
}
