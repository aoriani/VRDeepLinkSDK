/**
 * Copyright (c) 2016-present, Facebook, Inc. All rights reserved.
 * <p>
 * You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
 * copy, modify, and distribute this software in source code or binary form for use
 * in connection with the web services and APIs provided by Facebook.
 * <p>
 * As with any software that integrates with the Facebook platform, your use of
 * this software is subject to the Facebook Developer Principles and Policies
 * [http://developers.facebook.com/policy/]. This copyright notice shall be
 * included in all copies or substantial portions of the software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.facebook.vrdeeplinksdk;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

public class VRDeepLinkHelper {
    private static final String OCULUS_CINEMA_PACKAGE_NAME = "com.oculus.cinema";
    private static final String OCULUS_PHOTO_PACKAGE_NAME = "com.oculus.oculus360photos";

    private static final String URI_SCHEME_OCULUS = "oculus";
    private static final String URI_MEDIA_TYPE_VIDEO = "video";
    private static final String URI_MEDIA_TYPE_PHOTO = "photo";
    private static final String URI_MEDIA_SOURCE_FB = "fb";

    private static final String URI_SCHEME_HTTPS = "https";
    private static final String URI_FB_AUTHORITY = "m.facebook.com";

    public enum MediaType {
        VIDEO(OCULUS_CINEMA_PACKAGE_NAME, URI_MEDIA_TYPE_VIDEO),
        PHOTO(OCULUS_PHOTO_PACKAGE_NAME, URI_MEDIA_TYPE_PHOTO);

        private final String mOculusPackage;
        private final String mMediaPath;

        MediaType(@NonNull String oculusPackage, @NonNull String mediaPath) {
            mOculusPackage = oculusPackage;
            mMediaPath = mediaPath;
        }

        @NonNull
        String getOculusPackage() {
            return mOculusPackage;
        }

        @NonNull
        String getMediaPath() {
            return mMediaPath;
        }
    }

    /**
     * Helper function to create an intent to launch 360 photos/videos in Oculus Video/360Photo app.
     * If such app doesn't exist, we'll fallback to FB4A or m-site depending on application availability.
     *
     * @param context an android context
     * @param mediaFbId the fbid of the deeplink media
     * @param mediaType the media type
     * @return an Intent object to launch the deeplinked content.
     */
    @Nullable
    public static Intent createDeepLinkIntent(@NonNull Context context, @NonNull String mediaFbId, @NonNull MediaType mediaType) {
        if (TextUtils.isEmpty(mediaFbId)) {
            return null;
        }

        if (hasAppInstalled(context, mediaType.getOculusPackage())) {
            return createIntentForOculusApp(mediaFbId, mediaType);
        } else {
            return createIntentForFacebookApp(mediaFbId);
        }
    }

    /**
     * Checks whether an application is installed.
     *
     * @param context an android context
     * @param packageName the android package name of the application.
     * @return whether an app with the given package name is installed
     */
    @VisibleForTesting
    static boolean hasAppInstalled(@NonNull Context context, @NonNull String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @VisibleForTesting
    static Intent createIntentForOculusApp(@NonNull String mediaFbId, @NonNull MediaType mediaType) {
        final String packageName = mediaType.getOculusPackage();
        final String mediaTypePath = mediaType.getMediaPath();
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        final Uri.Builder builder = new Uri.Builder();
        builder.scheme(URI_SCHEME_OCULUS)
                .authority(mediaTypePath)
                .appendPath(URI_MEDIA_SOURCE_FB)
                .appendPath(mediaFbId);
        intent.setPackage(packageName);
        intent.setData(builder.build());
        return intent;
    }

    @VisibleForTesting
    static Intent createIntentForFacebookApp(@NonNull String mediaFbId) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        final Uri.Builder builder = new Uri.Builder();
        builder.scheme(URI_SCHEME_HTTPS)
                .authority(URI_FB_AUTHORITY)
                .appendPath(mediaFbId);
        intent.setData(builder.build());
        return intent;
    }

}
