package com.heroesports.heroci.data.repository

import com.heroesports.heroci.data.dao.MemberDao
import com.heroesports.heroci.data.entity.Member
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MemberRepository @Inject constructor(
    private val memberDao: MemberDao
) {
    fun getMembersByProjectId(projectId: Long): Flow<List<Member>> = 
        memberDao.getMembersByProjectId(projectId)

    suspend fun getMembersByProjectIdSync(projectId: Long): List<Member> =
        memberDao.getMembersByProjectIdSync(projectId)

    suspend fun insertMember(member: Member) = memberDao.insert(member)

    suspend fun deleteMember(projectId: Long, name: String) = 
        memberDao.deleteByProjectIdAndName(projectId, name)

    suspend fun deleteProjectMembers(projectId: Long) = 
        memberDao.deleteByProjectId(projectId)
} 