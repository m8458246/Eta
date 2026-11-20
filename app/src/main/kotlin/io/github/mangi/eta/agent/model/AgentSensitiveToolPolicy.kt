package io.github.mangi.eta.agent.model

/**
 * 自用分支：工具级脱敏已整体关闭。
 *
 * 原实现会把 MCP、通讯录、短信、剪贴板、memory 等工具的原始参数与结果替换成占位符，
 * 不写入持久会话。对有状态的多步工具（workspaceId / editSessionId / nextCursor /
 * targetVersion / locator 都要跨轮回传）来说，这等于把工具限制在单轮内使用。
 *
 * 这里保留类型与调用点，但任何工具都不再被判定为敏感；持久化逻辑见
 * [AgentConversationCodec.transcript]——它也不再做参数与结果替换。
 */
internal object AgentSensitiveToolPolicy {
    fun isSensitive(toolName: String): Boolean = false
}
