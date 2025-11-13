grammar MdtExpr;

@header {
package mdt.model.expr;
}

// Parser rules
start: expr EOF;

expr: valueLiteralSpec
        | submodelSpec
        | fullElementSpec
        | assignmentExpr
        | nullExpr
        ;

nullExpr: NULL;

assignmentExpr:
          fullElementSpec '=' valueLiteralSpec
        | fullElementSpec '=' fullElementSpec
        ;

fullInstanceSpec: 'mdt' ':' instanceSpec;
instanceSpec: idOrString;

submodelSpec:
          defaultSubmodelSpec
        | idBasedSubmodelSpec
        ;
defaultSubmodelSpec: instanceSpec ':' ( 'idShort' '=' )? idOrString;
idBasedSubmodelSpec: 'submodel' ':' 'id' '=' idOrString;

fullElementSpec:
          defaultElementSpec
        | 'param' ':' parameterSpec
        | 'oparg' ':' argumentSpec
        | 'opvar' ':' opVarSpec
        ;

defaultElementSpec: submodelSpec ':' idShortPath;
idShortPath: idOrString (idShortSeg)*;
idShortSeg: '.' idOrString | '[' INTEGER ']';

parameterSpec: instanceSpec ':' (idShortPath | INTEGER | '*');

argumentSpec: submodelSpec ':' ('in' | 'out') ':' (idOrString | INTEGER | '*');
opVarSpec: defaultElementSpec ':' ('in' | 'out' | 'inout') ':' (INTEGER);

valueLiteralSpec:
        propertyValueLiteralSpec
        | mlpPropertyValueLiteralSpec
        | fileValueLiteralSpec
        | rangeValueLiteralSpec
        ;

propertyValueLiteralSpec: ID | STRING | INTEGER | FLOAT | BOOLEAN;
mlpPropertyValueLiteralSpec: STRING '@' ID;
fileValueLiteralSpec: 'file' ':' STRING '(' STRING ')';
rangeValueLiteralSpec: '[' propertyValueLiteralSpec ',' propertyValueLiteralSpec ']';

idOrString: ID | STRING;

// Lexer rules
NULL: 'null';
ID: [a-zA-Z_] [a-zA-Z0-9_-]*;
INTEGER: '-'? [0-9]+;
FLOAT: '-'? [0-9]+ '.' [0-9]+;
BOOLEAN: 'true' | 'false';
STRING: ('"' .*? '"') | ('\'' .*? '\'');
WS: [ \t\r\n]+ -> skip; 