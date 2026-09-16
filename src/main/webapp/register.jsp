<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Create Account</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f0fdf4;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
        }

        .box {
            background: white;
            width: 400px;
            padding: 30px;
            border-radius: 12px;
            box-shadow: 0 5px 20px rgba(0,0,0,0.15);
        }

        h2 {
            text-align: center;
            color: #159447;
        }

        label {
            display: block;
            margin-top: 15px;
        }

        input {
            width: 100%;
            padding: 11px;
            margin-top: 6px;
            box-sizing: border-box;
        }

        button {
            width: 100%;
            padding: 12px;
            margin-top: 22px;
            background: #159447;
            color: white;
            border: none;
            cursor: pointer;
            font-size: 16px;
        }

        .login {
            text-align: center;
            margin-top: 15px;
        }
    </style>
</head>

<body>

<div class="box">

    <h2>Create Account</h2>

    <form action="RegisterServlet" method="post">

        <label>Full Name</label>
        <input type="text"
               name="fullName"
               placeholder="Enter your full name"
               required>

        <label>Email</label>
        <input type="email"
               name="email"
               placeholder="Enter your email"
               required>

        <label>Password</label>
        <input type="password"
               name="password"
               placeholder="Create password"
               required>

        <label>Confirm Password</label>
        <input type="password"
               name="confirmPassword"
               placeholder="Confirm password"
               required>

        <button type="submit">Create Account</button>

    </form>

    <div class="login">
        Already have an account?
        <a href="login.jsp">Login</a>
    </div>

</div>

</body>
</html>