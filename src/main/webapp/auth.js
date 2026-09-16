/* Shared authentication and role routing for the PayFlow HR demo. */
(function () {
    const USER_KEY = "payflowUsers";
    const CURRENT_USER_KEY = "currentPayFlowUser";

    const routes = {
        admin: "admin.html",
        system: "system.html",
        hr: "payroll_hr.html",
        payroll: "payroll_hr.html",
        manager: "manager.html",
        teamleader: "Team_leader.html",
        employee: "Employee2.html",
        intern: "Intern2.html"
    };

    const demoUsers = {
        "systemadmin@gmail.com": { name: "System Admin", password: "Admin@123", role: "admin" },
        "veer@gmail.com": { name: "Veer", password: "Reel@147", role: "system" },
        "neha@gmail.com": { name: "Neha Kapoor", password: "Hr@12345", role: "hr" },
        "amit@gmail.com": { name: "Amit Kumar", password: "Manager@123", role: "manager" },
        "aarav@gmail.com": { name: "Aarav Sharma", password: "Leader@123", role: "teamleader" },
        "rahul@gmail.com": { name: "Rahul Sharma", password: "Employee@123", role: "employee" },
        "priya@gmail.com": { name: "Priya Verma", password: "Intern@123", role: "intern" }
    };

    function getUsers() {
        try {
            return JSON.parse(localStorage.getItem(USER_KEY) || "{}");
        } catch {
            return {};
        }
    }

    function saveUsers(users) {
        localStorage.setItem(USER_KEY, JSON.stringify(users));
    }

    function ensureDemoUsers() {
        const users = getUsers();
        let changed = false;

        Object.entries(demoUsers).forEach(([email, user]) => {
            if (!users[email]) {
                users[email] = { ...user, createdAt: new Date().toISOString() };
                changed = true;
            }
        });

        if (changed) {
            saveUsers(users);
        }
    }

    function getCurrentUser() {
        try {
            return JSON.parse(localStorage.getItem(CURRENT_USER_KEY) || "null");
        } catch {
            return null;
        }
    }

    function setCurrentUser(user) {
        const currentUser = {
            email: user.email,
            name: user.name,
            role: user.role
        };

        localStorage.setItem(CURRENT_USER_KEY, JSON.stringify(currentUser));
        return currentUser;
    }

    function redirectForRole(user) {
        window.location.href = routes[user.role] || "HOME.html";
    }

    function login(email, password) {
        ensureDemoUsers();
        const normalizedEmail = email.trim().toLowerCase();

        if (!normalizedEmail.endsWith("@gmail.com")) {
            return null;
        }

        const user = getUsers()[normalizedEmail];

        if (!user || user.password !== password) {
            return null;
        }

        return setCurrentUser({ ...user, email: normalizedEmail });
    }

    function logout() {
        localStorage.removeItem(CURRENT_USER_KEY);
        sessionStorage.removeItem("payflowLoggedInEmployee");
        sessionStorage.removeItem("payflowLoggedInIntern");
        window.location.href = "HOME.html";
    }

    function requireRole(allowedRoles) {
        const user = getCurrentUser();

        if (!user || !allowedRoles.includes(user.role)) {
            window.location.replace(user ? (routes[user.role] || "HOME.html") : "HOME.html");
            return null;
        }

        return user;
    }

    function updateInterfaceProfile() {
        const user = getCurrentUser();

        if (!user) {
            return;
        }

        const name = user.name || "User";
        const email = user.email || "";
        const initials = name.trim().split(/\s+/).slice(0, 2)
            .map((part) => part.charAt(0).toUpperCase()).join("");
        const nameIds = ["sideName", "sidebarName", "sidebarEmployeeName", "topEmployeeName", "topUserName", "welcomeName", "profileName", "profileFullName", "hrProfileName", "dashboardInternName"];
        const emailIds = ["profileEmail", "dashboardEmail", "hrProfileEmail"];

        nameIds.forEach((id) => {
            const element = document.getElementById(id);
            if (element) element.textContent = name;
        });

        emailIds.forEach((id) => {
            const element = document.getElementById(id);
            if (element) element.textContent = email;
        });

        document.querySelectorAll(".profile-menu-text strong, .profile-text strong, .system-user strong").forEach((element) => {
            element.textContent = name;
        });

        document.querySelectorAll(".profile-menu .avatar, .user-mini .avatar, .system-user .avatar").forEach((element) => {
            element.textContent = initials;
        });

        document.querySelectorAll("#profile .detail, #profile .detail-item, #profile .mini").forEach((item) => {
            const label = item.querySelector("span")?.textContent.trim().toLowerCase();
            const value = item.querySelector("strong");

            if (!value) return;
            if (label === "full name") value.textContent = name;
            if (label === "email" || label === "work email") value.textContent = email;
        });
    }

    window.PayFlowAuth = {
        routes,
        ensureDemoUsers,
        getUsers,
        saveUsers,
        getCurrentUser,
        setCurrentUser,
        login,
        logout,
        redirectForRole,
        requireRole,
        updateInterfaceProfile
    };

    ensureDemoUsers();
    document.addEventListener("DOMContentLoaded", updateInterfaceProfile);
}());
