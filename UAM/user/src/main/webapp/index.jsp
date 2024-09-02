<!DOCTYPE html>
<html lang="en" dir="ltr">
<head>
    <meta charset="UTF-8">
    <title>Login Form</title>
    <link href="style.css" rel="stylesheet">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
     <!-- Prevent caching of this page -->
    <meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate">
    <meta http-equiv="Pragma" content="no-cache">
    <meta http-equiv="Expires" content="0">
    <script>
    // Function to prevent back navigation
    function preventBackNavigation() {
        // Intercept page load
        window.history.pushState(null, null, location.href);
        window.onpopstate = function() {
            window.history.go(1);
        };
        
        // Reload the page if it is restored from cache
        window.addEventListener('pageshow', function(event) {
            if (event.persisted) {
                window.location.reload();
            }
        });
    }

    // Apply back navigation prevention on page load
    window.addEventListener('load', function() {
        preventBackNavigation();
        // Check if there's an error parameter in the URL
        const urlParams = new URLSearchParams(window.location.search);
        if (urlParams.has('error')) {
            const error = urlParams.get('error');
            if (error === 'invalid_credentials') {
                // Show the error message
                document.getElementById('error-message').style.display = 'block';
                
                setTimeout(function() {
                    errorMessage.style.display = 'none';
                    window.location.reload();
                }, 2000); // 2000 milliseconds = 2 seconds
                
            }
        }
    });
</script>
<style>
    /* Style for the error message */
    #error-message {
        display: none;
        color: red;
        font-weight: bold;
        margin-bottom: 10px;
    }
</style>
</head>
<body>
<div class="container">
<div class="title">Login</div>
<div class="content">
  <form action="webapi/myresource/login" method="post">
    <div class="user-details">
      <div class="input-box">
        <span class="details">Username</span>
        <input type="text" placeholder="Enter your username" name="uname" required>
      </div>
      <div class="input-box">
        <span class="details">Password</span>
        <input type="password" placeholder="Enter your password" name="pswd" required>
      </div>
    </div>
    <div id="error-message">Invalid credentials. Please try again.</div>
    <div class="button">
      <input type="submit" value="Login">
    </div>
  </form>
  <div class="signup-link">
    <p><a href="forgetpassword.html">Forgot Password</a> | <a href="signup.html">Sign up here</a></p>
  </div>
</div>
</div>
</body>
</html>