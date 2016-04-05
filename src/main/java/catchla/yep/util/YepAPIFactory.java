package catchla.yep.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;

import org.mariotaku.restfu.ExceptionFactory;
import org.mariotaku.restfu.RestAPIFactory;
import org.mariotaku.restfu.RestRequest;
import org.mariotaku.restfu.http.Authorization;
import org.mariotaku.restfu.http.Endpoint;
import org.mariotaku.restfu.http.HttpRequest;
import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.restfu.http.SimpleValueMap;
import org.mariotaku.restfu.okhttp3.OkHttpRestClient;

import java.util.Locale;

import catchla.yep.BuildConfig;
import catchla.yep.Constants;
import catchla.yep.model.YepException;
import okhttp3.OkHttpClient;

/**
 * Created by mariotaku on 15/5/23.
 */
public class YepAPIFactory implements Constants {

    public static final String API_DOMAIN = "api.soyep.com";

    public static YepAPI getInstance(Context context, Account account) {
        if (account == null) return null;
        return getInstanceWithToken(context, getAuthToken(context, account));
    }

    public static YepAPI getInstanceWithToken(final Context context, final String accessToken) {
        RestAPIFactory<YepException> factory = new RestAPIFactory<>();
        final OkHttpClient client = getOkHttpClient(context);
        factory.setHttpClient(new OkHttpRestClient(client));
        factory.setEndpoint(new Endpoint(BuildConfig.API_ENDPOINT_REST));
        factory.setRestConverterFactory(new YepConverterFactory());
        factory.setExceptionFactory(new ExceptionFactory<YepException>() {
            @Override
            public YepException newException(final Throwable cause, final HttpRequest request, final HttpResponse response) {
                YepException exception;
                if (cause != null) {
                    exception = new YepException(cause);
                } else {
                    exception = new YepException();
                }
                exception.setRequest(request);
                exception.setResponse(response);
                return exception;
            }
        });
        factory.setAuthorization(new Authorization() {
            @Override
            public String getHeader(final Endpoint endpoint, final RestRequest info) {
                return getAuthorizationHeaderValue(accessToken);
            }

            @Override
            public boolean hasAuthorization() {
                return accessToken != null;
            }
        });
        SimpleValueMap constantPool = new SimpleValueMap();
        constantPool.put("accept_language", Locale.getDefault().toString());
        factory.setConstantPool(constantPool);

        return factory.build(YepAPI.class);
    }

    @SuppressLint("SSLCertificateSocketFactoryGetInsecure")
    public static OkHttpClient getOkHttpClient(Context context) {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
        DebugModeUtils.initForHttpClient(builder);
        return builder.build();
    }

    public static String getProviderOAuthUrl(final String providerName) {
        return "https://" + API_DOMAIN + "/users/auth/" + providerName;
    }

    public static String getAuthToken(final Context context, final Account account) {
        if (account == null) return null;
        final AccountManager am = AccountManager.get(context);
        return am.peekAuthToken(account, AUTH_TOKEN_TYPE);
    }

    public static boolean isAPIUrl(final Uri uri) {
        return API_DOMAIN.equalsIgnoreCase(uri.getHost());
    }

    public static boolean isAuthSuccessUrl(final String url) {
        return "yep://auth/success".equals(url);
    }

    public static boolean isAuthFailureUrl(final String url) {
        return "yep://auth/failure".equals(url);
    }

    public static String getAuthorizationHeaderValue(String accessToken) {
        return String.format(Locale.ROOT, "Token token=\"%s\"", accessToken);
    }

}