/*
 *  @(#)GetProfileImageReferenceResponse.java
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
 * GetProfileImageReferenceResponse represents the response containing the profile image reference.
 *
 * @param ref The reference to the profile image.
 */
public record GetProfileImageReferenceResponse(String ref) {
}
