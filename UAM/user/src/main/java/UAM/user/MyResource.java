package UAM.user;


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import UAM.user.User.RoleChangeRequest;


/**
 * Root resource (exposed at "myresource" path)
 */
@Path("myresource")
public class MyResource {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "Got it!";
    }
    @GET
    @Path("db")
    public String implement_db() throws Exception {
    	Connection c=Data_base.connect();
    	if(c!=null)return "connected";
    	return "not connected";
    	
    }
    
    //it is connected with the signup page/regitsration page 
    @POST
    @Path("registration")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String implement_registration(@FormParam("firstname") String first_name,
    		@FormParam("lastname") String last_name,
    		@FormParam("email") String email,
    		@FormParam("phno") String phoneno,
    		@FormParam("pwd") String pwd,
    		@FormParam("cpwd") String cpwd
    		) throws Exception{
    	User us=new User(first_name,last_name,email,phoneno,pwd,cpwd);
    	if(us.pwd_match())return "passwords did not match"+"<br><a href='http://localhost:8542/user/signup.html'>Register again</a>";
    	return "your username is :   "+us.createUser()+"<br><br><br><form action='http://localhost:8542/user/' method='get'><button style='background-color:#4a90e2;color:white;text-align:center;padding:15px;border-radius:10px' type='submit'>Proceed to Login</button></form>";
    }
    
    @POST
    @Path("adduser")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String implement_addUser(@FormParam("firstname") String first_name,
    		@FormParam("lastname") String last_name,
    		@FormParam("email") String email,
    		@FormParam("phno") String phoneno,
    		@FormParam("pwd") String pwd,
    		@FormParam("cpwd") String cpwd
    		) throws Exception{
    	User us=new User(first_name,last_name,email,phoneno,pwd,cpwd);
    	if(us.pwd_match())return "passwords did not match"+"<br><a href='http://localhost:8542/user/signup.html'>Register again</a>";
    	return "New User's username is :   "+us.createUser()+"<br><br><br><form action='http://localhost:8542/user/admindashboard.html' method='get'><button style='background-color:#4a90e2;color:white;text-align:center;padding:15px;border-radius:10px' type='submit'>Home</button></form>";
    }
    
    
    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void implement_login(@FormParam("uname") String uname,
                                @FormParam("pswd") String pswd,
                                @Context HttpServletRequest request,
                                @Context HttpServletResponse resp) throws Exception {
        User us1 = new User(uname, pswd);
        String usertype = us1.validateuser();
        
        HttpSession session = request.getSession();
        session.setAttribute("username", uname);
        
        if (usertype.equals("Admin")) {
            resp.sendRedirect("http://localhost:8542/user/admindashboard.html");
        } else if (usertype.equals("Employee")) {
            resp.sendRedirect("http://localhost:8542/user/userdashboard.html");
        } else if (usertype.equals("Manager")) {
            resp.sendRedirect("http://localhost:8542/user/managerdashboard.html");
        } else {
        	 resp.sendRedirect("http://localhost:8542/user/index.jsp?error=invalid_credentials");
       }
    }
    
    @POST
    @Path("logout")
    public Response logout(@Context HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // Invalidate the session
        }
        return Response.ok("Logged out successfully").build();
    }

    private Response validateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                           .entity("Please log in to access this page.")
                           .build();
        }
        return null; // Session is valid
    }

    
 // Added this method to retrieve the username of the logged-in user
    @GET
    @Path("username")
    @Produces(MediaType.TEXT_PLAIN)
    public String getUsername(@Context HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String username = (String) session.getAttribute("username");
            if (username != null) {
                return username;
            }
        }
        return "Guest"; // Default value if no user is logged in
    }

    
    @GET
    @Path("users")
    @Produces(MediaType.TEXT_PLAIN)
    public void getUsers(@Context HttpServletRequest request,@Context HttpServletResponse response) throws Exception {
    	Response sessionResponse = validateSession(request);
        if (sessionResponse != null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }
    	List<String> users = User.listUsers();
        
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        
        try (PrintWriter writer = response.getWriter()) {
            for (String user : users) {
                writer.println(user);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception as needed
        }
    }

    
    @POST
    @Path("addResource")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void addResource(
            @FormParam("resourceName") String resourceName,
            @Context HttpServletRequest request,
            @Context HttpServletResponse resp
    ) throws Exception {
    	Response sessionResponse = validateSession(request);
        if (sessionResponse != null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }
        Resource resource = new Resource(resourceName);
        String result = resource.addResource();
        
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");
        try (PrintWriter writer = resp.getWriter()) {
            writer.write(result);
        } catch (IOException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
        }
    }

    @POST
    @Path("removeResource")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void removeResource(
            @FormParam("resourceName") String resourceName,
            @Context HttpServletRequest request,
            @Context HttpServletResponse resp
    ) throws Exception {
    	Response sessionResponse = validateSession(request);
        if (sessionResponse != null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }
        String result = Resource.removeResource(resourceName);
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");
        try (PrintWriter writer = resp.getWriter()) {
            writer.write(result);
        } catch (IOException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
        }
    }
    
    @GET
    @Path("resou")
    @Produces(MediaType.TEXT_PLAIN)
    public void getResources(@Context HttpServletRequest request,@Context HttpServletResponse response) throws Exception {
    	 Response sessionResponse = validateSession(request);
    	    if (sessionResponse != null) {
    	        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    	        return;
    	    }
    	    
    	List<String> resor = Resource.listResources();
        
        
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        
        try (PrintWriter writer = response.getWriter()) {
            for (String re : resor) {
                writer.println(re);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception as needed
        }
    }
    
    @GET
    @Path("resources")
    @Produces(MediaType.TEXT_HTML)
    public void getResourcess(@Context HttpServletRequest request,@Context HttpServletResponse response) throws Exception {
    	 Response sessionResponse = validateSession(request);
    	    if (sessionResponse != null) {
    	        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    	        return;
    	    }
    	
    	List<String> resources = Resource.listResources();
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        
        try (PrintWriter writer = response.getWriter()) {
            writer.println("<option value=''disabled>Select Resource</option>");
            for (String resource : resources) {
                writer.println("<option value='" + resource + "'>" + resource + "</option>");
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception as needed
        }
    } 
    
    @GET
    @Path("userResources")
    @Produces(MediaType.TEXT_HTML)
    public void getUserResources(@Context HttpServletRequest request, @Context HttpServletResponse response) throws Exception {
    	 Response sessionResponse = validateSession(request);
    	    if (sessionResponse != null) {
    	        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    	        return;
    	    }
    	
    	String username = getUsername(request);
        List<String> resources = User.getUserResources(username);

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        
        try (PrintWriter writer = response.getWriter()) {
            writer.println("<option value=''disabled>  </option>");
            for (String resource : resources) {
                writer.println("<option value='" + resource + "'>" + resource + "</option>");
            }
        } catch (IOException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
        }
    }

    @POST
    @Path("removeUserResource")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void removeUserResource(
            @FormParam("resourceName") String resourceName,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) throws Exception {
    	 Response sessionResponse = validateSession(request);
    	    if (sessionResponse != null) {
    	        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    	        return;
    	    }
        String username = getUsername(request);
        String result = User.removeUserResource(username, resourceName);
        
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write(result);
        } catch (IOException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
        }
    }

    @POST
    @Path("removeResourceFromUser")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void removeUserFromResource(
            @FormParam("username") String username,
            @FormParam("resourceName") String resourceName,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response
    ) throws Exception {
    	 Response sessionResponse = validateSession(request);
    	    if (sessionResponse != null) {
    	        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    	        return;
    	    }
        String result = User.removeUserResource(username, resourceName);
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write(result);
        } catch (IOException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
        }
    }
    
    @GET
    @Path("resourcesByUser")
    @Produces(MediaType.TEXT_PLAIN)
    public void getResourcesByUser(
            @QueryParam("username") String username,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response) throws Exception {
    	 Response sessionResponse = validateSession(request);
    	    if (sessionResponse != null) {
    	        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    	        return;
    	    }
        List<String> resources = User.getResourcesByUser(username);
        
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        
        try (PrintWriter writer = response.getWriter()) {
            for (String resource : resources) {
                writer.println(resource);
            }
        } catch (IOException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
        }
    }
    
    @GET
    @Path("usersByResource")
    @Produces(MediaType.TEXT_PLAIN)
    public void getUsersByResource(
            @QueryParam("resourceName") String resourceName,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response) throws Exception {
    	 Response sessionResponse = validateSession(request);
    	    if (sessionResponse != null) {
    	        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    	        return;
    	    }
        List<String> users = User.getUsersByResource(resourceName);
        
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        
        try (PrintWriter writer = response.getWriter()) {
            if (users.isEmpty()) {
                writer.println("No users found for the specified resource.");
            } else {
                for (String user : users) {
                    writer.println(user);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
        }
    } 

    
    @GET
    @Path("resourcesList")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getResources(@Context HttpServletRequest request) throws Exception {
    	 Response sessionResponse = validateSession(request);
    	    if (sessionResponse != null) {
    	        return sessionResponse;
    	    }
        List<String> resources = Resource.listResources();
        StringBuilder sb = new StringBuilder();
        for (String resource : resources) {
            sb.append(resource).append("\n");
        }
        return Response.ok(sb.toString()).build();
    }

    @POST
    @Path("requestNewResource")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response requestResource(
            @FormParam("resourceName") String resourceName,
            @Context HttpServletRequest request
    ) throws Exception {
    	 Response sessionResponse = validateSession(request);
    	    if (sessionResponse != null) {
    	        return sessionResponse;
    	    }
        String username = getUsernames(request);
        String result = User.requestResource(username, resourceName);
        return Response.ok(result).build();
    }

    // Retrieve username from session
    private String getUsernames(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String username = (String) session.getAttribute("username");
            if (username != null) {
                return username;
            }
        }
        return "Guest";
    }
    
    @GET
    @Path("userRequests")
    @Produces(MediaType.TEXT_HTML)
    public Response getUserRequests(@Context HttpServletRequest request) {
    	 Response sessionResponse = validateSession(request);
    	    if (sessionResponse != null) {
    	        return sessionResponse;
    	    }
        String username = getUsernames(request);
        StringBuilder htmlResponse = new StringBuilder();
        
        try {
            List<User.ResourceRequest> requests = User.getRequestsByUser(username);
            
            // Start HTML table
            htmlResponse.append("<table border='1'>")
                        .append("<thead>")
                        .append("<tr>")
                        .append("<th colspan='25'>Resource Name</th>")
                        .append("<th colspan='25'>Status</th>")
                        .append("</tr>")
                        .append("</thead>")
                        .append("<tbody>");
            
            // Add rows for each request
            for (User.ResourceRequest requestd : requests) {
                htmlResponse.append("<tr>")
                            .append("<td colspan='25'>").append(requestd.getResourceName()).append("</td>")
                            .append("<td colspan='25'>").append(requestd.getStatus()).append("</td>")
                            .append("</tr>");
            }
            
            // End HTML table
            htmlResponse.append("</tbody>")
                        .append("</table>");
            
        } catch (Exception e) {
            e.printStackTrace();
            // Error message in HTML format
            htmlResponse.append("<p>An error occurred while retrieving requests.</p>");
        }
        
        return Response.ok(htmlResponse.toString()).build();
    }
    
    @POST
    @Path("approveRequest")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response approveRequest(@FormParam("username") String username,
                                   @FormParam("resourceName") String resourceName) throws Exception {
    	String result = User.approveRequest(username, resourceName);
        return Response.ok(result).build();
    }

    @POST
    @Path("rejectRequest")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response rejectRequest(@FormParam("username") String username,
                                  @FormParam("resourceName") String resourceName) throws Exception {
        String result = User.rejectRequest(username, resourceName);
        return Response.ok(result).build();
    }
    @GET
    @Path("checkRequests")
    @Produces(MediaType.TEXT_HTML)
    public Response checkRequests(@Context HttpServletRequest request) throws Exception {
    	Response sessionResponse = validateSession(request);
        if (sessionResponse != null) {
            return sessionResponse;
        }
        List<User.ResourceRequest> requests = User.checkRequests();
        StringBuilder htmlResponse = new StringBuilder();
        
        htmlResponse.append("<table style='border-collapse: collapse; width: 100%; border: 1px solid #ddd;'>")
                    .append("<thead>")
                    .append("<tr>")
                    .append("<th style='border: 1px solid #000000; padding: 8px;'>Username</th>")
                    .append("<th style='border: 1px solid #000000; padding: 8px;'>Resource Name</th>")
                    .append("<th style='border: 1px solid #000000; padding: 8px;'>Status</th>")
                    .append("<th style='border: 1px solid #000000; padding: 8px;'>Actions</th>")
                    .append("</tr>")
                    .append("</thead>")
                    .append("<tbody>");
        
        for (User.ResourceRequest requestd : requests) {
            htmlResponse.append("<tr>")
                        .append("<td style='border: 1px solid #000000; padding: 8px;'>").append(requestd.getUsername()).append("</td>")
                        .append("<td style='border: 1px solid #000000; padding: 8px;'>").append(requestd.getResourceName()).append("</td>")
                        .append("<td style='border: 1px solid #000000; padding: 8px;'>").append(requestd.getStatus()).append("</td>")
                        .append("<td style='border: 1px solid #000000; padding: 8px;'>");
            
            // Only show the approve and reject forms if the request is pending
            if ("Pending".equals(requestd.getStatus())) {
                htmlResponse.append("<form method='post' action='webapi/myresource/approveRequest' style='display:inline;'>")
                            .append("<input type='hidden' name='username' value='").append(requestd.getUsername()).append("'/>")
                            .append("<input type='hidden' name='resourceName' value='").append(requestd.getResourceName()).append("'/>")
                            .append("<input type='submit' value='Approve'/>")
                            .append("</form>")
                            .append("<form method='post' action='webapi/myresource/rejectRequest' style='display:inline;'>")
                            .append("<input type='hidden' name='username' value='").append(requestd.getUsername()).append("'/>")
                            .append("<input type='hidden' name='resourceName' value='").append(requestd.getResourceName()).append("'/>")
                            .append("<input type='submit' value='Reject'/>")
                            .append("</form>");
            } else {
                htmlResponse.append("No actions available");
            }
            
            htmlResponse.append("</td>")
                        .append("</tr>");
        }
        
        htmlResponse.append("</tbody>")
                    .append("</table>");
        
        return Response.ok(htmlResponse.toString()).build();
    }


    @GET
    @Path("usernames")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsernames() throws Exception {
        List<String> usernames = User.listUsers();
        return Response.ok(usernames).build();
    }

    @GET
    @Path("resourcesForUser")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResourcesForUser(@QueryParam("username") String username,@Context HttpServletRequest request) throws Exception {
    	Response sessionResponse = validateSession(request);
        if (sessionResponse != null) {
            return sessionResponse;
        }
    	List<String> resources = User.getResourcesForUser(username);
        return Response.ok(resources).build();
    }

    @POST
    @Path("removeResourceFromUser")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response removeResourceFromUser(
            @FormParam("username") String username,
            @FormParam("resourceName") String resourceName,@Context HttpServletRequest request) throws Exception {
    	Response sessionResponse = validateSession(request);
        if (sessionResponse != null) {
            return sessionResponse;
        }
    	String result = User.removeResourceFromUser(username, resourceName);
        return Response.ok(result).build();
    }
    
    @POST
    @Path("request")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    public Response requestRoleChange(String requestBody,@Context HttpServletRequest request) {
    	Response sessionResponse = validateSession(request);
        if (sessionResponse != null) {
            return sessionResponse;
        }
    	try {
            // Assuming requestBody is in the format "username:newRole"
            String[] parts = requestBody.split(":");
            if (parts.length != 2) {
                return Response.status(Response.Status.BAD_REQUEST)
                               .entity("Invalid request format").build();
            }
            String username = parts[0];
            String newRole = parts[1];
            
            String result = User.requestRoleChange(username, newRole);
            return Response.ok(result).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error requesting role change").build();
        }
    }
    
    @GET
    @Path("requests")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoleChangeRequests(@Context HttpServletRequest request) {
    	Response sessionResponse = validateSession(request);
        if (sessionResponse != null) {
            return sessionResponse;
        }
    	try {
            List<User.ResourceRequest> requests = User.checkRoleChangeRequests();
            return Response.ok(requests).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error retrieving role change requests").build();
        }
    }

    @POST
    @Path("approve")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response approveRoleChangeRequest(RoleChangeRequest request, @Context HttpServletRequest servletRequest) {
    	Response sessionResponse = validateSession(servletRequest);
        if (sessionResponse != null) {
            return sessionResponse;
        }
    	try {
            String managerName = getUsername(servletRequest);
            String result = User.approveRoleChangeRequest(request.getUsername(), request.getNewRole(), managerName);
            return Response.ok(result).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error approving role change request").build();
        }
    }

    @POST
    @Path("reject")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response rejectRoleChangeRequest(RoleChangeRequest request,@Context HttpServletRequest servletRequest) {
    	Response sessionResponse = validateSession(servletRequest);
        if (sessionResponse != null) {
            return sessionResponse;
        }
        try {
            String result = User.rejectRoleChangeRequest(request.getUsername(), request.getNewRole());
            return Response.ok(result).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error rejecting role change request").build();
        }
    }
    
    @GET
    @Path("/getTeamMembers/{managerName}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> done(@PathParam("managerName")String managerName,@Context HttpServletRequest request) throws Exception {
    	ArrayList<String> getTeamMembers = User.getTeamMembers(managerName);
        return getTeamMembers;
    }
    @GET
    @Path("/addToTeam")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> addToTeam(@Context HttpServletRequest request) throws Exception {
    	ArrayList<String> addToTeam = User.addToTeam();
        return addToTeam;
    }

    @POST
    @Path("/addMember/{userName}/{managerName}")
    @Produces(MediaType.APPLICATION_FORM_URLENCODED)
    public String addResource(@PathParam("userName")String userName,@PathParam("managerName")String managerName,@Context HttpServletRequest request) throws Exception {
        return User.addMember(userName,managerName);
    }
    
    @POST
    @Path("changepassword")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response changePassword(
            @FormParam("newPassword") String newPassword,
            @Context HttpServletRequest request) {
    	 Response sessionResponse = validateSession(request);
    	    if (sessionResponse != null) {
    	        return sessionResponse;
    	    }

        
        try {
            String username = getUsername(request);
            String result = User.changePassword(username, newPassword);

            if (result.equals("Password updated successfully.")) {
                return Response.ok(result).build(); // Returning plain text
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity(result).build(); // Returning plain text
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error updating password").build(); // Returning plain text
        }
    }
    
    @POST
    @Path("forgotpassword")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response forgotPassword(
        @FormParam("username") String username,
        @FormParam("email") String email,
        @FormParam("newPassword") String newPassword
    ) {
        try {
            // Call the forgotPassword method from your existing class
            String result = User.forgotPassword(username, email, newPassword);

            // Return a success response with a plain text message
            return Response.ok(result).build();
        } catch (Exception e) {
            // Log the exception and return an error response
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("An error occurred while updating the password.")
                           .build();
        }
    }

   
    
}
