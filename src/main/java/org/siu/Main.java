package org.siu;
import lombok.extern.slf4j.Slf4j;
import org.siu.error.ErrorHandler;
import org.siu.error.ErrorHandlerImpl;
import org.siu.lexer.Lexer;
import org.siu.lexer.LexerImpl;

@Slf4j
public class Main
{
    public static void main(final String[] args)
    {
        final ErrorHandler errorHandler = new ErrorHandlerImpl();
//        final Lexer lexer = new LexerImpl(errorHandler);

    }
}