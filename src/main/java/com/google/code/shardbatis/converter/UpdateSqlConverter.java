package com.google.code.shardbatis.converter;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.update.Update;

/**
 * @author sean.he
 * @author colddew
 */
public class UpdateSqlConverter extends AbstractSqlConverter {

    @Override
    protected Statement doConvert(Statement statement, Object params, String mapperId) {

        if (!(statement instanceof Update)) {
            throw new IllegalArgumentException("The argument statement must is instance of Update.");
        }

        Update update = (Update) statement;
        update.getTables().forEach(table -> table.setName(this.convertTableName(table.getName(), params, mapperId)));

//        String name = update.getTables().get(0).getName();
//        update.getTables().get(0).setName(this.convertTableName(name, params, mapperId));

        return update;
    }
}
