package com.stc.apm.utilities;

import com.stc.apm.constants.MainConstants;
import com.stc.apm.models.UserSignUpRequest;

import java.util.regex.Pattern;

import static com.stc.apm.constants.ConfigurationConstants.*;

public class RequestValidator {

    public static boolean validateUserSignUpRequest(UserSignUpRequest userSignUpRequest) {
        boolean isNameFirstValid = isValidField(userSignUpRequest.getNameFirst());
        boolean isNameLastValid = isValidField(userSignUpRequest.getNameLast());
        boolean isUsernameValid = matchStringWithRegex(userSignUpRequest.getUsername(), REGEX_FOR_VALIDATION_USERNAME);
        boolean isPasswordValid = matchStringWithRegex(userSignUpRequest.getPassword(), REGEX_FOR_VALIDATION_PASSWORD);

        boolean isEmailValid;
        if(userSignUpRequest.getEmailId().equalsIgnoreCase(MainConstants.STRING_EMPTY))
            isEmailValid = MainConstants.FLAG_SUCCESS;
        else
            isEmailValid = matchStringWithRegex(userSignUpRequest.getEmailId(), REGEX_FOR_VALIDATION_EMAIL);

        // Check all validations and return true if all fields are valid
        if (isNameFirstValid && isNameLastValid && isUsernameValid && isEmailValid && isPasswordValid)
            return MainConstants.FLAG_SUCCESS;
        else
            return MainConstants.FLAG_FAILURE;
    }

    private static boolean isValidField(String field) {
        // Basic check for non-empty strings
        return field != null && !field.isEmpty();
    }

    private static boolean matchStringWithRegex(String stringToBeMatched, String regex) {
        // Check if the username matches the regex pattern
        return isValidField(stringToBeMatched) && Pattern.matches(regex, stringToBeMatched);
    }

}
