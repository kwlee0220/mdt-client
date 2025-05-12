grammar MdtExpr;

@header {
package mdt.model.expr;
}

// Parser rules
start: expr EOF;

expr: valueLiteralSpec
        | fullSubmodelSpec
        | fullSubmodelElementSpec
        | assignmentExpr
        ;

assignmentExpr:
          fullSubmodelElementSpec '=' valueLiteralSpec
        | fullSubmodelElementSpec '=' fullSubmodelElementSpec
        ;

fullInstanceSpec: 'mdt' ':' instanceSpec;
instanceSpec: idOrString;

fullSubmodelSpec: submodelSpec;
submodelSpec:
          defaultSubmodelSpec
        | idBasedSubmodelSpec
        ;
defaultSubmodelSpec: instanceSpec ':' ( 'idShort' '=' )? idOrString;
idBasedSubmodelSpec: 'submodel' ':' 'id' '=' idOrString;

fullSubmodelElementSpec:
          defaultSubmodelElementSpec
        | 'param' ':' parameterSpec
        | 'oparg' ':' argumentSpec
        | 'opvar' ':' opVarSpec
        ;

defaultSubmodelElementSpec: submodelSpec ':' idShortPath;
idShortPath: idOrString (idShortSeg)*;
idShortSeg: '.' idOrString | '[' INTEGER ']';

parameterSpec: instanceSpec ':' (idOrString | INTEGER | '*');
argumentSpec: submodelSpec ':' ('in' | 'out') ':' (idOrString | INTEGER | '*');
opVarSpec: defaultSubmodelElementSpec ':' ('in' | 'out' | 'inout') ':' (INTEGER);

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
ID: [a-zA-Z_] [a-zA-Z0-9_-]*;
INTEGER: '-'? [0-9]+;
FLOAT: '-'? [0-9]+ '.' [0-9]+;
BOOLEAN: 'true' | 'false';
STRING: ('"' .*? '"') | ('\'' .*? '\'');
WS: [ \t\r\n]+ -> skip; 