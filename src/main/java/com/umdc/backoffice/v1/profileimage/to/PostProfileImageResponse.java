/*
 *  @(#)PostProfileImageResponse.java
 *
 *  Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *
 *   All rights to this product are owned by Luis Antonio Mata Mata and may only
 *  be used under the terms of its associated license document. You may NOT
 *  copy, modify, sublicense, or distribute this source file or portions of
 *  it unless previously authorized in writing by Luis Antonio Mata Mata.
 *  In any event, this notice and the above copyright must always be included
 *  verbatim with this file.
 */

package com.umdc.backoffice.v1.profileimage.to;

/**
 * Represents the response for posting a profile image.
 *
 * This response includes a reference string that provides
 * additional information or an identifier related to the
 * uploaded profile image.
 *
 * The {@code ref} field is typically used to store a reference
 * to the uploaded image for future retrieval or tracking purposes.
 */
public record PostProfileImageResponse(String ref) {
}
