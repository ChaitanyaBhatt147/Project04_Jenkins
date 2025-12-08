package in.co.rays.proj4.util;

import java.sql.*;
import java.util.ResourceBundle;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * <p>
 * JDBCDataSource is a Singleton class responsible for managing all database
 * connections using the C3P0 connection pooling library.
 * </p>
 *
 * <p>
 * This utility:
 * </p>
 * <ul>
 *     <li>Loads database configuration from system.properties file</li>
 *     <li>Creates and manages a shared connection pool</li>
 *     <li>Provides static methods to acquire and close connections</li>
 *     <li>Ensures efficient memory and resource usage</li>
 * </ul>
 *
 * <p>
 * Parameters loaded from the ResourceBundle:
 * </p>
 * <ul>
 *     <li>driver – JDBC driver class</li>
 *     <li>url – Database connection URL</li>
 *     <li>username – DB username</li>
 *     <li>password – DB password</li>
 *     <li>initialpoolsize – starting number of connections</li>
 *     <li>acquireincrement – connections added when pool is exhausted</li>
 *     <li>maxpoolsize – maximum allowed connections</li>
 * </ul>
 *
 * <p>
 * This class is used throughout the project to safely obtain and release
 * connections for all model classes.
 * </p>
 *
 * @author
 * @version 1.0
 */
public class JDBCDataSource {

    /** Singleton instance of JDBCDataSource */
    private static JDBCDataSource jds = null;

    /** C3P0 connection pool instance */
    private ComboPooledDataSource cpds = null;

    /** Loads DB configuration from system.properties file */
    private static ResourceBundle rb = ResourceBundle.getBundle("in.co.rays.proj4.bundle.system");

    /**
     * Private constructor to initialize connection pool.
     * This ensures Singleton implementation.
     */
    private JDBCDataSource() {

        try {
            cpds = new ComboPooledDataSource();
            cpds.setDriverClass(rb.getString("driver"));

            String env = System.getProperty("env");

            if ("docker".equals(env)) {
                cpds.setJdbcUrl(rb.getString("url.docker"));
            } else {
                cpds.setJdbcUrl(rb.getString("url.local"));
            }

           
            
            cpds.setUser(rb.getString("username"));
            cpds.setPassword(rb.getString("password"));
            cpds.setInitialPoolSize(Integer.parseInt(rb.getString("initialpoolsize")));
            cpds.setAcquireIncrement(Integer.parseInt(rb.getString("acquireincrement")));
            cpds.setMaxPoolSize(Integer.parseInt(rb.getString("maxpoolsize")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the Singleton instance of JDBCDataSource.
     *
     * @return JDBCDataSource instance
     */
    public static JDBCDataSource getInstance() {
        if (jds == null) {
            jds = new JDBCDataSource();
        }
        return jds;
    }

    /**
     * Returns a database connection from the C3P0 connection pool.
     *
     * @return Connection object or null if connection cannot be created
     */
    public static Connection getConnection() {
        try {
            return getInstance().cpds.getConnection();
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * Safely closes ResultSet, Statement, and Connection objects.
     *
     * @param conn the Connection to close
     * @param stmt the Statement to close
     * @param rs   the ResultSet to close
     */
    public static void closeConnection(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.close(); // returns connection back to the pool
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Safely closes Statement and Connection objects.
     *
     * @param conn the Connection to close
     * @param stmt the Statement to close
     */
    public static void closeConnection(Connection conn, Statement stmt) {
        closeConnection(conn, stmt, null);
    }

    /**
     * Safely closes only the Connection object.
     *
     * @param conn the Connection to close
     */
    public static void closeConnection(Connection conn) {
        closeConnection(conn, null);
    }
}
