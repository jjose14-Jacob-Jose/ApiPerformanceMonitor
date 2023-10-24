package com.jacob.apm.controllers;

/**
 * This class is generated by referring:
 * https://www.geeksforgeeks.org/spring-boot-3-0-jwt-authentication-with-spring-security-using-mysql-database/#
 */

import com.jacob.apm.models.APMUser;
import com.jacob.apm.models.AuthenticationRequest;
import com.jacob.apm.services.APMUserService;
import com.jacob.apm.services.JwtService;
import com.jacob.apm.utilities.APMLogger;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/auth")
public class APMUserController {
    @Autowired
    private APMUserService apmUserService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/loginPage")
    public ModelAndView login() {
        try {
            APMLogger.logMethodEntry("loginPage()");
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("loginPage"); // Set the view name to your error page (e.g., "error.html")
            return modelAndView;

        } catch (Exception exception) {
            APMLogger.logError( "loginPage()", exception);
            return null;
        }
    }

    @PostMapping("/login")
    public void login(@RequestBody AuthenticationRequest authenticationRequest, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword()));
        if (authentication.isAuthenticated()) {
            String token = jwtService.generateToken(authenticationRequest.getUsername());
            response.setHeader("Authorization", "Bearer " + token);
            response.setHeader("Set-Cookie", "Authorization=" + token + "; HttpOnly; Path=/");
        } else {
            throw new UsernameNotFoundException("invalid user request !");
        }
    }

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome this endpoint is not secure";
    }

    @PostMapping("/addNewUser")
    public String addNewUser(@RequestBody APMUser apmUser) {
        return apmUserService.saveUserToDatabase(apmUser);
    }

    @GetMapping("/user/userProfile")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String userProfile() {
        return "Welcome to User Profile";
    }

    @GetMapping("/admin/adminProfile")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String adminProfile() {
        return "Welcome to Admin Profile";
    }

    @PostMapping("/generateToken")
    public String authenticateAndGetToken(@RequestBody AuthenticationRequest authenticationRequest, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword()));
        if (authentication.isAuthenticated()) {
            String token = jwtService.generateToken(authenticationRequest.getUsername());

            response.setHeader("Authorization", "Bearer " + token);
            response.setHeader("Set-Cookie", "Authorization=" + token + "; HttpOnly; Path=/");

            return "Token generated successfully!";
        } else {
            throw new UsernameNotFoundException("invalid user request !");
        }
    }

}
