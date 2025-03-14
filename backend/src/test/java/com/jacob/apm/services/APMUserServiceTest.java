package com.jacob.apm.services;

import com.jacob.apm.constants.MainConstants;
import com.jacob.apm.models.APMUser;
import com.jacob.apm.repositories.APMUserRepository;
import com.jacob.apm.utilities.APISystemTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class APMUserServiceTest {

    @Mock
    private APMUserRepository apmUserRepository;

    @InjectMocks
    private APMUserService apmUserService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize annotated mocks
    }

    private List<APMUser> getAPMUserList() {

        List<APMUser> listAPMUsers = new ArrayList<APMUser>();

        // Create a user instance
        APMUser apmUser1 = new APMUser();
        apmUser1.setUsername("username");
        apmUser1.setNameFirst("First");
        apmUser1.setNameLast("Last");
        apmUser1.setEmailId("first.last@email.com");
        apmUser1.setPassword("passwordHash");
        apmUser1.setTimestampRegistration(APISystemTime.getInstantTimeAsString());
        apmUser1.setLoginAttemptsFailed(0);
        listAPMUsers.add(apmUser1);

        APMUser apmUser2 = new APMUser();
        apmUser2.setUsername("username2");
        apmUser2.setNameFirst("First2");
        apmUser2.setNameLast("Last2");
        apmUser2.setEmailId("first.last2@email.com");
        apmUser2.setPassword("passwordHash2");
        apmUser2.setTimestampRegistration(APISystemTime.getInstantTimeAsString());
        apmUser2.setLoginAttemptsFailed(1);
        listAPMUsers.add(apmUser2);

        return listAPMUsers;
    }


    @Test
    void testGetAPMUserWithUserName1() {

        APMUser apmUser = getAPMUserList().get(0);
        String username = "testUsernameForMockito";
        username = username.toLowerCase();
        apmUser.setUsername(username);

//        Specifying what repository should when it receives a call to 'findAPMUserByUser' with 'username' as variable.
        Mockito.when(apmUserRepository.findAPMUserByUsername(username)).thenReturn(apmUser);

//        Calling service this, this will invoking the Mockito method.
        APMUser apmUserWithUsernameFromDB = apmUserService.getAPMUserByUsername(username);

//        Ensuring the service class in previous line has called the mocked repository method.
        Mockito.verify(apmUserRepository).findAPMUserByUsername(username);

        assertNotNull(apmUserWithUsernameFromDB);
        assertEquals(apmUserWithUsernameFromDB, apmUser);

    }

    @Test
    void testGetAPMUserWithUserName2() {

        APMUser apmUser1 = getAPMUserList().get(0);
        String username = "testUsernameForMockito";
        username = username.toLowerCase();
        apmUser1.setUsername(username);

        APMUser apmUser2 = getAPMUserList().get(1);

//        Specifying what repository should when it receives a call to 'findAPMUserByUser' with 'username' as variable.
        Mockito.when(apmUserRepository.findAPMUserByUsername(username)).thenReturn(apmUser2);

//        Calling service this, this will invoking the Mockito method.
        APMUser apmUserWithUsernameFromDB = apmUserService.getAPMUserByUsername(username);

//        Ensuring the service class in previous line has called the mocked repository method.
        Mockito.verify(apmUserRepository).findAPMUserByUsername(username);

        assertNotNull(apmUserWithUsernameFromDB);
        assertNotEquals(apmUserWithUsernameFromDB, apmUser1);

    }

    @Test
    void testGetAPMUserWithEmailID1() {

        APMUser apmUser = getAPMUserList().get(0);
        String emailID = "testEmail@email.com";
        apmUser.setEmailId(emailID);

//        Specifying what repository should when it receives a call to 'findAPMUserByUser' with 'username' as variable.
        Mockito.when(apmUserRepository.findAPMUserByEmailId(emailID)).thenReturn(apmUser);

//        Calling service this, this will invoking the Mockito method.
        APMUser apmUserWithEmailIDFromDB = apmUserService.getAPMUserByEmailID(emailID);

//        Ensuring the service class in previous line has called the mocked repository method.
        Mockito.verify(apmUserRepository).findAPMUserByEmailId(emailID);

        assertNotNull(apmUserWithEmailIDFromDB);
        assertEquals(apmUserWithEmailIDFromDB, apmUser);

    }

    @Test
    void testGetAPMUserWithEmailID2() {

        APMUser apmUser = getAPMUserList().get(0);
        String emailID = "testEmail@email.com";
        apmUser.setEmailId(emailID);

        APMUser apmUser2 = getAPMUserList().get(1);

//        Specifying what repository should when it receives a call to 'findAPMUserByUser' with 'username' as variable.
        Mockito.when(apmUserRepository.findAPMUserByEmailId(emailID)).thenReturn(apmUser2);

//        Calling service this, this will invoking the Mockito method.
        APMUser apmUserWithEmailIDFromDB = apmUserService.getAPMUserByEmailID(emailID);

//        Ensuring the service class in previous line has called the mocked repository method.
        Mockito.verify(apmUserRepository).findAPMUserByEmailId(emailID);

        assertNotNull(apmUserWithEmailIDFromDB);
        assertNotEquals(apmUserWithEmailIDFromDB, apmUser);

    }

    @Test
    void testUnlockAPMUserAfterDurationInHours1() {

        long durationMaxForAccountLockInHours = 2;
        int loginAttemptsFailed = 2;
        Instant instantTimestampNow = Instant.now();

        APMUser apmUser = getAPMUserList().get(0);
        apmUser.setLoginAttemptsFailed(loginAttemptsFailed);
        apmUser.setTimestampAccountLocked(instantTimestampNow.toString());
        String accountStatus = apmUserService.unlockAPMUserAfterDurationInHours(apmUser, durationMaxForAccountLockInHours);

        assertEquals(accountStatus, MainConstants.MSG_ACCOUNT_LOCK_STATUS_LOCKED);
    }

    @Test
    void testUnlockAPMUserAfterDurationInHours2() {

        long durationMaxForAccountLockInHours = 2;
        int loginAttemptsFailed = 2;
        Instant instantTimestampNow = Instant.now().minus(Duration.ofHours(durationMaxForAccountLockInHours * 2));

        APMUser apmUser = getAPMUserList().get(0);
        apmUser.setLoginAttemptsFailed(loginAttemptsFailed);
        apmUser.setTimestampAccountLocked(instantTimestampNow.toString());
        String accountStatus = apmUserService.unlockAPMUserAfterDurationInHours(apmUser, durationMaxForAccountLockInHours);

        assertEquals(accountStatus, MainConstants.MSG_ACCOUNT_LOCK_STATUS_UNLOCKED);
    }
}