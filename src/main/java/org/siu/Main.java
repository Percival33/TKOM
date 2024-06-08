package org.siu;

import lombok.extern.slf4j.Slf4j;
import org.siu.error.ErrorHandler;
import org.siu.error.ErrorHandlerImpl;
import org.siu.interpreter.InterpretingVisitor;
import org.siu.interpreter.error.InterpreterException;
import org.siu.lexer.FilterCommentsLexer;
import org.siu.lexer.Lexer;
import org.siu.lexer.LexerImpl;
import org.siu.parser.Parser;

import java.io.*;

@Slf4j
public class Main {
    public static void main(final String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java -jar <jar-file> <path-to-source-file>");
            System.exit(1);
        }

        String filePath = args[0];
        final ErrorHandler errorHandler = new ErrorHandlerImpl();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            Lexer lexer = new LexerImpl(reader, errorHandler);
            Lexer filteredLexer = new FilterCommentsLexer(lexer);
            Parser parser = new Parser(filteredLexer, errorHandler);
            var program = parser.buildProgram();

            InterpretingVisitor visitor = new InterpretingVisitor(program, System.out);
            visitor.execute();

        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + filePath);
        } catch (IOException e) {
            System.err.println("Error reading file: " + filePath);
        } catch (InterpreterException e) {
            System.err.println("Interpreter error: " + e.getMessage());
        }
    }
}
