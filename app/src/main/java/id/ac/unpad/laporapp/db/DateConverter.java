package id.ac.unpad.laporapp.db;

import androidx.room.TypeConverter;

import java.util.Date;

//Mengkonversi Date java ke timestamp SQlite dan sebaliknya
public class DateConverter {
    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

}
