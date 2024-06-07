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
import java.nio.charset.StandardCharsets;

@Slf4j
public class Main
{
    public static void main(final String[] args)
    {
        final ErrorHandler errorHandler = new ErrorHandlerImpl();
        var reader = new InputStreamReader(Main.class.getClassLoader().getResourceAsStream("Copy.txt"));
        var lexer = new LexerImpl(new BufferedReader(reader), errorHandler);
        var filteredLexer = new FilterCommentsLexer(lexer);
        var parser = new Parser(filteredLexer, errorHandler);
        var program = parser.buildProgram();

        var output = new ByteArrayOutputStream();
        final String utf8 = StandardCharsets.UTF_8.name();
        try (var out = new PrintStream(output, true, utf8)) {
            var visitor = new InterpretingVisitor(program, System.out);
            visitor.execute();
        }
//        catch (InterpreterException e) {
//            System.out.println(e.getMessage());
//        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}