package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.statement.DatabaseFunction;

@DataTypeInfo(name="char", aliases = "java.sql.Types.CHAR", minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class CharType extends LiquibaseDataType {
    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
//        if (database instanceof MSSQLDatabase) {
//            Object[] parameters = getParameters();
//            if (parameters.length > 0) {
//                String param1 = parameters[0].toString();
//                if (!param1.matches("\\d+")
//                        || new BigInteger(param1).compareTo(BigInteger.valueOf(8000)) > 0) {
//
//                    DatabaseDataType type = new DatabaseDataType(database.escapeDataTypeName("char"), 8000);
//                    type.addAdditionalInformation(getAdditionalInformation());
//                    return type;
//                }
//            }
//            if (parameters.length == 0) {
//                parameters = new Object[] { 1 };
//            } else if (parameters.length > 1) {
//                parameters = Arrays.copyOfRange(parameters, 0, 1);
//            }
//            DatabaseDataType type = new DatabaseDataType(database.escapeDataTypeName("char"), parameters);
//            type.addAdditionalInformation(getAdditionalInformation());
//            return type;
//        }
        return super.toDatabaseDataType(database);
    }

    @Override
    public String objectToSql(Object value, Database database) {
        if (value == null || value.toString().equalsIgnoreCase("null")) {
            return null;
        }

        if (value instanceof DatabaseFunction) {
            return value.toString();
        }

        String val = String.valueOf(value);
//        if (database instanceof MSSQLDatabase && !StringUtils.isAscii(val)) {
//            return "N'"+database.escapeStringForDatabase(val)+"'";
//        }

        return "'"+database.escapeStringForDatabase(val)+"'";
    }

    /**
     * Return the size of this data type definition. If unknown or unspecified, return -1
     */
    protected int getSize() {
        if (getParameters().length == 0) {
            return -1;
        }

        if (getParameters()[0] instanceof String) {
            return Integer.valueOf((String) getParameters()[0]);
        }

        if (getParameters()[0] instanceof Number) {
            return ((Number) getParameters()[0]).intValue();
        }

        return -1;
    }


}
