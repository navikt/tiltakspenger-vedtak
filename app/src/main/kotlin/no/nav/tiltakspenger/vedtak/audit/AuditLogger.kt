package no.nav.tiltakspenger.vedtak.audit

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.callid.*
import mu.KotlinLogging
import no.nav.tiltakspenger.vedtak.tilgang.JWTInnloggetSaksbehandlerProvider
import java.lang.String.join
import java.util.*

data class AuditLogEvent(
    val navIdent: String,
    val berørtBrukerId: String,
    val action: Action = Action.ACCESS,
    val behandlingId: UUID? = null,
    val callId: String?,
    val logLevel: Level = Level.INFO
) {
    /**
     * Hva slags CRUD-operasjon blir gjort
     */
    enum class Action(val value: String) {
        /** Bruker har sett data. */
        ACCESS("audit:access"),

        /** Bruker har endret data */
        UPDATE("audit:update"),

        /** Bruker har lagt inn nye data */
        CREATE("audit:create"),

        /** Minimalt innsyn, f.eks. ved visning i liste. */
        SEARCH("audit:search"),
    }

    enum class Level {
        INFO, WARN
    }
}

enum class CefFieldName(val kode: String) {
    /**
     * Tidspunkt for når hendelsen skjedde.
     */
    EVENT_TIME("end"),

    /**
     * Brukeren som startet hendelsen (saksbehandler/veileder/...).
     */
    USER_ID("suid"),

    /**
     * Bruker (søker/part/...) som har personopplysninger som blir berørt.
     */
    BERORT_BRUKER_ID("duid"),

    /**
     * Reservert til bruk for "Behandling". Det er godkjent med både behandlingsUuid
     * og behandlingsId, men førstnevnte er foretrukket. Denne skal unikt identifisere
     * behandlingen.
     */
    BEHANDLING_VERDI("flexString2"),

    /**
     * Reservert til bruk for "behandlingId".
     */
    BEHANDLING_LABEL("flexString2Label"),

    /**
     * Call-id, prosess-id
     */
    CALL_ID("sproc"),

    /**
     * Om handlingen blir tillatt eller ikke (permit/deny)
     */
    DECISION_VERDI("flexString1"),
    DECISION_LABEL("flexString1Label");
}

data class CefField(val cefFieldName: CefFieldName, val value: String)

/**
 * Logger til auditlogg på formatet
 *
 * CEF:0|su-se-bakover|auditLog|1.0|audit:access|su-se-bakover audit log|INFO|
 * end=1618308696856 suid=X123456 duid=01010199999
 * flexString1Label=Decision flexString1=Permit
 * flexString2Label=behandlingId flexString2=2dc4c100-395a-4e25-b1e9-6ea52f49b9e1
 * sproc=40e4608e-7157-415d-86c2-697f4c3c7358
 */
object AuditLogger {
    private const val applicationName = "tiltakspenger-vedtak"

    private val auditLogger = KotlinLogging.logger("audit")

    fun log(logEvent: AuditLogEvent) {
        when (logEvent.logLevel) {
            AuditLogEvent.Level.INFO -> auditLogger.info(compileLogMessage(logEvent))
            AuditLogEvent.Level.WARN -> auditLogger.warn(compileLogMessage(logEvent))
        }
    }

    private fun compileLogMessage(logEvent: AuditLogEvent): String {
        // Field descriptions from CEF documentation (#tech-logg_analyse_og_datainnsikt):
        /*
        Set to: 0 (zero)
         */
        val version = "CEF:0"
        /*
        Arena, Bisys etc
         */
        val deviceVendor = applicationName
        /*
        The name of the log that originated the event. Auditlog, leselogg, ABAC-Audit, Sporingslogg
         */
        val deviceProduct = "auditLog"
        /*
        The version of the logformat. 1.0
         */
        val deviceVersion = "1.0"
        /*
        The text representing the type of the event. For example audit:access, audit:edit
         */
        val deviceEventClassId = logEvent.action.value
        /*
        The description of the event. For example 'ABAC sporingslogg' or 'Database query'
         */
        val name = "$applicationName audit log"
        /*
        The severity of the event (INFO or WARN)
         */
        val severity = logEvent.logLevel.name

        val extensions = join(" ", getExtensions(logEvent).map { "${it.cefFieldName.kode}=${it.value}" })

        return join(
            "|",
            listOf(
                version,
                deviceVendor,
                deviceProduct,
                deviceVersion,
                deviceEventClassId,
                name,
                severity,
                extensions,
            ),
        )
    }

    private fun getExtensions(logEvent: AuditLogEvent): List<CefField> =
        listOfNotNull(
            CefField(CefFieldName.EVENT_TIME, System.currentTimeMillis().toString()),
            CefField(CefFieldName.USER_ID, logEvent.navIdent),
            CefField(CefFieldName.BERORT_BRUKER_ID, logEvent.berørtBrukerId),
            CefField(CefFieldName.DECISION_LABEL, "Decision"),
            CefField(CefFieldName.DECISION_VERDI, "Permit"),
            CefField(CefFieldName.CALL_ID, logEvent.callId.toString()),
        ).plus(
            logEvent.behandlingId?.let {
                listOf(
                    CefField(CefFieldName.BEHANDLING_LABEL, "behandlingId"),
                    CefField(CefFieldName.BEHANDLING_VERDI, it.toString()),
                )
            }.orEmpty(),
        )
}

internal fun ApplicationCall.audit(
    navIdent: String,
    berørtBruker: String,
    action: AuditLogEvent.Action = AuditLogEvent.Action.ACCESS,
    behandlingId: UUID? = null,
) {
    AuditLogger.log(
        AuditLogEvent(
            navIdent = navIdent,
            berørtBrukerId = berørtBruker,
            action = action,
            behandlingId = behandlingId,
            callId = this.callId,
        ),
    )
}

internal suspend fun ApplicationCall.auditHvisInnlogget(
    berørtBruker: String,
    action: AuditLogEvent.Action = AuditLogEvent.Action.ACCESS,
    behandlingId: UUID? = null,
) {
    this.principal<JWTPrincipal>()?.let {
        AuditLogger.log(
            AuditLogEvent(
                navIdent = JWTInnloggetSaksbehandlerProvider().hentSaksbehandler(it).navIdent,
                berørtBrukerId = berørtBruker,
                action = action,
                behandlingId = behandlingId,
                callId = this.callId,
            ),
        )
    }
}
