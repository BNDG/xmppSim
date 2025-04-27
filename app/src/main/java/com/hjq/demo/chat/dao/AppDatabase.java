package com.hjq.demo.chat.dao;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.hjq.demo.chat.entity.Address;
import com.hjq.demo.chat.entity.AreaEntity;
import com.hjq.demo.chat.entity.AvatarEntity;
import com.hjq.demo.chat.entity.ChatMessage;
import com.hjq.demo.chat.entity.ChatRoomEntity;
import com.hjq.demo.chat.entity.ConversationInfo;
import com.hjq.demo.chat.entity.DeviceInfo;
import com.hjq.demo.chat.entity.FriendApply;
import com.hjq.demo.chat.entity.GroupMember;
import com.hjq.demo.chat.entity.RegionEntity;
import com.hjq.demo.chat.entity.User;

@Database(entities = {Address.class, ChatMessage.class, ConversationInfo.class,
        ChatRoomEntity.class, AvatarEntity.class, GroupMember.class, AreaEntity.class,
        RegionEntity.class, User.class, FriendApply.class}, version = 13)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase mAppDatabase;

    // TODO 在实例化 AppDatabase 对象时应遵循单例设计模式。每个 RoomDatabase 实例的成本相当高，几乎不需要在单个进程中访问多个实例。
    public static AppDatabase getInstance(Context context) {
        if (mAppDatabase == null) {
            synchronized (AppDatabase.class) {
                if (mAppDatabase == null) {
                    mAppDatabase = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "ChatMsg.db")
                            .addMigrations()
                            // 默认不允许在主线程中连接数据库
                            .allowMainThreadQueries()
                            .addMigrations(MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12
                                    , MIGRATION_12_13)
                            .build();
                }
            }
        }
        return mAppDatabase;
    }

    private static Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            //执行升级相关操作
            database.execSQL("CREATE TABLE contact (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "userId TEXT," +
                    "unionid TEXT," +
                    "userType TEXT," +
                    "userNickName TEXT," +
                    "userPhone TEXT," +
                    "userAccount TEXT," +
                    "userAvatar TEXT," +
                    "userHeader TEXT," +
                    "userSex TEXT," +
                    "userRegion TEXT," +
                    "userSign TEXT," +
                    "userEmail TEXT," +
                    "userIsEmailLinked TEXT," +
                    "isFriend TEXT," +
                    "subscribeStatus TEXT," +
                    "belongAccount TEXT," +
                    "userContactMobiles TEXT," +
                    "userContactAlias TEXT," +
                    "userContactDesc TEXT," +
                    "userContactPrivacy TEXT," +
                    "userContactHideMyPosts TEXT," +
                    "userContactHideHisPosts TEXT," +
                    "isStarred TEXT," +
                    "isBlocked TEXT" +
                    ");");
        }
    };

    private static Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            //执行升级相关操作
            database.execSQL("CREATE TABLE friendApply (" +
                    "applyId TEXT PRIMARY KEY NOT NULL," +
                    "friendUserId TEXT," +
                    "applyRemark TEXT," +
                    "createTime TEXT," +
                    "friendNickname TEXT," +
                    "friendUserAvatar TEXT," +
                    "friendUserSex TEXT," +
                    "friendUserSign TEXT," +
                    "belongAccount TEXT," +
                    "status TEXT" +
                    ")");
        }
    };
    // 索引变动需要迁移
    private static Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 添加新索引
            database.execSQL("CREATE INDEX IF NOT EXISTS index_chat_message_status ON chat_message(status)");
        }
    };
    // 添加新字段
    private static Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE chat_message ADD COLUMN extraData TEXT");
        }
    };

    private static Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE avatars ADD COLUMN photoHash TEXT");
        }
    };

    // 注意新增字段在实体类中是可空的类型
    private static Migration MIGRATION_11_12 = new Migration(11, 12) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE chat_room ADD COLUMN moderated INTEGER DEFAULT 0");
        }
    };

    private static Migration MIGRATION_12_13 = new Migration(12, 13) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE chat_message ADD COLUMN isRead INTEGER NOT NULL DEFAULT 0");
        }
    };


    public abstract AddressDao addressDao();

    public abstract ChatMessageDao messageDao();

    public abstract ChatConversationDao chatConversationDao();

    public abstract ChatRoomDao chatRoomDao();

    public abstract AvatarDao avatarDao();

    public abstract ChatGroupMemberDao groupMemberDao();

    public abstract AreaDao areaDao();

    public abstract RegionDao regionDao();

    public abstract ContactDao contactDao();

    public abstract FriendRequestDao friendRequestDao();
}
