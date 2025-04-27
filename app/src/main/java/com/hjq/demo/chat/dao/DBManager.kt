package com.hjq.demo.chat.dao

import android.content.Context
import androidx.room.rxjava3.EmptyResultSetException
import com.bndg.smack.enums.SmartConversationType
import com.hjq.demo.chat.cons.Constant
import com.hjq.demo.chat.entity.AreaEntity
import com.hjq.demo.chat.entity.AvatarEntity
import com.hjq.demo.chat.entity.ChatRoomEntity
import com.hjq.demo.chat.entity.ConversationInfo
import com.hjq.demo.chat.entity.FriendApply
import com.hjq.demo.chat.entity.GroupMember
import com.hjq.demo.chat.entity.MemberVoiceEntity
import com.hjq.demo.chat.entity.RegionEntity
import com.hjq.demo.chat.entity.User
import com.hjq.demo.chat.utils.PreferencesUtil
import com.hjq.demo.utils.Trace
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers


/**
 * @author r
 * @date 2024/8/1
 * @description Brief description of the file content.
 */
class DBManager(context: Context) {
    private var mDb: AppDatabase? = AppDatabase.getInstance(context)

    companion object {
        @Volatile
        private var instance: DBManager? = null
        fun getInstance(context: Context): DBManager {
            if (instance == null) {
                synchronized(DBManager::class.java) {
                    if (instance == null) {
                        instance = DBManager(context)
                    }
                }
            }
            return instance!!
        }
    }

    /**
     * 获取会话列表
     */
    fun getConversationListByUserId(userId: String): Single<List<ConversationInfo>>? {
        return extraSingle(mDb?.chatConversationDao()?.getConversationList(userId))
    }

    /**
     * 获取指定会话类型的会话列表
     */
    fun getConversationListByUserIdAndType(
        userId: String,
        type: String = SmartConversationType.GROUP.name
    ): Single<List<ConversationInfo>>? {
        return extraSingle(
            mDb?.chatConversationDao()?.getConversationByConversationType(userId, type)
        )
    }

    /**
     * 获取指定的会话
     */
    fun getConversationByConversationId(
        belongAccount: String,
        conversationId: String
    ): Single<List<ConversationInfo>>? {
        return extraSingle(
            mDb?.chatConversationDao()?.getConversationById(belongAccount, conversationId)
        )
    }

    /**
     * 获取用户头像
     */
    fun getAvatarByUserId(
        userId: String
    ): Single<List<AvatarEntity>>? {
        return extraSingle(
            mDb?.avatarDao()?.get(userId)
        )
    }

    fun getAvatarByConversationId(
        conversationId: String
    ): Single<List<AvatarEntity>>? {
        return extraSingle(
            mDb?.avatarDao()?.getAvatarByConversationId(conversationId)
        )
    }

    /**
     * 获取用户头像
     */
    fun getAvatarByUserIdOrHash(
        userId: String,
        avatarHash: String?
    ): Single<List<AvatarEntity>>? {
        var hash = avatarHash
        if (avatarHash.isNullOrEmpty()) {
            hash = "";
        }
        return extraSingle(
            mDb?.avatarDao()?.getAvatarByIdOrHash(userId, hash!!)
        )
    }

    /**
     * 保存头像
     */
    fun saveAvatar(avatarEntity: AvatarEntity): Completable? {
        return extraCompletable(mDb?.avatarDao()?.insertAvatar(avatarEntity))
    }

    /**
     * 事务更新保存头像
     */
    fun saveAvatarOrUpdate(avatarEntity: AvatarEntity): Completable? {
        return Completable.fromAction {
            mDb?.avatarDao()?.saveOrUpdateAvatar(avatarEntity)
        }.subscribeOn(Schedulers.io()) // 在 IO 线程执行数据库操作
            .observeOn(AndroidSchedulers.mainThread()) // 切换回主线程处理结果（如果需要）
    }

    /**
     * 保存会话
     */
    fun saveConversation(conversationInfo: ConversationInfo): Completable? {
        return extraCompletable(mDb?.chatConversationDao()?.insertConversation(conversationInfo))
    }

    /**
     * 事务更新会话
     */
    fun saveOrUpdateConversation(conversationInfo: ConversationInfo): Completable? {
        return Completable.fromAction {
            mDb?.chatConversationDao()?.saveOrUpdateConversation(conversationInfo)
        }?.subscribeOn(Schedulers.io()) // 在 IO 线程执行数据库操作
            ?.observeOn(AndroidSchedulers.mainThread()) // 切换回主线程处理结果（如果需要）
    }

    /**
     *  删除会话
     */
    fun deleteConversation(
        belongAccount: String = PreferencesUtil.getInstance().user.userId,
        conversationId: String
    ): Completable? {
        return Completable.fromAction {
            mDb?.chatConversationDao()?.deleteConversationById(belongAccount, conversationId)
        }?.subscribeOn(Schedulers.io()) // 在 IO 线程执行数据库操作
            ?.observeOn(AndroidSchedulers.mainThread()) // 切换回主线程处理结果（如果需要）
    }

    /**
     * 保存聊天室
     */
    fun saveChatRoom(chatRoomEntity: ChatRoomEntity): Completable? {
        Trace.d("saveChatRoom: 保存聊天室")
        return extraCompletable(mDb?.chatRoomDao()?.insertChatRoom(chatRoomEntity))
    }

    /**
     * 更新聊天室
     */
    fun saveOrUpdateChatRoom(chatRoomEntity: ChatRoomEntity): Completable? {
        return Completable.fromAction {
            mDb?.chatRoomDao()?.saveOrUpdateChatRoom(chatRoomEntity)
        }
            .subscribeOn(Schedulers.io()) // 在 IO 线程执行数据库操作
            .observeOn(AndroidSchedulers.mainThread()) // 切换回主线程处理结果（如果需要）
    }

    /**
     * 获取聊天室
     */
    fun getChatRoomByRoomId(chatRoomJid: String): Single<List<ChatRoomEntity>>? {
        return extraSingle(mDb?.chatRoomDao()?.getChatRoomByRoomId(chatRoomJid))
    }

    /**
     * 获取我的职位和聊天室设置 判断是否允许访客发言
     */
    fun getMemberVoiceInGroup(chatRoomJid: String, account: String): Single<List<MemberVoiceEntity>>? {
        return extraSingle(mDb?.chatRoomDao()?.getMemberVoiceInGroup(chatRoomJid, account))
    }

    /**
     * 获取某个群所有的群成员
     */
    fun getGroupMemberByGroupId(groupId: String): Single<List<GroupMember>>? {
        return extraSingle(
            mDb?.groupMemberDao()
                ?.findMemberByGroupId(groupId, PreferencesUtil.getInstance().userId)
        )
    }

    /**
     * 获取某个群所有的群成员 分页
     */
    fun getGroupMemberByGroupId(groupId: String, pageNum: Int): Single<List<GroupMember>>? {
        var pageSize: Int = MessageDao.PAGE_SIZE
        val offset: Int = pageNum * pageSize
        return extraSingle(
            mDb?.groupMemberDao()
                ?.findMemberByGroupId(
                    groupId,
                    PreferencesUtil.getInstance().userId,
                    pageSize,
                    offset
                )
        )
    }

    /**
     * 查找某个群成员
     */
    fun findMemberByGroupIdAndAccount(
        memberAccount: String,
        groupId: String
    ): Single<List<GroupMember>>? {
        return extraSingle(
            mDb?.groupMemberDao()
                ?.findMemberByGroupIdAndAccount(
                    memberAccount,
                    groupId,
                    PreferencesUtil.getInstance().userId
                )
        )
    }

    fun findMemberByGroupIdAndRealId(
        memberRealUserId: String,
        groupId: String
    ): Single<List<GroupMember>>? {
        return extraSingle(
            mDb?.groupMemberDao()
                ?.findMemberByGroupIdAndRealId(
                    memberRealUserId,
                    groupId,
                    PreferencesUtil.getInstance().userId
                )
        )
    }

    /**
     * 保存群成员
     */
    fun saveGroupMember(groupMember: GroupMember): Completable? {

        return Completable.fromAction {
            mDb?.groupMemberDao()?.saveOrUpdateMember(groupMember)
        }?.subscribeOn(Schedulers.io()) // 在 IO 线程执行数据库操作
            ?.observeOn(AndroidSchedulers.mainThread()) // 切换回主线程处理结果（如果需要）
    }

    /**
     * 批量保存群成员
     */
    fun saveGroupMembers(groupMemberList: MutableList<GroupMember>): Completable? {
        Trace.d("saveGroupMembers: 批量保存群成员")
        return Completable.fromAction {
            mDb?.groupMemberDao()?.saveGroupMembers(groupMemberList)
        }?.subscribeOn(Schedulers.io()) // 在 IO 线程执行数据库操作
            ?.observeOn(AndroidSchedulers.mainThread()) // 切换回主线程处理结果（如果需要）
    }

    fun deleteGroupMembers(groupMemberList: MutableList<GroupMember>): Completable? {
        return Completable.fromAction {
            mDb?.groupMemberDao()?.deleteGroupMembers(groupMemberList)
        }?.subscribeOn(Schedulers.io()) // 在 IO 线程执行数据库操作
            ?.observeOn(AndroidSchedulers.mainThread()) // 切换回主线程处理结果（如果需要）
    }

    /**
     * 移除群成员
     *
     * @param groupId
     * @param nickname
     * @param reason
     */
    fun kickedMember(groupMember: GroupMember): Completable? {
        return Completable.fromAction {
            mDb?.groupMemberDao()?.kickedMember(groupMember)
        }?.subscribeOn(Schedulers.io()) // 在 IO 线程执行数据库操作
            ?.observeOn(AndroidSchedulers.mainThread()) // 切换回主线程处理结果（如果需要）
    }

    /**
     * 删除所有的群成员
     * 退出群聊的时候-解散群聊的时候
     *
     * @param groupId
     */
    fun deleteMemberByGroupId(groupId: String) {
        extraCompletable(
            mDb?.groupMemberDao()
                ?.deleteMemberByGroupId(groupId, PreferencesUtil.getInstance().userId)
        )?.subscribe();
    }

    /**
     * 保存联系人
     */
    fun saveContact(contact: User): Completable? {
        return Completable.fromAction {
            mDb?.contactDao()?.saveContact(contact)
            Trace.d("saveContact: 保存联系人>>>> ${contact.userId}: ");
        }?.subscribeOn(Schedulers.io()) // 在 IO 线程执行数据库操作
            ?.observeOn(AndroidSchedulers.mainThread()) // 切换回主线程处理结果（如果需要）
    }

    /**
     * 保存联系人
     */
    fun saveOrUpdateContact(contact: User): Completable? {
        return Completable.fromAction {
            mDb?.contactDao()?.saveContactSync(contact)
        }?.subscribeOn(Schedulers.io()) // 在 IO 线程执行数据库操作
            ?.observeOn(AndroidSchedulers.mainThread()) // 切换回主线程处理结果（如果需要）
    }

    /**
     * 获取联系人
     */
    fun getUserById(userId: String): Single<List<User>>? {
        return extraSingle(
            mDb?.contactDao()?.getUserById(userId, PreferencesUtil.getInstance().userId)
        )
    }

    /**
     * 获取所有好友
     */
    fun getAllFriendList(): Single<List<User>>? {
        return extraSingle(
            mDb?.contactDao()?.getAllFriendList(
                Constant.IS_FRIEND,
                Constant.CONTACT_IS_NOT_BLOCKED,
                PreferencesUtil.getInstance().userId
            )
        )
    }

    /**
     * 获取所有被屏蔽的好友
     */
    fun getAllBlockedUserList(): Single<List<User>>? {
        return extraSingle(
            mDb?.contactDao()?.getAllBlockedUserList(
                Constant.CONTACT_IS_BLOCKED,
                PreferencesUtil.getInstance().userId
            )
        )
    }

    /**
     * 获取所有收藏的好友
     */
    fun getAllStarredContactList(): Single<MutableList<User>>? {
        return extraSingle(
            mDb?.contactDao()?.getAllStarredContactList(
                Constant.CONTACT_IS_STARRED,
                Constant.CONTACT_IS_NOT_BLOCKED,
                PreferencesUtil.getInstance().userId
            )
        )
    }

    /**
     * 删除联系人
     */
    fun deleteContact(user: User) {
        extraCompletable(
            mDb?.contactDao()?.deleteContact(user)
        )?.subscribe()
    }

    /**
     * 获取好友申请
     */
    fun getFriendApplyByApplyId(applyId: String): Single<List<FriendApply>>? {
        return extraSingle(mDb?.friendRequestDao()?.getFriendApplyByApplyId(applyId))
    }

    /**
     * 获取好友申请
     */
    fun getFriendApplyByFromUserId(userId: String): Single<List<FriendApply>>? {
        return extraSingle(
            mDb?.friendRequestDao()
                ?.getFriendApplyByUserId(userId, PreferencesUtil.getInstance().userId)
        )
    }

    fun getFriendApplies(): Single<List<FriendApply>>? {
        return extraSingle(
            mDb?.friendRequestDao()?.getFriendApplies(PreferencesUtil.getInstance().userId)
        )
    }

    fun getUnReadFriendApplyCount(): Single<List<FriendApply>>? {
        return extraSingle(
            mDb?.friendRequestDao()?.getUnReadFriendApplies(
                Constant.FRIEND_APPLY_STATUS_NONE,
                PreferencesUtil.getInstance().userId
            )
        )
    }

    fun deleteFriendApply(friendApply: FriendApply): Completable? {
        return Completable.fromAction {
            mDb?.friendRequestDao()?.deleteFriendApply(friendApply)
        }?.subscribeOn(Schedulers.io()) // 在 IO 线程执行数据库操作
            ?.observeOn(AndroidSchedulers.mainThread()) // 切换回主线程处理结果（如果需要）
    }

    fun saveFriendApply(mFriendApply: FriendApply): Completable? {
        return Completable.fromAction {
            mDb?.friendRequestDao()?.saveOrUpdateFriendApply(mFriendApply)
        }?.subscribeOn(Schedulers.io()) // 在 IO 线程执行数据库操作
            ?.observeOn(AndroidSchedulers.mainThread()) // 切换回主线程处理结果（如果需要）
    }

    /**
     * 获取省份
     */
    fun getProvinceList(): Single<MutableList<AreaEntity>>? {
        return extraSingle(mDb?.areaDao()?.getProvinceList(Constant.AREA_TYPE_PROVINCE))
    }

    /**
     *  获取城市
     */
    fun getCityListByProvinceName(type: String): Single<MutableList<AreaEntity>>? {
        return extraSingle(mDb?.areaDao()?.getCityListByProvinceName(Constant.AREA_TYPE_CITY, type))
    }

    /**
     *  获取区县
     */
    fun getDistrictListByCityName(cityName: String): Single<MutableList<AreaEntity>>? {
        return extraSingle(
            mDb?.areaDao()?.getDistrictListByCityName(Constant.AREA_TYPE_DISTRICT, cityName)
        )
    }

    /**
     * 获取区县
     */
    fun getDistrictByCityNameAndDistrictName(
        cityName: String,
        districtName: String
    ): Single<MutableList<AreaEntity>>? {
        return extraSingle(
            mDb?.areaDao()?.getDistrictByCityNameAndDistrictName(
                Constant.AREA_TYPE_DISTRICT,
                cityName,
                districtName
            )
        )
    }

    /**
     * 保存地区
     */
    fun saveAreas(areas: MutableList<AreaEntity>): Completable? {
        return Completable.fromAction {
            mDb?.areaDao()?.saveAreas(areas)
        }?.subscribeOn(Schedulers.io()) // 在 IO 线程执行数据库操作
            ?.observeOn(AndroidSchedulers.mainThread()) // 切换回主线程处理结果（如果需要）
    }

    /**
     * 获取地区
     */
    fun getRegionList(): Single<MutableList<RegionEntity>>? {
        return extraSingle(mDb?.regionDao()?.getRegionList())
    }

    /**
     * 保存地区
     */
    fun saveRegions(regionEntitys: MutableList<RegionEntity>): Completable? {
        return Completable.fromAction {
            mDb?.regionDao()?.saveRegions(regionEntitys)
        }?.subscribeOn(Schedulers.io()) // 在 IO 线程执行数据库操作
            ?.observeOn(AndroidSchedulers.mainThread()) // 切换回主线程处理结果（如果需要）
    }

    private fun extraCompletable(completable: Completable?): Completable? {
        return completable
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.doOnError { error ->
                // 处理错误逻辑，例如打印日志或者显示错误提示
                Trace.d("处理错误逻辑 error:$error")
            }
            ?.onErrorResumeNext { throwable ->
                Trace.d("extraSingle", "捕获其他异常: $throwable")
                Completable.complete()
            }
    }

    private fun <T> extraSingle(single: Single<T>?): Single<T>? {
        return single
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.onErrorResumeNext { throwable ->
                if (throwable is NullPointerException || throwable is EmptyResultSetException) {
                    // 如果抛出 NullPointerException 或 EmptyResultSetException，返回一个发送 null 的 Single
                    Trace.d(
                        "extraSingle",
                        "捕获 NullPointerException 或 EmptyResultSetException: $throwable"
                    )
                    Single.create { emitter ->
                        emitter.onSuccess(null) // 发送 null 值作为成功结果
                    }
                } else {
                    // 其他情况，继续传播错误
                    Trace.d("extraSingle", "捕获其他异常: $throwable")
                    Single.error(throwable)
                }
            }
    }

    private fun <T> extraMaybe(maybe: Maybe<T>?): Maybe<T>? {
        Trace.d("extraMaybe: $maybe")
        return maybe
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.doOnError { error ->
                // 处理错误逻辑，例如打印日志或者显示错误提示
                Trace.d("处理错误逻辑 error:$error")
            }
    }

    private fun <T> extra(flowable: Flowable<T>?): Flowable<T>? {
        return flowable
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.doOnError { error ->
                // 处理错误逻辑，例如打印日志或者显示错误提示
            }
    }


}