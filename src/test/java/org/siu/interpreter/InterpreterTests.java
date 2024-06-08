package org.siu.interpreter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.siu.ast.Program;
import org.siu.error.ErrorHandler;
import org.siu.error.ErrorHandlerImpl;
import org.siu.lexer.FilterCommentsLexer;
import org.siu.lexer.LexerImpl;
import org.siu.parser.Parser;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InterpreterTests {

    Program buildProgram(String code) {
        final ErrorHandler errorHandler = new ErrorHandlerImpl();
        var reader = new StringReader(code);
        var lexer = new LexerImpl(new BufferedReader(reader), errorHandler);
        var filteredLexer = new FilterCommentsLexer(lexer);
        var parser = new Parser(filteredLexer, errorHandler);
        return parser.buildProgram();
    }

    @ParameterizedTest
    @CsvSource({
            "reference-test.txt, 5",
            "printer-test.txt, 'Hello there!'",
            "pass-by-copy-test.txt, 0",
            "pass-struct-member-by-copy-test.txt, '2\n2'",
            "variant-test.txt, 33",
            "variant-as-fncall-test.txt, Marcin"
    })
    void testInterpreter(String fileName, String expectedOutput) throws IOException {
        String code = readFileFromResources(fileName);
        var program = buildProgram(code);
        var output = new ByteArrayOutputStream();
        final String utf8 = StandardCharsets.UTF_8.name();
        PrintStream out = new PrintStream(output, true, utf8);
        var visitor = new InterpretingVisitor(program, out);

        visitor.execute();
        assertEquals(expectedOutput.trim(), output.toString().trim());
    }

    /*
    TODO: dodanie structow roznych typow
    TODO: zmiana wartości w wariancie
    TODO: przekazywanie structa przez referencje
    TODO: przekazuwanie variantu jako fn call
    TODO: przekazuwanie match na nie istniejacym typie
    TODO: przekazuwanie match na nie istniejacym typie
     */

    @ParameterizedTest
    @CsvSource({
            "error-negate-non-numeric-types.txt, ArithmeticOperationNotSupportedForNonNumericTypes",
            "error-perform-arithmetic-operation-on-non-numeric-types.txt, ArithmeticOperationNotSupportedForNonNumericTypes",
            "error-relation-operation-non-numeric-type.txt, CompareOperationNotSupportedForNonNumericTypes",
            "error-eq-relation-operation-non-numeric-type.txt, CompareOperationNotSupportedForNonNumericTypes",
            "error-void-fn-value-expected-test.txt, ExpressionDidNotEvaluateException",
            "error-fn-donot-return-test.txt, FunctionDidNotReturnException",
            "error-fn-return-no-value-test.txt, FunctionDidNotReturnValueException",
            "error-types-do-not-match.txt, TypesDoNotMatchException",
            "error-pass-struct-test.txt, Unsupported value type: Point",
    })
    void testInterpreterErrors(String fileName, String expectedError) throws IOException {
        String code = readFileFromResources(fileName);
        var program = buildProgram(code);
        var output = new ByteArrayOutputStream();
        final String utf8 = StandardCharsets.UTF_8.name();
        PrintStream out = new PrintStream(output, true, utf8);
        var visitor = new InterpretingVisitor(program, out);

        visitor.execute();
        String fullOutput = output.toString();

        String errorName = extractErrorName(fullOutput);

        assertEquals(expectedError.trim(), errorName.trim());
    }

    private String extractErrorName(String output) {
        String prefix = "Error while interpreting: ";
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
