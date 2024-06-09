# TKOM 24L - Język `siu`

```
Marcin Jarczewski
```

Język `siu`.

<!-- TOC -->

* [TKOM 24L - Język `siu`](#tkom-24l---język-siu)
    * [Zasady działania języka](#zasady-działania-języka)
        * [Analiza wymagań](#analiza-wymagań)
        * [Typy danych](#typy-danych)
        * [Operatory](#operatory)
    * [Przykłady języka](#przykłady-języka)
        * [Konwersja typów i operator rzutowania](#konwersja-typów-i-operator-rzutowania)
        * [Stałe](#stałe)
        * [Zasady widoczności zmiennych](#zasady-widoczności-zmiennych)
        * [Zasady przekazywania zmiennych do funkcji](#zasady-przekazywania-zmiennych-do-funkcji)
        * [Przeciążanie funkcji](#przeciążanie-funkcji)
        * [Kombinacja typów](#kombinacja-typów)
        * [Słowa kluczowe](#słowa-kluczowe)
        * [Biblioteka standardowa](#biblioteka-standardowa)
        * [Komunikaty o błędach](#komunikaty-o-błędach)
    * [Struktura projektu](#struktura-projektu)
        * [Testowanie](#testowanie)
    * [Gramatyka języka](#gramatyka-języka)
    * [Sposób uruchomienia](#sposób-uruchomienia)
    * [Środowisko programistyczne](#środowisko-programistyczne)

<!-- TOC -->

## Zasady działania języka

Dany język będzie silnie i statycznie typowany. Wartości będą domyślnie mutowalne oraz przekazywane do funkcji poprzez
referencje.

### Analiza wymagań

- Język silnie i statycznie typowany wymaga operatora konwersji typów.
- Domyślnie zmienne są mutowalne, zatem żeby zablokować taką możliwość należy poprzedzić deklarację słowem
  kluczowym `const`, co zablokuje możliwość przypisania nowej wartości do danej zmiennej.
- W języku zostanie dodany specjalny operator `@` pozwalający na przekazanie argumentów do funkcji poprzez wartość.
  Kopiowanie obiektu odbędzie się w ramach instrukcji wywołania funkcji.

### Typy danych

- dostępne typy danych

| Typy proste | Wartości                                                                              |
|-------------|---------------------------------------------------------------------------------------|
| int         | od -2.147.483.647 do 2.147.483.647                                                    |
| float       | pojedyncza precyzja 32-bit IEEE 754 floating point. (7 cyfr znacznących po przecinku) |
| bool        | true / false                                                                          |
| string      | "A", "pies", "imię pies to\t\"Pluto\"!\nWow!"                                         |

| Typy złożone | Przykłady                                               |                                                                                             |
|--------------|---------------------------------------------------------|---------------------------------------------------------------------------------------------|
| struct       | struct Car {<br>    int age;<br>    string model;<br>}; | struktura, której polami mogą być również inne struktury                                    |
| variant      | variant V { int, float };                               | typ wariantowy, który pozwala przechowywać wartość jednego z wymienionych w definicji typów |

### Operatory

| Operator                       | Priorytet | Asocjacyjność |
|--------------------------------|-----------|---------------|
| `-` (negacja)                  | 6         | -             |
| `/`                            | 5         | lewostronna   |
| `%`                            | 5         | lewostronna   |
| `*`,                           | 5         | lewostronna   |
| `+`                            | 4         | lewostronna   |
| `-`              (odejmowanie) | 4         | lewostronna   |
| `>`                            | 3         | -             |
| `>=`                           | 3         | -             |
| `<`                            | 3         | -             |
| `<=`                           | 3         | -             |
| `!=`                           | 3         | -             |
| `==`                           | 3         | -             |
| `and`                          | 2         | lewostronna   |
| `or`                           | 1         | lewostronna   |

Przykład:

```
int x = 4 * (3 + 1) # 16
```

Dla operatorów relacyjnych, manipulowanie kolejnością wykonania odbywa się poprzez użycie nawiasów.

## Przykłady języka

- komentarze jednolinijkowe będą poprzedzone znakiem `#` a komentarz blokowy poprzez `/*` oraz `*/`
- instrukcja warunkowe

```
if (expression) {}

if (expression) {} 
elif(expression) {}
else {}


int a = 0;
if (a % 2 == 0) {
	a = 5;	
} else {
	a = 6;
}
```

- instrukcja pętli

```
int a = 0;
while(a < 10) {
	a = a + 1;
}
```

- funkcje

```
fn factorial(int n): int {
	if (n == 1) { return 1; }
	return n * factorial(n - 1);
}

fn foo() { # funkcja bezargumentowa, nie zwracająca wartości
	print("pluto");
}

fn bar(): float { # funkcja bezargumentowa, zwaracająca wartość
	return 3.14159;
}
```

- napisy i operacje na nich

```
string text = "Hello there!\nGeneral Kenobi";

string A = "Try not.";
string B = "Do or do not.";
string C = "There is no try.";

string result = A + B + C;
```

- structures

```
struct Breed {
	string name; 
	int popularity;
};

struct Dog { 
	int age; 
	string name; 
	Breed breed; 
};

struct Cat {
	int age;
	Breed breed;
};
Breed golden = Breed { "Golden retriever", 5 };
Dog pluto = Dog { 14, "pluto", golden };

struct Point {
    int x;
    int y;
};

Point pt = Point { a, f(10) };
Point pt2 = pt;
```

- variant

```
variant Var { int row; int col; };
Var v = Var::row(3);

fn inspect(Var v) {
    match(v) {
        Var::row(x) { return x; }
        Var::col(y) { return y; }
    }
}
```

- constant

```
const int x = 5; 
x = 6; # error
```

- mutable

```
int x = 5; 
print((string) x); # 5 
x = 6; 
print((string) x); # 6
```

### Konwersja typów i operator rzutowania

| Typ początkowy | Typ końcowy |                                                                                  |
|----------------|-------------|----------------------------------------------------------------------------------|
| `int`          | `bool`      | `0` przyjmuje wartość `false` a pozostałe wartości są rzutowane na `1`           |
| `int`          | `float`     | bezstratnie                                                                      |
| `int`          | `string`    | wartość numeryczna liczby                                                        |
| `float`        | `bool`      | `0.0` przyjmuje wartość `false` a pozostałe wartości są rzutowane na `true`      |
| `float`        | `int`       | obcięcie części ułamkowej, operacja podłogi                                      |
| `float`        | `string`    | wartość numeryczna liczby                                                        |
| `string`       | `bool`      | pusty napis (`""`) ma wartość `false`  pozostałe napisy przyjmują wartość `true` |
| `string`       | `float`     | pusty napis (`""`) ma wartość `0.0`  pozostałe napisy przyjmują wartość `1.0`    |
| `string`       | `int`       | pusty napis (`""`) ma wartość `0`  pozostałe napisy przyjmują wartość `1`        |
| `bool`         | `int`       | `0` = `false`, `1`=`true`                                                        |
| `bool`         | `float`     | `0.0` = `false`, `1.0`=`true`                                                    |
| `bool`         | `string`    | zmienia wartosci na napisy o odpowiedniej wartości                               |

operator rzutowania wygląda następująco: `(nowy typ) zmienna` i obsługuje wyłącznie typy proste jak w przykładzie
poniżej:

```
float a = 3.14;
int b = (int) a; # b = 3;
```

### Stałe

Odbywa się poprzez dodanie `const` przed deklaracją zmiennej. Blokuje to przypisanie nowej wartości do zmiennej.

### Zasady widoczności zmiennych

Zmienne są tylko widoczne wyłącznie w bloku, w którym są zadeklarowane

```
fn main() {
	int x = 0;
	if (true) {
		int y = 3;
	}
	x = y; # błąd
}
```

### Zasady przekazywania zmiennych do funkcji

Argumenty są przekazywane domyślnie poprzez referencję. Istnieje możliwość przekazania typów do funkcji przez wartość,
przy użyciu operatora `@`, kopiowanie typów odbędzie się w ramach instrukcji wywołania funkcji.

### Przeciążanie funkcji

Funkcji nie da się przeciążać.

### Kombinacja typów

Operatory wieloargumentowe, np. porównania, wymagają tego samego typu zmiennych. Przy czym nie da porównywać się
własnych typów

### Słowa kluczowe

`string`, `int`, `float`, `variant`, `struct`, `if`,`elif`, `else`, `const`, `print`, `while`, `@`, `true`, `false`, `return`, `fn`, `::`,
`>=`, `>`, `<`, `<=`, `or`, `not`, `and`

operatory matematyczne:
`+`, `-`, `*`, `\`, `%` (operator modulo)

`(`, `)`

### Biblioteka standardowa

- `print()` funkcja print obsługuje wyłącznie typ string. Zmienne należy jawnie rzutować.

### Komunikaty o błędach

Wyróżniam 3 rodzaje błędów

1. błędy analizatora leksykalnego
    - np. niepoprawne użycie typów - przekazanie za dużej liczby do
      inta `error: number is too big for this type at line:15`.
    - za długi identyfikator `error: Identificator is too long at line:19`
1. błędy analizatora składniowego
    - redefinicja funkcji `error: function already declared at line:5`
    - pusty typ wariantowy `EmptyVariantException`
    - brak warunku w instrukcji warunkowej `InvalidConditionExpressionException`
    - token, którego nie da się obsłużyć w parserze `InvalidTokenException`
    - brak bloku w instrukcji warunkowej / ciele funkcji `InvalidBlockException`
    - brak wyrażenia `MissingExpressionException`
    - brak średnika `MissingSemicolonException`
    - brak instrukcji po `const`
    - brak składni `SyntaxException`
2. błędy analizatora semantycznego
    - użycie `return` w funkcji nie zwracającej typu
1. błędy interpretera
    - błąd operacji
      matematycznych `Error while interpreting: ZeroDivisionException(super=InterpreterException(position=Position(line=2, column=15))).`
      oraz `ArithmeticOperationNotSupportedForNonNumericTypes` i `CompareOperationNotSupportedForNonNumericTypes` dla
      operacji na innych typach niż liczbowe oraz
    - powtórzenie zmiennych w argumentach funkcji oraz w definicji struktury
    - przypisanie nowej wartości do zmiennej
      oznaczonej `const`, `Error while interpreting: ReassignConstVariableException`
    - wyrażenie nie zwracające wartości
    - dodanie różnych typów bez rzutowania `Error while interpreting: TypesDoNotMatchException`
    - przekroczenie limitu stosu wywołań
    - niepoprawna liczba argumentów funkcji
    - podanie złego typu argumentu do instrukcji match
    - próba przypisania wartości do zmiennej (w tym typu w strukturze), która nie istnieje
    - próba użycia zmiennej, która nie istnieje
    - próba przypisania nowej wartości do już zdefiniowanej stałej
    - próba przypisania do zmiennej wartości innego typu
    - dzielenie przez zero
    - przekazanie złego typu zmiennej jako parameter
      funkcji: `error: function parameter and provided types mismatch at line:20`

## Struktura projektu

- lekser
    - moduł abstrakcji źródła - odpowiada za konwertowanie białych znaków - unifikuje znaki końca linii do znaku `\n` a
      koniec pliku znakiem `ETX`
    - moduł odpowiedzialny za tworzenie tokenów ze źródła, w sposób *leniwy* tzn. wczytuje kolejne znaki, dopiero gdy
      zostanie poproszony, przez parser, o następny token
    - moduł filtra który odrzuca komentarze, przed zwróceniem tokena, parserowi
- parser
    - moduł odpowiedzialny za budowę drzewa składniowe na podstawie tokenów (dostarczonych przez lekser) - hierarchiczna
      struktura odzwierciedlająca strukturę języka w postaci
    - moduł `analizy semantycznej`, który będzie analizował zbudowane drzewko składniowe, badając czy operacje są
      dozwolowe
- interpreter
    - moduł przechodzi po drzewie składniowym danego programu (wykonanego przez parser) odwiedzając kolejne węzły i
      interpretując je
- obsługa błędów
    - interfejs dla powyższych modułów

### Testowanie

- lekser
    - Testy polegają na przyjmowaniu napisów i porównywaniu wygenerowanych tokenów
        - test: niepoprawnego napisu, który zakończony jest końcem pliku
- parser
    - Testy porównują wyjściową konstrukcje z oczekiwaną konstrukcją drzewa składniowego.
- interpreter
    - Testy symulują błędy, które mogą powstać w kodzie użytkownika sprawdzane będzie sposób obsługi takich
      błędów.
    - Testy symulują przypadki użycia i sprawdzają czy wynik jest zgodny z oczekiwaniami.

Łącznie testów jednostkowych i integracyjnych jest 174.

## Gramatyka języka

```
/* wyrażenia regularne */
letter                  = [a-zA-Z];
non_zero_digit          = [1-9];
digit                   = [0-9];
zero                    = "0";
relation_operator       = ">" | ">=" | "<" | "<=" | "!=" | "==";
additive_operator       = "+" | "-";
multiplication_operator = "*" | "/" | "%";
character               = ?;
```

```
CHARACTERS              = {character};
IDENTIFIER              = letter, {letter | digit};
INTEGER                 = zero
                        | non_zero_digit, {digit};
                        
FLOAT                   = INTEGER, ".", digit, {digit};

NUMBER                  = INTEGER | FLOAT;

BOOLEAN                 = "true"
                        | "false";
                        
STRING                  = '"', '"'
                        | '"', {character}, '"';

LITERAL                 = NUMBER
                        | BOOLEAN
                        | STRING;
                        
SIMPLE_TYPE             = "int"
                        | "float"
                        | "bool"
                        | "string";
```

### Instrukcje

```                        
TYPE_DEFINITION                 = STRUCT_TYPE_DEFINITION
                                | VARIANT_TYPE_DEFINITION;

VARIANT_TYPE_DEFINITION         = "variant", IDENTIFIER, "{", STRUCT_TYPE_DEF, {, ";", STRUCT_TYPE_DEF, ";" }, "}", ";" ;                            
STRUCT_TYPE_DEFINITION          = "struct", IDENTIFIER, "{", { STRUCT_TYPE_DEF }, "}", ";";
            
VARIANT_RET_TYPE                = "variant", "{", VARIANT_TYPE_DEF, { ",", VARIANT_TYPE_DEF }, "}"            
                            
STRUCT_TYPE_DEF                = VARIANT_TYPE_DEF, IDENTIFIER;
VARIANT_TYPE_DEF               = SIMPLE_TYPE | IDENTIFIER;
                    
DECLARATION                     = ["const"], VARIABLE_DECLARATION, ";";

VARIABLE_DECLARATION            = SIMPLE_TYPE_AS_ARG, IDENTIFIER, "=", EXPRESSION, ";"
                                | IDENTIFIER, IDENTIFIER, "=", EXPRESSION, ";"
                        
STRUCT_MEMBER                   = LITERAL 
                                | FN_CALL
                                | IDENTIFIER_OR_STRUCT

IF_STATEMENT                    = "if", "(", EXPRESSION, ")", BLOCK, 
                                    { "elif", "(", EXPRESSION, ")", BLOCK, },
                                    [ "else", BLOCK ];
                            
WHILE_STATEMENT                 = "while", "(", EXPRESSION, ")", BLOCK;

FN_DEFINITION                   = "fn", IDENTIFIER, "(", [ FN_PARAMS, { ",", FN_PARAMS }], ")", [":", FN_RET_TYPES], BLOCK;
FN_PARAMS                       = SIMPLE_TYPE_AS_ARG 
                                | STRUCT_AS_ARG
                                | VARIANT_AS_ARG;
                        
                                                
VARIANT_AS_ARG                  = IDENTIFIER, IDENTIFIER;                            
STRUCT_AS_ARG                   = IDENTIFIER, IDENTIFIER;
SIMPLE_TYPE_AS_ARG              = SIMPLE_TYPE, IDENTIFIER;                        
                        
FN_RET_TYPES                    = SIMPLE_TYPE 
                                | IDENTIFIER;
                        
RETURN_STATEMENT                = "return", EXPRESSION, ";"
                                | "return", ";";

MATCH                           = "match", "(", EXPRESSION, ")", "{", { MATCH_EXP }, "}"
MATCH_CASE_STATEMENT            = IDENTIFIER, "::", IDENTIFIER, "(", IDENTIFIER, ")", BLOCK;

ASSINGMENT                      = IDENTIFIER, "=", EXPRESSION, ";"
                                | IDENTIFIER, ".", IDENTIFIER, "=", EXPRESSION, ";"

STATEMENT                       = IF_STATEMENT
                                | WHILE_STATEMENT
                                | DECLARATION
                                | RETURN_STATEMENT
                                | ASSINGMENT
                                | EXPRESSION, ";"
                                | MATCH;

BLOCK                           = "{", { STATEMENT }, "}";
```

### Wyrażenia

```
EXPRESSION                      = AND_EXPRESSION, { "or", AND_EXPRESSION };

AND_EXPRESSION                  = RELATION_EXPRESSION, { "and", RELATION_EXPRESSION }

RELATION_EXPRESSION             = ["not"], MATH_EXPRESSION, [ relation_operator, MATH_EXPRESSION ];
                        
MATH_EXPRESSION                 = TERM, { additive_operator, TERM };

TERM                            = CASTED_FACTOR, { multiplication_operator, CASTED_FACTOR };    

CASTED_FACTOR                   = [ "(", SIMPLE_TYPE, ")" ], UNARY_FACTOR;

UNARY_FACTOR                    = ["-"], FACTOR;   

FACTOR                          = LITERAL
                                | '(', EXPRESSION, ')'
                                | '{', EXPRESSION, { ',', EXPRESSION }, '}' ( * struct definition *)
                                | IDENTIFIER_FNCALL_MEM_VARIANT

IDENTIFIER_FNCALL_MEM_VARIANT   = IDENTIFIER, 
                                [
                                    ".", IDENTIFIER,
                                    | "(", [ FN_ARGUMENTS ], ")",
                                    | "::", IDENTIFIER, "(", EXPRESSION ")"
                                    | "{", STRUCT_MEMBER, { ",",  STRUCT_MEMBER }, "}" (* struct *)
                                ]     

FN_ARGUMENTS                    = ["@"] EXPRESSION, { "," ["@"], EXPRESSION };  
```

```
PROGRAM                         = { TYPE_DEFINITION | FN_DEFINITION | DECLARATION | FN_CALL };
```

## Sposób uruchomienia

Uruchomienie poprzez podanie pliku do uruchomienia. Przykłady znajdują się w folderze `examples`.

```
java -jar siulang-1.0.0-fat.jar <file>
```

## Środowisko programistyczne

projekt zostanie napisany przy użyciu języka `Java` w wersji 17 z wykorzystaniem narzędzia gradle.
Dodatkowo zostaną użyte biblioteki lombok i Simple Logging Facade for Java (SLF4J), a do testowania biblioteki Junit,
AssertJ.
