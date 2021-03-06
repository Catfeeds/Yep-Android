package catchla.yep.model;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.mariotaku.library.objectcursor.annotation.AfterCursorObjectCreated;
import org.mariotaku.library.objectcursor.annotation.BeforeWriteContentValues;
import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.sqliteqb.library.Expression;

import java.util.Date;
import java.util.Locale;

import catchla.yep.model.util.LoganSquareCursorFieldConverter;
import catchla.yep.model.util.TimestampToDateConverter;
import catchla.yep.model.util.YepTimestampDateConverter;
import catchla.yep.provider.YepDataStore;
import catchla.yep.provider.YepDataStore.Conversations;
import catchla.yep.util.Utils;

/**
 * Created by mariotaku on 15/5/29.
 */
@ParcelablePlease
@JsonObject
@CursorObject(valuesCreator = true, tableInfo = true)
public class Conversation implements Parcelable {

    public static final Creator<Conversation> CREATOR = new Creator<Conversation>() {
        @Override
        public Conversation createFromParcel(Parcel in) {
            return new Conversation(in);
        }

        @Override
        public Conversation[] newArray(int size) {
            return new Conversation[size];
        }
    };

    @CursorField(value = YepDataStore.Circles._ID, type = YepDataStore.TYPE_PRIMARY_KEY, excludeWrite = true)
    long _id;

    @ParcelableThisPlease
    @JsonField(name = "media_type")
    @CursorField(Conversations.MEDIA_TYPE)
    String mediaType;
    /**
     * Corresponding to {@link Message#getSender()}
     */
    @ParcelableThisPlease
    @JsonField(name = "user")
    @CursorField(value = Conversations.USER, converter = LoganSquareCursorFieldConverter.class)
    User user;
    @ParcelableThisPlease
    @JsonField(name = "account_id")
    @CursorField(Conversations.ACCOUNT_ID)
    String accountId;
    @ParcelableThisPlease
    @JsonField(name = "sender")
    @CursorField(value = Conversations.SENDER, converter = LoganSquareCursorFieldConverter.class)
    User sender;
    @ParcelableThisPlease
    @JsonField(name = "circle")
    @CursorField(value = Conversations.CIRCLE, converter = LoganSquareCursorFieldConverter.class)
    Circle circle;
    /**
     * Corresponding to {@link Message#getRecipientType()}
     */
    @ParcelableThisPlease
    @JsonField(name = "recipient_type")
    @CursorField(Conversations.RECIPIENT_TYPE)
    String recipientType;
    /**
     * Corresponding to {@link Message#getTextContent()}
     */
    @ParcelableThisPlease
    @JsonField(name = "text_content")
    @CursorField(Conversations.TEXT_CONTENT)
    String textContent;
    @ParcelableThisPlease
    @JsonField(name = "title")
    @CursorField(Conversations.TITLE)
    String title;
    @ParcelableThisPlease
    @JsonField(name = "id")
    @CursorField(Conversations.CONVERSATION_ID)
    String id;
    @ParcelableThisPlease
    @JsonField(name = "updated_at", typeConverter = YepTimestampDateConverter.class)
    @CursorField(value = Conversations.UPDATED_AT, converter = TimestampToDateConverter.class)
    @Nullable
    Date updatedAt;
    @ParcelableThisPlease
    @JsonField(name = "last_seen_at", typeConverter = YepTimestampDateConverter.class)
    @CursorField(value = Conversations.LAST_SEEN_AT, converter = TimestampToDateConverter.class)
    @Nullable
    Date lastSeenAt;
    @ParcelableThisPlease
    @JsonField(name = "last_read_at", typeConverter = YepTimestampDateConverter.class)
    @CursorField(value = Conversations.LAST_READ_AT, converter = TimestampToDateConverter.class)
    @Nullable
    Date lastReadAt;

    protected Conversation(Parcel in) {
        ConversationParcelablePlease.readFromParcel(this, in);
    }

    public Conversation() {

    }

    public static Conversation fromUser(User user, String accountId) {
        final Conversation conversation = new Conversation();
        conversation.setAccountId(accountId);
        conversation.setId(generateId(Message.RecipientType.USER, user.getId()));
        conversation.setRecipientType(Message.RecipientType.USER);
        conversation.setUser(user);
        return conversation;
    }


    public static Conversation fromMessage(Message message, String accountId) {
        final Conversation conversation = new Conversation();
        conversation.setAccountId(accountId);
        conversation.setId(generateId(message, accountId));
        conversation.setRecipientType(message.recipientType);
        return conversation;
    }

    public static String generateId(Message message, String accountId) {
        final String recipientType = message.getRecipientType();
        if (Message.RecipientType.CIRCLE.equalsIgnoreCase(recipientType)) {
            return generateId(recipientType, message.getRecipientId());
        } else if (Message.RecipientType.USER.equalsIgnoreCase(recipientType)) {
            final String senderId = message.getSender().getId();
            if (TextUtils.equals(accountId, senderId)) {
                // This is an outgoing message
                return generateId(recipientType, message.getRecipientId());
            }
            return generateId(recipientType, senderId);
        }
        throw new UnsupportedOperationException();
    }

    public static String generateId(final String recipientType, final String id) {
        return recipientType.toLowerCase(Locale.US) + ":" + id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(final User sender) {
        this.sender = sender;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(final String mediaType) {
        this.mediaType = mediaType;
    }

    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public String getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(final String recipientType) {
        this.recipientType = recipientType;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(final String textContent) {
        this.textContent = textContent;
    }

    public Circle getCircle() {
        return circle;
    }

    public void setCircle(final Circle circle) {
        this.circle = circle;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getRecipientId() {
        if (Message.RecipientType.CIRCLE.equalsIgnoreCase(recipientType)) {
            return circle.getId();
        } else if (Message.RecipientType.USER.equalsIgnoreCase(recipientType)) {
            return user.getId();
        }
        throw new UnsupportedOperationException();
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(final String accountId) {
        this.accountId = accountId;
    }

    @Nullable
    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(@Nullable final Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Nullable
    public Date getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(@Nullable final Date lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    @Nullable
    public Date getLastReadAt() {
        return lastReadAt;
    }

    public void setLastReadAt(@Nullable final Date lastReadAt) {
        this.lastReadAt = lastReadAt;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        ConversationParcelablePlease.writeToParcel(this, dest, flags);
    }

    public boolean isValid() {
        if (id == null) return false;
        if (Message.RecipientType.CIRCLE.equalsIgnoreCase(recipientType)) return circle != null;
        if (Message.RecipientType.USER.equalsIgnoreCase(recipientType)) return user != null;
        return true;
    }

    @AfterCursorObjectCreated
    void afterCursorObjectCreated() {
        if (title == null) {
            setTitle(Utils.INSTANCE.getConversationName(this));
        }
    }

    @OnJsonParseComplete
    void onJsonParseComplete() {
        if (title == null) {
            setTitle(Utils.INSTANCE.getConversationName(this));
        }
    }

    @BeforeWriteContentValues
    void beforeCreateValues(ContentValues values) {
        if (title == null) {
            setTitle(Utils.INSTANCE.getConversationName(this));
        }
    }

    public static Conversation query(final ContentResolver cr, final String accountId, final String conversationId) {
        final String selection = Expression.and(Expression.equalsArgs(Conversations.ACCOUNT_ID),
                Expression.equalsArgs(Conversations.CONVERSATION_ID)).getSQL();
        final String[] selectionArgs = {accountId, conversationId};
        Cursor cursor = cr.query(Conversations.CONTENT_URI, ConversationTableInfo.COLUMNS, selection, selectionArgs, null);
        if (cursor == null) return null;
        try {
            if (cursor.moveToFirst()) {
                return new ConversationCursorIndices(cursor).newObject(cursor);
            }
        } finally {
            cursor.close();
        }
        return null;
    }
}
