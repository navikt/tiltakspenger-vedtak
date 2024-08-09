package no.nav.tiltakspenger.saksbehandling.domene.stønadsdager

import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltak
import java.time.LocalDateTime
import kotlin.math.roundToInt

fun Tiltak.tilStønadsdagerRegisterSaksopplysning(): StønadsdagerSaksopplysning.Register =
    // B: Hvorfor kan deltagelsen være null fra tiltaksappen? Får vi null-verdier fra Arena eller Komet?
    if (antallDagerPerUke != null) {
        StønadsdagerSaksopplysning.Register(
            tiltakNavn = gjennomføring.typeNavn,
            antallDager = antallDagerPerUke.roundToInt(),
            periode = deltakelsesperiode,
            kilde = kilde,
            tidsstempel = LocalDateTime.now(),
        )
    } else if (deltakelseProsent != null) {
        StønadsdagerSaksopplysning.Register(
            tiltakNavn = gjennomføring.typeNavn,
            // B: Så på tidligere kode som gjorde dette, kan deltakelseprosent være noe annet enn 100?
            antallDager = if (deltakelseProsent == 100f) 5 else 0,
            periode = deltakelsesperiode,
            kilde = kilde,
            tidsstempel = LocalDateTime.now(),
        )
    } else {
        StønadsdagerSaksopplysning.Register(
            tiltakNavn = gjennomføring.typeNavn,
            antallDager = 0,
            periode = deltakelsesperiode,
            kilde = kilde,
            tidsstempel = LocalDateTime.now(),
        )
    }
