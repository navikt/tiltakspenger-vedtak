package no.nav.tiltakspenger.vedtak.clients.utbetaling

import no.nav.tiltakspenger.libs.tiltak.TiltakstypeSomGirRett
import no.nav.tiltakspenger.utbetaling.domene.Utbetalingsperiode
import no.nav.tiltakspenger.utbetaling.domene.Utbetalingsvedtak
import no.nav.tiltakspenger.vedtak.db.serialize
import no.nav.utsjekk.kontrakter.felles.Personident
import no.nav.utsjekk.kontrakter.felles.Satstype
import no.nav.utsjekk.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.utsjekk.kontrakter.iverksett.ForrigeIverksettingV2Dto
import no.nav.utsjekk.kontrakter.iverksett.IverksettV2Dto
import no.nav.utsjekk.kontrakter.iverksett.StønadsdataTiltakspengerV2Dto
import no.nav.utsjekk.kontrakter.iverksett.UtbetalingV2Dto
import no.nav.utsjekk.kontrakter.iverksett.VedtaksdetaljerV2Dto

fun Utbetalingsvedtak.toDTO(): String {
    val vedtak: Utbetalingsvedtak = this
    return IverksettV2Dto(
        sakId = vedtak.saksnummer.toString(),
        // Brukes som dedupliseringsnøkkel av helved dersom iverksettingId er null.
        behandlingId = vedtak.id.uuidPart(),
        // Dersom en vedtaksløsning har behov for å sende flere utbetalinger per behandling/vedtak, kan dette feltet brukes for å skille de. Denne blir brukt som oppdragId mot OS/UR. Se slack tråd: https://nav-it.slack.com/archives/C06SJTR2X3L/p1724136342664969
        iverksettingId = null,
        personident = Personident(verdi = vedtak.fnr.verdi),
        vedtak =
        VedtaksdetaljerV2Dto(
            vedtakstidspunkt = vedtak.vedtakstidspunkt,
            saksbehandlerId = vedtak.saksbehandler,
            beslutterId = vedtak.saksbehandler,
            utbetalinger =
            vedtak.utbetalingsperiode.perioderSomSkalUtbetales
                .toUtbetalingDto(vedtak.brukerNavkontor)
                .toList(),
        ),
        forrigeIverksetting =
        vedtak.forrigeUtbetalingsvedtak?.let {
            ForrigeIverksettingV2Dto(
                behandlingId = it.uuidPart(),
            )
        },
    ).let {
        serialize(it)
    }
}

private fun List<Utbetalingsperiode.SkalUtbetale>.toUtbetalingDto(brukersNavKontor: String): List<UtbetalingV2Dto> =
    this.map {
        UtbetalingV2Dto(
            beløp = it.beløpPerDag.toUInt(),
            fraOgMedDato = it.fraOgMed,
            tilOgMedDato = it.tilOgMed,
            stønadsdata =
            StønadsdataTiltakspengerV2Dto(
                stønadstype = it.tiltakstype.mapStønadstype(),
                // TODO post-mvp: Legg til barnetillegg
                barnetillegg = false,
                brukersNavKontor = brukersNavKontor,
            ),
            satstype = Satstype.DAGLIG,
        )
    }

private fun TiltakstypeSomGirRett.mapStønadstype(): StønadTypeTiltakspenger =
    when (this) {
        TiltakstypeSomGirRett.ARBEIDSFORBEREDENDE_TRENING -> StønadTypeTiltakspenger.ARBEIDSFORBEREDENDE_TRENING
        TiltakstypeSomGirRett.ARBEIDSRETTET_REHABILITERING -> StønadTypeTiltakspenger.ARBEIDSRETTET_REHABILITERING
        TiltakstypeSomGirRett.ARBEIDSTRENING -> StønadTypeTiltakspenger.ARBEIDSTRENING
        TiltakstypeSomGirRett.AVKLARING -> StønadTypeTiltakspenger.AVKLARING
        TiltakstypeSomGirRett.DIGITAL_JOBBKLUBB -> StønadTypeTiltakspenger.DIGITAL_JOBBKLUBB
        TiltakstypeSomGirRett.ENKELTPLASS_AMO -> StønadTypeTiltakspenger.ENKELTPLASS_AMO
        TiltakstypeSomGirRett.ENKELTPLASS_VGS_OG_HØYERE_YRKESFAG -> StønadTypeTiltakspenger.ENKELTPLASS_VGS_OG_HØYERE_YRKESFAG
        TiltakstypeSomGirRett.GRUPPE_AMO -> StønadTypeTiltakspenger.GRUPPE_AMO
        TiltakstypeSomGirRett.GRUPPE_VGS_OG_HØYERE_YRKESFAG -> StønadTypeTiltakspenger.GRUPPE_VGS_OG_HØYERE_YRKESFAG
        TiltakstypeSomGirRett.HØYERE_UTDANNING -> StønadTypeTiltakspenger.HØYERE_UTDANNING
        TiltakstypeSomGirRett.INDIVIDUELL_JOBBSTØTTE -> StønadTypeTiltakspenger.INDIVIDUELL_JOBBSTØTTE
        TiltakstypeSomGirRett.INDIVIDUELL_KARRIERESTØTTE_UNG -> StønadTypeTiltakspenger.INDIVIDUELL_KARRIERESTØTTE_UNG
        TiltakstypeSomGirRett.JOBBKLUBB -> StønadTypeTiltakspenger.JOBBKLUBB
        TiltakstypeSomGirRett.OPPFØLGING -> StønadTypeTiltakspenger.OPPFØLGING
        TiltakstypeSomGirRett.UTVIDET_OPPFØLGING_I_NAV -> StønadTypeTiltakspenger.UTVIDET_OPPFØLGING_I_NAV
        TiltakstypeSomGirRett.UTVIDET_OPPFØLGING_I_OPPLÆRING -> StønadTypeTiltakspenger.UTVIDET_OPPFØLGING_I_OPPLÆRING
        TiltakstypeSomGirRett.FORSØK_OPPLÆRING_LENGRE_VARIGHET -> StønadTypeTiltakspenger.FORSØK_OPPLÆRING_LENGRE_VARIGHET
    }
