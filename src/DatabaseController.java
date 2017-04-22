import javax.swing.tree.RowMapper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 18.04.2017.
**/
public class DatabaseController implements AutoCloseable {

    private Connection connection;
    private PreparedStatement statement;
    private ResultSet rs;

    public DatabaseController(String url, String login, String password) {
        try {
            this.connection = DriverManager.getConnection(url, login, password);
            this.rs = null;
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }
/*
*  Select запрос к базе данных. Без экранирования, использовать осторожно
*/
    protected ResultSet select (String fields, String tableName, String where, String orderBy, String limitOffset) {
        if (this.rs != null) {
            try {this.rs.close();} catch (SQLException se) {se.printStackTrace();}
        }
        this.rs = null;

        try {
            this.statement = this.connection.prepareStatement("SELECT " + fields + " FROM " + tableName +
                    (where.length() > 0 ? " WHERE " + where : "") +
                    (orderBy.length() > 0 ? " ORDER BY " + orderBy : "") +
                    (limitOffset.length() > 0 ? " LIMIT " + limitOffset : ""));

            this.rs = this.statement.executeQuery();
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            //try {this.statement.close();} catch (SQLException se) {}
            return this.rs;
        }
    }

/*
*  Select запрос к базе данных. С экранированием условия
*/
    protected ResultSet select (String fields, String tableName, String where, String[] whereValues, String orderBy, String limitOffset) {
        if (this.rs != null) {
            try {this.rs.close();} catch (SQLException se) {se.printStackTrace();}
        }
        this.rs = null;

        try {
            this.statement = this.connection.prepareStatement("SELECT " + fields + " FROM " + tableName +
                    (where.length() > 0 ? " WHERE " + where : "") +
                    (orderBy.length() > 0 ? " ORDER BY " + orderBy : "") +
                    (limitOffset.length() > 0 ? " LIMIT " + limitOffset : ""));

            for (int i = 1; i <= whereValues.length; i++)
                this.statement.setString(i,whereValues[i-1]);

            rs = this.statement.executeQuery();
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            //try {this.statement.close();} catch (SQLException se) {}
            return rs;
        }
    }

/*
*  Insert запрос к базе данных. Вставляет одну строку, использует экранирование
*  */
    protected int insert (String tableName, String fields, String[] values) {
        int insertedId = 0;
        if (values.length == 0)
            return insertedId;

        try {
            String preparedValues = "";
            for (String value : values) {
                preparedValues += "?,";
            }
            preparedValues = preparedValues.substring(0,preparedValues.length()-1);


            this.statement = this.connection.prepareStatement("INSERT INTO " + tableName + " (" + fields + ") VALUES (" + preparedValues + ")");
            for (int i = 1; i <= values.length; i++) {
                this.statement.setString(i,values[i-1]);
            }

            insertedId = this.statement.executeUpdate();
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {this.statement.close();} catch (SQLException se) {se.printStackTrace();}
            return insertedId;
        }
    }

/*
*  Insert запрос к базе данных. Вставляет множество строк, использует экранирование
*  */
    protected int insert (String tableName, String fields, ArrayList<String[]> values) {
        int lastInsertedId = 0;
        if (values.size() == 0)
            return lastInsertedId;

        try {
            String preparedValues = "";
            for (String[] valueArray : values) {
                preparedValues += "(";
                for (String value : valueArray) {
                    preparedValues += "?,";
                }
                preparedValues = preparedValues.substring(0,preparedValues.length()-1);
                preparedValues += "),";
            }
            preparedValues = preparedValues.substring(0,preparedValues.length()-1);


            this.statement = this.connection.prepareStatement("INSERT INTO " + tableName + " (" + fields + ") VALUES " + preparedValues);
            int index = 1;
            for (String[] valueArray : values) {
                for (String value : valueArray) {
                    this.statement.setString(index++,value);
                }
            }

            lastInsertedId = this.statement.executeUpdate();
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {this.statement.close();} catch (SQLException se) {se.printStackTrace();}
            return lastInsertedId;
        }
    }

    /*
    *  UPDATE запрос к базе. С экранизацией */
    protected int update (String tableName, String[] fields, String[] values, String where, String[] whereValues) {
        int updateId = 0;

        if ((fields.length != values.length) || fields.length == 0)
            return updateId;

        try {
            String setStatement = "";
            for (String field : fields) {
                setStatement += field + " = ?,";
            }
            setStatement = setStatement.substring(0,setStatement.length()-1);

            this.statement = this.connection.prepareStatement("UPDATE " + tableName + " SET " + setStatement +
                    (where.length() > 0 ? " WHERE " + where : ""));

            int escapeIndex = 1;
            for (String value : values) {
                this.statement.setString(escapeIndex++,value);
            }

            if (whereValues.length > 0) {
                for (String value : whereValues) {
                    this.statement.setString(escapeIndex++,value);
                }
            }

            updateId = this.statement.executeUpdate();
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {this.statement.close();} catch (SQLException se) {}
            return updateId;
        }
    }

    protected int delete (String tableName, String where, String[] whereValues) {
        int deletedId = 0;

        try {

            this.statement = this.connection.prepareStatement("DELETE FROM " + tableName +
                    (where.length() > 0 ? " WHERE " + where : ""));

            int escapeIndex = 1;

            if (whereValues.length > 0) {
                for (String value : whereValues) {
                    this.statement.setString(escapeIndex++,value);
                }
            }

            deletedId = this.statement.executeUpdate();
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {this.statement.close();} catch (SQLException se) {}
            return deletedId;
        }
    }

    protected void closeStatementSet() {
        try{
            if (this.statement != null) {
                if (!this.statement.isClosed()) {
                    try {this.statement.close();} catch (SQLException se) {se.printStackTrace();}
                }
            }

            if (this.rs != null) {
                if (!this.rs.isClosed()) {
                    try {this.rs.close();} catch (SQLException se) {se.printStackTrace();}
                }
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }

    }

    @Override
    public void close() throws Exception {
        try {this.connection.close();} catch (SQLException se) {se.printStackTrace();}
    }
}
