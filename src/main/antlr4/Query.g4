grammar Query;

@header {
package org.herring.manager.query;
}

import CommonLexerRules ;

// Single Comparison
singleComparisonExpression
    : left=(FIELD_IDENTIFIER | DATE_LITERAL | POSITIVE_INTEGER | NEGATIVE_INTEGER | POSITIVE_REAL | NEGATIVE_REAL | SMALL_QUOTED_STRING | LARGE_QUOTED_STRING)
      operator=(LESS_THAN | LESS_THAN_OR_EQUAL | GREATER_THAN | GREATER_THAN_OR_EQUAL | EQUAL | NOT_EQUAL | IN)
      right=(FIELD_IDENTIFIER | DATE_LITERAL | POSITIVE_INTEGER | NEGATIVE_INTEGER | POSITIVE_REAL | NEGATIVE_REAL | SMALL_QUOTED_STRING | LARGE_QUOTED_STRING)
    ;

// Combined Comparison
combinedComparisonExpression
    : OPEN_BRACE combinedComparisonExpression CLOSE_BRACE               # comparisonWrappedExpression
    | combinedComparisonExpression AND combinedComparisonExpression     # comparisonAndCombinedExpression
    | combinedComparisonExpression OR combinedComparisonExpression      # comparisonOrCombinedExpression
    | NOT combinedComparisonExpression                                  # comparisonNotCombinedExpression
    | singleComparisonExpression                                        # comparisonSingleExpression
    ;

// Time Expression
timeExpression
    : date=DATE_LITERAL ('T' time=TIME_LITERAL)? ('Z' zone=ZONE_LITERAL)?
    ;

// Time Range Expression
timeRangeExpression
    : BETWEEN from=timeExpression AND to=timeExpression                         # absoluteTimeRangeExpression
    | IN timeValue=POSITIVE_INTEGER timeUnit=(DAYS | MONTHS | HOURS | MINUTES)  # relativeTimeRangeExpression
    ;

// Aggregate Detail Expression
aggregateExpression
    : function=(AVERAGE | COUNT | MIN | MAX | SUM | MEDIAN) OPEN_BRACE funcVar=FIELD_IDENTIFIER CLOSE_BRACE     # calcAggregateFunction
    ;

// ASK Query Syntax
askQuerySyntax
    : ASK table=FIELD_IDENTIFIER timeRangeExpression
    ;

// Filter Query Syntax
filterQuerySyntax
    : FILTER combinedComparisonExpression
    ;

// Aggregate Query Syntax
aggregateByFieldQuerySyntax
    : CALCULATE resultVar=FIELD_IDENTIFIER EQUAL aggregateExpression (BY groupVar=FIELD_IDENTIFIER)?
    ;

aggregateByTimeQuerySyntax
    : AGGREGATE resultVar=FIELD_IDENTIFIER EQUAL aggregateExpression BY TIME timeValue=POSITIVE_INTEGER timeUnit=(DAYS | MONTHS | HOURS | MINUTES)
    ;

optionalQuerySyntax
    : PIPE filterQuerySyntax
    | PIPE aggregateByFieldQuerySyntax
    | PIPE aggregateByTimeQuerySyntax
    ;

// All Query Syntax
querySyntax
    : askQuerySyntax optionalQuerySyntax+
    ;