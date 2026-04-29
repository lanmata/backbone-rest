package com.umdc.backoffice.v1.profileimage.service;

import com.umdc.backoffice.util.JwtUtil;
import com.umdc.backoffice.v1.profileimage.client.CloudflareR2StorageClient;
import com.umdc.backoffice.v1.profileimage.to.GetProfileImageReferenceResponse;
import com.umdc.backoffice.v1.profileimage.to.PostProfileImageResponse;
import com.prx.persistence.general.domains.*;
import com.prx.persistence.general.repositories.ApplicationRoleUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

class ProfileImageServiceImplTest {

    @Mock
    private ApplicationRoleUserRepository applicationRoleUserRepository;

    @Mock
    private CloudflareR2StorageClient r2StorageClient;

    @InjectMocks
    private ProfileImageServiceImpl profileImageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSave_Success() throws Exception {

        try(MockedStatic<JwtUtil> mockedStatic = Mockito.mockStatic(JwtUtil.class)) {
            // Arrange
            String token = "valid-token";
            UUID applicationId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            byte[] image = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}; // JPEG signature

            ApplicationEntity application = new ApplicationEntity();
            application.setCodeName("app-code");
            application.setId(applicationId);
            
            RoleEntity roleEntity = new RoleEntity();
            roleEntity.setActive(true);
            roleEntity.setDescription("role-description");
            roleEntity.setName("role-name");
            roleEntity.setId(UUID.randomUUID());

            UserEntity userEntity = new UserEntity();
            userEntity.setId(userId);
            userEntity.setAlias("alias");
            userEntity.setPassword("password");
            userEntity.setEmail("email@test.com");
            
            ApplicationRoleUserEntity applicationRoleUser = new ApplicationRoleUserEntity();
            ApplicationRoleUserEntityId  applicationRoleUserEntityId = new ApplicationRoleUserEntityId();
            applicationRoleUserEntityId.setApplicationId(applicationId);
            applicationRoleUserEntityId.setRoleId(roleEntity.getId());
            applicationRoleUserEntityId.setUserId(userId);
            applicationRoleUser.setId(applicationRoleUserEntityId);
            applicationRoleUser.setApplication(application);
            applicationRoleUser.setRole(roleEntity);
            applicationRoleUser.setUser(userEntity);
            
            mockedStatic.when(() -> JwtUtil.getUidFromToken(anyString())).thenReturn(userId);
            when(applicationRoleUserRepository.findByUserAndApplication(userId, applicationId))
                    .thenReturn(applicationRoleUser);
            when(r2StorageClient.uploadImage(any(byte[].class), anyString(), anyString()))
                    .thenReturn("images/app-code/test.jpg");
            when(r2StorageClient.getPublicUrl(anyString()))
                    .thenReturn("https://cdn.example.com/images/app-code/test.jpg");
            when(applicationRoleUserRepository.save(any(ApplicationRoleUserEntity.class)))
                    .thenReturn(applicationRoleUser);

            // Act
            ResponseEntity<PostProfileImageResponse> response = profileImageService.save(token, applicationId, image);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().ref().contains("cdn.example.com"));
            verify(r2StorageClient, times(1)).uploadImage(any(byte[].class), anyString(), eq("image/jpeg"));
            verify(applicationRoleUserRepository, times(1)).save(any(ApplicationRoleUserEntity.class));
        }
    }

    @Test
    void testSave_InvalidToken() throws Exception {
        // Arrange
        String token = "invalid-token";
        UUID applicationId = UUID.randomUUID();
        byte[] image = new byte[]{1, 2, 3};

        try(MockedStatic<JwtUtil> mockedStatic = Mockito.mockStatic(JwtUtil.class)) {
            mockedStatic.when(() -> JwtUtil.getUidFromToken(anyString())).thenReturn(null);

            // Act
            ResponseEntity<PostProfileImageResponse> response = profileImageService.save(token, applicationId, image);

            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("", response.getBody().ref());
        }
    }

    @Test
    void testSave_NotFound() throws Exception {
        // Arrange
        String token = "valid-token";
        UUID applicationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        byte[] image = new byte[]{1, 2, 3};

        try(MockedStatic<JwtUtil> mockedStatic = Mockito.mockStatic(JwtUtil.class)) {
            mockedStatic.when(() -> JwtUtil.getUidFromToken(anyString())).thenReturn(userId);
            when(applicationRoleUserRepository.findByUserAndApplication(userId, applicationId))
                    .thenReturn(null);

            // Act
            ResponseEntity<PostProfileImageResponse> response = profileImageService.save(token, applicationId, image);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("", response.getBody().ref());
        }
    }

    @Test
    void testSave_R2UploadFailure() throws Exception {
        try(MockedStatic<JwtUtil> mockedStatic = Mockito.mockStatic(JwtUtil.class)) {
            // Arrange
            String token = "valid-token";
            UUID applicationId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            byte[] image = new byte[]{1, 2, 3};

            ApplicationEntity application = new ApplicationEntity();
            application.setCodeName("app-code");
            application.setId(applicationId);
            
            RoleEntity roleEntity = new RoleEntity();
            roleEntity.setId(UUID.randomUUID());
            UserEntity userEntity = new UserEntity();
            userEntity.setId(userId);

            ApplicationRoleUserEntity applicationRoleUser = new ApplicationRoleUserEntity();
            ApplicationRoleUserEntityId  applicationRoleUserEntityId = new ApplicationRoleUserEntityId();
            applicationRoleUserEntityId.setApplicationId(applicationId);
            applicationRoleUserEntityId.setRoleId(roleEntity.getId());
            applicationRoleUserEntityId.setUserId(userId);
            applicationRoleUser.setId(applicationRoleUserEntityId);
            applicationRoleUser.setApplication(application);
            applicationRoleUser.setRole(roleEntity);
            applicationRoleUser.setUser(userEntity);

            mockedStatic.when(() -> JwtUtil.getUidFromToken(anyString())).thenReturn(userId);
            when(applicationRoleUserRepository.findByUserAndApplication(userId, applicationId))
                    .thenReturn(applicationRoleUser);
            when(r2StorageClient.uploadImage(any(byte[].class), anyString(), anyString()))
                    .thenThrow(new RuntimeException("R2 upload failed"));

            // Act
            ResponseEntity<PostProfileImageResponse> response = profileImageService.save(token, applicationId, image);

            // Assert
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("", response.getBody().ref());
        }
    }

    @Test
    void testGetProfileImageReference_Success() throws Exception {
        // Arrange
        String token = "valid-token";
        UUID applicationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String profileImageRef = "images/app-code/image.jpg";

        ApplicationEntity application = new ApplicationEntity();
        application.setCodeName("app-code");
        
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setActive(true);
        roleEntity.setDescription("role-description");
        roleEntity.setName("role-name");
        roleEntity.setId(UUID.randomUUID());

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setAlias("alias");
        userEntity.setPassword("password");
        userEntity.setEmail("email@test.com");

        ApplicationRoleUserEntity applicationRoleUser = new ApplicationRoleUserEntity();
        ApplicationRoleUserEntityId  applicationRoleUserEntityId = new ApplicationRoleUserEntityId();
        applicationRoleUserEntityId.setApplicationId(applicationId);
        applicationRoleUserEntityId.setRoleId(roleEntity.getId());
        applicationRoleUserEntityId.setUserId(userId);
        applicationRoleUser.setId(applicationRoleUserEntityId);
        applicationRoleUser.setApplication(application);
        applicationRoleUser.setRole(roleEntity);
        applicationRoleUser.setUser(userEntity);
        applicationRoleUser.setProfileImageRef(profileImageRef);

        try(MockedStatic<JwtUtil> mockedStatic = Mockito.mockStatic(JwtUtil.class)) {
            mockedStatic.when(() -> JwtUtil.getUidFromToken(anyString())).thenReturn(userId);
            when(applicationRoleUserRepository.findByUserAndApplication(userId, applicationId))
                    .thenReturn(applicationRoleUser);
            when(r2StorageClient.getPublicUrl(profileImageRef))
                    .thenReturn("https://cdn.example.com/" + profileImageRef);

            // Act
            ResponseEntity<GetProfileImageReferenceResponse> response = 
                    profileImageService.getProfileImageReference(token, applicationId);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().ref().contains(profileImageRef));
        }
    }

    @Test
    void testGetProfileImageReference_InvalidToken() throws Exception {
        // Arrange
        String token = "invalid-token";
        UUID applicationId = UUID.randomUUID();

        try(MockedStatic<JwtUtil> mockedStatic = Mockito.mockStatic(JwtUtil.class)) {
            mockedStatic.when(() -> JwtUtil.getUidFromToken(anyString())).thenReturn(null);

            // Act
            ResponseEntity<GetProfileImageReferenceResponse> response = 
                    profileImageService.getProfileImageReference(token, applicationId);

            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertNull(response.getBody());
        }
    }

    @Test
    void testGetProfileImageReference_NotFound() throws Exception {
        // Arrange
        String token = "valid-token";
        UUID applicationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        try(MockedStatic<JwtUtil> mockedStatic = Mockito.mockStatic(JwtUtil.class)) {
            mockedStatic.when(() -> JwtUtil.getUidFromToken(anyString())).thenReturn(userId);
            when(applicationRoleUserRepository.findByUserAndApplication(userId, applicationId))
                    .thenReturn(null);
            
            // Act
            ResponseEntity<GetProfileImageReferenceResponse> response = 
                    profileImageService.getProfileImageReference(token, applicationId);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNull(response.getBody());
        }
    }

    @Test
    void testGetProfileImageReference_NoImageRef() throws Exception {
        // Arrange
        String token = "valid-token";
        UUID applicationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ApplicationEntity application = new ApplicationEntity();
        application.setCodeName("app-code");
        
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(UUID.randomUUID());
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);

        ApplicationRoleUserEntity applicationRoleUser = new ApplicationRoleUserEntity();
        ApplicationRoleUserEntityId  applicationRoleUserEntityId = new ApplicationRoleUserEntityId();
        applicationRoleUserEntityId.setApplicationId(applicationId);
        applicationRoleUserEntityId.setRoleId(roleEntity.getId());
        applicationRoleUserEntityId.setUserId(userId);
        applicationRoleUser.setId(applicationRoleUserEntityId);
        applicationRoleUser.setApplication(application);
        applicationRoleUser.setRole(roleEntity);
        applicationRoleUser.setUser(userEntity);
        applicationRoleUser.setProfileImageRef(null); // No image ref

        try(MockedStatic<JwtUtil> mockedStatic = Mockito.mockStatic(JwtUtil.class)) {
            mockedStatic.when(() -> JwtUtil.getUidFromToken(anyString())).thenReturn(userId);
            when(applicationRoleUserRepository.findByUserAndApplication(userId, applicationId))
                    .thenReturn(applicationRoleUser);

            // Act
            ResponseEntity<GetProfileImageReferenceResponse> response = 
                    profileImageService.getProfileImageReference(token, applicationId);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNull(response.getBody());
        }
    }
}

