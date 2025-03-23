package com.stc.apm.services;

import com.stc.apm.constants.MainConstants;
import com.stc.apm.models.ApmUser;
import com.stc.apm.models.ApiCall;
import com.stc.apm.models.AuthenticationRequest;
import com.stc.apm.models.UserInfoDetails;
import com.stc.apm.models.UserSignUpRequest;
import com.stc.apm.repositories.ApmUserRepository;
import com.stc.apm.utilities.ApiSystemTime;
import com.stc.apm.utilities.RecaptchaUtil;
import com.stc.apm.utilities.RequestValidator;
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

import static com.stc.apm.constants.ConfigurationConstants.DEFAULT_VALUE_LOGIN_ATTEMPTS_FAILED;
import static com.stc.apm.constants.ConfigurationConstants.ROLE_USER;

@Service
public class ApmUserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(ApmUserService.class.getName());

    private final ApiLogService apiLogService;

    @Autowired
    private ApmUserRepository apmUserRepository;

    @Autowired
    @Lazy
    private PasswordEncoder encoder;

    @Autowired
    public ApmUserService(ApiLogService apiLogService) {
        this.apiLogService = apiLogService;
    }

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

        if(getApmUserByUsername(userSignUpRequest.getUsername()) != null) {
            String errorMessage = "The username is already exists.";
            logger.error(errorMessage);
            return errorMessage;
        }

        ApmUser apmUser = new ApmUser();
        apmUser.setUsername(userSignUpRequest.getUsername().toLowerCase());
        apmUser.setNameFirst(userSignUpRequest.getNameFirst());
        apmUser.setNameLast(userSignUpRequest.getNameLast());
        apmUser.setPassword(encoder.encode(userSignUpRequest.getPassword()));
        apmUser.setTimestampRegistration(ApiSystemTime.getInstantTimeAsString());
        apmUser.setLoginAttemptsFailed(DEFAULT_VALUE_LOGIN_ATTEMPTS_FAILED);
        apmUser.setTimestampAccountLocked(MainConstants.STRING_EMPTY);
        apmUser.setRoles(ROLE_USER);

        try {
            apmUserRepository.save(apmUser);
            logger.info("User saved to database.");
            apiLogService.saveToDatabaseApiCall(
                new ApiCall(String.format("New user account created. Welcome '%s'.", apmUser.getUsername()), "APM Web Interface")
            );
            return MainConstants.MSG_SUCCESS;
        } catch (Exception exception) {
            logger.info("Exception while saving user  to database." + exception.toString());
            apiLogService.saveToDatabaseApiCall(
                    new ApiCall(String.format("Failed to create an account for '%s'.", apmUser.getUsername()), "APM Web Interface")
            );
            return exception.getMessage();
        }
    }

    public UserDetails loadUserByUsername(String username) {
        logger.info("Calling loadUserByUsername(). username : {}", username);

        if (username == null || username.equalsIgnoreCase(MainConstants.STRING_EMPTY)) {
            return null;
        }
        try {
            Optional<ApmUser> userDetailOptional = apmUserRepository.findByUsername(username.toLowerCase());
            if (userDetailOptional.isPresent()) {
                ApmUser apmUser = userDetailOptional.get();
                // You should create a custom UserDetails implementation, e.g., UserInfoDetails,
                // and map ApmUser to UserDetails using a constructor or a converter.
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
     * @return ApmUser object of user with 'username' (if found), 'null' if no entry found.
     */
    public ApmUser getApmUserByUsername(String username) {
        logger.info("Calling getApmUserByUsername(). username : {}", username);

        if (username == null || username.equalsIgnoreCase(MainConstants.STRING_EMPTY)) {
            logger.error("Username is null or empty.");
            return null;
        }

        username = username.toLowerCase();
        ApmUser apmUserFromDB = null;
        try {
            apmUserFromDB = apmUserRepository.findApmUserByUsername(username);
        } catch (Exception exception) {
            logger.error("Failed to retrieve the user with username. exception: {}", exception.toString());
            apiLogService.saveToDatabaseApiCall(
                    new ApiCall(String.format("Failed to retrieve the user with username '%s'.", username), "APM Web Interface")
            );
        }
        return apmUserFromDB;
    }

    /**
     * Check if there is a user with specified emailD.
     * @param emailID : email ID to be searched in database.
     * @return ApmUser object of user with 'emailID' (if found), 'null' if no entry found.
     */
    public ApmUser getApmUserByEmailID(String emailID) {
        logger.info("Calling getApmUserByEmailID(). emailID : {}", emailID);

        if (emailID == null || emailID.equalsIgnoreCase(MainConstants.STRING_EMPTY)) {
            return null;
        }

        ApmUser apmUserFromDB = null;
        try {
            apmUserFromDB = apmUserRepository.findApmUserByEmailId(emailID);
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
    public String unlockApmUserAfterDurationInHours(ApmUser apmUser, long durationMaxForAccountLockInHours) {
        logger.info("Calling unlockApmUserAfterDurationInHours(). apmUser : {}", apmUser.toLogString());
        if (apmUser == null || durationMaxForAccountLockInHours < 0) {
            logger.error("ApmUser is null or empty.");
            return "ApmUser is null or duration for account lock is less than zero.";
        }


        Instant instantCurrentUTCTime = ApiSystemTime.getInstantTimeAsInstant();
        Instant userAccountLockedUTCTimeAsInstant = Instant.parse(apmUser.getTimestampAccountLocked());

        Duration duration = Duration.between(userAccountLockedUTCTimeAsInstant, instantCurrentUTCTime);
        long durationInHours = duration.toHours();

        if (durationInHours >= durationMaxForAccountLockInHours) {

            apmUser.setLoginAttemptsFailed(MainConstants.LOGIN_ATTEMPTS_FAILED_RESET_VALUE);
            apmUser.setTimestampAccountLocked(MainConstants.STRING_EMPTY);
            logger.info("ApmUser account is unlocked.");
            return MainConstants.MSG_ACCOUNT_LOCK_STATUS_UNLOCKED;

        } else {
//            User Account is still locked.
            logger.info("ApmUser account is still locked.");
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

        ApmUser apmUserFromDb = getApmUserByUsername(userSignUpRequest.getUsername());

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
                apiLogService.saveToDatabaseApiCall(
                        new ApiCall(String.format("Welcome '%s'.",authenticationRequest.getUsername()), "APM Web Interface")
                );
                return ResponseEntity.status(HttpStatus.OK).body(MainConstants.MSG_SUCCESS);
            }
        }catch (Exception exception) {
            String messageForLog = "Could not create generate JWT token for username: "+authenticationRequest.getUsername();
            logger.error(messageForLog);
            apiLogService.saveToDatabaseApiCall(
                    new ApiCall(String.format("User '%s' is facing issue trying to log-in.", authenticationRequest.getUsername()), "APM Web Interface")
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception);
        }

        String errorMessage = "Invalid credentials. " + authenticationRequest.toString();
        logger.error(errorMessage);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMessage);
    }

}
