import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class MySQLManager {

	private String driver = "com.mysql.jdbc.Driver";
	private String dbURL = "jdbc:mysql://127.0.0.1:3306/db";
	private String user = "root"; 
	private String password = "1111";
    
	private Connection conn;
    
	MySQLManager()
	{
		
	}
	
	public boolean importPlaces(JSONArray places) throws SQLException, ClassNotFoundException
	{
		// Load Driver
        Class.forName(driver);

        // Connect to Database
        conn = (Connection) DriverManager.getConnection(dbURL, user, password);
        if(!conn.isClosed()) 
        	;//System.out.println("Succeeded connecting to the Database!");
        else
        {
        	//System.out.println("Failed connecting to the Database!");
        	return false;
        }
        
		// Insert Places into Database
		Statement statement = (Statement) conn.createStatement();
		for(int i=0;i<places.size();i++)
		{
			JSONObject place = (JSONObject) places.get(i);
			JSONObject position = (JSONObject) place.get("location");
			String sql = "insert into "
					   + "shop(shopName, address, shopPhone, longitude, latitude)"
					   + "VALUES('" + place.get("name") + "'"
					   		  +",'" + place.get("address") + "'"
					   		  +",'" + place.get("telephone") + "'"
					   		  +",'" + position.get("lng") + "'"
					   		  +",'" + position.get("lat") + "')";
			statement.executeUpdate(sql);
		}
		
        return true;
	}
	
	public int getIdByPlaceName(String name) throws SQLException
	{
		conn = (Connection) DriverManager.getConnection(dbURL, user, password);
        if(!conn.isClosed()) 
        	;//System.out.println("Succeeded connecting to the Database!");
        else
        	;//System.out.println("Failed connecting to the Database!");
        
		Statement statement = (Statement) conn.createStatement();
		String sql = "select shopId from shop where shopName='"+name+"';";
		ResultSet rs = statement.executeQuery(sql);
		
		if(!rs.next())
			return -1;
		
		// Debug
		//System.out.println("Name: "+name+" Id: "+rs.getInt("shopId"));
		
		return rs.getInt("shopId");
	}
}
