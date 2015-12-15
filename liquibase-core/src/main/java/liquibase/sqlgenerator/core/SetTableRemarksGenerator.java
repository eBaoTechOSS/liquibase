package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.SetTableRemarksStatement;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;

public class SetTableRemarksGenerator extends AbstractSqlGenerator<SetTableRemarksStatement> {

	@Override
	public boolean supports(SetTableRemarksStatement statement, Database database) {
		return database instanceof MySQLDatabase || database instanceof OracleDatabase || database instanceof PostgresDatabase
				|| database instanceof DB2Database || database instanceof MSSQLDatabase;
	}

	@Override
    public ValidationErrors validate(SetTableRemarksStatement setTableRemarksStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
		ValidationErrors validationErrors = new ValidationErrors();
		validationErrors.checkRequiredField("tableName", setTableRemarksStatement.getTableName());
		return validationErrors;
	}

	@Override
    public Sql[] generateSql(SetTableRemarksStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
		String sql;
		String remarks = database.escapeStringForDatabase(statement.getRemarks());
		if (database instanceof MySQLDatabase) {
			sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " COMMENT = '" + StringUtils.trimToEmpty(remarks)
					+ "'";
		} else if (database instanceof MSSQLDatabase) {
			String schemaName = statement.getSchemaName();
			if (schemaName == null) {
				schemaName = database.getDefaultSchemaName();
			}
			if (schemaName == null) {
				schemaName = "dbo";
			}

			sql = "DECLARE @TableName SYSNAME " +
					"set @TableName = N'" + statement.getTableName() + "'; " +
					"DECLARE @FullTableName SYSNAME; " +
					"SET @FullTableName = N'" + schemaName+"."+statement.getTableName() + "';" +
					"DECLARE @MS_DescriptionValue NVARCHAR(200); " +
					"SET @MS_DescriptionValue = N'" + statement.getRemarks() + "';" +
					"DECLARE @MS_Description NVARCHAR(200) " +
					"set @MS_Description = NULL; " +
					"SET @MS_Description = (SELECT CAST(Value AS NVARCHAR(200)) AS [MS_Description] " +
					"FROM sys.extended_properties AS ep " +
					"WHERE ep.major_id = OBJECT_ID(@FullTableName) " +
					"AND ep.name = N'MS_Description' AND ep.minor_id=0); " +
					"IF @MS_Description IS NULL " +
					"BEGIN " +
					"EXEC sys.sp_addextendedproperty " +
					"@name  = N'MS_Description', " +
					"@value = @MS_DescriptionValue, " +
					"@level0type = N'SCHEMA', " +
					"@level0name = N'" + schemaName + "', " +
					"@level1type = N'TABLE', " +
					"@level1name = @TableName; " +
					"END " +
					"ELSE " +
					"BEGIN " +
					"EXEC sys.sp_updateextendedproperty " +
					"@name  = N'MS_Description', " +
					"@value = @MS_DescriptionValue, " +
					"@level0type = N'SCHEMA', " +
					"@level0name = N'" + schemaName + "', " +
					"@level1type = N'TABLE', " +
					"@level1name = @TableName; " +
					"END";
		} else {
			String command = "COMMENT";

			sql = command + " ON TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " IS '"
					+ database.escapeStringForDatabase(remarks) + "'";
		}

		return new Sql[] { new UnparsedSql(sql, getAffectedTable(statement)) };
	}

    protected Relation getAffectedTable(SetTableRemarksStatement statement) {
        return new Table().setName(statement.getTableName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }
}