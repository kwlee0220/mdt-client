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
          'param' ':' parameterSpec
        | 'oparg' ':' argumentSpec
        | 'opvar' ':' opVarSpec
        | 'timeseries' ':' timeseriesSpec
        |  defaultElementSpec
        ;

defaultElementSpec: submodelSpec ':' idShortPath;
idShortPath: idOrString (idShortSeg)*;
idShortSeg: '.' idOrString | '[' INTEGER ']';

parameterSpec: instanceSpec ':' (idShortPath | INTEGER | '*');

argumentSpec: submodelSpec ':' ('in' | 'out') ':' (idOrString | INTEGER | '*');
opVarSpec: defaultElementSpec ':' ('in' | 'out' | 'inout') ':' (INTEGER);

// range는 '#', projection은 '|'로 구분해 콜론 과부하/모호성을 제거한다.
// 예: timeseries:inst:sm:elem#last=5|colA,colB
timeseriesSpec: submodelSpec ('#' tsRangeSpec)? ('|' tsProjectionSpec)?;
tsRangeSpec: tsRangeLastSpec | tsRangeAbsoluteSpec;
// 'last=N'(접미사 없는 정수)은 마지막 N개 레코드를, 'last=dur'은 마지막 기간 구간을 의미한다.
// 기간의 기준점(anchor)은 '@latest'(기본, 가장 최신 데이터 시각) 또는 '@now'(현재 시각)으로 명시한다.
//   예: last=10 (마지막 10개), last=10s (마지막 10초), last=1h@now
tsRangeLastSpec: 'last' '=' ( INTEGER | durationValue ('@' tsRangeAnchor)? );
tsRangeAnchor: 'now' | 'latest';
durationValue: durationLiteralSpec | iso8601DurationLiteralSpec;
// 절대 시간 범위: '~'를 범위 연산자로 사용한다(개시/종료 한쪽 생략 가능).
//   from~to: TIMESTAMP '~' TIMESTAMP,  from~: TIMESTAMP '~',  ~to: '~' TIMESTAMP
// 예: elem#2024-01-01~2024-02-01,  elem#2024-01-01T09:00:00Z~,  elem#~2024-02-01
tsRangeAbsoluteSpec: TIMESTAMP '~' TIMESTAMP
                   | TIMESTAMP '~'
                   | '~' TIMESTAMP;
// projection('|' 절)을 생략하면 전체 컬럼을 선택한 것으로 해석한다.
// (visitTimeseriesSpec 구현 시 projection 컨텍스트가 없으면 전체 선택으로 처리할 것)
tsProjectionSpec: idOrString (',' idOrString)*;

valueLiteralSpec:
        propertyValueLiteralSpec
        | mlpPropertyValueLiteralSpec
        | fileValueLiteralSpec
        | rangeValueLiteralSpec
        ;

// 값 리터럴에서만 음수를 허용한다. (length, 배열 인덱스, 파라미터/연산변수 인덱스, duration 등은
// 부호 없는 INTEGER만 받으므로 음수가 차단된다.)
propertyValueLiteralSpec: ID | STRING | '-'? INTEGER | '-'? FLOAT | BOOLEAN;
mlpPropertyValueLiteralSpec: STRING '@' ID;
fileValueLiteralSpec: 'file' ':' STRING '(' STRING ')';
rangeValueLiteralSpec: '[' propertyValueLiteralSpec ',' propertyValueLiteralSpec ']';

durationLiteralSpec: INTEGER ('ms' | 's' | 'm' | 'h' | 'd');
iso8601DurationLiteralSpec: ISO8601_DURATION;

// 문법 키워드를 식별자(idShort, 인스턴스명, projection 컬럼명 등)로도 쓸 수 있게 허용한다.
// (예: 'length', 'last', 'in' 같은 이름을 따옴표 없이 사용 가능)
idOrString: ID | STRING | keyword;
keyword: 'idShort' | 'submodel' | 'id'
       | 'param' | 'oparg' | 'opvar' | 'timeseries'
       | 'in' | 'out' | 'inout'
       | 'last' | 'now' | 'latest'
       | 'mdt' | 'file';

// Lexer rules
NULL: 'null';
BOOLEAN: 'true' | 'false';
// ISO8601 기간(시간 기반, java.time.Duration 호환): 일/시/분/초(소수 초 포함) 복합형 지원.
// 예: P1D, PT2H30M, P1DT2H30M, PT0.5S. 최소 한 개 컴포넌트 필요.
// (달력 단위 Y(년)/M(월)/W(주)는 절대 기간으로 모호하여 제외)
ISO8601_DURATION: 'P' [0-9]+ 'D' ( 'T' ISO8601_TIME )?
                | 'P' 'T' ISO8601_TIME;
fragment ISO8601_TIME: [0-9]+ 'H' ([0-9]+ 'M')? ([0-9]+ ('.' [0-9]+)? 'S')?
                     | [0-9]+ 'M' ([0-9]+ ('.' [0-9]+)? 'S')?
                     | [0-9]+ ('.' [0-9]+)? 'S';
// ISO8601 날짜/일시: YYYY-MM-DD  또는  YYYY-MM-DDThh:mm[:ss[.fff]][Z|±hh:mm]
TIMESTAMP: [0-9][0-9][0-9][0-9] '-' [0-9][0-9] '-' [0-9][0-9]
           ( 'T' [0-9][0-9] ':' [0-9][0-9] ( ':' [0-9][0-9] ( '.' [0-9]+ )? )?
             ( 'Z' | ('+' | '-') [0-9][0-9] ':' [0-9][0-9] )? )?;
ID: [a-zA-Z_] [a-zA-Z0-9_-]*;
INTEGER: [0-9]+;
FLOAT: [0-9]+ '.' [0-9]+;
STRING: ('"' .*? '"') | ('\'' .*? '\'');
WS: [ \t\r\n]+ -> skip; 