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
            "match-fn-call.txt, 'x'",
            "match-invalid-type.txt, ''",
            "modify-struct-and-return.txt, '-1\n-2'",
            "nested-struct.txt, 'Persian'",
            "pass-by-copy-test.txt, 0",
            "pass-struct-member-by-copy-test.txt, '2\n2'",
            "print-fn-return.txt, 'General Kenobi!'",
            "reference-test.txt, 5",
            "return-struct.txt, '3\n4'",
            "test-pass-struct-by-copy.txt, 1",
            "test-pass-struct-by-ref.txt, 2",
            "test-pass-variant-by-copy.txt, 1",
            "test-pass-variant-by-ref.txt, 3",
            "test-print-string-expression.txt, 'Hello there!'",
            "test-return-flow-test.txt, '10\n9\n8\n7\n6\n5\n4\n-1'",
            "test-scope-shadowing.txt, '2\n5'",
            "test-string-concatenation.txt, 'AB'",
            "variant-as-fncall-test.txt, Marcin",
            "variant-test.txt, 33"
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

    @ParameterizedTest
    @CsvSource({
            "error-negate-non-numeric-types.txt,                            ArithmeticOperationNotSupportedForNonNumericTypes",
            "error-perform-arithmetic-operation-on-non-numeric-types.txt,   ArithmeticOperationNotSupportedForNonNumericTypes",
            "error-add-structs.txt,                                         ArithmeticOperationNotSupportedForNonNumericTypes",
            "error-relation-operation-non-numeric-type.txt,                 CompareOperationNotSupportedForNonNumericTypes",
            "error-eq-relation-operation-non-numeric-type.txt,              CompareOperationNotSupportedForNonNumericTypes",
            "error-duplicated-variable-test.txt,                            DuplicatedVariableException",
            "error-duplicated-variable2-test.txt,                           DuplicatedVariableException",
            "error-void-fn-value-expected-test.txt,                         ExpressionDidNotEvaluateException",
            "error-fn-donot-return-test.txt,                                FunctionDidNotReturnException",
            "error-fn-return-no-value-test.txt,                             FunctionDidNotReturnValueException",
            "error-call-non-function.txt,                                   FunctionNotDefinedException",
            "error-stack-limit.txt,                                         FunctionStackLimitException",
            "error-invalid-fn-call-not-enough-args-test.txt,                InvalidNumberOfArgumentsException",
            "error-invalid-fn-call-too-many-args-test.txt,                  InvalidNumberOfArgumentsException",
            "error-invalid-type-for-match.txt,                              InvalidTypeForMatchException",
            "error-invalid-variant-member.txt,                              InvalidVariantField",
            "error-invalid-struct-member.txt,                               NotExistingStructMemberException",
            "error-no-variable-in-scope-test.txt,                           NoVariableException",
            "error-tunneling-no-variable-exception.txt,                     NoVariableException",
            "error-invalid-operation-on-string-test.txt,                    OperationNotSupported",
            "error-pass-const-reference.txt,                                ReassignConstVariableException",
            "error-reassign-const.txt,                                      ReassignConstVariableException",
            "error-declaration-types-do-not-match-test.txt,                 TypesDoNotMatchException",
            "error-print-not-string.txt,                                    TypesDoNotMatchException",
            "error-eq-types-do-not-match.txt,                               TypesDoNotMatchException",
            "error-change-variant-type.txt,                                 TypesDoNotMatchException",
            "error-invalid-variant-get-value.txt,                           UnexpectedTypeException",
            "error-zerodivision-test.txt,                                   ZeroDivisionException",
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
    // format CsvSource and sort alphabeticaly by Error name

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
