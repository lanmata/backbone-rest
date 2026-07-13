package com.umdc.backoffice.constant;

public final class BackboneAppConstants {

    public static final String APPLICATION_NAME = "Backbone REST";

    public static final String ENTITY_PACKAGE = "com.umdc.persistence.general.domains";
    public static final String REPOSITORY_PACKAGE = "com.umdc.persistence.general.repositories";

    /**
     * Local entity package for audit infrastructure (backbone-rest-owned JPA entities).
     */
    public static final String BACKBONE_ENTITY_PACKAGE = "com.umdc.backoffice.jpa.domain";

    /**
     * Local repository package for audit infrastructure (backbone-rest-owned Spring Data repos).
     */
    public static final String BACKBONE_REPOSITORY_PACKAGE = "com.umdc.backoffice.jpa.repository";

    public static final String IMAGE_JPEG = "image/jpeg";
    public static final String IMAGE_PNG = "image/png";
    public static final String IMAGE_GIF = "image/gif";
    public static final String IMAGE_WEBP = "image/webp";

    private BackboneAppConstants() { throw new IllegalStateException("Utility class"); }
}
