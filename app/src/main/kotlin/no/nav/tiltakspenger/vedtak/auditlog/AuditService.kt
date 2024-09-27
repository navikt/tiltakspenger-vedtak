package no.nav.tiltakspenger.vedtak.auditlog

import arrow.core.Either
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.sikkerlogg
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonService
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import java.lang.String.join
import java.util.UUID

private val logg = KotlinLogging.logger {}

data class AuditLogEvent(
    val navIdent: String,
    val berørtBrukerId: String,
    val action: Action = Action.ACCESS,
    val message: String,
    val callId: String?,
    val behandlingId: UUID? = null,
    val logLevel: Level = Level.INFO,
) {
    /**
     * Hva slags CRUD-operasjon blir gjort
     */
    enum class Action(
        val value: String,
    ) {
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
        INFO,
        WARN,
    }
}

enum class CefFieldName(
    val kode: String,
) {
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
     * Message er et fritekstfelt som skal beskrive noe rundt konteksten til loggingen
     */
    MESSAGE("msg"),

    /**
     * Om handlingen blir tillatt eller ikke (permit/deny)
     * Denne brukes ikke nå, men skal kanskje tas i bruk:
     * https://nav-it.slack.com/archives/C034K7M30JY/p1725870505466469
     */
    DECISION_VERDI("flexString1"),
    DECISION_LABEL("flexString1Label"),
}

data class CefField(
    val cefFieldName: CefFieldName,
    val value: String,
)

/**
 * Logger til auditlogg på formatet
 *
 * CEF:0|tiltakspenger-vedtak|auditLog|1.0|audit:access|tiltakspenger-vedtak audit log|INFO|
 * end=1618308696856 suid=X123456 duid=01010199999
 * flexString1Label=Decision flexString1=Permit
 * flexString2Label=behandlingId flexString2=2dc4c100-395a-4e25-b1e9-6ea52f49b9e1
 * sproc=40e4608e-7157-415d-86c2-697f4c3c7358
 */
object AuditLogger {
    private const val APPLICATION_NAME = "tiltakspenger-vedtak"

    private val auditLogger = KotlinLogging.logger("audit")

    fun log(logEvent: AuditLogEvent) {
        when (logEvent.logLevel) {
            AuditLogEvent.Level.INFO -> auditLogger.info(compileLogMessage(logEvent))
            AuditLogEvent.Level.WARN -> auditLogger.warn(compileLogMessage(logEvent))
        }
    }

    private fun compileLogMessage(logEvent: AuditLogEvent): String {
        // Field descriptions from CEF documentation (#tech-logg_analyse_og_datainnsikt):
        val version = "CEF:0"
        /*
        For å redusere antall duplikate logger har Team Auditlog kommet med et ønske om at vi skal legges i
        'samme bås' som Tiltaksgjennomføring siden vi er av lik karakter- og er avhengige av denne applikasjonen.
        Dette gir mening å gjøre i en auditlog-kontekst, og har ingen praktisk betydning.
        https://nav-it.slack.com/archives/C014576K5TQ/p1726490122242699?thread_ts=1726042802.693829&cid=C014576K5TQ
         */
        val deviceVendor = "Tiltaksgjennomforing"
        /*
        Dette er også ønske fra Team Auditlog. At 'deviceProduct' er applikasjonsnavnet.
        https://nav-it.slack.com/archives/C014576K5TQ/p1726490122242699?thread_ts=1726042802.693829&cid=C014576K5TQ
         */
        val deviceProduct = APPLICATION_NAME
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
        val name = "$APPLICATION_NAME audit log"
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
            CefField(CefFieldName.MESSAGE, logEvent.message),
        ).plus(
            logEvent.behandlingId
                ?.let {
                    listOf(
                        CefField(CefFieldName.BEHANDLING_LABEL, "behandlingId"),
                        CefField(CefFieldName.BEHANDLING_VERDI, it.toString()),
                    )
                }.orEmpty(),
        )
}

class AuditService(
    private val personService: PersonService,
) {
    fun logMedBehandlingId(
        behandlingId: BehandlingId,
        navIdent: String,
        action: AuditLogEvent.Action,
        contextMessage: String,
        callId: String?,
    ) {
        Either.catch {
            val berørtBrukerId = personService.hentFnrForBehandlingId(behandlingId)

            AuditLogger.log(
                AuditLogEvent(
                    navIdent = navIdent,
                    berørtBrukerId = berørtBrukerId.verdi,
                    action = action,
                    behandlingId = behandlingId.uuid(),
                    callId = callId,
                    message = contextMessage,
                    logLevel = AuditLogEvent.Level.INFO,
                ),
            )
        }.onLeft { logg.error { "Det oppstod en feil ved auditlogging" } }
    }

    fun logMedMeldekortId(
        meldekortId: MeldekortId,
        navIdent: String,
        action: AuditLogEvent.Action,
        callId: String?,
        contextMessage: String,
        behandlingUUID: UUID? = null,
    ) {
        Either.catch {
            val berørtBrukerId = personService.hentFnrForMeldekortId(meldekortId)

            AuditLogger.log(
                AuditLogEvent(
                    navIdent = navIdent,
                    berørtBrukerId = berørtBrukerId.verdi,
                    action = action,
                    behandlingId = behandlingUUID,
                    callId = callId,
                    message = contextMessage,
                    logLevel = AuditLogEvent.Level.INFO,
                ),
            )
        }.onLeft { logg.error { "Det oppstod en feil ved auditlogging" } }
    }

    fun logMedSakId(
        sakId: SakId,
        navIdent: String,
        action: AuditLogEvent.Action,
        callId: String?,
        contextMessage: String,
        behandlingUUID: UUID? = null,
    ) {
        Either.catch {
            val berørtBrukerId = personService.hentFnrForSakId(sakId)

            AuditLogger.log(
                AuditLogEvent(
                    navIdent = navIdent,
                    berørtBrukerId = berørtBrukerId.verdi,
                    action = action,
                    behandlingId = behandlingUUID,
                    callId = callId,
                    message = contextMessage,
                    logLevel = AuditLogEvent.Level.INFO,
                ),
            )
        }.onLeft { logg.error { "Det oppstod en feil ved auditlogging" } }
    }

    fun logMedSaksnummer(
        saksnummer: Saksnummer,
        navIdent: String,
        action: AuditLogEvent.Action,
        callId: String?,
        contextMessage: String,
        behandlingUUID: UUID? = null,
    ) {
        Either.catch {
            val berørtBrukerId = personService.hentFnrForSaksnummer(saksnummer = saksnummer)

            AuditLogger.log(
                AuditLogEvent(
                    navIdent = navIdent,
                    berørtBrukerId = berørtBrukerId.verdi,
                    action = action,
                    behandlingId = behandlingUUID,
                    callId = callId,
                    message = contextMessage,
                    logLevel = AuditLogEvent.Level.INFO,
                ),
            )
        }.onLeft { logg.error { "Det oppstod en feil ved auditlogging" } }
    }

    fun logMedBrukerId(
        brukerId: Fnr,
        navIdent: String,
        action: AuditLogEvent.Action,
        callId: String?,
        contextMessage: String,
        behandlingUUID: UUID? = null,
    ) {
        Either.catch {
            AuditLogger.log(
                AuditLogEvent(
                    navIdent = navIdent,
                    berørtBrukerId = brukerId.verdi,
                    action = action,
                    behandlingId = behandlingUUID,
                    callId = callId,
                    message = contextMessage,
                    logLevel = AuditLogEvent.Level.INFO,
                ),
            )
        }.onLeft { logg.error { "Det oppstod en feil ved auditlogging" } }
    }

    fun logForSøknadId(
        søknadId: SøknadId,
        navIdent: String,
        action: AuditLogEvent.Action,
        callId: String?,
        contextMessage: String,
        behandlingUUID: UUID? = null,
    ) {
        Either.catch {
            val berørtBrukerFnr = personService.hentFnrForSøknadId(søknadId)

            AuditLogger.log(
                AuditLogEvent(
                    navIdent = navIdent,
                    berørtBrukerId = berørtBrukerFnr.verdi,
                    action = action,
                    behandlingId = behandlingUUID,
                    callId = callId,
                    message = contextMessage,
                    logLevel = AuditLogEvent.Level.INFO,
                ),
            )
        }.onLeft { logg.error { "Det oppstod en feil ved auditlogging" } }
    }

    fun logMedVedtakId(
        vedtakId: VedtakId,
        navIdent: String,
        action: AuditLogEvent.Action,
        callId: String?,
        contextMessage: String,
        behandlingUUID: UUID? = null,
    ) {
        Either.catch {
            val berørtBrukerId = personService.hentFnrForVedtakId(vedtakId = vedtakId)

            AuditLogger.log(
                AuditLogEvent(
                    navIdent = navIdent,
                    berørtBrukerId = berørtBrukerId.verdi,
                    action = action,
                    behandlingId = behandlingUUID,
                    callId = callId,
                    message = contextMessage,
                    logLevel = AuditLogEvent.Level.INFO,
                ),
            )
        }.onLeft {
            logg.error { "Det oppstod en feil ved auditlogging. Se sikkerlogg for mer exception." }
            sikkerlogg.error(it) { "Det oppstod en feil ved auditlogging" }
        }
    }
}
