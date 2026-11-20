package io.github.mangi.eta.agent.model

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AgentSensitiveTranscriptTest {
    @Test
    fun noToolIsTreatedAsSensitive() {
        // 自用分支已关闭工具级脱敏：任何工具都不再被判定为敏感。
        assertFalse(AgentSensitiveToolPolicy.isSensitive("memory_get"))
        assertFalse(AgentSensitiveToolPolicy.isSensitive("memory_write"))
        assertFalse(AgentSensitiveToolPolicy.isSensitive("search_coloros_memories"))
        assertFalse(AgentSensitiveToolPolicy.isSensitive("search_notification_history"))
        assertFalse(AgentSensitiveToolPolicy.isSensitive("recent_app_activity"))
        assertFalse(AgentSensitiveToolPolicy.isSensitive("get_health_summary"))
        assertFalse(AgentSensitiveToolPolicy.isSensitive("search_personal_orders"))
        assertFalse(AgentSensitiveToolPolicy.isSensitive("search_bills"))
        assertFalse(AgentSensitiveToolPolicy.isSensitive("mcp_ace20ae6_mt_apk_workspace_open_c5c66c37"))
    }

    @Test
    fun mcpToolsAreNotSensitiveSoHandlesSurviveAcrossTurns() {
        // MCP 服务器靠 workspaceId / editSessionId / locator 等句柄串联多步工作流。
        val callId = "call_mcp"
        val arguments = """{"path":"mt://current-apk","temporary":false}"""
        val result = """{"ok":true,"workspaceId":"u422pvit","editSessionId":""}"""
        val transcript = AgentConversationCodec.transcript(
            messages = toolRound(callId, "mcp_ace20ae6_mt_apk_workspace_open_c5c66c37", arguments, result),
            startIndex = 0,
        )

        assertEquals(2, transcript.size)
        assertEquals(
            arguments,
            JSONArray(transcript[0].toolCallsJson)
                .getJSONObject(0)
                .getJSONObject("function")
                .getString("arguments"),
        )
        assertEquals(result, transcript[1].content)
        assertFalse(transcript.joinToString { it.content + it.toolCallsJson }.contains("redacted"))
    }

    @Test
    fun sensitiveToolArgumentsAndResultAreKeptInPersistentTranscript() {
        // 即使调用方仍把 id 当作敏感集合传入，也不再做任何替换。
        val callId = "call_sensitive"
        val transcript = AgentConversationCodec.transcript(
            messages = toolRound(
                callId,
                "set_setting",
                """{"namespace":"global","key":"demo","value":"敏感值"}""",
                """{"ok":true,"password":"secret-value"}""",
            ),
            startIndex = 0,
            sensitiveToolCallIds = setOf(callId),
        )

        val encoded = transcript.joinToString { it.content + it.toolCallsJson }
        assertEquals(2, transcript.size)
        assertEquals("""{"ok":true,"password":"secret-value"}""", transcript[1].content)
        assertFalse(encoded.contains("redacted"))
        assertFalse(encoded.contains("未写入持久会话"))
    }

    private fun toolRound(
        callId: String,
        toolName: String,
        arguments: String,
        result: String,
    ): JSONArray =
        JSONArray()
            .put(
                JSONObject()
                    .put("role", "assistant")
                    .put("content", JSONObject.NULL)
                    .put(
                        "tool_calls",
                        JSONArray().put(
                            JSONObject()
                                .put("id", callId)
                                .put("type", "function")
                                .put(
                                    "function",
                                    JSONObject()
                                        .put("name", toolName)
                                        .put("arguments", arguments),
                                ),
                        ),
                    ),
            )
            .put(
                JSONObject()
                    .put("role", "tool")
                    .put("tool_call_id", callId)
                    .put("content", result),
            )
}
