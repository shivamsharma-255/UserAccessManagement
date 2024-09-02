package UAM.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;



public class User {
	public String first_name;
	public String last_name;
	public String email;
	public String phoneno;
	public String pwd;
	public String cpwd;
	private String paswd;
	public String uname;
	public String resourceName;
	
	User(String first_name,String last_name,String email,String phoneno,String pwd,String cpwd){
		this.first_name=first_name;
		this.last_name=last_name;
		this.email=email;
		this.phoneno=phoneno;
		this.pwd=pwd;
		this.cpwd=cpwd;
	}
	User(String uname,String pswd){
		this.uname=uname;
		this.pwd=pswd;
	}
	User(String resourceName){
		this.resourceName=resourceName;
		
	}
	User(String first_name,String last_name,String email){
		this.first_name=first_name;
		this.last_name=last_name;
		this.email=email;
	}
	User(){
		
	}
	// Define a new class for resource request representation
			public static class ResourceRequest {
				 private String username;
			    private String resourceName;
			    private String status;

			    public ResourceRequest(String resourceName, String status) {
			        this.resourceName = resourceName;
			        this.status = status;
			    }

			    public String getResourceName() {
			        return resourceName;
			    }

			    public String getStatus() {
			        return status;
			    }
			   

			    public ResourceRequest(String username, String resourceName, String status) {
			        this.username = username;
			        this.resourceName=resourceName;
			        this.status=status;
			    }

			    public String getUsername() {
			        return username;
			    }

			}
			public static class RoleChangeRequest {
		        private String username;
		        private String newRole;

		        // Getters and Setters
		        public String getUsername() { return username; }
		        public void setUsername(String username) { this.username = username; }
		        public String getNewRole() { return newRole; }
		        public void setNewRole(String newRole) { this.newRole = newRole; }
		    }
			public static void logout(HttpServletRequest request) {
		        HttpSession session = request.getSession(false);
		        if (session != null) {
		            session.invalidate();
		        }
		    }
	//to validate user based on their usertype
	public String validateuser() throws Exception{
		Connection c=Data_base.connect();
		String cquery="select userrole from user where username=? and passwrd=?";
		PreparedStatement ps=c.prepareStatement(cquery);
		ps.setString(1, uname);
		ps.setString(2, encrypt_pwd());
		ResultSet rs=ps.executeQuery();
		String utype="";
		if(rs.next()) {
			utype=rs.getString(1);
		}
		return utype;
	}
	public boolean pwd_match() {
		if(pwd.equals(cpwd))return false;
		return true;
	}
	//it will take the firstname and lastname and then concatenate them by using "." in between to create username
	public String check_username() throws Exception {
		Connection c=Data_base.connect();
		String uname=first_name+"."+last_name;
		String cntquery="select count(*) from user where username like ?";
		PreparedStatement pc=c.prepareStatement(cntquery);
		pc.setString(1,uname+"%");
		ResultSet rs=pc.executeQuery();
		int cnt=0;
		if(rs.next()) {
			cnt=rs.getInt(1);
		}
		if(cnt!=0)uname=uname+cnt;
		return uname;
	}
	//whoever logins first that person should be give userrole  as admin
	public String define_role() throws Exception{
		Connection c=Data_base.connect();
		String userrole="Employee";
		String query="select count(*) from user";
		PreparedStatement ps=c.prepareStatement(query);
		ResultSet rs=ps.executeQuery();
		if(rs.next()) {
			if(rs.getInt(1)==0)userrole="Admin";
		}
		return userrole;
	}
	//this funcion is too encrypt the given string based on the below pattern present in the string
	private String encrypt_pwd() {
		String check="ABCDEFGHI,JKLMNOPQR,STUVWXYZa,bcdefghij,klmnopqrs,tuvwxyz01,23456789`,~!@#$%^&*,()-_=+[{],}|;:',<.>,/?";
		String[] arr=check.split(",");
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<pwd.length();i++) {
			char ch=pwd.charAt(i);
			for(int j=0;j<arr.length;j++) {
				if(arr[j].indexOf(ch)!=-1) {
					sb.append(j);
					sb.append(arr[j].indexOf(ch));
				}
			}
		}
		return sb.toString();
		
	}
	//it can be used to get the crrentdatetime ,whenever the user is created
	public static String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        String formattedDateTime = now.format(formatter);
        return formattedDateTime;
    }
	
	//it can be used to create the user
	public String createUser() throws Exception{
		Connection c=Data_base.connect();
		String uname=check_username();
		String userrole=define_role();
		paswd=encrypt_pwd();
		String date_of_joining=getCurrentDateTime();
    	String query="insert into user (first_name,last_name,email,phno,passwrd,username,date_of_joining,userrole)values(?,?,?,?,?,?,?,?)";
    	PreparedStatement ps=c.prepareStatement(query);
    	ps.setString(1, first_name);
    	ps.setString(2, last_name);
    	ps.setString(3, email);
    	ps.setString(4, phoneno);
    	ps.setString(5, paswd);
    	ps.setString(6, uname);
    	ps.setString(7, date_of_joining);
    	ps.setString(8, userrole);
    	ps.executeUpdate();
    	return uname;
	} 
	private static String encrypt_pswd(String pwd) {
        String check = "ABCDEFGHI,JKLMNOPQR,STUVWXYZa,bcdefghij,klmnopqrs,tuvwxyz01,23456789`,~!@#$%^&*,()-_=+[{],}|;:',<.>,/?";
        String[] arr = check.split(",");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pwd.length(); i++) {
            char ch = pwd.charAt(i);
            for (int j = 0; j < arr.length; j++) {
                if (arr[j].indexOf(ch) != -1) {
                    sb.append(j);
                    sb.append(arr[j].indexOf(ch));
                }
            }
        }
        return sb.toString();
    }
	//below function will change the currentpassword and then encrypt it and then store it in the db
	public static String changePassword(String username, String newPassword) throws Exception {
        Connection c = Data_base.connect();
        String encryptedNewPassword = encrypt_pswd(newPassword);
        String queryUpdatePassword = "UPDATE user SET passwrd = ? WHERE username = ?";
        PreparedStatement psUpdatePassword = c.prepareStatement(queryUpdatePassword);
        psUpdatePassword.setString(1, encryptedNewPassword);
        psUpdatePassword.setString(2, username);
        int affectedRows = psUpdatePassword.executeUpdate();

        if (affectedRows > 0) {
            return "Password updated successfully.";
        } else {
            return "Failed to update password.";
        }
    }

	//it is used to print all the users present in the DB
	public static List<String> listUsers() throws Exception {
        List<String> users = new ArrayList<>();
        Connection c = Data_base.connect();
        String query = "SELECT username FROM user where userrole!='Admin'";
        PreparedStatement ps = c.prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            users.add(rs.getString("username"));
        }
        return users;
    }

	//below function can be used to check the resource of the user
	public static List<String> getUserResources(String username) throws Exception {
        List<String> resources = new ArrayList<>();
        Connection c = Data_base.connect();
        String query = "SELECT resource_name FROM user_resource WHERE username = ?";
        PreparedStatement ps = c.prepareStatement(query);
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            resources.add(rs.getString("resource_name"));
        }
        return resources;
    }
	
	
	//it will delete the user resources which are selected
	public static String removeUserResource(String username, String resourceName) throws Exception {
	    Connection c = Data_base.connect();
	    String query = "DELETE FROM user_resource WHERE username = ? AND resource_name = ?";
	    PreparedStatement ps = c.prepareStatement(query);
	    ps.setString(1, username);
	    ps.setString(2, resourceName);
	    int affectedRows = ps.executeUpdate();
	    
	    if (affectedRows > 0) {
	        return "Resource removed successfully.";
	    } else {
	        return "Resource not found or could not be removed.";
	    }
	}
	
	//we can get the resources by username 
	public static List<String> getResourcesByUser(String username) throws Exception {
	    List<String> resources = new ArrayList<>();
	    Connection c = Data_base.connect();
	    String query = "SELECT resource_name FROM user_resource WHERE username = ?";
	    PreparedStatement ps = c.prepareStatement(query);
	    ps.setString(1, username);
	    ResultSet rs = ps.executeQuery();
	    while (rs.next()) {
	        resources.add(rs.getString("resource_name"));
	    }
	    return resources;
	}
	
	//we can get the users by the resourcename
	public static List<String> getUsersByResource(String resourceName) throws Exception {
        List<String> users = new ArrayList<>();
        Connection c = Data_base.connect();  // Assume Data_base.connect() establishes a database connection
        String query = "SELECT username FROM user_resource WHERE resource_name = ?";
        PreparedStatement ps = c.prepareStatement(query);
        ps.setString(1, resourceName);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            users.add(rs.getString("username"));
        }
        rs.close();  // Close ResultSet
        ps.close();  // Close PreparedStatement
        c.close();   // Close Connection
        return users;
    }

	
	
	//here we can request for the new resources
	public static String requestResource(String username, String resourceName) throws Exception {
	    Connection c = Data_base.connect();
	    try {
	        // Check if there is already a pending or approved request for the same resource by the same user
	        String checkQuery = "SELECT COUNT(*) FROM resource_requests WHERE username = ? AND resource_name = ? AND (status = 'Pending' OR status = 'Approved')";
	        PreparedStatement checkStmt = c.prepareStatement(checkQuery);
	        checkStmt.setString(1, username);
	        checkStmt.setString(2, resourceName);
	        ResultSet rs = checkStmt.executeQuery();

	        if (rs.next() && rs.getInt(1) > 0) {
	            // There is already a pending or approved request for this resource by this user
	            return "You have already requested this resource, and it is either still pending or approved.";
	        }

	        // If no pending or approved request exists, insert a new request
	        String insertQuery = "INSERT INTO resource_requests (username, resource_name, status) VALUES (?, ?, ?)";
	        PreparedStatement insertStmt = c.prepareStatement(insertQuery);
	        insertStmt.setString(1, username);
	        insertStmt.setString(2, resourceName);
	        insertStmt.setString(3, "Pending"); // Initial status
	        int affectedRows = insertStmt.executeUpdate();

	        if (affectedRows > 0) {
	            return "Resource request submitted successfully.";
	        } else {
	            return "Failed to submit resource request.";
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        throw new Exception("An error occurred while processing the request.", e);
	    } finally {
	        if (c != null) {
	            c.close();
	        }
	    }
	}

	 
	 //request to  admin , it will be used to display the requests for resources on the admin page
	 public static List<ResourceRequest> getRequestsByUser(String username) throws Exception {
		    List<ResourceRequest> requests = new ArrayList<>();
		    Connection c = Data_base.connect();
		    String query = "SELECT resource_name, status FROM resource_requests WHERE username = ?";
		    PreparedStatement ps = c.prepareStatement(query);
		    ps.setString(1, username);
		    ResultSet rs = ps.executeQuery();
		    while (rs.next()) {
		        String resourceName = rs.getString("resource_name");
		        String status = rs.getString("status");
		        requests.add(new ResourceRequest(resourceName, status));
		    }
		    return requests;
		}
	 
	 
	 //the admin can check the requests made by the user for the resources by using below functionality
	 public static List<ResourceRequest> checkRequests() throws Exception {
		    List<ResourceRequest> requests = new ArrayList<>();
		    Connection c = Data_base.connect();
		    String query = "SELECT username, resource_name, status FROM resource_requests";
		    PreparedStatement ps = c.prepareStatement(query);
		    ResultSet rs = ps.executeQuery();
		    while (rs.next()) {
		        String username = rs.getString("username");
		        String resourceName = rs.getString("resource_name");
		        String status = rs.getString("status");
		        requests.add(new ResourceRequest(username, resourceName, status));
		    }
		    return requests;
		}
	 //this below function can be used to approve the request for the resource
	 public static String approveRequest(String username, String resourceName) throws Exception {
		    Connection c = Data_base.connect();
		    
		    // Update the request status to approved
		    String updateRequestQuery = "UPDATE resource_requests SET status = ? WHERE username = ? AND resource_name = ?";
		    PreparedStatement psUpdateRequest = c.prepareStatement(updateRequestQuery);
		    psUpdateRequest.setString(1, "Approved");
		    psUpdateRequest.setString(2, username);
		    psUpdateRequest.setString(3, resourceName);
		    psUpdateRequest.executeUpdate();
		    
		    // Add the resource to the user_resources
		    String insertResourceQuery = "INSERT INTO user_resource (username, resource_name) VALUES (?, ?)";
		    PreparedStatement psInsertResource = c.prepareStatement(insertResourceQuery);
		    psInsertResource.setString(1, username);
		    psInsertResource.setString(2, resourceName);
		    psInsertResource.executeUpdate();
		    
		    return "Request approved and resource added.";
		}
	 
	//this below function can be used to reject the request for the resource
		public static String rejectRequest(String username, String resourceName) throws Exception {
		    Connection c = Data_base.connect();
		    
		    // Update the request status to rejected
		    String updateRequestQuery = "UPDATE resource_requests SET status = ? WHERE username = ? AND resource_name = ?";
		    PreparedStatement psUpdateRequest = c.prepareStatement(updateRequestQuery);
		    psUpdateRequest.setString(1, "Rejected");
		    psUpdateRequest.setString(2, username);
		    psUpdateRequest.setString(3, resourceName);
		    psUpdateRequest.executeUpdate();
		    
		    return "Request rejected.";
		}
       
		//it will display all the resource related to the particular user
		public static List<String> getResourcesForUser(String username) throws Exception {
		    List<String> resources = new ArrayList<>();
		    Connection c = Data_base.connect();
		    String query = "SELECT resource_name FROM user_resource WHERE username = ?";
		    PreparedStatement ps = c.prepareStatement(query);
		    ps.setString(1, username);
		    ResultSet rs = ps.executeQuery();
		    while (rs.next()) {
		        resources.add(rs.getString("resource_name"));
		    }
		    return resources;
		}
        
		//it can be used to remove resource from the particular resource
		public static String removeResourceFromUser(String username, String resourceName) throws Exception {
		    Connection c = Data_base.connect();
		    String query = "DELETE FROM user_resource WHERE username = ? AND resource_name = ?";
		    PreparedStatement ps = c.prepareStatement(query);
		    ps.setString(1, username);
		    ps.setString(2, resourceName);
		    int affectedRows = ps.executeUpdate();
		    
		    if (affectedRows > 0) {
		        return "Resource removed successfully.";
		    } else {
		        return "Resource not found or could not be removed.";
		    }
		}
         
		
		//the below method can be to pose the request for the rolechange
		public static String requestRoleChange(String username, String newRole) throws Exception {
		    Connection c = Data_base.connect();
		    try {
		        // Check if there is already a pending or approved request for the same role change by the same user
		        String checkQuery = "SELECT COUNT(*) FROM userType_requests WHERE username = ? AND userrole = ? AND (status = 'Pending' OR status = 'Approved')";
		        PreparedStatement checkStmt = c.prepareStatement(checkQuery);
		        checkStmt.setString(1, username);
		        checkStmt.setString(2, newRole);
		        ResultSet rs = checkStmt.executeQuery();

		        if (rs.next() && rs.getInt(1) > 0) {
		            // There is already a pending or approved request for this role change by this user
		            return "You have already requested this role change, and it is either still pending or approved.";
		        }

		        // If no pending or approved request exists, insert a new request
		        String insertQuery = "INSERT INTO userType_requests (username, userrole, status) VALUES (?, ?, ?)";
		        PreparedStatement insertStmt = c.prepareStatement(insertQuery);
		        insertStmt.setString(1, username);
		        insertStmt.setString(2, newRole);
		        insertStmt.setString(3, "Pending"); // Initial status
		        int affectedRows = insertStmt.executeUpdate();

		        if (affectedRows > 0) {
		            return "Role change request submitted successfully.";
		        } else {
		            return "Failed to submit role change request.";
		        }
		    } catch (SQLException e) {
		        e.printStackTrace();
		        throw new Exception("An error occurred while processing the role change request.", e);
		    } finally {
		        if (c != null) {
		            c.close();
		        }
		    }
		}

		
		//the below method can be used to check what all the request admin got for the rolechange
		public static List<ResourceRequest> checkRoleChangeRequests() throws Exception {
		    List<ResourceRequest> requests = new ArrayList<>();
		    Connection c = Data_base.connect();
		    String query = "SELECT username, userrole, status FROM userType_requests";
		    PreparedStatement ps = c.prepareStatement(query);	
		    ResultSet rs = ps.executeQuery();
		    while (rs.next()) {
		        String username = rs.getString("username");
		        String userrole = rs.getString("userrole");
		        String status = rs.getString("status");
		        requests.add(new ResourceRequest(username, userrole, status));
		    }
		    return requests;
		}
		
		//the below function will handle the approval of the rolechange requests
		public static String approveRoleChangeRequest(String username, String newRole,String managerName) throws Exception {
		    Connection c = Data_base.connect();

		    // Update the request status to approved
		    String updateRequestQuery = "UPDATE userType_requests SET status = ? WHERE username = ? AND userrole = ?";
		    PreparedStatement psUpdateRequest = c.prepareStatement(updateRequestQuery);
		    psUpdateRequest.setString(1, "Approved");
		    psUpdateRequest.setString(2, username);
		    psUpdateRequest.setString(3, newRole);
		    psUpdateRequest.executeUpdate();

		    // Update the user role in the user table
		    String updateUserRoleQuery = "UPDATE user SET userrole = ? WHERE username = ?";
		    PreparedStatement psUpdateUserRole = c.prepareStatement(updateUserRoleQuery);
		    psUpdateUserRole.setString(1, newRole);
		    psUpdateUserRole.setString(2, username);
		    psUpdateUserRole.executeUpdate();
		    
		    if ("Manager".equals(newRole)) {
	            String updateManagerNameQuery = "UPDATE user SET manager_name = ? WHERE username = ?";
	            PreparedStatement psUpdateManagerName = c.prepareStatement(updateManagerNameQuery);
	            psUpdateManagerName.setString(1, managerName);
	            psUpdateManagerName.setString(2, username);
	            psUpdateManagerName.executeUpdate();
	        }

		    return "Role change request approved and user role updated.";
		}
       
		//the below function will handle the reject of the rolechange requests
		public static String rejectRoleChangeRequest(String username, String newRole) throws Exception {
		    Connection c = Data_base.connect();

		    // Update the request status to rejected
		    String query = "UPDATE userType_requests SET status = ? WHERE username = ? AND userrole = ?";
		    PreparedStatement ps = c.prepareStatement(query);
		    ps.setString(1, "Rejected");
		    ps.setString(2, username);
		    ps.setString(3, newRole);
		    ps.executeUpdate();

		    return "Role change request rejected.";
		}
        
		//the below functionality can be used to get all the team members present for that particular manager
		public static ArrayList<String> getTeamMembers(String managerName) {
	        ArrayList<String> resourcesList = new ArrayList<>();
	        try {
	            Connection c = Data_base.connect();
	            String query = "SELECT username FROM user where manager_name = ?";
	            PreparedStatement ps = c.prepareStatement(query);
	            ps.setString(1,managerName);
	            ResultSet rs = ps.executeQuery(); 
	            while (rs.next()) {
	            	 String username = rs.getString(1);
	            	 System.out.println(username);
	            	resourcesList.add(username);
	            }
	            rs.close();
	            ps.close();
	            c.close();
	        }
	        catch (Exception ex) {	
	        	 ex.printStackTrace();
	        }
 
	        return resourcesList;
	    }
		
		//this below function will display all the employees who are not present in the any managers team
	 public static ArrayList<String> addToTeam() {
	        ArrayList<String> members = new ArrayList<>();
	        try {
	            Connection c = Data_base.connect();
	            String query = "SELECT username FROM user WHERE manager_name IS NULL and userrole = 'Employee'";
	            Statement st = c.createStatement(); 
	            ResultSet rs = st.executeQuery(query);   
	            while (rs.next()) {
	                String username = rs.getString(1);  
	                members.add(username); 
	            }	            
	            rs.close();
	            st.close();
	            c.close();
	        }
	        catch (Exception ex) {	
	        	 ex.printStackTrace();
	        }
 
	        return members;
	    }
	 
	 //whenever manager wants to add the employee in to their team this operation will be performed
	 public static String addMember(String username, String managerName) throws Exception {
		    Connection c =  Data_base.connect();
		    String query = "UPDATE user SET manager_name = ? WHERE username = ?";
		    PreparedStatement ps = c.prepareStatement(query);
		    ps.setString(1, managerName);
		    ps.setString(2, username);
		    int affectedRows = ps.executeUpdate();
		    if (affectedRows > 0) {
		        return "Manager assigned successfully.";
		    } else {
		        return "Failed to assign manager.";
		    }
		}
	 
	 //the users who have forgotten their password can use forgot password to regain their access
	 public static String forgotPassword(String username, String email, String newPassword) throws Exception {
		    Connection c = Data_base.connect();
		    String encryptedNewPassword = encrypt_pswd(newPassword);
		    String queryUpdatePassword = "UPDATE user SET passwrd = ? WHERE username = ? AND email = ?";
		    PreparedStatement psUpdatePassword = c.prepareStatement(queryUpdatePassword);
		    psUpdatePassword.setString(1, encryptedNewPassword);
		    psUpdatePassword.setString(2, username);
		    psUpdatePassword.setString(3, email);
		    int affectedRows = psUpdatePassword.executeUpdate();
		    if (affectedRows > 0) {
		        return "Password updated successfully.";
		    } else {
		        return "Failed to update password. Please check your username and email.";
		    }
		}   



}