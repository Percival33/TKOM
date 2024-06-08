package org.siu.interpreter.state;


import lombok.Builder;
import lombok.experimental.FieldDefaults;

@Builder(toBuilder = true)
@lombok.Value
public class Result {
    Value value;

    @Builder.Default
    boolean present = true;

    @Builder.Default
    boolean returned = false;

    @Builder.Default
    boolean copy = false;

    @Builder.Default
    boolean constant = false;

    public static Result ok(Value value) {
        return Result.builder()
                .value(value)
                .build();
    }

    public static Result empty() {
        return Result.builder()
                .present(false)
                .build();
    }

}
