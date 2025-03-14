package com.jacob.apm.utilities;

import com.jacob.apm.constants.MainConstants;
import com.jacob.apm.models.ReCaptchaResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


public class RecaptchaUtil {

    @Value("${urls.google_recaptcha_server}")
    private static String GOOGLE_RECAPTCHA_SERVER_URL;

    @Value("${secrets.google_recaptcha_server}")
    private static String GOOGLE_RECAPTCHA_SERVER_KEY;

    /**
     * Method to validate Google reCaptcha.
     * @param userResponse: Google reCaptcha token from Google.
     * @return: MainConstants.FLAG_SUCCESS: If human users; MainConstants.FLAG_FAILURE: if a bot. 
     */
    public static boolean validateRecaptcha(String userResponse) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("secret", GOOGLE_RECAPTCHA_SERVER_KEY);
        map.add("response", userResponse);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<ReCaptchaResponse> reCaptchaApiResponse = restTemplate.postForEntity(GOOGLE_RECAPTCHA_SERVER_URL, request, ReCaptchaResponse.class);

        if (reCaptchaApiResponse.getStatusCode().is2xxSuccessful() && reCaptchaApiResponse.getBody().isSuccess()) {
            return MainConstants.FLAG_SUCCESS;
        }
        return MainConstants.FLAG_FAILURE;
    }
}
