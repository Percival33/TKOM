package org.siu.token;

import lombok.*;


@AllArgsConstructor
@Getter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Builder(toBuilder = true)
public class Position {
    @Builder.Default
    int line = 1;
    @Builder.Default
    int column = 0;

    public void nextLine() { line++; column = 0; }
    public void nextCharacter() { column++; }

    public Position copy() {
        return this.toBuilder().build();
    }
    public String toPositionString() {
        return "line, column " + line + ":" + column;
    }
}
