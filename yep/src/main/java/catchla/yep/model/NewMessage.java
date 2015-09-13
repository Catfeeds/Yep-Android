package catchla.yep.model;

import android.content.ContentValues;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.restfu.http.ContentType;
import org.mariotaku.restfu.http.SimpleValueMap;
import org.mariotaku.restfu.http.mime.StringTypedData;
import org.mariotaku.restfu.http.mime.TypedData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import catchla.yep.model.util.ValueMapJsonMapper;
import catchla.yep.provider.YepDataStore.Messages;
import catchla.yep.util.JsonSerializer;
import catchla.yep.util.ParseUtils;

/**
 * Created by mariotaku on 15/6/12.
 */
public class NewMessage extends SimpleValueMap {

    private static final ValueMapJsonMapper<NewMessage> sMapper = new ValueMapJsonMapper<>();

    private String conversationId;
    private long createdAt;
    private Circle circle;
    private User sender;
    private LocalMetadata[] localMetadata;

    public NewMessage recipientId(String recipientId) {
        put("recipient_id", recipientId);
        return this;
    }

    public NewMessage recipientType(String recipientType) {
        put("recipient_type", recipientType);
        return this;
    }

    public NewMessage mediaType(String mediaType) {
        put("media_type", mediaType);
        return this;
    }

    public NewMessage textContent(String textContent) {
        put("text_content", textContent);
        return this;
    }

    public NewMessage conversationId(final String conversationId) {
        this.conversationId = conversationId;
        return this;
    }

    public NewMessage createdAt(final long createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public NewMessage parentId(String parentId) {
        put("parent_id", parentId);
        return this;
    }

    public NewMessage location(double latitude, double longitude) {
        put("latitude", latitude);
        put("longitude", longitude);
        return this;
    }

    public String conversationId() {
        return conversationId;
    }

    public long createdAt() {
        return createdAt;
    }

    public String parentId() {
        return ParseUtils.parseString(get("parent_id"), null);
    }

    public String recipientId() {
        return ParseUtils.parseString(get("recipient_id"), null);
    }

    public String recipientType() {
        return ParseUtils.parseString(get("recipient_type"), null);
    }

    public void circle(final Circle circle) {
        this.circle = circle;
    }

    public Circle circle() {
        return circle;
    }

    public void sender(final User sender) {
        this.sender = sender;
    }

    public User sender() {
        return sender;
    }

    public String textContent() {
        return ParseUtils.parseString(get("text_content"), null);
    }

    public <T extends Attachment> void attachment(final T attachment) {
        if (attachment == null) return;
        //noinspection unchecked
        put("attachments", attachment);
    }

    public String mediaType() {
        return ParseUtils.parseString(get("media_type"));
    }

    public double latitude() {
        return ParseUtils.parseDouble(ParseUtils.parseString(get("latitude")), Double.NaN);
    }

    public double longitude() {
        return ParseUtils.parseDouble(ParseUtils.parseString(get("longitude")), Double.NaN);
    }

    public JsonBody toJson() {
        try {
            final String json = sMapper.serialize(this);
            return new JsonBody(json);
        } catch (IOException e) {
            return null;
        }
    }

    public NewMessage localMetadata(final LocalMetadata[] localMetadata) {
        this.localMetadata = localMetadata;
        return this;
    }

    public ContentValues toDraftValues() {
        final ContentValues values = new ContentValues();
        values.put(Messages.RECIPIENT_ID, recipientId());
        values.put(Messages.TEXT_CONTENT, textContent());
        values.put(Messages.CREATED_AT, createdAt);
        values.put(Messages.SENDER, JsonSerializer.serialize(sender, User.class));
        values.put(Messages.RECIPIENT_TYPE, recipientType());
        values.put(Messages.CIRCLE, JsonSerializer.serialize(circle, Circle.class));
        values.put(Messages.PARENT_ID, parentId());
        values.put(Messages.CONVERSATION_ID, conversationId);
        values.put(Messages.STATE, Messages.MessageState.SENDING);
        values.put(Messages.OUTGOING, true);
        values.put(Messages.LATITUDE, latitude());
        values.put(Messages.LONGITUDE, longitude());
        values.put(Messages.MEDIA_TYPE, mediaType());
        values.put(Messages.LOCAL_METADATA, JsonSerializer.serializeArray(localMetadata, LocalMetadata.class));
        return values;
    }

    public interface Attachment {

        @JsonObject
        class File {
            @JsonField(name = "file")
            String file;
            @JsonField(name = "metadata")
            String metadata;

            public File() {
            }

            public File(final String file, final Object metadata) {
                setFile(file);
                setMetadata(metadata);
            }

            public void setFile(final String file) {
                this.file = file;
            }

            public void setMetadata(final Object metadata) {
                this.metadata = JsonSerializer.serialize(metadata);
            }
        }
    }

    @JsonObject
    public static class LocalMetadata {
        String name;
        String value;

        public LocalMetadata() {
        }

        public LocalMetadata(final String name, final String value) {
            this.name = name;
            this.value = value;
        }
    }

    public static final class JsonBody implements TypedData {

        private final StringTypedData delegated;

        private JsonBody(String json) {
            delegated = new StringTypedData(json,
                    ContentType.parse("application/json").charset(Charset.defaultCharset()));
        }

        @Override
        @Nullable
        public ContentType contentType() {
            return delegated.contentType();
        }

        @Override
        public String contentEncoding() {
            return delegated.contentEncoding();
        }

        @Override
        public long length() throws IOException {
            return delegated.length();
        }

        @Override
        public long writeTo(@NonNull final OutputStream os) throws IOException {
            return delegated.writeTo(os);
        }

        @Override
        @NonNull
        public InputStream stream() throws IOException {
            return delegated.stream();
        }

        @Override
        public void close() throws IOException {
            delegated.close();
        }

    }

    @JsonObject
    public static class ImageAttachment implements Attachment {

        @JsonField(name = "image")
        File[] image;

        public ImageAttachment() {

        }

        public ImageAttachment(S3UploadToken token, Message.Attachment.ImageMetadata metadata) {
            image = new File[]{new File(token.getOptions().getKey(), metadata)};
        }

    }@JsonObject
    public static class AudioAttachment implements Attachment {

        @JsonField(name = "image")
        File[] image;

        public AudioAttachment() {

        }

        public AudioAttachment(S3UploadToken token, Message.Attachment.AudioMetadata metadata) {
            image = new File[]{new File(token.getOptions().getKey(), metadata)};
        }

    }
}

