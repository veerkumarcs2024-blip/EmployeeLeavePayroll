<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Login - Employee Leave & Payroll</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: linear-gradient(135deg, #1e3a8a, #2563eb);
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
        }

        .login-box {
            width: 400px;
            background: white;
            padding: 40px;
            border-radius: 18px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.25);
        }

        h1 {
            text-align: center;
            margin-bottom: 10px;
            color: #1e3a8a;
        }

        .subtitle {
            text-align: center;
            color: #666;
            margin-bottom: 30px;
        }

        label {
            display: block;
            margin-bottom: 8px;
            font-weight: bold;
        }

        input {
            width: 100%;
            padding: 13px;
            margin-bottom: 20px;
            border: 1px solid #ccc;
            border-radius: 8px;
            font-size: 15px;
        }

        input:focus {
            outline: none;
            border-color: #2563eb;
        }

        button {
            width: 100%;
            padding: 14px;
            border: none;
            border-radius: 8px;
            background: #2563eb;
            color: white;
            font-size: 16px;
            font-weight: bold;
            cursor: pointer;
        }

        button:hover {
            background: #1d4ed8;
        }

        .error {
            background: #fee2e2;
            color: #b91c1c;
            padding: 12px;
            border-radius: 8px;
            margin-bottom: 20px;
            text-align: center;
        }

        .footer {
            text-align: center;
            margin-top: 25px;
            color: #777;
            font-size: 13px;
        }
    </style>
</head>

<body>

<div class="login-box">

    <h1>Employee Portal</h1>

    <div class="subtitle">
        Leave & Payroll Management System
    </div>

    <% 
        String error = request.getParameter("error");

        if (error != null && !error.isEmpty()) {
    %>

        <div class="error">
            <%= error %>
        </div>

    <% } %>

    <form action="LoginServlet" method="post">

        <label for="email">Email</label>

        <input
            type="email"
            id="email"
            name="email"
            placeholder="Enter your email"
            required
        >

        <label for="password">Password</label>

        <input
            type="password"
            id="password"
            name="password"
            placeholder="Enter your password"
            required
        >

        <button type="submit">
            Login
        </button>

    </form>

    <div class="footer">
        Employee Leave & Payroll Management System
    </div>

</div>

</body>
</html>