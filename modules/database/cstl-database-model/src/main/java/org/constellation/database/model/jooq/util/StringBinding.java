
package org.constellation.database.model.jooq.util;

import org.jooq.*;
import org.jooq.impl.DSL;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.util.Objects;

/**
 *
 * @author guilhem
 */
public class StringBinding implements Binding {

    @Override
    public Converter converter() {
        return new StringConverter();
    }

    @Override
    public void sql(BindingSQLContext ctx) throws SQLException {
        ctx.render().castMode(RenderContext.CastMode.NEVER);
        ctx.render().visit(DSL.val(ctx.convert(converter()).value()));
        ctx.render().castMode(RenderContext.CastMode.ALWAYS);
    }

    @Override
    public void register(BindingRegisterContext ctx) throws SQLException {
        ctx.statement().registerOutParameter(ctx.index(), Types.VARCHAR);
    }

    @Override
    public void set(BindingSetStatementContext ctx) throws SQLException {
        ctx.statement().setString(ctx.index(), Objects.toString(ctx.convert(converter()).value(), null));
    }

    @Override
    public void set(BindingSetSQLOutputContext ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void get(BindingGetResultSetContext ctx) throws SQLException {
        ctx.convert(converter()).value(ctx.resultSet().getString(ctx.index()));
    }

    @Override
    public void get(BindingGetStatementContext ctx) throws SQLException {
        ctx.convert(converter()).value(ctx.statement().getString(ctx.index()));
    }

    @Override
    public void get(BindingGetSQLInputContext ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    
    
}
