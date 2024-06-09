package org.siu.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.siu.ast.Parameter;
import org.siu.ast.Program;
import org.siu.ast.statement.StructTypeDefinitionStatement;
import org.siu.ast.statement.VariantTypeDefinitionStatement;
import org.siu.ast.type.*;
import org.siu.error.ErrorHandler;
import org.siu.lexer.Lexer;
import org.siu.lexer.LexerImpl;
import org.siu.token.Position;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefinitionTests {
    private ErrorHandler errorHandler;
    private Position position;

    @BeforeEach
    void setUp() {
        errorHandler = Mockito.mock(ErrorHandler.class);
        position = Mockito.mock(Position.class);
    }

    Parser toParser(String text) {
        Lexer lexer = new LexerImpl(text, errorHandler);
        return new Parser(lexer, errorHandler);
    }

    @Test
    void testVariantDefinition() {
        String sourceCode = "variant Var { int row; int col; };";
        Parser parser = toParser(sourceCode);
        Program program = parser.buildProgram();
        VariantTypeDefinitionStatement expectedVariant = new VariantTypeDefinitionStatement(
                "Var",
                List.of(
                        new Parameter(new TypeDeclaration(ValueType.INT), "row"),
                        new Parameter(new TypeDeclaration(ValueType.INT), "col")
                ),
                position
        );


        assertEquals(expectedVariant, program.getTypeDefinitions().get("Var"));
    }

    @Test
    void testEmptyVariantDefinition() {
        String sourceCode = "variant Var { };";
        Parser parser = toParser(sourceCode);
        Exception exception = assertThrows(RuntimeException.class, parser::buildProgram);
        assertEquals("org.siu.error.EmptyVariantException at: Position(line=1, column=13)", exception.getMessage());
    }

    @Test
    void testRedefinitionStruct() {
        String sourceCode = "struct A { int a; };\n" +
                "struct A { int a; };";
        Parser parser = toParser(sourceCode);
        Exception exception = assertThrows(RuntimeException.class, parser::buildProgram);
        assertEquals("RedefinitionError(details=A is already defined) at: Position(line=2, column=8)", exception.getMessage());
    }

    @Test
    void testRedefinitionVariant() {
        String sourceCode = "variant A { int a; };\n" +
                "variant A { int a; };";
        Parser parser = toParser(sourceCode);
        Exception exception = assertThrows(RuntimeException.class, parser::buildProgram);
        assertEquals("RedefinitionError(details=A is already defined) at: Position(line=2, column=11)", exception.getMessage());
    }

    @Test
    void testStructDefinitionStatement() {
        String sourceCode = """
                struct Dog {\s
                	int age;\s
                	string name;\s
                	Breed breed;\s
                };
                """;
        Parser parser = toParser(sourceCode);
        Program program = parser.buildProgram();
        StructTypeDefinitionStatement expectedStruct = new StructTypeDefinitionStatement(
                "Dog",
                List.of(
                        new Parameter(new TypeDeclaration(ValueType.INT), "age"),
                        new Parameter(new TypeDeclaration(ValueType.STRING), "name"),
                        new Parameter(new TypeDeclaration(ValueType.CUSTOM, "Breed"), "breed")
                ),
                position
        );


        assertEquals(expectedStruct, program.getTypeDefinitions().get("Dog"));
    }

    @Test
    void testWrongStructDeclaration() {
        String sourceCode = """
            struct Dog {\s
                int age
                string name;\s
                Breed breed;\s
            };
            """;
        Parser parser = toParser(sourceCode);

        Exception exception = assertThrows(RuntimeException.class, parser::buildProgram);
        assertEquals("org.siu.error.SyntaxError at: Position(line=3, column=5)", exception.getMessage());
    }

    @Test
    void testEmptyStructDeclaration() {
        String sourceCode = """
            struct Dog {};
            """;
        Parser parser = toParser(sourceCode);
        Program program = parser.buildProgram();

        StructTypeDefinitionStatement expectedStruct = new StructTypeDefinitionStatement(
                "Dog",
                List.of(),
                position
        );


        assertEquals(expectedStruct, program.getTypeDefinitions().get("Dog"));
    }
}