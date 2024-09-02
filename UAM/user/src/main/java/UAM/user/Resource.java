package UAM.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Resource {
	private String res_name;
	
	public Resource(String re_name){
		this.res_name=re_name;
		
	}
	
	public String getRes_name() {
		return res_name;
	}



	public void setRes_name(String res_name) {
		this.res_name = res_name;
	}



	
	

	//add resource to the table
	public String addResource() throws Exception {
        String message;
        try (Connection con = Data_base.connect()) {
            String checkQuery = "SELECT COUNT(*) FROM resources WHERE resource_name = ?";
            try (PreparedStatement checkPst = con.prepareStatement(checkQuery)) {
                checkPst.setString(1, res_name);
                ResultSet rs = checkPst.executeQuery();
                rs.next();
                int count = rs.getInt(1);
                if (count > 0) {
                    message = "Resource already exists.";
                } else {
                    String insertQuery = "INSERT INTO resources (resource_name) VALUES (?)";
                    try (PreparedStatement insertPst = con.prepareStatement(insertQuery)) {
                        insertPst.setString(1, res_name);
                        int rowsAffected = insertPst.executeUpdate();
                        if (rowsAffected > 0) {
                            message = "Resource added successfully.";
                        } else {
                            message = "Failed to add resource.";
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            message = "Database error.";
        }
        return message;
    }
	
	//delete the resource from the table resource
    public static String removeResource(String resourceName) throws Exception {
        if (resourceName == null || resourceName.trim().isEmpty()) {
            return "Resource name cannot be null or empty.";
        }
        resourceName = resourceName.trim();
        String message;
        try (Connection con = Data_base.connect()) {
            // Prepare SQL queries
            String deleteQuery = "DELETE FROM resources WHERE resource_name = ?";
            String deleteQuery1 = "DELETE FROM user_resource WHERE resource_name = ?";
            String deleteQuery2 = "DELETE FROM resource_requests WHERE resource_name = ?";
            
            // Perform the resource deletion
            try (PreparedStatement deletePst = con.prepareStatement(deleteQuery)) {
                deletePst.setString(1, resourceName);
                int rowsAffected = deletePst.executeUpdate();
                
                // If the resource was deleted, remove related entries
                if (rowsAffected > 0) {
                    try (PreparedStatement deletePst1 = con.prepareStatement(deleteQuery1);
                         PreparedStatement deletePst2 = con.prepareStatement(deleteQuery2)) {
                         
                        deletePst1.setString(1, resourceName);
                        deletePst1.executeUpdate();
                        
                        deletePst2.setString(1, resourceName);
                        deletePst2.executeUpdate();
                        
                        message = "Resource removed successfully.";
                    } catch (SQLException e) {
                        // Handle exceptions during related entry deletions
                        e.printStackTrace();
                        message = "Partial success: Resource removed, but there was an error cleaning up related entries.";
                    }
                } else {
                    message = "Resource not found.";
                }
            } catch (SQLException e) {
                // Handle exceptions during resource deletion
                e.printStackTrace();
                message = "Error deleting resource from the database.";
            }
        } catch (SQLException e) {
            // Handle exceptions during database connection
            e.printStackTrace();
            message = "Error connecting to the database.";
        }

        return message;
    }
    
    //to list all the resource from the resource table
    public static List<String> listResources() throws Exception {
        List<String> reso = new ArrayList<>();
        Connection c = Data_base.connect();
        String query = "SELECT resource_name FROM resources";
        PreparedStatement ps = c.prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            reso.add(rs.getString("resource_name"));
        }
        return reso;
    }
}
