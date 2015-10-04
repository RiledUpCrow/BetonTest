/**
 * BetonTest - quiz plugin for Bukkit
 * Copyright (C) 2015  Jakub "Co0sh" Sapalski
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.betoncraft.betontest.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Handles MySQL database.
 *
 * @author Jakub Sapalski
 */
public class Database {

	private Connection con;
	private final String user;
	private final String database;
	private final String password;
	private final String port;
	private final String hostname;

	public Database(String hostname, String port,
			String database, String username, String password) {
		this.hostname = hostname;
		this.port = port;
		this.database = database;
		this.user = username;
		this.password = password;
	}

	public Connection getConnection() {
		if (con == null) {
			con = openConnection();
		}
		return con;
	}

	private Connection openConnection() {
		Connection connection = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://"
					+ this.hostname + ":" + this.port + "/" + this.database,
					this.user, this.password);
		} catch (Exception e) {}
		return connection;
	}

	public void closeConnection() {
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		con = null;
	}

	public void createTables() {
		// create tables if they don't exist
		Connection connection = getConnection();
		try {
			connection.createStatement().executeUpdate(
					"CREATE TABLE IF NOT EXISTS tests (id INTEGER PRIMARY KEY "
					+ "AUTO_INCREMENT, player VARCHAR(36) NOT NULL, test "
					+ "VARCHAR(512) NOT NULL, passed BOOLEAN NOT NULL, date "
					+ "TIMESTAMP DEFAULT CURRENT_TIMESTAMP);");
			connection.createStatement().executeUpdate(
					"CREATE TABLE IF NOT EXISTS answers (id INTEGER PRIMARY "
					+ "KEY AUTO_INCREMENT, player VARCHAR(36) NOT NULL, "
					+ "test VARCHAR(512) NOT NULL, "
					+ "question TEXT NOT NULL, answer TEXT NOT NULL, correct "
					+ "BOOLEAN NOT NULL, date TIMESTAMP DEFAULT "
					+ "CURRENT_TIMESTAMP);");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
