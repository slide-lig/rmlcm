package com.rapidminer.lcm.io;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import com.rapidminer.lcm.obj.ResultListIOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;

public class RMSQLiteWriter extends Operator {

	private static final String dbname = "database name";
	private static final String tablename = "table name";

	private InputPort input = this.getInputPorts().createPort("input");

	private int tbsize;

	public RMSQLiteWriter(OperatorDescription description) {
		super(description);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void doWork() throws OperatorException {

		ResultListIOObject result = input.getData(ResultListIOObject.class);

		Connection c = null;

		String nameofdb = this.getParameter(dbname);

		connectDB(c, nameofdb);

		String nameoftb = this.getParameter(tablename);

		tbsize = this.lenthofLongestPattern(result);

		createTable(c, nameofdb, nameoftb, tbsize);

		insert(c, nameofdb, nameoftb, result, tbsize);

		// commitAndclose(c);

		// String table = "RESULT";

	}

	public void connectDB(Connection c, String nameofdb) {

		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + nameofdb + ".db");
		} catch (ClassNotFoundException e) {
			System.err.println("Class not found!");
			e.printStackTrace();
		} catch (SQLException e) {
			System.err.println("Connection error!");
			e.printStackTrace();
		}
		System.out.println("Opened database successfully!");
	}

	public void createTable(Connection c, String nameofdb, String nameoftb,
			int rowsize) {

		Statement stmt = null;

		try {

			// c = DriverManager.getConnection("jdbc:sqlite:" + nameofdb +
			// ".db");
			c = DriverManager.getConnection("jdbc:sqlite:" + nameofdb + ".db");
			// System.out.println("Opened database successfully");

			stmt = c.createStatement();

			StringBuilder items = new StringBuilder();

			for (int i = 0; i < rowsize - 2; i++) {
				if (i != rowsize - 3) {
					items.append("ITEM" + (i + 1) + " INT DEFAULT NULL, ");
				} else {
					items.append("ITEM" + (i + 1) + " INT DEFAULT NULL");
				}
			}

			// DatabaseMetaData dbm = c.getMetaData();
			//
			// ResultSet tables = dbm.getTables(null, null, nameoftb, null);
			//
			// if (tables.next()) {
			// // nameoftb = "new_" + nameoftb;
			// String dorp = "DROP TABLE " + nameoftb;
			// stmt.executeUpdate(dorp);
			// }

			String dorp = "DROP TABLE IF EXISTS " + nameoftb+ ";";
			stmt.executeUpdate(dorp);

			String sql = "CREATE TABLE IF NOT EXISTS " + nameoftb
					+ " (ID INTEGER PRIMARY KEY  NOT NULL, "
					+ "SUPPORT INT NOT NULL, " + items.toString() + ");";

			//System.out.println("-- :" + sql);

			stmt.executeUpdate(sql);
			stmt.close();
			c.close();

			System.out.println("Create Table OK");
		} catch (SQLException e) {
			System.err.println("Create statement error!");
			e.printStackTrace();
		}

	}

	public void insert(Connection c, String nameofdb, String nameoftb,
			ResultListIOObject result, int rowsize) {

		Statement stmt = null;
		try {
			c = DriverManager.getConnection("jdbc:sqlite:" + nameofdb + ".db");
			c.setAutoCommit(false);

			stmt = c.createStatement();

			String sql = null;

			StringBuilder items = new StringBuilder();

			for (int i = 0; i < rowsize - 2; i++) {
				if (i != rowsize - 3) {
					items.append("ITEM" + (i + 1) + ",");
				} else {
					items.append("ITEM" + (i + 1));
				}
			}

			int id = 0;

			StringBuilder sbvalues = new StringBuilder();

			String[] values = new String[rowsize - 1];
			Arrays.fill(values, null);

			for (int[] list : result.getResultlist()) {

				// for (int i = 0; i < list.length-1; i++) {
				// System.out.print("** " + list[i] + " ");
				// }

				for (int j = 0; j < values.length; j++) {

					if (j != values.length - 1) {
						if (j < list.length - 1) {
							sbvalues.append(String.valueOf(list[j]));
							sbvalues.append(",");
						} else {
							sbvalues.append(values[j]);
							sbvalues.append(",");
						}
					} else {
						if (j == list.length - 2) {
							sbvalues.append(list[j]);
						} else {
							sbvalues.append(values[j]);
						}
					}
				}

				sql = "INSERT INTO " + nameoftb + "(ID,SUPPORT,"
						+ items.toString() + ") " + "VALUES (" + id + ","
						+ sbvalues.toString() + ");";

				Arrays.fill(values, null);
				sbvalues.setLength(0);
				//System.out.println("++ " + sql);
				stmt.executeUpdate(sql);

				id = id + 1;
			}

			stmt.close();

			c.commit();
			c.close();
		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			// System.exit(0);
			System.err.println("set auto-commit false failed!");
			e.printStackTrace();
		}

	}

	public int lenthofLongestPattern(ResultListIOObject result) {
		int length = 0;
		for (int[] item : result.getResultlist()) {
			if (item.length > length) {
				length = item.length;
			}
		}
		return length;
	}

	public void commitAndclose(Connection c) {
		try {

			c.commit();
			c.close();
		} catch (SQLException e) {
			System.err.println("commit and close failed!");
			e.printStackTrace();
		}
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		types.add(new ParameterTypeString(dbname, "Name of Database", "res"));

		types.add(new ParameterTypeString(tablename, "Name of table", "tbl"));

		return types;
	}
}
