package com.google.code.shardbatis.converter;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.values.ValuesStatement;

import java.util.Iterator;

/**
 * @author sean.he
 * @author colddew
 */
public class SelectSqlConverter extends AbstractSqlConverter {

    @Override
    protected Statement doConvert(Statement statement, final Object params, final String mapperId) {

        if (!(statement instanceof Select)) {
            throw new IllegalArgumentException("The argument statement must is instance of Select.");
        }

        TableNameModifier modifier = new TableNameModifier(params, mapperId);
        ((Select) statement).getSelectBody().accept(modifier);

        return statement;
    }

    private class TableNameModifier implements SelectVisitor, FromItemVisitor, ExpressionVisitor, ItemsListVisitor {

        private Object params;
        private String mapperId;

        TableNameModifier(Object params, String mapperId) {
            this.params = params;
            this.mapperId = mapperId;
        }

        private void visitBinaryExpression(BinaryExpression binaryExpression) {
            binaryExpression.getLeftExpression().accept(this);
            binaryExpression.getRightExpression().accept(this);
        }

        // SelectVisitor interface
        @Override
        public void visit(PlainSelect plainSelect) {

            // from
            plainSelect.getFromItem().accept(this);

            // join
            if (plainSelect.getJoins() != null) {
                plainSelect.getJoins().forEach(join -> join.getRightItem().accept(this));
            }

            // where
            if (plainSelect.getWhere() != null) {
                plainSelect.getWhere().accept(this);
            }

            // order by
            if (plainSelect.getOrderByElements() != null) {
                plainSelect.getOrderByElements().forEach(orderByElement -> {
                    orderByElement.setAscDescPresent(true);
                    orderByElement.getExpression().accept(this);
                });
            }

            // having
            if (plainSelect.getHaving() != null) {
                plainSelect.getHaving().accept(this);
            }

            // limit
            if (plainSelect.getLimit() != null) {
                plainSelect.getLimit().getOffset().accept(this);
                plainSelect.getLimit().getRowCount().accept(this);
            }
        }

        @Override
        public void visit(SetOperationList setOpList) {
            setOpList.getSelects().forEach(selectBody -> selectBody.accept(this));
        }

        @Override
        public void visit(WithItem withItem) {

        }

        @Override
        public void visit(ValuesStatement aThis) {

        }

        // FromItemVisitor
        @Override
        public void visit(Table tableName) {

            String table = tableName.getName();
            table = convertTableName(table, params, mapperId);
            // convert table name
            tableName.setName(table);

            if(tableName.getAlias() != null) {
                tableName.getAlias().setUseAs(true);
            }
        }

        @Override
        public void visit(SubSelect subSelect) {
            subSelect.getSelectBody().accept(this);
        }

        @Override
        public void visit(SubJoin subjoin) {
            subjoin.getLeft().accept(this);
            //todo
            subjoin.getJoinList().forEach(join -> join.getRightItem().accept(this));
        }

        @Override
        public void visit(LateralSubSelect lateralSubSelect) {

        }

        @Override
        public void visit(ValuesList valuesList) {

        }

        @Override
        public void visit(TableFunction tableFunction) {

        }

        @Override
        public void visit(ParenthesisFromItem aThis) {

        }

        // ExpressionVisitor
        @Override
        public void visit(BitwiseRightShift aThis) {

        }

        @Override
        public void visit(BitwiseLeftShift aThis) {

        }

        @Override
        public void visit(NullValue nullValue) {

        }

        @Override
        public void visit(Function function) {

        }

        @Override
        public void visit(SignedExpression signedExpression) {

        }

        @Override
        public void visit(JdbcParameter jdbcParameter) {

        }

        @Override
        public void visit(JdbcNamedParameter jdbcNamedParameter) {

        }

        @Override
        public void visit(DoubleValue doubleValue) {

        }

        @Override
        public void visit(LongValue longValue) {

        }

        @Override
        public void visit(HexValue hexValue) {

        }

        @Override
        public void visit(DateValue dateValue) {

        }

        @Override
        public void visit(TimeValue timeValue) {

        }

        @Override
        public void visit(TimestampValue timestampValue) {

        }

        @Override
        public void visit(Parenthesis parenthesis) {
            parenthesis.getExpression().accept(this);
        }

        @Override
        public void visit(StringValue stringValue) {

        }

        @Override
        public void visit(Addition addition) {
            visitBinaryExpression(addition);
        }

        @Override
        public void visit(Division division) {
            visitBinaryExpression(division);
        }

        @Override
        public void visit(Multiplication multiplication) {
            visitBinaryExpression(multiplication);
        }

        @Override
        public void visit(Subtraction subtraction) {
            visitBinaryExpression(subtraction);
        }

        @Override
        public void visit(AndExpression andExpression) {
            visitBinaryExpression(andExpression);
        }

        @Override
        public void visit(OrExpression orExpression) {
            visitBinaryExpression(orExpression);
        }

        @Override
        public void visit(Between between) {
            between.getLeftExpression().accept(this);
            between.getBetweenExpressionStart().accept(this);
            between.getBetweenExpressionEnd().accept(this);
        }

        @Override
        public void visit(EqualsTo equalsTo) {
            visitBinaryExpression(equalsTo);
        }

        @Override
        public void visit(GreaterThan greaterThan) {
            visitBinaryExpression(greaterThan);
        }

        @Override
        public void visit(GreaterThanEquals greaterThanEquals) {
            visitBinaryExpression(greaterThanEquals);
        }

        @Override
        public void visit(InExpression inExpression) {

            inExpression.getLeftExpression().accept(this);

            //todo
            if (inExpression.getLeftItemsList() != null) {
                inExpression.getLeftItemsList().accept(this);
            }

            inExpression.getRightItemsList().accept(this);
        }

        @Override
        public void visit(IsNullExpression isNullExpression) {

        }

        @Override
        public void visit(LikeExpression likeExpression) {
            visitBinaryExpression(likeExpression);
        }

        @Override
        public void visit(MinorThan minorThan) {
            visitBinaryExpression(minorThan);
        }

        @Override
        public void visit(MinorThanEquals minorThanEquals) {
            visitBinaryExpression(minorThanEquals);
        }

        @Override
        public void visit(NotEqualsTo notEqualsTo) {
            visitBinaryExpression(notEqualsTo);
        }

        @Override
        public void visit(Column tableColumn) {

        }

        @Override
        public void visit(CaseExpression caseExpression) {

        }

        @Override
        public void visit(WhenClause whenClause) {

        }

        @Override
        public void visit(ExistsExpression existsExpression) {
            existsExpression.getRightExpression().accept(this);
        }

        @Override
        public void visit(AllComparisonExpression allComparisonExpression) {
            allComparisonExpression.getSubSelect().getSelectBody().accept(this);
        }

        @Override
        public void visit(AnyComparisonExpression anyComparisonExpression) {
            anyComparisonExpression.getSubSelect().getSelectBody().accept(this);
        }

        @Override
        public void visit(Concat concat) {
            visitBinaryExpression(concat);
        }

        @Override
        public void visit(Matches matches) {
            visitBinaryExpression(matches);
        }

        @Override
        public void visit(BitwiseAnd bitwiseAnd) {
            visitBinaryExpression(bitwiseAnd);
        }

        @Override
        public void visit(BitwiseOr bitwiseOr) {
            visitBinaryExpression(bitwiseOr);
        }

        @Override
        public void visit(BitwiseXor bitwiseXor) {
            visitBinaryExpression(bitwiseXor);
        }

        @Override
        public void visit(CastExpression cast) {

        }

        @Override
        public void visit(Modulo modulo) {

        }

        @Override
        public void visit(AnalyticExpression aexpr) {

        }

        @Override
        public void visit(ExtractExpression eexpr) {

        }

        @Override
        public void visit(IntervalExpression iexpr) {

        }

        @Override
        public void visit(OracleHierarchicalExpression oexpr) {

        }

        @Override
        public void visit(RegExpMatchOperator rexpr) {

        }

        @Override
        public void visit(JsonExpression jsonExpr) {

        }

        @Override
        public void visit(JsonOperator jsonExpr) {

        }

        @Override
        public void visit(RegExpMySQLOperator regExpMySQLOperator) {

        }

        @Override
        public void visit(UserVariable var) {

        }

        @Override
        public void visit(NumericBind bind) {

        }

        @Override
        public void visit(KeepExpression aexpr) {

        }

        @Override
        public void visit(MySQLGroupConcat groupConcat) {

        }

        @Override
        public void visit(ValueListExpression valueList) {

        }

        @Override
        public void visit(RowConstructor rowConstructor) {

        }

        @Override
        public void visit(OracleHint hint) {

        }

        @Override
        public void visit(TimeKeyExpression timeKeyExpression) {

        }

        @Override
        public void visit(DateTimeLiteralExpression literal) {

        }

        @Override
        public void visit(NotExpression aThis) {

        }

        @Override
        public void visit(NextValExpression aThis) {

        }

        @Override
        public void visit(CollateExpression aThis) {

        }

        // ItemsListVisitor
        @Override
        public void visit(ExpressionList expressionList) {
            for (Iterator iter = expressionList.getExpressions().iterator(); iter.hasNext(); ) {
                Expression expression = (Expression) iter.next();
                expression.accept(this);
            }
        }

        @Override
        public void visit(NamedExpressionList namedExpressionList) {

        }

        @Override
        public void visit(MultiExpressionList multiExprList) {

        }
    }
}
