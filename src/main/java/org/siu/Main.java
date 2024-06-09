package org.siu;

import lombok.extern.slf4j.Slf4j;
import org.siu.ast.Program;
import org.siu.error.ErrorHandler;
import org.siu.error.ErrorHandlerImpl;
import org.siu.interpreter.FunctionReturnTypeVisitor;
import org.siu.interpreter.InterpretingVisitor;
import org.siu.interpreter.error.InterpreterException;
import org.siu.interpreter.error.InvalidReturnTypeException;
import org.siu.lexer.FilterCommentsLexer;
import org.siu.lexer.Lexer;
import org.siu.lexer.LexerImpl;
import org.siu.parser.Parser;

import java.io.*;

@Slf4j
public class Main {
    public static void main(final String[] args) {
        if (args.length != 1) {
            log.error("Usage: java -jar <jar-file> <path-to-source-file>");
            System.exit(1);
        }

        String filePath = args[0];
        final ErrorHandler errorHandler = new ErrorHandlerImpl();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            Lexer lexer = new LexerImpl(reader, errorHandler);
            Lexer filteredLexer = new FilterCommentsLexer(lexer);
            Parser parser = new Parser(filteredLexer, errorHandler);
            var program = parser.buildProgram();

            checkReturnTypes(program);
            interpretProgram(program);
        } catch (InvalidReturnTypeException e) {
            log.error("Invalid return type: {}", e.getMessage());
            System.exit(2);
        } catch (FileNotFoundException e) {
            log.error("File not found: {}", filePath);
            System.exit(3);
        } catch (IOException e) {
            log.error("Error reading file: {}", filePath);
            System.exit(4);
        } catch (InterpreterException e) {
            log.error("Interpreter error: {}", e.getMessage());
            System.exit(5);
        } catch (UnsupportedOperationException e) {
            log.error("Unsupported operation: {}", e.getMessage());
            System.exit(6);
        } catch (RuntimeException e) {
            log.error("Error while interpreting: {}", e.getMessage());
            System.exit(7);
        }
    }

    private static void checkReturnTypes(Program program) throws InvalidReturnTypeException {
        FunctionReturnTypeVisitor returnTypeVisitor = new FunctionReturnTypeVisitor(program, System.out);
        returnTypeVisitor.execute();
        if (returnTypeVisitor.hasErrorOccurred()) {
            throw new RuntimeException(returnTypeVisitor.getErrorDetails());
        }
    }

    private static void interpretProgram(Program program) throws InterpreterException {
        InterpretingVisitor visitor = new InterpretingVisitor(program, System.out);
        visitor.execute();
    }
}
