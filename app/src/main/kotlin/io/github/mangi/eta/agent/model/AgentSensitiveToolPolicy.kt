package io.github.mangi.eta.agent.model

/**
 * 标记原始参数或结果不得进入持久会话的工具。
 *
 * 注意：MCP 工具不再纳入此列表。MCP 服务器普遍是「多步、有状态、靠不透明句柄串联」的
 * 工作流（workspaceId / editSessionId / nextCursor / targetVersion / locator 都要跨轮回传），
 * 一旦参数与结果被替换成占位符，历史里就只剩 {"redacted":true}，模型下一轮无法继续，
 * 甚至会把占位符当合法参数重放。MCP 服务器由用户自己配置在本机，结果需要完整持久化。
 */
internal object AgentSensitiveToolPolicy {
    fun isSensitive(toolName: String): Boolean =
        toolName in sensitiveTools || toolName in io.github.mangi.eta.agent.context.PersonalSearchTools.names || toolName in AgentPhoneToolCatalog.names

    private val sensitiveTools = setOf(
        "get_setting",
        "wifi_credentials",
        "recent_notifications",
        "search_notification_history",
        "recent_app_activity",
        "app_usage_summary",
        "get_current_location",
        "get_device_environment",
        "list_alarms",
        "list_active_timers",
        "search_clipboard_history",
        "get_health_summary",
        "read_sms_code",
        "get_logcat",
        "search_media",
        "search_audio",
        "search_recordings",
        "search_files",
        "search_calendar_events",
        "search_contacts",
        "search_call_history",
        "search_messages",
        "search_downloads",
        "search_coloros_notes",
        "search_notes",
        "search_coloros_recordings",
        "search_recording_summaries",
        "search_coloros_memories",
        "search_system_memories",
        "search_saved_places",
        "search_personal_orders",
        "search_qq_chat_images",
        "search_wechat_chat_images",
        "read_image",
        "set_setting",
        "memory_get",
        "memory_write",
        "character_memory_get",
        "character_memory_write",
    )
}
