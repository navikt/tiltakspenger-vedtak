package no.nav.tiltakspenger.vedtak.repository.meldekort

import arrow.core.toNonEmptyListOrNull
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag.IkkeUtfylt
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag.Utfylt.Deltatt.DeltattMedLønnITiltaket
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag.Utfylt.Deltatt.DeltattUtenLønnITiltaket
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag.Utfylt.Fravær.Syk.SykBruker
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag.Utfylt.Fravær.Syk.SyktBarn
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag.Utfylt.Fravær.Velferd.VelferdGodkjentAvNav
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag.Utfylt.Fravær.Velferd.VelferdIkkeGodkjentAvNav
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag.Utfylt.IkkeDeltatt
import no.nav.tiltakspenger.meldekort.domene.Meldekortdag.Utfylt.Sperret
import no.nav.tiltakspenger.meldekort.domene.Meldeperiode
import no.nav.tiltakspenger.meldekort.domene.ReduksjonAvYtelsePåGrunnAvFravær
import no.nav.tiltakspenger.vedtak.db.deserializeList
import no.nav.tiltakspenger.vedtak.db.serialize
import no.nav.tiltakspenger.vedtak.repository.meldekort.MeldekortdagDbJson.ReduksjonAvYtelsePåGrunnAvFraværDb
import no.nav.tiltakspenger.vedtak.repository.meldekort.MeldekortdagDbJson.StatusDb.DELTATT_MED_LØNN_I_TILTAKET
import no.nav.tiltakspenger.vedtak.repository.meldekort.MeldekortdagDbJson.StatusDb.DELTATT_UTEN_LØNN_I_TILTAKET
import no.nav.tiltakspenger.vedtak.repository.meldekort.MeldekortdagDbJson.StatusDb.FRAVÆR_SYK
import no.nav.tiltakspenger.vedtak.repository.meldekort.MeldekortdagDbJson.StatusDb.FRAVÆR_SYKT_BARN
import no.nav.tiltakspenger.vedtak.repository.meldekort.MeldekortdagDbJson.StatusDb.FRAVÆR_VELFERD_GODKJENT_AV_NAV
import no.nav.tiltakspenger.vedtak.repository.meldekort.MeldekortdagDbJson.StatusDb.FRAVÆR_VELFERD_IKKE_GODKJENT_AV_NAV
import no.nav.tiltakspenger.vedtak.repository.meldekort.MeldekortdagDbJson.StatusDb.IKKE_DELTATT
import no.nav.tiltakspenger.vedtak.repository.meldekort.MeldekortdagDbJson.StatusDb.IKKE_UTFYLT
import no.nav.tiltakspenger.vedtak.repository.meldekort.MeldekortdagDbJson.StatusDb.SPERRET
import no.nav.tiltakspenger.vedtak.repository.tiltak.toDb
import no.nav.tiltakspenger.vedtak.repository.tiltak.toTiltakstypeSomGirRett
import java.time.LocalDate

/**
 * @property reduksjon null dersom den ikke er utfylt
 */
private data class MeldekortdagDbJson(
    val tiltakstype: String,
    val dato: String,
    val status: StatusDb,
    val reduksjon: ReduksjonAvYtelsePåGrunnAvFraværDb?,
) {
    enum class StatusDb {
        SPERRET,
        IKKE_UTFYLT,
        DELTATT_UTEN_LØNN_I_TILTAKET,
        DELTATT_MED_LØNN_I_TILTAKET,
        IKKE_DELTATT,
        FRAVÆR_SYK,
        FRAVÆR_SYKT_BARN,
        FRAVÆR_VELFERD_GODKJENT_AV_NAV,
        FRAVÆR_VELFERD_IKKE_GODKJENT_AV_NAV,
    }

    enum class ReduksjonAvYtelsePåGrunnAvFraværDb {
        IngenReduksjon,
        DelvisReduksjon,
        YtelsenFallerBort,
        ;

        fun toDomain(): ReduksjonAvYtelsePåGrunnAvFravær =
            when (this) {
                IngenReduksjon -> ReduksjonAvYtelsePåGrunnAvFravær.IngenReduksjon
                DelvisReduksjon -> ReduksjonAvYtelsePåGrunnAvFravær.DelvisReduksjon
                YtelsenFallerBort -> ReduksjonAvYtelsePåGrunnAvFravær.YtelsenFallerBort
            }
    }

    fun toMeldekortdag(meldekortId: MeldekortId): Meldekortdag {
        val parsedDato = LocalDate.parse(dato)
        val parsedTiltakstype = tiltakstype.toTiltakstypeSomGirRett()
        return when (status) {
            SPERRET -> Sperret(meldekortId, parsedDato, parsedTiltakstype)
            IKKE_UTFYLT -> IkkeUtfylt(meldekortId, parsedDato, parsedTiltakstype)
            DELTATT_UTEN_LØNN_I_TILTAKET -> DeltattUtenLønnITiltaket(meldekortId, parsedDato, parsedTiltakstype)
            DELTATT_MED_LØNN_I_TILTAKET -> DeltattUtenLønnITiltaket(meldekortId, parsedDato, parsedTiltakstype)
            IKKE_DELTATT -> IkkeDeltatt(meldekortId, parsedDato, parsedTiltakstype)
            FRAVÆR_SYK -> SykBruker(meldekortId, parsedDato, parsedTiltakstype, reduksjon!!.toDomain())
            FRAVÆR_SYKT_BARN -> SyktBarn(meldekortId, parsedDato, parsedTiltakstype, reduksjon!!.toDomain())
            FRAVÆR_VELFERD_GODKJENT_AV_NAV -> VelferdGodkjentAvNav(meldekortId, parsedDato, parsedTiltakstype)
            FRAVÆR_VELFERD_IKKE_GODKJENT_AV_NAV -> VelferdIkkeGodkjentAvNav(meldekortId, parsedDato, parsedTiltakstype)
        }
    }
}

internal fun Meldeperiode.toDbJson(): String =
    when (this) {
        is Meldeperiode.IkkeUtfyltMeldeperiode -> this.toDbJson()
        is Meldeperiode.UtfyltMeldeperiode -> this.toDbJson()
    }

private fun Meldeperiode.IkkeUtfyltMeldeperiode.toDbJson(): String =
    verdi
        .map { meldekortdag ->
            MeldekortdagDbJson(
                tiltakstype = tiltakstype.toDb(),
                dato = meldekortdag.dato.toString(),
                status = MeldekortdagDbJson.StatusDb.IKKE_UTFYLT,
                reduksjon = null,
            )
        }.let { serialize(it) }

private fun Meldeperiode.UtfyltMeldeperiode.toDbJson(): String =
    verdi
        .map { meldekortdag ->
            MeldekortdagDbJson(
                tiltakstype = tiltakstype.toDb(),
                dato = meldekortdag.dato.toString(),
                reduksjon = meldekortdag.reduksjon?.toDb(),
                status =
                when (meldekortdag) {
                    is DeltattMedLønnITiltaket -> DELTATT_MED_LØNN_I_TILTAKET
                    is DeltattUtenLønnITiltaket -> DELTATT_UTEN_LØNN_I_TILTAKET
                    is SykBruker -> FRAVÆR_SYK
                    is SyktBarn -> FRAVÆR_SYKT_BARN
                    is VelferdGodkjentAvNav -> FRAVÆR_VELFERD_GODKJENT_AV_NAV
                    is VelferdIkkeGodkjentAvNav -> FRAVÆR_VELFERD_IKKE_GODKJENT_AV_NAV
                    is IkkeDeltatt -> IKKE_DELTATT
                    is Sperret -> SPERRET
                },
            )
        }.let { serialize(it) }

private fun ReduksjonAvYtelsePåGrunnAvFravær.toDb(): ReduksjonAvYtelsePåGrunnAvFraværDb =
    when (this) {
        ReduksjonAvYtelsePåGrunnAvFravær.IngenReduksjon -> ReduksjonAvYtelsePåGrunnAvFraværDb.IngenReduksjon
        ReduksjonAvYtelsePåGrunnAvFravær.DelvisReduksjon -> ReduksjonAvYtelsePåGrunnAvFraværDb.DelvisReduksjon
        ReduksjonAvYtelsePåGrunnAvFravær.YtelsenFallerBort -> ReduksjonAvYtelsePåGrunnAvFraværDb.YtelsenFallerBort
    }

internal fun String.toUtfyltMeldekortperiode(sakId: SakId, meldekortId: MeldekortId): Meldeperiode.UtfyltMeldeperiode =
    deserializeList<MeldekortdagDbJson>(this)
        .map {
            it.toMeldekortdag(meldekortId) as Meldekortdag.Utfylt
        }.let {
            Meldeperiode.UtfyltMeldeperiode(sakId, it.toNonEmptyListOrNull()!!)
        }

internal fun String.toIkkeUtfyltMeldekortperiode(sakId: SakId, meldekortId: MeldekortId): Meldeperiode.IkkeUtfyltMeldeperiode =
    deserializeList<MeldekortdagDbJson>(this)
        .map {
            it.toMeldekortdag(meldekortId) as IkkeUtfylt
        }.let {
            Meldeperiode.IkkeUtfyltMeldeperiode(sakId, it.toNonEmptyListOrNull()!!)
        }
