//package org.siu.interpreter;
//
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.CsvSource;
//import org.siu.ast.Program;
//import org.siu.error.ErrorHandler;
//import org.siu.error.ErrorHandlerImpl;
//import org.siu.lexer.FilterCommentsLexer;
//import org.siu.lexer.LexerImpl;
//import org.siu.parser.Parser;
//
//import java.io.*;
//import java.nio.charset.StandardCharsets;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//
//public class InterpreterIntegrationTests {
//
//    Program buildProgram(String code) {
//        final ErrorHandler errorHandler = new ErrorHandlerImpl();
//        var reader = new StringReader(code);
//        var lexer = new LexerImpl(new BufferedReader(reader), errorHandler);
//        var filteredLexer = new FilterCommentsLexer(lexer);
//        var parser = new Parser(filteredLexer, errorHandler);
//        return parser.buildProgram();
//    }
//
//    void testInvalidDeclaration() throws IOException {
//        String code = readFileFromResources("error-invalid-declaration.txt");
//        var program = buildProgram(code);
//        var output = new ByteArrayOutputStream();
//        final String utf8 = StandardCharsets.UTF_8.name();
//        PrintStream out = new PrintStream(output, true, utf8);
//        var visitor = new InterpretingVisitor(program, out);
//
//        assertThrows(Pa.class, visitor::execute);
//    }
//
//    private String extractErrorName(String output) {
//        String prefix = "Error while interpreting: ";
//        int startIndex = output.indexOf(prefix);
//        if (startIndex != -1) {
//            String errorPart = output.substring(startIndex + prefix.length()).split("\n")[0].trim();
//            return errorPart.split("\\(")[0].trim();
//        }
//        return "";
//    }
//
//    private String readFileFromResources(String fileName) throws IOException {
//        ClassLoader classLoader = getClass().getClassLoader();
//        try (InputStream is = classLoader.getResourceAsStream(fileName)) {
//            if (is == null) {
//                throw new FileNotFoundException("File not found: " + fileName);
//            }
//            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
//        }
//    }
//}
