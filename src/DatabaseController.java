import javax.swing.tree.RowMapper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by User on 18.04.2017.
**/
public class DatabaseController implements AutoCloseable {

    private Connection connection;
    private ArrayList<PreparedStatement> statements;
    private ArrayList<ResultSet> rss;

    public DatabaseController(String url, String login, String password) {
        try {
            this.connection = DriverManager.getConnection(url, login, password);
            this.statements = new ArrayList<PreparedStatement>();
            this.rss = new ArrayList<ResultSet>();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }
/*
*  Select запрос к базе данных. Без экранирования, использовать осторожно
*/
    protected ResultSet select (String fields, String tableName, String where, String orderBy, String limitOffset) {
        PreparedStatement stm = null;
        ResultSet rs = null;

        statements.add(stm);
        rss.add(rs);

        try {
            stm = this.connection.prepareStatement("SELECT " + fields + " FROM " + tableName +
                    (where.length() > 0 ? " WHERE " + where : "") +
                    (orderBy.length() > 0 ? " ORDER BY " + orderBy : "") +
                    (limitOffset.length() > 0 ? " LIMIT " + limitOffset : ""));


            rs = stm.executeQuery();
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            return rs;
        }
    }

/*
*  Select запрос к базе данных. С экранированием условия
*/
    protected ResultSet select (String fields, String tableName, String where, String[] whereValues, String orderBy, String limitOffset) {
        PreparedStatement stm = null;
        ResultSet rs = null;

        statements.add(stm);
        rss.add(rs);

        try {
            stm = this.connection.prepareStatement("SELECT " + fields + " FROM " + tableName +
                    (where.length() > 0 ? " WHERE " + where : "") +
                    (orderBy.length() > 0 ? " ORDER BY " + orderBy : "") +
                    (limitOffset.length() > 0 ? " LIMIT " + limitOffset : ""));

            for (int i = 1; i <= whereValues.length; i++)
                stm.setString(i,whereValues[i-1]);

            rs = stm.executeQuery();
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
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

        PreparedStatement stm = null;

        try {
            String preparedValues = "";
            for (String value : values) {
                preparedValues += "?,";
            }
            preparedValues = preparedValues.substring(0,preparedValues.length()-1);


            stm = this.connection.prepareStatement("INSERT INTO " + tableName + " (" + fields + ") VALUES (" + preparedValues + ")");
            for (int i = 1; i <= values.length; i++) {
                stm.setString(i,values[i-1]);
            }

            insertedId = stm.executeUpdate();
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {stm.close();} catch (SQLException se) {se.printStackTrace();}
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

        PreparedStatement stm = null;

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


            stm = this.connection.prepareStatement("INSERT INTO " + tableName + " (" + fields + ") VALUES " + preparedValues);
            int index = 1;
            for (String[] valueArray : values) {
                for (String value : valueArray) {
                    stm.setString(index++,value);
                }
            }

            lastInsertedId = stm.executeUpdate();
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {stm.close();} catch (SQLException se) {se.printStackTrace();}
            return lastInsertedId;
        }
    }

    /*
    *  UPDATE запрос к базе. С экранизацией */
    protected int update (String tableName, String[] fields, String[] values, String where, String[] whereValues) {
        int updateId = 0;

        if ((fields.length != values.length) || fields.length == 0)
            return updateId;

        PreparedStatement stm = null;

        try {
            String setStatement = "";
            for (String field : fields) {
                setStatement += field + " = ?,";
            }
            setStatement = setStatement.substring(0,setStatement.length()-1);

            stm = this.connection.prepareStatement("UPDATE " + tableName + " SET " + setStatement +
                    (where.length() > 0 ? " WHERE " + where : ""));

            int escapeIndex = 1;
            for (String value : values) {
                stm.setString(escapeIndex++,value);
            }

            if (whereValues.length > 0) {
                for (String value : whereValues) {
                    stm.setString(escapeIndex++,value);
                }
            }

            updateId = stm.executeUpdate();
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {stm.close();} catch (SQLException se) {}
            return updateId;
        }
    }

    protected int delete (String tableName, String where, String[] whereValues) {
        int deletedId = 0;

        PreparedStatement stm = null;

        try {

            stm = this.connection.prepareStatement("DELETE FROM " + tableName +
                    (where.length() > 0 ? " WHERE " + where : ""));

            int escapeIndex = 1;

            if (whereValues.length > 0) {
                for (String value : whereValues) {
                    stm.setString(escapeIndex++,value);
                }
            }

            deletedId = stm.executeUpdate();
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {stm.close();} catch (SQLException se) {}
            return deletedId;
        }
    }

    protected void closeStatementSet() {
        try{
            ArrayList<PreparedStatement> delStm = new ArrayList<PreparedStatement>();
            for (PreparedStatement stm : statements) {
                if (stm != null) {
                    if (!stm.isClosed()) {
                        try {stm.close();} catch (SQLException se) {se.printStackTrace();}
                    }
                }
                delStm.add(stm);
            }
            for (PreparedStatement dStm : delStm)
                statements.remove(dStm);

            ArrayList<ResultSet> delRs = new ArrayList<ResultSet>();
            for (ResultSet rs : rss) {
                if (rs != null) {
                    if (!rs.isClosed()) {
                        try {rs.close();} catch (SQLException se) {se.printStackTrace();}
                    }
                }
                delRs.add(rs);
            }
            for (ResultSet dRs : delRs)
                rss.remove(dRs);

        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    protected void closeStatementSet(ResultSet outRs) {
        try{
            for (int i = 0; i < rss.size(); i ++) {
                if (rss.get(i) == outRs) {
                    if (rss.get(i) != null) {
                        if (!rss.get(i).isClosed()) {
                            try {rss.get(i).close();} catch (SQLException se) {se.printStackTrace();}
                        }
                    }
                    rss.remove(i);

                    if (statements.get(i) != null) {
                        if (!statements.get(i).isClosed()) {
                            try {statements.get(i).close();} catch (SQLException se) {se.printStackTrace();}
                        }
                    }
                    statements.remove(i);
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
