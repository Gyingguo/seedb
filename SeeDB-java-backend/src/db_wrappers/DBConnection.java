package db_wrappers;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import common.DBSettings;

/**
 * Provides DB independent interface to the DBMS
 * @author manasi
 *
 */
public class DBConnection {
	public static String[] supportedDatabases = new String[] {"PostgreSQL"};
	private Connection connection = null;
	public String databaseType;
	public String database;
	
	/**
	 * Is this DBMS supported
	 * @param databaseType
	 * @return
	 */
	public static boolean isDBSupported(String databaseType) {
		for (String db : supportedDatabases) {
			if (db.equalsIgnoreCase(databaseType)) return true;
		}
		return false;
	}
	
	/**
	 * Connect to the the given database
	 * @param database
	 * @param databaseType
	 * @param username
	 * @param password
	 * @return
	 */
	public boolean connectToDatabase(String database, String databaseType, 
			String username, String password) {
		if ((database == null) || (databaseType == null) || (username == null) 
				|| (password == null)) return false;
		if (!isDBSupported(databaseType)) return false;
		//find driver
		try {
		    Class.forName("org." + databaseType + ".Driver");
		    System.out.println("DB driver found");
		} catch (ClassNotFoundException e) {
			connection = null;
		    System.out.println("DB driver not found");
		    e.printStackTrace();
		    return false;
		}
		
		//attempt to connect
		try {
			connection = DriverManager.getConnection(
					"jdbc:" + databaseType + "://" + database, username,
					password);
		} catch (SQLException e) {
			connection = null;
			System.out.println("DB connection failed.");
			e.printStackTrace();
			return false;
		}
		this.databaseType = databaseType;
		this.database = database;
		return true;
	}

	/**
	 * Connect to database with default settings
	 * @param settings
	 * @return
	 */
	public boolean connectToDatabase(DBSettings settings) {
		return connectToDatabase(settings.database, settings.databaseType, 
				settings.username, settings.password);
	}
	
	public void setConnection(Connection c) {
		this.connection = c;
	}
	
	/**
	 * Is there is valid connection to the database
	 * @return
	 */
	public boolean hasConnection() {
		return connection != null;
	}
	
	/**
	 * What DBMS system are we connected to
	 * @return
	 */
	public String getDatabaseType() {
		return this.databaseType;
	}
	
	/**
	 * Get name of database we are connecting to
	 * @return
	 */
	public String getDatabaseName() {
		return this.database;
	}
	
	/**
	 * Execute query and return resultset
	 * @param query
	 * @return
	 */
	public ResultSet executeQuery(String query)
	{
		Statement stmt = null;
		ResultSet rs = null;
		try {
			// Get a statement from the connection
		    stmt = connection.createStatement() ;

		    // Execute the query
		    rs = stmt.executeQuery(query) ;
		}
		catch(Exception e)
		{
			System.out.println("Error in executing query");
			e.printStackTrace();
		}
		return rs;
	}
	
	/**
	 * Query table columns and return resultset
	 * @param table
	 * @return
	 */
	public ResultSet getTableColumns(String table) {
		if (table == null) {
			throw new NullPointerException("Table is null.");
		}
		DatabaseMetaData dbmd = null;
		ResultSet rs = null;
		try {
			dbmd = connection.getMetaData();
			rs = dbmd.getColumns(null, null, table, null);
		} catch (SQLException e) {
			System.out.println("Error in executing query");
			e.printStackTrace();
			return null;
		}
		return rs;
	}
	
	
}