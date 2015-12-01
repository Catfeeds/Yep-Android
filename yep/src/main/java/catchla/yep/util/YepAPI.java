package catchla.yep.util;

import android.support.annotation.StringDef;

import java.util.ArrayList;

import catchla.yep.model.AccessToken;
import catchla.yep.model.Circle;
import catchla.yep.model.Client;
import catchla.yep.model.ContactUpload;
import catchla.yep.model.CreateRegistrationResult;
import catchla.yep.model.DiscoverQuery;
import catchla.yep.model.DribbbleShots;
import catchla.yep.model.GithubUserInfo;
import catchla.yep.model.InstagramMediaList;
import catchla.yep.model.MarkAsReadResult;
import catchla.yep.model.Message;
import catchla.yep.model.NewMessage;
import catchla.yep.model.NewTopic;
import catchla.yep.model.PagedFriendships;
import catchla.yep.model.PagedMessages;
import catchla.yep.model.PagedSkillCategories;
import catchla.yep.model.PagedTopics;
import catchla.yep.model.PagedUsers;
import catchla.yep.model.Paging;
import catchla.yep.model.ProfileUpdate;
import catchla.yep.model.S3UploadToken;
import catchla.yep.model.Topic;
import catchla.yep.model.UrlResponse;
import catchla.yep.model.User;
import catchla.yep.model.UserSettings;
import catchla.yep.model.VerificationMethod;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.PATCH;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.QueryMap;

/**
 * Created by mariotaku on 15/5/12.
 */
public interface YepAPI {

    @POST("/v1/registration/create")
    @FormUrlEncoded
    CreateRegistrationResult createRegistration(@Field("mobile") String mobile,
                                                @Field("phone_code") String phoneCode,
                                                @Field("nickname") String nickname,
                                                @Field("longitude") double longitude,
                                                @Field("latitude") double latitude) throws YepException;

    @PUT("/v1/registration/update")
    @FormUrlEncoded
    AccessToken updateRegistration(@Field("mobile") String mobile,
                                   @Field("phone_code") String phoneCode,
                                   @Field("token") String token,
                                   @Field("client") Client client,
                                   @Field("expiring") long expiringInseconds) throws YepException;

    @PUT("/v1/auth/token_by_mobile")
    @FormUrlEncoded
    AccessToken tokenByMobile(@Field("mobile") String mobile,
                              @Field("phone_code") String phoneCode,
                              @Field("token") String token,
                              @Field("client") Client client,
                              @Field("expiring") long expiringInseconds) throws YepException;

    @POST("/v1/sms_verification_codes")
    @FormUrlEncoded
    void sendVerifyCode(@Field("mobile") String mobile,
                        @Field("phone_code") String phoneCode,
                        @Field("method") VerificationMethod method) throws YepException;

    @PATCH("/v1/user")
    @FormUrlEncoded
    User updateProfile(@FieldMap ProfileUpdate profileUpdate) throws YepException;

    @GET("/v1/user")
    User getUser() throws YepException;

    @GET("/v1/users/{id}")
    User showUser(@Path("id") String userId) throws YepException;

    @GET("/v1/user/discover")
    PagedUsers getDiscover(@QueryMap DiscoverQuery query, @QueryMap Paging paging) throws YepException;

    @GET("/v1/skill_categories")
    PagedSkillCategories getSkillCategories() throws YepException;

    @GET("/v1/friendships")
    PagedFriendships getFriendships(@QueryMap Paging paging) throws YepException;

    @GET("/v1/messages/unread")
    PagedMessages getUnreadMessages() throws YepException;

    @GET("/v1/{recipient_type}/{recipient_id}/messages")
    PagedMessages getHistoricalMessages(@Path("recipient_type") String recipientType, @Path("recipient_id")
    String recipientId, @QueryMap Paging paging) throws YepException;

    @GET("/v1/messages/sent_unread")
    PagedMessages getSentUnreadMessages(@QueryMap Paging paging) throws YepException;

    @GET("/v1/users/{id}/dribbble")
    DribbbleShots getDribbbleShots(@Path("id") String userId) throws YepException;

    @GET("/v1/users/{id}/github")
    GithubUserInfo getGithubUserInfo(@Path("id") String userId) throws YepException;

    @GET("/v1/users/{id}/instagram")
    InstagramMediaList getInstagramMediaList(@Path("id") String userId) throws YepException;

    @POST("/v1/{recipient_type}/{recipient_id}/messages")
    @Multipart
    Message createMessage(@Path("recipient_type") String recipientType, @Path("recipient_id") String recipientId,
                          @Body NewMessage message) throws YepException;

    @DELETE("/v1/learning_skills/{id}")
    void removeLearningSkill(@Path("id") String id) throws YepException;

    @POST("/v1/learning_skills")
    @FormUrlEncoded
    void addLearningSkill(@Field("skill_id") String id) throws YepException;

    @DELETE("/v1/master_skills/{id}")
    void removeMasterSkill(@Path("id") String id) throws YepException;

    @POST("/v1/master_skills")
    @FormUrlEncoded
    void addMasterSkill(@Field("skill_id") String id) throws YepException;

    @POST("/v1/do_not_disturb_users")
    @FormUrlEncoded
    void addDoNotDisturb(@Field("user_id") String id) throws YepException;

    @POST("/v1/user_reports")
    @FormUrlEncoded
    void reportUser(@Field("recipient_id") String id, @Field("report_type") int reportType, @Field("reason") String reason) throws YepException;

    @DELETE("/v1/do_not_disturb_users/{user_id}")
    void removeDoNotDisturb(@Path("user_id") String id) throws YepException;

    @GET("/v1/attachments/{kind}/s3_upload_form_fields")
    S3UploadToken getS3UploadToken(@Path("kind") String kind) throws YepException;

    @POST("/v1/contacts/upload")
    @FormUrlEncoded
    ArrayList<User> uploadContact(@FieldMap ContactUpload contactUpload) throws YepException;

    @GET("/v1/users/search")
    PagedUsers searchUsers(@Query("q") String query, @QueryMap Paging paging) throws YepException;

    @PATCH("/v1/{recipient_type}/{recipient_id}/messages/batch_mark_as_read")
    @FormUrlEncoded
    MarkAsReadResult batchMarkAsRead(@MarkAsReadRecipientType @Path("recipient_type") String recipientType, @Path("recipient_id") String recipientId,
                                     @Field("max_id") String maxId) throws YepException;

    @GET("/v1/blocked_users")
    PagedUsers getBlockedUsers(@QueryMap Paging paging) throws YepException;

    @POST("/v1/blocked_users")
    void blockUser(@Field("user_id") String id) throws YepException;

    @DELETE("/v1/blocked_users/{id}")
    void unblockUser(@Path("id") String id) throws YepException;

    @GET("/v1/users/{id}/settings_with_current_user")
    UserSettings getUserSettings(@Path("id") String id) throws YepException;

    @GET("/v1/topics/discover")
    PagedTopics getDiscoverTopics(@Query("sort") @Topic.SortOrder String sortOrder, @QueryMap Paging paging) throws YepException;

    @GET("/v1/topics")
    PagedTopics getTopics(@QueryMap Paging paging) throws YepException;

    @GET("/v2/users/{id}/topics")
    PagedTopics getTopics(@Path("id") String userId, @QueryMap Paging paging) throws YepException;

    @POST("/v1/topics")
    @Multipart
    Topic postTopic(@Body NewTopic topic) throws YepException;

    @PUT("/v1/topics/{id}")
    @FormUrlEncoded
    void updateTopic(@Path("id") String id, @Field("allow_comment") boolean allowComment);

    @DELETE("/v1/topics/{id}")
    void deleteTopic(@Path("id") String id);

    @POST("/v1/feedbacks")
    @FormUrlEncoded
    void postFeedback(@Field("content") String content, @Field("device_info") String deviceInfo) throws YepException;

    @POST("/v1/circles/{id}/share")
    UrlResponse getCircleShareUrl(@Path("id") String id) throws YepException;

    @POST("/v1/circles/{id}/join")
    Circle joinCircle(@Path("id") String circleId) throws YepException;

    @DELETE("/v1/circles/{id}/leave")
    Circle leaveCircle(@Path("id") String circleId) throws YepException;

    interface AttachmentKind {
        String MESSAGE = "message";
        String TOPIC = "topic";
        String AVATAR = "avatar";
    }

    @StringDef({MarkAsReadRecipientType.USERS, MarkAsReadRecipientType.CIRCLES})
    @interface MarkAsReadRecipientType {
        String USERS = "users";
        String CIRCLES = "circles";
    }

}

