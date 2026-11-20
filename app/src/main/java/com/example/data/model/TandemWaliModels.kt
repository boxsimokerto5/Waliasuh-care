package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

data class TandemWaliMember(
    val username: String,
    val name: String,
    val addedAt: Long = 0L
)

data class TandemJoinRequest(
    val username: String,
    val name: String,
    val requestedAt: Long = 0L
)

data class ResolvedWaliBadgeInfo(
    val username: String,
    val name: String,
    val isPrimary: Boolean,
    val account: WaliAccount?,
    val isOnDutyNow: Boolean = false,
    val shiftLabel: String = ""
)

data class IncomingTandemRequestBundle(
    val requesterUsername: String,
    val requesterName: String,
    val students: List<ChildStudent>,
    val latestRequestedAt: Long
)

object TandemWaliHelper {
    const val MAX_TANDEM_MEMBERS = 2
    const val MAX_TOTAL_WALIS = 3 // 1 Wali Utama + maks. 2 Wali Tandem

    fun matchesIdentity(
        targetUsername: String,
        targetName: String,
        candidateUsername: String?,
        candidateName: String
    ): Boolean {
        val cleanTargetUser = targetUsername.trim()
        val cleanTargetName = targetName.trim()
        val cleanCandUser = candidateUsername?.trim().orEmpty()
        val cleanCandName = candidateName.trim()
        val userMatches = cleanTargetUser.isNotBlank() && cleanCandUser.isNotBlank() &&
            cleanTargetUser.equals(cleanCandUser, ignoreCase = true)
        val nameMatches = cleanTargetName.isNotBlank() && cleanCandName.isNotBlank() &&
            cleanTargetName.equals(cleanCandName, ignoreCase = true)
        return userMatches || nameMatches
    }

    fun parseMembers(json: String?): List<TandemWaliMember> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(json)
            val result = mutableListOf<TandemWaliMember>()
            val seenKeys = mutableSetOf<String>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val username = obj.optString("username", "").trim()
                val name = obj.optString("name", "").trim()
                val addedAt = obj.optLong("addedAt", 0L)
                if (username.isBlank() && name.isBlank()) continue
                val key = username.ifBlank { name }.lowercase()
                if (seenKeys.add(key)) {
                    result.add(
                        TandemWaliMember(
                            username = username,
                            name = name.ifBlank { username },
                            addedAt = addedAt
                        )
                    )
                }
            }
            result.take(MAX_TANDEM_MEMBERS)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun serializeMembers(members: List<TandemWaliMember>): String {
        val clean = mutableListOf<TandemWaliMember>()
        val seenKeys = mutableSetOf<String>()
        members.forEach { m ->
            val u = m.username.trim()
            val n = m.name.trim()
            if (u.isBlank() && n.isBlank()) return@forEach
            val key = u.ifBlank { n }.lowercase()
            if (seenKeys.add(key)) {
                clean.add(
                    TandemWaliMember(
                        username = u,
                        name = n.ifBlank { u },
                        addedAt = m.addedAt
                    )
                )
            }
        }
        val capped = clean.take(MAX_TANDEM_MEMBERS)
        if (capped.isEmpty()) return ""
        val arr = JSONArray()
        capped.forEach { m ->
            arr.put(
                JSONObject().apply {
                    put("username", m.username)
                    put("name", m.name)
                    put("addedAt", m.addedAt)
                }
            )
        }
        return arr.toString()
    }

    fun parseRequests(json: String?): List<TandemJoinRequest> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(json)
            val result = mutableListOf<TandemJoinRequest>()
            val seenKeys = mutableSetOf<String>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val username = obj.optString("username", "").trim()
                val name = obj.optString("name", "").trim()
                val requestedAt = obj.optLong("requestedAt", 0L)
                if (username.isBlank() && name.isBlank()) continue
                val key = username.ifBlank { name }.lowercase()
                if (seenKeys.add(key)) {
                    result.add(
                        TandemJoinRequest(
                            username = username,
                            name = name.ifBlank { username },
                            requestedAt = requestedAt
                        )
                    )
                }
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun serializeRequests(requests: List<TandemJoinRequest>): String {
        val clean = mutableListOf<TandemJoinRequest>()
        val seenKeys = mutableSetOf<String>()
        requests.forEach { r ->
            val u = r.username.trim()
            val n = r.name.trim()
            if (u.isBlank() && n.isBlank()) return@forEach
            val key = u.ifBlank { n }.lowercase()
            if (seenKeys.add(key)) {
                clean.add(
                    TandemJoinRequest(
                        username = u,
                        name = n.ifBlank { u },
                        requestedAt = r.requestedAt
                    )
                )
            }
        }
        if (clean.isEmpty()) return ""
        val arr = JSONArray()
        clean.forEach { r ->
            arr.put(
                JSONObject().apply {
                    put("username", r.username)
                    put("name", r.name)
                    put("requestedAt", r.requestedAt)
                }
            )
        }
        return arr.toString()
    }

    fun hasPrimaryWali(student: ChildStudent): Boolean =
        student.assignedWaliUsername.isNotBlank() || student.assignedWaliName.isNotBlank()

    fun isPrimaryWali(
        student: ChildStudent,
        username: String?,
        displayName: String
    ): Boolean {
        if (!hasPrimaryWali(student)) return false
        return matchesIdentity(
            targetUsername = student.assignedWaliUsername,
            targetName = student.assignedWaliName,
            candidateUsername = username,
            candidateName = displayName
        )
    }

    fun isTandemMember(
        student: ChildStudent,
        username: String?,
        displayName: String
    ): Boolean {
        return parseMembers(student.tandemWalisJson).any { m ->
            matchesIdentity(
                targetUsername = m.username,
                targetName = m.name,
                candidateUsername = username,
                candidateName = displayName
            )
        }
    }

    fun isAssignedWali(
        student: ChildStudent,
        username: String?,
        displayName: String
    ): Boolean {
        return isPrimaryWali(student, username, displayName) ||
            isTandemMember(student, username, displayName)
    }

    fun hasPendingRequest(
        student: ChildStudent,
        username: String?,
        displayName: String
    ): Boolean {
        return parseRequests(student.tandemRequestsJson).any { r ->
            matchesIdentity(
                targetUsername = r.username,
                targetName = r.name,
                candidateUsername = username,
                candidateName = displayName
            )
        }
    }

    fun tandemMembers(student: ChildStudent): List<TandemWaliMember> =
        parseMembers(student.tandemWalisJson)

    fun pendingRequests(student: ChildStudent): List<TandemJoinRequest> =
        parseRequests(student.tandemRequestsJson)

    fun totalAssignedCount(student: ChildStudent): Int {
        val primaryCount = if (hasPrimaryWali(student)) 1 else 0
        return primaryCount + tandemMembers(student).size
    }

    fun remainingTandemSlots(student: ChildStudent): Int {
        if (!hasPrimaryWali(student)) return 0
        return (MAX_TANDEM_MEMBERS - tandemMembers(student).size).coerceAtLeast(0)
    }

    fun canRequestTandemJoin(
        student: ChildStudent,
        username: String?,
        displayName: String
    ): Boolean {
        if (!hasPrimaryWali(student)) return false
        if (isAssignedWali(student, username, displayName)) return false
        if (hasPendingRequest(student, username, displayName)) return false
        return remainingTandemSlots(student) > 0
    }

    fun allAssignedWaliRefs(
        student: ChildStudent?,
        link: ParentAccessLink? = null
    ): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        val seenKeys = mutableSetOf<String>()

        fun addIfUnique(user: String, name: String) {
            val u = user.trim()
            val n = name.trim()
            if (u.isBlank() && n.isBlank()) return
            val key = u.ifBlank { n }.lowercase()
            if (seenKeys.add(key)) {
                result.add(u to n.ifBlank { u })
            }
        }

        val primaryUser = student?.assignedWaliUsername?.ifBlank { link?.assignedWaliUsername.orEmpty() }
            ?: link?.assignedWaliUsername.orEmpty()
        val primaryName = student?.assignedWaliName?.ifBlank { link?.assignedWaliName.orEmpty() }
            ?: link?.assignedWaliName.orEmpty()
        addIfUnique(primaryUser, primaryName)

        val tandemJson = student?.tandemWalisJson?.takeIf { it.isNotBlank() }
            ?: link?.tandemWalisJson.orEmpty()
        parseMembers(tandemJson).forEach { m ->
            addIfUnique(m.username, m.name)
        }
        return result.take(MAX_TOTAL_WALIS)
    }

    fun resolveWaliBadges(
        student: ChildStudent?,
        link: ParentAccessLink?,
        waliAccounts: List<WaliAccount>,
        scheduleEntries: List<WaliScheduleEntry> = emptyList(),
        shiftCodes: List<WaliShiftCode> = emptyList(),
        schoolId: String = "",
        nowMillis: Long = System.currentTimeMillis()
    ): List<ResolvedWaliBadgeInfo> {
        val refs = allAssignedWaliRefs(student, link)
        if (refs.isEmpty()) return emptyList()

        val activeSchool = schoolId.ifBlank {
            student?.schoolId?.ifBlank { link?.schoolId.orEmpty() } ?: link?.schoolId.orEmpty()
        }.ifBlank { "SRT1-KDR" }
        val monthKey = WaliScheduleHelper.currentMonthKey(nowMillis)
        val day = WaliScheduleHelper.currentDayOfMonth(nowMillis)
        val hour = WaliScheduleHelper.currentHourOfDay(nowMillis)
        val shiftMap = WaliScheduleHelper.buildShiftMap(shiftCodes, activeSchool)
        val monthEntries = scheduleEntries.filter {
            it.monthKey == monthKey && it.schoolId.equals(activeSchool, ignoreCase = true)
        }

        return refs.mapIndexed { idx, (u, n) ->
            val account = waliAccounts.firstOrNull { acc ->
                matchesIdentity(acc.username, acc.name, u, n)
            }
            val resolvedName = account?.name?.takeIf { it.isNotBlank() } ?: n.ifBlank { u }
            val resolvedUser = account?.username?.takeIf { it.isNotBlank() } ?: u

            val entry = monthEntries.firstOrNull { e ->
                matchesIdentity(e.waliUsername, e.waliName, resolvedUser, resolvedName)
            }
            val codeToday = entry?.let { WaliScheduleHelper.parseDayCodes(it.dayCodesJson)[day] }
                ?.let { WaliScheduleHelper.normalizeCode(it) }
                .orEmpty()
            val shiftDef = if (codeToday.isNotBlank()) shiftMap[codeToday] else null
            val isOnDuty = shiftDef != null && WaliScheduleHelper.isShiftActiveAtHour(shiftDef, hour)
            val shiftLabel = when {
                shiftDef != null && shiftDef.timeRange.isNotBlank() && shiftDef.shiftGroup !in listOf(
                    WaliScheduleHelper.GROUP_LEPAS,
                    WaliScheduleHelper.GROUP_IZIN
                ) -> "${shiftDef.name} (${shiftDef.timeRange})"
                shiftDef != null -> shiftDef.name
                codeToday.isNotBlank() -> codeToday
                else -> ""
            }

            ResolvedWaliBadgeInfo(
                username = resolvedUser,
                name = resolvedName,
                isPrimary = idx == 0,
                account = account,
                isOnDutyNow = isOnDuty,
                shiftLabel = shiftLabel
            )
        }
    }

    fun formatAllWaliSummary(
        student: ChildStudent,
        isEn: Boolean = false
    ): String {
        val refs = allAssignedWaliRefs(student)
        if (refs.isEmpty()) return "-"
        if (refs.size == 1) return refs.first().second
        val primaryLabel = refs.first().second
        val tandemLabels = refs.drop(1).joinToString(", ") { it.second }
        return if (isEn) {
            "$primaryLabel (Lead) + $tandemLabels (Tandem)"
        } else {
            "$primaryLabel (Utama) + $tandemLabels (Tandem)"
        }
    }

    fun groupIncomingRequestsForPrimary(
        students: List<ChildStudent>,
        primaryUsername: String?,
        primaryName: String
    ): List<IncomingTandemRequestBundle> {
        val grouped = linkedMapOf<String, MutableList<Pair<ChildStudent, TandemJoinRequest>>>()
        students.forEach { st ->
            if (!isPrimaryWali(st, primaryUsername, primaryName)) return@forEach
            pendingRequests(st).forEach { req ->
                val key = req.username.ifBlank { req.name }.trim().lowercase()
                grouped.getOrPut(key) { mutableListOf() }.add(st to req)
            }
        }
        return grouped.values.mapNotNull { items ->
            val firstReq = items.firstOrNull()?.second ?: return@mapNotNull null
            val displayName = items.map { it.second.name }.firstOrNull { it.isNotBlank() } ?: firstReq.name
            val user = items.map { it.second.username }.firstOrNull { it.isNotBlank() } ?: firstReq.username
            IncomingTandemRequestBundle(
                requesterUsername = user,
                requesterName = displayName,
                students = items.map { it.first }.distinctBy { it.id }.sortedBy { it.name },
                latestRequestedAt = items.maxOfOrNull { it.second.requestedAt } ?: 0L
            )
        }.sortedByDescending { it.latestRequestedAt }
    }

    fun withAddedRequest(
        student: ChildStudent,
        requesterUsername: String,
        requesterName: String,
        nowMillis: Long = System.currentTimeMillis()
    ): ChildStudent {
        if (!canRequestTandemJoin(student, requesterUsername, requesterName)) return student
        val cleanUser = requesterUsername.trim()
        val cleanName = requesterName.trim().ifBlank { cleanUser }
        val updated = pendingRequests(student) + TandemJoinRequest(
            username = cleanUser,
            name = cleanName,
            requestedAt = nowMillis
        )
        return student.copy(tandemRequestsJson = serializeRequests(updated))
    }

    fun withCancelledRequest(
        student: ChildStudent,
        requesterUsername: String?,
        requesterName: String
    ): ChildStudent {
        val updated = pendingRequests(student).filterNot { req ->
            matchesIdentity(req.username, req.name, requesterUsername, requesterName)
        }
        return student.copy(tandemRequestsJson = serializeRequests(updated))
    }

    fun withApprovedRequest(
        student: ChildStudent,
        requesterUsername: String,
        requesterName: String,
        nowMillis: Long = System.currentTimeMillis()
    ): ChildStudent {
        val cleanedRequests = pendingRequests(student).filterNot { req ->
            matchesIdentity(req.username, req.name, requesterUsername, requesterName)
        }
        if (!hasPrimaryWali(student) ||
            isAssignedWali(student, requesterUsername, requesterName) ||
            remainingTandemSlots(student) <= 0
        ) {
            return student.copy(tandemRequestsJson = serializeRequests(cleanedRequests))
        }
        val cleanUser = requesterUsername.trim()
        val cleanName = requesterName.trim().ifBlank { cleanUser }
        val updatedMembers = tandemMembers(student) + TandemWaliMember(
            username = cleanUser,
            name = cleanName,
            addedAt = nowMillis
        )
        return student.copy(
            tandemWalisJson = serializeMembers(updatedMembers),
            tandemRequestsJson = serializeRequests(cleanedRequests)
        )
    }

    fun withInvitedTandemMember(
        student: ChildStudent,
        targetUsername: String,
        targetName: String,
        nowMillis: Long = System.currentTimeMillis()
    ): ChildStudent {
        if (!hasPrimaryWali(student) ||
            isAssignedWali(student, targetUsername, targetName) ||
            remainingTandemSlots(student) <= 0
        ) {
            return student
        }
        val cleanUser = targetUsername.trim()
        val cleanName = targetName.trim().ifBlank { cleanUser }
        val cleanedRequests = pendingRequests(student).filterNot { req ->
            matchesIdentity(req.username, req.name, cleanUser, cleanName)
        }
        val updatedMembers = tandemMembers(student) + TandemWaliMember(
            username = cleanUser,
            name = cleanName,
            addedAt = nowMillis
        )
        return student.copy(
            tandemWalisJson = serializeMembers(updatedMembers),
            tandemRequestsJson = serializeRequests(cleanedRequests)
        )
    }

    fun withRemovedTandemMember(
        student: ChildStudent,
        targetUsername: String?,
        targetName: String
    ): ChildStudent {
        val updatedMembers = tandemMembers(student).filterNot { m ->
            matchesIdentity(m.username, m.name, targetUsername, targetName)
        }
        return student.copy(tandemWalisJson = serializeMembers(updatedMembers))
    }

    fun withPromotedTandemToPrimary(
        student: ChildStudent,
        targetUsername: String?,
        targetName: String,
        nowMillis: Long = System.currentTimeMillis()
    ): ChildStudent {
        val currentMembers = tandemMembers(student)
        val promoted = currentMembers.firstOrNull { m ->
            matchesIdentity(m.username, m.name, targetUsername, targetName)
        } ?: return student

        val oldPrimaryUser = student.assignedWaliUsername.trim()
        val oldPrimaryName = student.assignedWaliName.trim()
        val remainingMembers = currentMembers.filterNot { m ->
            matchesIdentity(m.username, m.name, promoted.username, promoted.name)
        }.toMutableList()
        if (oldPrimaryUser.isNotBlank() || oldPrimaryName.isNotBlank()) {
            remainingMembers.add(
                0,
                TandemWaliMember(
                    username = oldPrimaryUser,
                    name = oldPrimaryName.ifBlank { oldPrimaryUser },
                    addedAt = nowMillis
                )
            )
        }
        return student.copy(
            assignedWaliUsername = promoted.username,
            assignedWaliName = promoted.name,
            tandemWalisJson = serializeMembers(remainingMembers)
        )
    }

    fun withAdminTeamAssignment(
        student: ChildStudent,
        primaryWali: WaliAccount?,
        tandemWalis: List<WaliAccount>,
        nowMillis: Long = System.currentTimeMillis()
    ): ChildStudent {
        if (primaryWali == null) {
            return student.copy(
                assignedWaliUsername = "",
                assignedWaliName = "",
                tandemWalisJson = "",
                tandemRequestsJson = ""
            )
        }
        val cleanTandems = tandemWalis
            .filterNot { acc ->
                matchesIdentity(acc.username, acc.name, primaryWali.username, primaryWali.name)
            }
            .distinctBy { it.username.trim().ifBlank { it.name.trim() }.lowercase() }
            .take(MAX_TANDEM_MEMBERS)
            .map { acc ->
                TandemWaliMember(
                    username = acc.username.trim(),
                    name = acc.name.trim().ifBlank { acc.username.trim() },
                    addedAt = nowMillis
                )
            }
        val remainingRequests = pendingRequests(student).filterNot { req ->
            matchesIdentity(req.username, req.name, primaryWali.username, primaryWali.name) ||
                cleanTandems.any { m -> matchesIdentity(req.username, req.name, m.username, m.name) }
        }
        return student.copy(
            assignedWaliUsername = primaryWali.username.trim(),
            assignedWaliName = primaryWali.name.trim().ifBlank { primaryWali.username.trim() },
            tandemWalisJson = serializeMembers(cleanTandems),
            tandemRequestsJson = serializeRequests(remainingRequests)
        )
    }

    fun withReleasedActor(
        student: ChildStudent,
        actorUsername: String?,
        actorName: String,
        isAdmin: Boolean
    ): ChildStudent {
        if (isAdmin) {
            return student.copy(
                assignedWaliUsername = "",
                assignedWaliName = "",
                tandemWalisJson = "",
                tandemRequestsJson = ""
            )
        }
        if (isPrimaryWali(student, actorUsername, actorName)) {
            val existingTandems = tandemMembers(student)
            return if (existingTandems.isNotEmpty()) {
                val nextPrimary = existingTandems.first()
                val rest = existingTandems.drop(1)
                student.copy(
                    assignedWaliUsername = nextPrimary.username,
                    assignedWaliName = nextPrimary.name,
                    tandemWalisJson = serializeMembers(rest)
                )
            } else {
                student.copy(
                    assignedWaliUsername = "",
                    assignedWaliName = "",
                    tandemWalisJson = "",
                    tandemRequestsJson = ""
                )
            }
        }
        if (isTandemMember(student, actorUsername, actorName)) {
            return withRemovedTandemMember(student, actorUsername, actorName)
        }
        return student
    }

    fun applyWaliRename(
        student: ChildStudent,
        oldName: String,
        account: WaliAccount
    ): ChildStudent? {
        val newName = account.name.trim()
        val cleanUser = account.username.trim()
        if (newName.isBlank()) return null

        var changed = false
        var primaryUser = student.assignedWaliUsername
        var primaryName = student.assignedWaliName
        if (matchesIdentity(primaryUser, primaryName, cleanUser, oldName)) {
            if (primaryName != newName || primaryUser.isBlank()) {
                primaryName = newName
                if (primaryUser.isBlank()) primaryUser = cleanUser
                changed = true
            }
        }

        val updatedMembers = parseMembers(student.tandemWalisJson).map { m ->
            if (matchesIdentity(m.username, m.name, cleanUser, oldName)) {
                changed = true
                m.copy(username = m.username.ifBlank { cleanUser }, name = newName)
            } else m
        }

        val updatedRequests = parseRequests(student.tandemRequestsJson).map { r ->
            if (matchesIdentity(r.username, r.name, cleanUser, oldName)) {
                changed = true
                r.copy(username = r.username.ifBlank { cleanUser }, name = newName)
            } else r
        }

        if (!changed) return null
        return student.copy(
            assignedWaliUsername = primaryUser,
            assignedWaliName = primaryName,
            tandemWalisJson = serializeMembers(updatedMembers),
            tandemRequestsJson = serializeRequests(updatedRequests)
        )
    }

    fun applyWaliRename(
        link: ParentAccessLink,
        oldName: String,
        account: WaliAccount,
        nowMillis: Long = System.currentTimeMillis()
    ): ParentAccessLink? {
        val newName = account.name.trim()
        val cleanUser = account.username.trim()
        if (newName.isBlank()) return null

        var changed = false
        var primaryUser = link.assignedWaliUsername
        var primaryName = link.assignedWaliName
        if (matchesIdentity(primaryUser, primaryName, cleanUser, oldName)) {
            if (primaryName != newName || primaryUser.isBlank()) {
                primaryName = newName
                if (primaryUser.isBlank()) primaryUser = cleanUser
                changed = true
            }
        }

        val updatedMembers = parseMembers(link.tandemWalisJson).map { m ->
            if (matchesIdentity(m.username, m.name, cleanUser, oldName)) {
                changed = true
                m.copy(username = m.username.ifBlank { cleanUser }, name = newName)
            } else m
        }

        if (!changed) return null
        return link.copy(
            assignedWaliUsername = primaryUser,
            assignedWaliName = primaryName,
            tandemWalisJson = serializeMembers(updatedMembers),
            updatedAt = nowMillis
        )
    }
}
