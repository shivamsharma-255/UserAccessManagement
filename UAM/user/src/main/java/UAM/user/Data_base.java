package UAM.user;

import java.sql.Connection;
import java.sql.DriverManager;

public class Data_base {
	public static Connection connect() throws Exception {
		String driver="com.mysql.cj.jdbc.Driver",url="jdbc:mysql://localhost:3306/UAM",username="root",password="root";
		Class.forName(driver);
		Connection c=DriverManager.getConnection(url, username, password);
		return c;
	}
}