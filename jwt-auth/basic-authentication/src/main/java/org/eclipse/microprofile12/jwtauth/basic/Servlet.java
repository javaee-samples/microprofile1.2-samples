package org.eclipse.microprofile12.jwtauth.basic;

import java.io.IOException;

import javax.annotation.security.DeclareRoles;
import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/servlet")
@ServletSecurity(@HttpConstraint(rolesAllowed = "architect"))
@DeclareRoles({"architect", "bar", "kaz"})
public class Servlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String webName = null;
        if (request.getUserPrincipal() != null) {
            webName = request.getUserPrincipal().getName();
        }
        
        response.setContentType("text/plain");
        
        response.getWriter().write(
                "This is a protected servlet \n" +
        
                    "web username: " + webName + "\n" +
                            
                    "web user has role \"architect\": " + request.isUserInRole("architect") + "\n" +
                    "web user has role \"bar\": " + request.isUserInRole("bar") + "\n" +
                    "web user has role \"kaz\": " + request.isUserInRole("kaz") + "\n"
                    );
    }

}
