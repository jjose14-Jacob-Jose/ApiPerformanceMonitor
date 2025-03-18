package com.jacob.apm.services;

import com.jacob.apm.constants.MainConstants;
import com.jacob.apm.models.APMUser;
import com.jacob.apm.models.AuthenticationRequest;
import com.jacob.apm.models.UserInfoDetails;
import com.jacob.apm.models.UserSignUpRequest;
import com.jacob.apm.repositories.APMUserRepository;
import com.jacob.apm.utilities.APISystemTime;
import com.jacob.apm.utilities.RecaptchaUtil;
import com.jacob.apm.utilities.RequestValidator;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static com.jacob.apm.constants.ConfigurationConstants.DEFAULT_VALUE_LOGIN_ATTEMPTS_FAILED;
import static com.jacob.apm.constants.ConfigurationConstants.ROLE_USER;

@Service
public class APMUserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(APMUserService.class.getName());

    @Autowired
    private APMUserRepository apmUserRepository;

    @Autowired
    @Lazy
    private PasswordEncoder encoder;

    /**
     * Save user to database.
     * @param userSignUpRequest : object to be saved to database.
     * @return 'MainConstants.MSG_SUCCESS' or 'MainConstants.MSG_FAILURE'
     */
    public String saveUserToDatabase(UserSignUpRequest userSignUpRequest) {
        logger.info("Calling saveUserToDatabase(). userSignUpRequest : {}", userSignUpRequest.toString());

        if (userSignUpRequest == null){
            String errorMessage = "Request object(userSignUpRequest) is null.";
            logger.error(errorMessage);
            return errorMessage;
        }

        if(RequestValidator.validateUserSignUpRequest(userSignUpRequest) == MainConstants.FLAG_FAILURE) {
            String errorMessage = "Request object(userSignupRequest) validation failed";
            logger.error(errorMessage);
            return errorMessage;
        }

        if(! (RecaptchaUtil.validateRecaptcha(userSignUpRequest.getGoogleReCaptchaToken()))) {
            String errorMessage = "Failed validating Google reCaptcha";
            logger.error(errorMessage);
            return errorMessage;
        }

        if(getAPMUserByUsername(userSignUpRequest.getUsername()) != null) {
            String errorMessage = "The username is already exists.";
            logger.error(errorMessage);
            return errorMessage;
        }

        APMUser apmUser = new APMUser();
        apmUser.setUsername(userSignUpRequest.getUsername().toLowerCase());
        apmUser.setNameFirst(userSignUpRequest.getNameFirst());
        apmUser.setNameLast(userSignUpRequest.getNameLast());
        apmUser.setPassword(encoder.encode(userSignUpRequest.getPassword()));
        apmUser.setTimestampRegistration(APISystemTime.getInstantTimeAsString());
        apmUser.setLoginAttemptsFailed(DEFAULT_VALUE_LOGIN_ATTEMPTS_FAILED);
        apmUser.setTimestampAccountLocked(MainConstants.STRING_EMPTY);
        apmUser.setRoles(ROLE_USER);

        try {
            apmUserRepository.save(apmUser);
            logger.info("User saved to database.");
            return MainConstants.MSG_SUCCESS;
        } catch (Exception exception) {
            logger.info("Exception while saving user  to database." + exception.toString());
            return exception.getMessage();
        }
    }

    public UserDetails loadUserByUsername(String username) {
        logger.info("Calling loadUserByUsername(). username : {}", username);

        if (username == null || username.equalsIgnoreCase(MainConstants.STRING_EMPTY)) {
            return null;
        }
        try {
            Optional<APMUser> userDetailOptional = apmUserRepository.findByUsername(username.toLowerCase());
            if (userDetailOptional.isPresent()) {
                APMUser apmUser = userDetailOptional.get();
                // You should create a custom UserDetails implementation, e.g., UserInfoDetails,
                // and map APMUser to UserDetails using a constructor or a converter.
                return new UserInfoDetails(apmUser);
            }
        } catch (Exception exception) {
            logger.error("Failed to retrieve the user. exception: {}", exception.toString());
        }
        return null;
    }

    /**
     * Check if there is a user with specified username.
     * @param username : username to be searched in database.
     * @return APMUser object of user with 'username' (if found), 'null' if no entry found.
     */
    public APMUser getAPMUserByUsername(String username) {
        logger.info("Calling getAPMUserByUsername(). username : {}", username);

        if (username == null || username.equalsIgnoreCase(MainConstants.STRING_EMPTY)) {
            logger.error("Username is null or empty.");
            return null;
        }

        username = username.toLowerCase();
        APMUser apmUserFromDB = null;
        try {
            apmUserFromDB = apmUserRepository.findAPMUserByUsername(username);
        } catch (Exception exception) {
            logger.error("Failed to retrieve the user with username. exception: {}", exception.toString());
        }
        return apmUserFromDB;
    }

    /**
     * Check if there is a user with specified emailD.
     * @param emailID : email ID to be searched in database.
     * @return APMUser object of user with 'emailID' (if found), 'null' if no entry found.
     */
    public APMUser getAPMUserByEmailID(String emailID) {
        logger.info("Calling getAPMUserByEmailID(). emailID : {}", emailID);

        if (emailID == null || emailID.equalsIgnoreCase(MainConstants.STRING_EMPTY)) {
            return null;
        }

        APMUser apmUserFromDB = null;
        try {
            apmUserFromDB = apmUserRepository.findAPMUserByEmailId(emailID);
        } catch (Exception exception) {
            logger.error("Failed to retrieve the user with emailId. exception: {}", exception.toString());
        }
        return apmUserFromDB;
    }

    /**
     * Checks if and unlocks a user account if its 'durationMaxForAccountLockInHours' past since the account has been locked.
     * @param apmUser : User whose account is locked.
     * @param durationMaxForAccountLockInHours : Duration (in hours) for the account to be locked.
     * @return 'MainConstants.MSG_ACCOUNT_LOCK_STATUS_UNLOCKED' or 'MainConstants.MSG_ACCOUNT_LOCK_STATUS_LOCKED'
     */
    public String unlockAPMUserAfterDurationInHours(APMUser apmUser, long durationMaxForAccountLockInHours) {
        logger.info("Calling unlockAPMUserAfterDurationInHours(). apmUser : {}", apmUser.toLogString());
        if (apmUser == null || durationMaxForAccountLockInHours < 0) {
            logger.error("APMUser is null or empty.");
            return "APMUser is null or duration for account lock is less than zero.";
        }


        Instant instantCurrentUTCTime = APISystemTime.getInstantTimeAsInstant();
        Instant userAccountLockedUTCTimeAsInstant = Instant.parse(apmUser.getTimestampAccountLocked());

        Duration duration = Duration.between(userAccountLockedUTCTimeAsInstant, instantCurrentUTCTime);
        long durationInHours = duration.toHours();

        if (durationInHours >= durationMaxForAccountLockInHours) {

            apmUser.setLoginAttemptsFailed(MainConstants.LOGIN_ATTEMPTS_FAILED_RESET_VALUE);
            apmUser.setTimestampAccountLocked(MainConstants.STRING_EMPTY);
            logger.info("APMUser account is unlocked.");
            return MainConstants.MSG_ACCOUNT_LOCK_STATUS_UNLOCKED;

        } else {
//            User Account is still locked.
            logger.info("APMUser account is still locked.");
            return MainConstants.MSG_ACCOUNT_LOCK_STATUS_LOCKED;
        }

    }

    /**
     * Checks if there is a user with the specified username.
     * @param userSignUpRequest Object container username to be searched.
     * @return MainConstants.FLAG_SUCCESS if username not found.
     *  MainConstants.FLAG_FAILURE if username is found.
     */
    public boolean isUsernameIsAvailable(UserSignUpRequest userSignUpRequest) {
        logger.info("Calling isUsernameIsAvailable(). userSignUpRequest : {}", userSignUpRequest.toLogString());

        if (userSignUpRequest == null || userSignUpRequest.getUsername() == null) {
            logger.error("UserSignUpRequest is null or empty.");
            return MainConstants.FLAG_FAILURE;
        }

        APMUser apmUserFromDb = getAPMUserByUsername(userSignUpRequest.getUsername());

        if (apmUserFromDb == null)
        {
            logger.info("Username not found in DB.");
            return MainConstants.FLAG_SUCCESS;
        } else {
            logger.info("Username already exists.");
            return MainConstants.FLAG_FAILURE;
        }
    }

    public ResponseEntity<?> generateToken(AuthenticationRequest authenticationRequest, HttpServletResponse response, JwtService jwtService, AuthenticationManager authenticationManager) {

        logger.info("Calling generateToken. authenticationRequest: " + authenticationRequest.toLogString());
        if (authenticationRequest == null) {
            String errorMessage = "AuthenticationRequest object is null. ";
            logger.error(errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        }

//        Validate Google reCaptcha.
        if(! (RecaptchaUtil.validateRecaptcha(authenticationRequest.getGoogleReCaptcha()))) {
            String errorMessage = "Google reCaptcha server-side verification failed. Client token: "+authenticationRequest.getGoogleReCaptcha();
            logger.error(errorMessage);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMessage);
        }

        try {
            authenticationRequest.setUsername(authenticationRequest.getUsername().toLowerCase());
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername().toLowerCase(), authenticationRequest.getPassword()));
            if (authentication.isAuthenticated()) {
                String token = jwtService.generateToken(authenticationRequest.getUsername());

                // Set the token as an HTTP-only cookie
                Cookie cookieHttpOnly = new Cookie(MainConstants.COOKIE_HEADER_AUTHORIZATION, token);
                cookieHttpOnly.setHttpOnly(true);
                cookieHttpOnly.setPath("/");
                response.addCookie(cookieHttpOnly);

                Cookie cookieUsername = new Cookie(MainConstants.COOKIE_HEADER_PREFIX_USERNAME, authenticationRequest.getUsername());
                cookieUsername.setMaxAge(MainConstants.DURATION_MILLISECONDS_IN_ONE_HOUR);
                cookieUsername.setPath("/");
                response.addCookie(cookieUsername);

                String messageForLog = "Successfully generated JWT token and cookie for username: "+authenticationRequest.getUsername();
                logger.info(messageForLog);
                return ResponseEntity.status(HttpStatus.OK).body(MainConstants.MSG_SUCCESS);
            }
        }catch (Exception exception) {
            String messageForLog = "Could not create generate JWT token for username: "+authenticationRequest.getUsername();
            logger.error(messageForLog);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception);
        }

        String errorMessage = "Invalid credentials. " + authenticationRequest.toString();
        logger.error(errorMessage);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMessage);
    }

}
