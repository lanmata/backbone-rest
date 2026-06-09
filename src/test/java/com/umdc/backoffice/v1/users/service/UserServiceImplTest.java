package com.umdc.backoffice.v1.users.service;

import com.umdc.backoffice.constant.keys.UserMessageKey;
import com.umdc.backoffice.v1.application.service.ApplicationService;
import com.umdc.backoffice.v1.contacts.mapper.ContactMapper;
import com.umdc.backoffice.v1.contacttypes.mapper.ContactTypeMapper;
import com.umdc.backoffice.v1.iam.audit.service.AuditEventService;
import com.umdc.backoffice.v1.iam.passwords.service.PasswordPolicyService;
import com.umdc.backoffice.v1.people.mapper.PersonMapper;
import com.umdc.backoffice.v1.users.api.to.UserCreateRequest;
import com.umdc.backoffice.v1.users.api.to.UserCreateResponse;
import com.umdc.backoffice.v1.users.api.to.UserTO;
import com.umdc.backoffice.v1.users.mapper.UserMapper;
import com.umdc.commons.general.pojo.Application;
import com.umdc.persistence.general.domains.*;
import com.umdc.persistence.general.repositories.ApplicationRoleUserRepository;
import com.umdc.persistence.general.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Mock
    ApplicationRoleUserRepository applicationRoleUserRepository;

    @Mock
    ApplicationService applicationService;

    @Mock
    UserApplicationRoleService userApplicationRoleService;

    @Mock
    UserMapper userMapper;

    @Mock
    PersonMapper personMapper;

    @Mock
    ContactMapper contactMapper;

    @Mock
    ContactTypeMapper contactTypeMapper;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    PasswordPolicyService passwordPolicyService;

    @Mock
    AuditEventService auditEventService;

    @InjectMocks
    UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByAliasWithoutApplicationId() {
        String alias = "testAlias";
        UserEntity userEntity = new UserEntity();
        userEntity.setAlias(alias);

        when(userRepository.findByAlias(alias)).thenReturn(userEntity);
        when(userMapper.toTarget(userEntity)).thenReturn(new UserTO());

        ResponseEntity<UserTO> result = userService.findUserByAlias(alias, null);

        assertNotNull(result);
        verify(userRepository, times(1)).findByAlias(alias);
        verify(userMapper, times(1)).toTarget(userEntity);
    }

    @Test
    void testFindByAliasWithApplicationId() {
        String alias = "testAlias";
        UUID applicationId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        userEntity.setAlias(alias);

        when(userRepository.findByAliasAndApplication(alias, applicationId)).thenReturn(Optional.of(userEntity));
        when(userMapper.toTarget(userEntity)).thenReturn(new UserTO());

        ResponseEntity<UserTO> result = userService.findUserByAlias(alias, applicationId);

        assertNotNull(result);
        verify(userRepository, times(1)).findByAliasAndApplication(alias, applicationId);
        verify(userMapper, times(1)).toTarget(userEntity);
    }

    @Test
    void testFindByAliasNotFound() {
        String alias = "testAlias";
        UUID applicationId = UUID.randomUUID();

        when(userRepository.findByAliasAndApplication(alias, applicationId)).thenReturn(Optional.empty());

        ResponseEntity<UserTO> result = userService.findUserByAlias(alias, applicationId);

        assertNotNull(result);
        assertNull(result.getBody());
        verify(userRepository, times(1)).findByAliasAndApplication(alias, applicationId);
        verifyNoInteractions(userMapper);
    }

    @Test
    void testFindAllWithApplicationId_UsersFound() {
        UUID applicationId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        List<UserEntity> userEntities = new ArrayList<>();
        userEntities.add(userEntity);
        UserTO userTO = new UserTO();
        when(userRepository.findByApplication(applicationId)).thenReturn(userEntities);
        when(userMapper.toTarget(userEntity)).thenReturn(userTO);

        ResponseEntity<List<UserTO>> result = userService.findAll(applicationId);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertFalse(result.getBody().isEmpty());
        verify(userRepository, times(1)).findByApplication(applicationId);
        verify(userMapper, times(1)).toTarget(userEntity);
    }

    @Test
    void testFindAllWithApplicationId_NoUsers() {
        UUID applicationId = UUID.randomUUID();
        when(userRepository.findByApplication(applicationId)).thenReturn(new ArrayList<>());

        ResponseEntity<List<UserTO>> result = userService.findAll(applicationId);
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());
        verify(userRepository, times(1)).findByApplication(applicationId);
    }

    @Test
    void testFindAllWithoutApplicationId_UsersFound() {
        // null applicationId is rejected with 400 to prevent cross-tenant data leakage
        ResponseEntity<List<UserTO>> result = userService.findAll(null);
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void testFindAllWithoutApplicationId_NoUsers() {
        // null applicationId is rejected with 400 to prevent cross-tenant data leakage
        ResponseEntity<List<UserTO>> result = userService.findAll(null);
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void testFindUserByAlias_StatusCodes() {
        String alias = "testAlias";
        UUID applicationId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        userEntity.setAlias(alias);
        UserTO userTO = new UserTO();
        when(userRepository.findByAliasAndApplication(alias, applicationId)).thenReturn(Optional.of(userEntity));
        when(userMapper.toTarget(userEntity)).thenReturn(userTO);
        ResponseEntity<UserTO> found = userService.findUserByAlias(alias, applicationId);
        assertEquals(HttpStatus.OK, found.getStatusCode());
        when(userRepository.findByAliasAndApplication(alias, applicationId)).thenReturn(Optional.empty());
        ResponseEntity<UserTO> notFound = userService.findUserByAlias(alias, applicationId);
        assertEquals(HttpStatus.CONFLICT, notFound.getStatusCode());
    }

    @Test
    void testValidateEmail_Exists() {
        String email = "test@example.com";
        UUID appId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        when(userRepository.findByEmailAndApplication(email, appId)).thenReturn(Optional.of(userEntity));
        ResponseEntity<Void> response = userService.validateEmail(email, appId);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void testValidateEmail_NotExists() {
        String email = "test@example.com";
        UUID appId = UUID.randomUUID();
        when(userRepository.findByEmailAndApplication(email, appId)).thenReturn(Optional.empty());
        ResponseEntity<Void> response = userService.validateEmail(email, appId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testValidateAlias_Exists() {
        String alias = "alias";
        UUID appId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        when(userRepository.findByAliasAndApplication(alias, appId)).thenReturn(Optional.of(userEntity));
        ResponseEntity<Void> response = userService.validateAlias(alias, appId);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void testValidateAlias_NotExists() {
        String alias = "alias";
        UUID appId = UUID.randomUUID();
        when(userRepository.findByAliasAndApplication(alias, appId)).thenReturn(Optional.empty());
        ResponseEntity<Void> response = userService.validateAlias(alias, appId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testFindUserByUserId_Found() {
        UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        UserTO userTO = new UserTO();
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userMapper.toTarget(userEntity)).thenReturn(userTO);
        ResponseEntity<UserTO> response = userService.findUserById(userId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userTO, response.getBody());
    }

    @Test
    void testFindUserByUserId_NotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        ResponseEntity<UserTO> response = userService.findUserById(userId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testCreate_BadRequest_NullRequest() {
        ResponseEntity<UserCreateResponse> response = userService.create((UserCreateRequest) null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testCreate_BadRequest_BlankAlias() {
        UserCreateRequest req = mock(UserCreateRequest.class);
        when(req.alias()).thenReturn("");
        ResponseEntity<UserCreateResponse> response = userService.create(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testCreate_BadRequest_BlankPassword() {
        UserCreateRequest req = mock(UserCreateRequest.class);
        when(req.alias()).thenReturn("alias");
        when(req.password()).thenReturn("");
        ResponseEntity<UserCreateResponse> response = userService.create(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testCreate_BadRequest_NullRole() {
        UserCreateRequest req = mock(UserCreateRequest.class);
        when(req.alias()).thenReturn("alias");
        when(req.password()).thenReturn("password");
        when(req.roleId()).thenReturn(null);
        ResponseEntity<UserCreateResponse> response = userService.create(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testUpdate_BadRequest_NullUserId() {
        UserTO userTO = new UserTO();
        ResponseEntity<UserTO> response = userService.update(null, userTO);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testDeleteUserByApplicationAndUserId_ApplicationNotFound() {
        UUID appId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(applicationService.find(appId)).thenReturn(ResponseEntity.notFound().build());
        ResponseEntity<Void> response = userService.deleteUserByApplicationAndUserId(appId, userId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeleteUserByApplicationAndUserId_UserNotFound() {
        UUID appId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Application application = new Application();
        application.setId(appId);
        when(applicationService.find(appId)).thenReturn(ResponseEntity.ok(application));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        ResponseEntity<Void> response = userService.deleteUserByApplicationAndUserId(appId, userId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeleteUserByApplicationAndUserId_UserNotInApplication() {
        UUID appId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Application application = new Application();
        application.setId(appId);
        var userEntity = mock(UserEntity.class);
        when(applicationService.find(appId)).thenReturn(ResponseEntity.ok(application));
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userEntity.getApplicationRoleUser()).thenReturn(java.util.Set.of());
        ResponseEntity<Void> response = userService.deleteUserByApplicationAndUserId(appId, userId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeleteUserByApplicationAndUserId_InvalidIds() {
        ResponseEntity<Void> response = userService.deleteUserByApplicationAndUserId(null, null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testCreate_Success() {
        UUID appId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        UserCreateRequest request = mock(UserCreateRequest.class);
        when(request.alias()).thenReturn("newuser");
        when(request.password()).thenReturn("password123");
        when(request.roleId()).thenReturn(roleId);
        when(request.applicationId()).thenReturn(appId);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(UUID.randomUUID());
        userEntity.setAlias("newuser");

        when(userRepository.findByAliasAndApplication("newuser", appId)).thenReturn(Optional.empty());
        when(userMapper.toSource(request)).thenReturn(userEntity);
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(userMapper.toUserCreateResponse(userEntity)).thenReturn(mock(UserCreateResponse.class));

        ResponseEntity<UserCreateResponse> response = userService.create(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testCreate_UserAlreadyExists() {
        UUID appId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        UserCreateRequest request = mock(UserCreateRequest.class);
        when(request.alias()).thenReturn("existinguser");
        when(request.password()).thenReturn("password123");
        when(request.roleId()).thenReturn(roleId);
        when(request.applicationId()).thenReturn(appId);

        UserEntity existingUser = new UserEntity();
        when(userRepository.findByAliasAndApplication("existinguser", appId)).thenReturn(Optional.of(existingUser));
        when(userMapper.toTarget(existingUser)).thenReturn(new UserTO());

        ResponseEntity<UserCreateResponse> response = userService.create(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testUpdate_Success() {
        UUID userId = UUID.randomUUID();
        UserEntity existingUser = new UserEntity();
        existingUser.setId(userId);
        existingUser.setAlias("testuser");
        existingUser.setPassword("oldpassword");

        UserTO updateData = new UserTO();
        updateData.setDisplayName("Updated Name");
        updateData.setPassword("newpassword");
        updateData.setNotificationEmail(true);
        updateData.setNotificationSms(false);
        updateData.setPrivacyDataOutActive(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);
        when(userMapper.toTarget(existingUser)).thenReturn(updateData);

        ResponseEntity<UserTO> response = userService.update(userId, updateData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testUpdate_UserNotFound() {
        UUID userId = UUID.randomUUID();
        UserTO updateData = new UserTO();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseEntity<UserTO> response = userService.update(userId, updateData);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testUpdate_WithPersonData() {
        UUID userId = UUID.randomUUID();
        UserEntity existingUser = new UserEntity();
        existingUser.setId(userId);

        PersonEntity personEntity = new PersonEntity();
        existingUser.setPerson(personEntity);

        UserTO updateData = new UserTO();
        var person = new com.umdc.commons.general.pojo.Person();
        person.setFirstName("John");
        person.setMiddleName("M");
        person.setLastName("Doe");
        person.setGender("M");
        person.setBirthdate(java.time.LocalDate.now());
        updateData.setPerson(person);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);
        when(userMapper.toTarget(existingUser)).thenReturn(updateData);

        ResponseEntity<UserTO> response = userService.update(userId, updateData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testUpdate_WithNewPersonEntity() {
        UUID userId = UUID.randomUUID();
        UserEntity existingUser = new UserEntity();
        existingUser.setId(userId);
        existingUser.setPerson(null); // No existing person

        UserTO updateData = new UserTO();
        var person = new com.umdc.commons.general.pojo.Person();
        person.setFirstName("Jane");
        person.setLastName("Smith");
        updateData.setPerson(person);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);
        when(userMapper.toTarget(existingUser)).thenReturn(updateData);

        ResponseEntity<UserTO> response = userService.update(userId, updateData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(existingUser.getPerson());
    }

    @Test
    void testUpdate_WithContacts() {
        UUID userId = UUID.randomUUID();
        UserEntity existingUser = new UserEntity();
        existingUser.setId(userId);

        PersonEntity personEntity = new PersonEntity();
        existingUser.setPerson(personEntity);

        UserTO updateData = new UserTO();
        var person = new com.umdc.commons.general.pojo.Person();

        var contact = new com.umdc.commons.general.pojo.Contact();
        contact.setId(UUID.randomUUID());
        contact.setContent("test@example.com");

        var contactType = new com.umdc.commons.general.pojo.ContactType();
        contactType.setId(UUID.randomUUID());
        contactType.setName("Email");
        contactType.setDescription("Email Address");
        contactType.setActive(true);
        contact.setContactType(contactType);

        person.setContacts(List.of(contact));
        updateData.setPerson(person);

        ContactEntity contactEntity = new ContactEntity();
        ContactTypeEntity contactTypeEntity = new ContactTypeEntity();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(contactMapper.toSource(any())).thenReturn(contactEntity);
        when(contactTypeMapper.toSource(any())).thenReturn(contactTypeEntity);
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);
        when(userMapper.toTarget(existingUser)).thenReturn(updateData);

        ResponseEntity<UserTO> response = userService.update(userId, updateData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testDeleteUserByApplicationAndUserId_Success() {
        UUID appId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        var application = new com.umdc.commons.general.pojo.Application();
        application.setId(appId);

        ApplicationEntity appEntity = new ApplicationEntity();
        appEntity.setId(appId);

        ApplicationRoleUserEntity aru = new ApplicationRoleUserEntity();
        ApplicationRoleUserEntityId aruId = new ApplicationRoleUserEntityId();
        aruId.setApplicationId(appId);
        aru.setId(aruId);
        aru.setApplication(appEntity);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setApplicationRoleUser(Set.of(aru));

        when(applicationService.find(appId)).thenReturn(ResponseEntity.ok(application));
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        doNothing().when(applicationRoleUserRepository).deleteByUserIdAndApplicationId(userId, appId);

        ResponseEntity<Void> response = userService.deleteUserByApplicationAndUserId(appId, userId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(applicationRoleUserRepository, times(1)).deleteByUserIdAndApplicationId(userId, appId);
    }

    @Test
    void testUnlink_NullParams_ReturnsBadRequest() {
        ResponseEntity<UserTO> response = userService.unlink(null, null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testFind_DelegatesToFindUserById() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        ResponseEntity<UserTO> response = userService.find(userId);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testFindAll_WithNullApplicationId_ReturnsUsers() {
        // null applicationId is rejected with 400 to prevent cross-tenant data leakage
        ResponseEntity<List<UserTO>> response = userService.findAll(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testCreate_WithApplicationError() {
        UUID appId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        UserCreateRequest request = mock(UserCreateRequest.class);
        when(request.alias()).thenReturn("newuser");
        when(request.password()).thenReturn("password123");
        when(request.roleId()).thenReturn(roleId);
        when(request.applicationId()).thenReturn(appId);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(UUID.randomUUID());

        when(userRepository.findByAliasAndApplication("newuser", appId)).thenReturn(Optional.empty());
        when(userMapper.toSource(request)).thenReturn(userEntity);
        doThrow(new com.umdc.commons.exception.StandardException(UserMessageKey.USER_NOT_FOUND))
                .when(userApplicationRoleService).refreshRoleByApplication(any(), any());

        ResponseEntity<UserCreateResponse> response = userService.create(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testUpdate_WithEmptyPersonData() {
        UUID userId = UUID.randomUUID();
        UserEntity existingUser = new UserEntity();
        existingUser.setId(userId);

        UserTO updateData = new UserTO();
        updateData.setPerson(null); // No person data
        updateData.setDisplayName("Test User");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);
        when(userMapper.toTarget(existingUser)).thenReturn(updateData);

        ResponseEntity<UserTO> response = userService.update(userId, updateData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testUpdate_WithPartialPersonData() {
        UUID userId = UUID.randomUUID();
        UserEntity existingUser = new UserEntity();
        existingUser.setId(userId);

        PersonEntity personEntity = new PersonEntity();
        personEntity.setName("OldName");
        existingUser.setPerson(personEntity);

        UserTO updateData = new UserTO();
        var person = new com.umdc.commons.general.pojo.Person();
        person.setFirstName(""); // Empty string should not update
        person.setLastName("NewLastName");
        updateData.setPerson(person);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);
        when(userMapper.toTarget(existingUser)).thenReturn(updateData);

        ResponseEntity<UserTO> response = userService.update(userId, updateData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OldName", personEntity.getName()); // Should not be updated
        assertEquals("NewLastName", personEntity.getLastName());
    }

    @Test
    void testUpdate_PasswordNotChangedIfSame() {
        UUID userId = UUID.randomUUID();
        UserEntity existingUser = new UserEntity();
        existingUser.setId(userId);
        existingUser.setPassword("samepassword");

        UserTO updateData = new UserTO();
        updateData.setPassword("samepassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);
        when(userMapper.toTarget(existingUser)).thenReturn(updateData);

        ResponseEntity<UserTO> response = userService.update(userId, updateData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("samepassword", existingUser.getPassword());
    }

    @Test
    void testUpdate_WithNullDisplayName() {
        UUID userId = UUID.randomUUID();
        UserEntity existingUser = new UserEntity();
        existingUser.setId(userId);
        existingUser.setDisplayName("OldDisplayName");

        UserTO updateData = new UserTO();
        updateData.setDisplayName(null); // Null should not update

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);
        when(userMapper.toTarget(existingUser)).thenReturn(updateData);

        ResponseEntity<UserTO> response = userService.update(userId, updateData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OldDisplayName", existingUser.getDisplayName());
    }

    @Test
    void testUpdate_WithEmptyContacts() {
        UUID userId = UUID.randomUUID();
        UserEntity existingUser = new UserEntity();
        existingUser.setId(userId);

        PersonEntity personEntity = new PersonEntity();
        existingUser.setPerson(personEntity);

        UserTO updateData = new UserTO();
        var person = new com.umdc.commons.general.pojo.Person();
        person.setContacts(Collections.emptyList());
        updateData.setPerson(person);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);
        when(userMapper.toTarget(existingUser)).thenReturn(updateData);

        ResponseEntity<UserTO> response = userService.update(userId, updateData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testUpdate_WithContactTypeMinimalData() {
        UUID userId = UUID.randomUUID();
        UserEntity existingUser = new UserEntity();
        existingUser.setId(userId);

        PersonEntity personEntity = new PersonEntity();
        existingUser.setPerson(personEntity);

        UserTO updateData = new UserTO();
        var person = new com.umdc.commons.general.pojo.Person();

        var contact = new com.umdc.commons.general.pojo.Contact();
        contact.setContent("555-1234");

        var contactType = new com.umdc.commons.general.pojo.ContactType();
        contactType.setId(UUID.randomUUID());
        // Only ID set, no other fields
        contact.setContactType(contactType);

        person.setContacts(List.of(contact));
        updateData.setPerson(person);

        ContactEntity contactEntity = new ContactEntity();
        ContactTypeEntity contactTypeEntity = new ContactTypeEntity();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(contactMapper.toSource(any())).thenReturn(contactEntity);
        when(contactTypeMapper.toSource(any())).thenReturn(contactTypeEntity);
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);
        when(userMapper.toTarget(existingUser)).thenReturn(updateData);

        ResponseEntity<UserTO> response = userService.update(userId, updateData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testUpdate_WithAllPersonFields() {
        UUID userId = UUID.randomUUID();
        UserEntity existingUser = new UserEntity();
        existingUser.setId(userId);

        PersonEntity personEntity = new PersonEntity();
        existingUser.setPerson(personEntity);

        UserTO updateData = new UserTO();
        var person = new com.umdc.commons.general.pojo.Person();
        person.setFirstName("UpdatedFirst");
        person.setMiddleName("UpdatedMiddle");
        person.setLastName("UpdatedLast");
        person.setGender("F");
        person.setBirthdate(java.time.LocalDate.of(1990, 1, 1));
        updateData.setPerson(person);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);
        when(userMapper.toTarget(existingUser)).thenReturn(updateData);

        ResponseEntity<UserTO> response = userService.update(userId, updateData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("UpdatedFirst", personEntity.getName());
        assertEquals("UpdatedMiddle", personEntity.getMiddleName());
        assertEquals("UpdatedLast", personEntity.getLastName());
        assertEquals("F", personEntity.getGender());
    }

    @Test
    void testUpdate_WithException() {
        UUID userId = UUID.randomUUID();
        UserEntity existingUser = new UserEntity();
        existingUser.setId(userId);

        UserTO updateData = new UserTO();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<UserTO> response = userService.update(userId, updateData);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    }

    @Test
    void testFindByAliasWithNull_ApplicationId() {
        String alias = "testAlias";
        UserEntity userEntity = new UserEntity();
        userEntity.setAlias(alias);

        when(userRepository.findByAlias(alias)).thenReturn(userEntity);
        when(userMapper.toTarget(userEntity)).thenReturn(new UserTO());

        ResponseEntity<UserTO> result = userService.findUserByAlias(alias, null);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    void testCreate_WithBlankAliasAfterTrim() {
        UserCreateRequest req = mock(UserCreateRequest.class);
        when(req.alias()).thenReturn("   ");
        when(req.password()).thenReturn("password");

        ResponseEntity<UserCreateResponse> response = userService.create(req);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testCreate_WithBlankPasswordAfterTrim() {
        UserCreateRequest req = mock(UserCreateRequest.class);
        when(req.alias()).thenReturn("validalias");
        when(req.password()).thenReturn("   ");

        ResponseEntity<UserCreateResponse> response = userService.create(req);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testUpdate_WithAllNotificationFlags() {
        UUID userId = UUID.randomUUID();
        UserEntity existingUser = new UserEntity();
        existingUser.setId(userId);
        existingUser.setNotificationEmail(false);
        existingUser.setNotificationSms(false);
        existingUser.setPrivacyDataOutActive(false);

        UserTO updateData = new UserTO();
        updateData.setNotificationEmail(true);
        updateData.setNotificationSms(true);
        updateData.setPrivacyDataOutActive(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);
        when(userMapper.toTarget(existingUser)).thenReturn(updateData);

        ResponseEntity<UserTO> response = userService.update(userId, updateData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(existingUser.getNotificationEmail());
        assertTrue(existingUser.getNotificationSms());
        assertTrue(existingUser.getPrivacyDataOutActive());
    }

    @Test
    void testUpdate_WithNullNotificationFlags() {
        UUID userId = UUID.randomUUID();
        UserEntity existingUser = new UserEntity();
        existingUser.setId(userId);
        existingUser.setNotificationEmail(true);
        existingUser.setNotificationSms(true);
        existingUser.setPrivacyDataOutActive(true);

        UserTO updateData = new UserTO();
        updateData.setNotificationEmail(null);
        updateData.setNotificationSms(null);
        updateData.setPrivacyDataOutActive(null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);
        when(userMapper.toTarget(existingUser)).thenReturn(updateData);

        ResponseEntity<UserTO> response = userService.update(userId, updateData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Should not change existing values
        assertTrue(existingUser.getNotificationEmail());
        assertTrue(existingUser.getNotificationSms());
        assertTrue(existingUser.getPrivacyDataOutActive());
    }

    @Test
    void testUpdate_WithMultipleContacts() {
        UUID userId = UUID.randomUUID();
        UserEntity existingUser = new UserEntity();
        existingUser.setId(userId);

        PersonEntity personEntity = new PersonEntity();
        existingUser.setPerson(personEntity);

        UserTO updateData = new UserTO();
        var person = new com.umdc.commons.general.pojo.Person();

        var contact1 = new com.umdc.commons.general.pojo.Contact();
        contact1.setContent("email@test.com");
        var contactType1 = new com.umdc.commons.general.pojo.ContactType();
        contactType1.setId(UUID.randomUUID());
        contact1.setContactType(contactType1);

        var contact2 = new com.umdc.commons.general.pojo.Contact();
        contact2.setContent("555-1234");
        var contactType2 = new com.umdc.commons.general.pojo.ContactType();
        contactType2.setId(UUID.randomUUID());
        contact2.setContactType(contactType2);

        person.setContacts(List.of(contact1, contact2));
        updateData.setPerson(person);

        ContactEntity contactEntity = new ContactEntity();
        ContactTypeEntity contactTypeEntity = new ContactTypeEntity();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(contactMapper.toSource(any())).thenReturn(contactEntity);
        when(contactTypeMapper.toSource(any())).thenReturn(contactTypeEntity);
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);
        when(userMapper.toTarget(existingUser)).thenReturn(updateData);

        ResponseEntity<UserTO> response = userService.update(userId, updateData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(contactMapper, times(2)).toSource(any());
    }

    @Test
    void testUpdate_WithContactNoContactType() {
        UUID userId = UUID.randomUUID();
        UserEntity existingUser = new UserEntity();
        existingUser.setId(userId);

        PersonEntity personEntity = new PersonEntity();
        existingUser.setPerson(personEntity);

        UserTO updateData = new UserTO();
        var person = new com.umdc.commons.general.pojo.Person();

        var contact = new com.umdc.commons.general.pojo.Contact();
        contact.setContent("test@example.com");
        contact.setContactType(null); // No contact type

        person.setContacts(List.of(contact));
        updateData.setPerson(person);

        ContactEntity contactEntity = new ContactEntity();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(contactMapper.toSource(any())).thenReturn(contactEntity);
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);
        when(userMapper.toTarget(existingUser)).thenReturn(updateData);

        ResponseEntity<UserTO> response = userService.update(userId, updateData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testDeleteUserByApplicationAndUserId_NullApplicationId() {
        UUID userId = UUID.randomUUID();
        ResponseEntity<Void> response = userService.deleteUserByApplicationAndUserId(null, userId);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testDeleteUserByApplicationAndUserId_NullUserId() {
        UUID appId = UUID.randomUUID();
        ResponseEntity<Void> response = userService.deleteUserByApplicationAndUserId(appId, null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void unlink_nullUserId_returnsBadRequest() {
        UUID roleId = UUID.randomUUID();
        ResponseEntity<UserTO> response = userService.unlink(null, roleId);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void unlink_nullRoleId_returnsBadRequest() {
        UUID userId = UUID.randomUUID();
        ResponseEntity<UserTO> response = userService.unlink(userId, null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void unlink_userNotFound_returnsNotFound() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseEntity<UserTO> response = userService.unlink(userId, roleId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void unlink_roleNotFoundOnUser_returnsNotFound() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        // User exists but has no matching role in applicationRoleUser
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setApplicationRoleUser(new HashSet<>());

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        ResponseEntity<UserTO> response = userService.unlink(userId, roleId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void unlink_success_returns200WithUpdatedUser() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        ApplicationRoleUserEntityId aruId = new ApplicationRoleUserEntityId();
        aruId.setRoleId(roleId);

        ApplicationRoleUserEntity aru = new ApplicationRoleUserEntity();
        aru.setId(aruId);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setApplicationRoleUser(new HashSet<>(Set.of(aru)));

        UserTO expectedUserTO = new UserTO();
        expectedUserTO.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(userMapper.toTarget(userEntity)).thenReturn(expectedUserTO);

        ResponseEntity<UserTO> response = userService.unlink(userId, roleId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedUserTO, response.getBody());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

}
