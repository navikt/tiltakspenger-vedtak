package no.nav.tiltakspenger.vedtak.repository.utbetaling

import io.kotest.assertions.json.shouldEqualJson
import no.nav.tiltakspenger.saksbehandling.ports.SendtUtbetaling
import no.nav.tiltakspenger.vedtak.db.objectMapper
import org.junit.jupiter.api.Test

internal class SendtUtbetalingExTest {

    @Test
    fun `empty and plain string`() {
        val actual = SendtUtbetaling("", "response").toJson()
        objectMapper.readTree(actual)
        actual.shouldEqualJson(
            """
            {
            "request": "",
            "response": "response"
            }
            """.trimIndent(),
        )
    }

    @Test
    fun `blank string`() {
        val actual = SendtUtbetaling(" ", " a ").toJson()
        objectMapper.readTree(actual)
        actual.shouldEqualJson(
            """
            {
            "request": "",
            "response": " a "
            }
            """.trimIndent(),
        )
    }

    @Test
    fun `escapes non-json string`() {
        val nonEscapedString = """"\/\b\f\n\r\t"""

        val actual = SendtUtbetaling("request", nonEscapedString).toJson()
        objectMapper.readTree(actual)
        actual.shouldEqualJson(
            """
            {
            "request": "request",
            "response": "\"\\\/\\b\\f\\n\\r\\t"
            }
            """.trimIndent(),
        )
    }

    @Test
    fun `valid json string`() {
        val validJsonString = """{"key": "value"}"""

        val actual = SendtUtbetaling("request", validJsonString).toJson()
        objectMapper.readTree(actual)
        actual.shouldEqualJson(
            """
            {
            "request": "request",
            "response": {"key": "value"}
            }
            """.trimIndent(),
        )
    }

    @Test
    fun `valid json string with escaped characters`() {
        val validJsonString = """{"key": "value", "escaped": "\"\\\/\\b\\f\\n\\r\\t"}"""

        val actual = SendtUtbetaling("request", validJsonString).toJson()
        objectMapper.readTree(actual)
        actual.shouldEqualJson(
            """
            {
            "request": "request",
            "response": {"key": "value", "escaped": "\"\\\/\\b\\f\\n\\r\\t"}
            }
            """.trimIndent(),
        )
    }
}
