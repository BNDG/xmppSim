package com.hjq.demo.chat.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.hjq.demo.chat.entity.GroupMember
import com.hjq.demo.utils.Trace
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

/**
 * @author r
 * @date 2024/9/9
 * @description Brief description of the file content.
 */
@Dao
interface ChatGroupMemberDao {
    @Query(
        "select * from group_member where groupId = (:groupId) AND belongAccount = (:belongAccount) order by " +
                "case affiliation when 'owner' then 0 when 'admin' then 1 when 'member' then 2 else 3 end "
    )
    fun findMemberByGroupId(groupId: String, belongAccount: String): Single<List<GroupMember>>


    @Query("select * from group_member where groupId = (:groupId) AND belongAccount = (:belongAccount)  limit (:pageSize) offset (:pageNumber)")
    fun findMemberByGroupId(
        groupId: String,
        belongAccount: String,
        pageSize: Int,
        pageNumber: Int
    ): Single<List<GroupMember>>

    @Query("delete from group_member where groupId = (:groupId) AND belongAccount = (:belongAccount)")
    fun deleteMemberByGroupId(groupId: String, belongAccount: String): Completable?

    @Query("select * from group_member where groupId = (:groupId) AND belongAccount = (:belongAccount) and memberAccount = (:memberAccount) ")
    fun findMemberByGroupIdAndAccount(
        memberAccount: String,
        groupId: String,
        belongAccount: String
    ): Single<List<GroupMember>>

    @Query("select * from group_member where groupId = (:groupId) AND belongAccount = (:belongAccount) and memberRealUserId = (:memberRealUserId) ")
    fun findMemberByGroupIdAndRealId(
        memberRealUserId: String,
        groupId: String,
        belongAccount: String
    ): Single<List<GroupMember>>

    @Query("select * from group_member where groupId = (:groupId) AND belongAccount = (:belongAccount) and memberRealUserId = (:memberRealUserId) limit 1")
    fun getMemberByRealId(
        memberRealUserId: String,
        groupId: String,
        belongAccount: String
    ): GroupMember?

    @Query("select * from group_member where groupId = (:groupId) AND belongAccount = (:belongAccount) and memberAccount = (:memberAccount) limit 1")
    fun getMemberByAccount(
        memberAccount: String,
        groupId: String,
        belongAccount: String
    ): GroupMember?

    @Query("select * from group_member where groupId = (:groupId) AND belongAccount = (:belongAccount) and memberAccount = (:memberAccount)  LIMIT 1")
    fun getMemberSync(
        groupId: String,
        belongAccount: String,
        memberAccount: String
    ): GroupMember?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMember(groupMember: GroupMember)

    @Delete
    fun deleteMember(groupMember: GroupMember)

    @Delete
    fun deleteGroupMembers(groupMemberList: MutableList<GroupMember>)

    @Insert
    fun saveGroupMembers(groupMemberList: MutableList<GroupMember>)

    @Transaction
    fun saveOrUpdateMember(groupMember: GroupMember) {
        val findMemberByGroupIdAndAccount = getMemberSync(
            groupMember.groupId,
            groupMember.belongAccount,
            groupMember.memberAccount
        )
        if (findMemberByGroupIdAndAccount != null) {
            groupMember.id = findMemberByGroupIdAndAccount.id
            groupMember.memberName = groupMember.memberName.takeIf { !it.isNullOrEmpty() }
                ?: findMemberByGroupIdAndAccount.memberName
            groupMember.memberRealUserId =
                groupMember.memberRealUserId.takeIf { !it.isNullOrEmpty() }
                    ?: findMemberByGroupIdAndAccount.memberRealUserId
            groupMember.role = groupMember.role.takeIf { !it.isNullOrEmpty() }
                ?: findMemberByGroupIdAndAccount.role
            groupMember.affiliation = groupMember.affiliation.takeIf { !it.isNullOrEmpty() }
                ?: findMemberByGroupIdAndAccount.affiliation
        } else {
            /*Trace.d(
                "新的成员 memberaccount: ${groupMember.memberAccount}",
                "memberName: ${groupMember.memberName}",
                "groupid: ${groupMember.groupId}"
            )*/
        }

        insertMember(groupMember)
    }

    @Transaction
    fun kickedMember(groupMember: GroupMember) {
        val findMemberByGroupIdAndAccount = getMemberSync(
            groupMember.groupId,
            groupMember.belongAccount,
            groupMember.memberAccount
        )
        Trace.d(
            "remove groupmember id: " + findMemberByGroupIdAndAccount?.id,
            "memberaccount: ${groupMember.memberAccount}",
            "groupid: ${groupMember.groupId}"
        )
        if (findMemberByGroupIdAndAccount != null) {
            groupMember.id = findMemberByGroupIdAndAccount.id
        }
        deleteMember(groupMember)
    }
}