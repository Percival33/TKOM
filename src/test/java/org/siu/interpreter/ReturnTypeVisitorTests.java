package org.siu.interpreter;

import org.junit.jupiter.api.Test;
import org.siu.ast.Program;
import org.siu.error.ErrorHandler;
import org.siu.error.ErrorHandlerImpl;
import org.siu.interpreter.error.InvalidReturnTypeException;
import org.siu.lexer.FilterCommentsLexer;
import org.siu.lexer.LexerImpl;
import org.siu.parser.Parser;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReturnTypeVisitorTests {
    Program buildProgram(String code) {
        final ErrorHandler errorHandler = new ErrorHandlerImpl();
        var reader = new StringReader(code);
        var lexer = new LexerImpl(new BufferedReader(reader), errorHandler);
        var filteredLexer = new FilterCommentsLexer(lexer);
        var parser = new Parser(filteredLexer, errorHandler);
        return parser.buildProgram();
    }

    @Test
    void voidFnReturnInt() throws IOException {
        String code = readFileFromResources("error-void-return-int-test.txt");
        var program = buildProgram(code);
        var output = new ByteArrayOutputStream();
        final String utf8 = StandardCharsets.UTF_8.name();
        PrintStream out = new PrintStream(output, true, utf8);
        var visitor = new FunctionReturnTypeVisitor(program, out);
        visitor.execute();

        String oi = output.toString().trim();
        String errorName = extractErrorName(oi);

        assertEquals("InvalidReturnTypeException".trim(), errorName.trim());
    }

    @Test
    void fnCallStackException() throws IOException {
        String code = readFileFromResources("error-stack-limit.txt");
        var program = buildProgram(code);
        var output = new ByteArrayOutputStream();
        final String utf8 = StandardCharsets.UTF_8.name();
        PrintStream out = new PrintStream(output, true, utf8);
        var visitor = new FunctionReturnTypeVisitor(program, out);
        visitor.execute();

        String oi = output.toString().trim();
        String errorName = extractErrorName(oi);

        assertEquals("FunctionStackLimitException".trim(), errorName.trim());
    }

    @Test
    void voidFnNestedReturn() throws IOException {
        var program = buildProgram("""
                fn foo() {
                    if(true) {
                        if(false) {
                            while(1) {
                                if(true) {
                                    return 1;
                                }
                            }
                        }
                    }
                }
                
                fn main() {
                    foo();
                }
                """);
        var output = new ByteArrayOutputStream();
        final String utf8 = StandardCharsets.UTF_8.name();
        PrintStream out = new PrintStream(output, true, utf8);
        var visitor = new FunctionReturnTypeVisitor(program, out);

        visitor.execute();
        String oi = output.toString().trim();
        String errorName = extractErrorName(oi);

        assertEquals("InvalidReturnTypeException".trim(), errorName.trim());
    }

    private String extractErrorName(String output) {
        String prefix = "Error while return type checking: ";
        int startIndex = output.indexOf(prefix);
        if (startIndex != -1) {
            String errorPart = output.substring(startIndex + prefix.length()).split("\n")[0].trim();
            return errorPart.split("\\(")[0].trim();
        }
        return "";
    }

    private String readFileFromResources(String fileName) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(fileName)) {
            if (is == null) {
                throw new FileNotFoundException("File not found: " + fileName);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
