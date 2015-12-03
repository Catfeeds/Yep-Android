package catchla.yep.model.util;

import android.content.ContentValues;
import android.database.Cursor;

import org.mariotaku.library.objectcursor.converter.CursorFieldConverter;

import java.lang.reflect.ParameterizedType;
import java.util.Date;

/**
 * Created by mariotaku on 15/12/3.
 */
public class TimestampToDateConverter implements CursorFieldConverter<Date> {
    @Override
    public Date parseField(final Cursor cursor, final int columnIndex, final ParameterizedType fieldType) {
        return new Date(cursor.getLong(columnIndex));
    }

    @Override
    public void writeField(final ContentValues values, final Date object, final String columnName, final ParameterizedType fieldType) {
        values.put(columnName, object.getTime());
    }
}
