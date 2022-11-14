package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.Personopplysninger
import no.nav.tiltakspenger.vedtak.Skjerming
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.YtelseSak
import no.nav.tiltakspenger.vedtak.meldinger.ArenaTiltakMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.PersonopplysningerMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.SkjermingMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.SøknadMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.YtelserMottattHendelse
import java.util.*

fun nySøknadMottattHendelse(
    journalpostId: String = Random().nextInt().toString(),
    søknad: Søknad = nySøknadMedArenaTiltak(),
    aktivitetslogg: Aktivitetslogg = Aktivitetslogg(forelder = null),
): SøknadMottattHendelse {
    return SøknadMottattHendelse(
        aktivitetslogg = aktivitetslogg,
        journalpostId = journalpostId,
        søknad = søknad,
    )
}

fun nyPersonopplysningHendelse(
    journalpostId: String = Random().nextInt().toString(),
    personopplysninger: List<Personopplysninger> = listOf(personopplysningKjedeligFyr(strengtFortroligUtland = false)),
    aktivitetslogg: Aktivitetslogg = Aktivitetslogg(forelder = null),
): PersonopplysningerMottattHendelse {
    return PersonopplysningerMottattHendelse(
        journalpostId = journalpostId,
        personopplysninger = personopplysninger,
        aktivitetslogg = aktivitetslogg,
    )
}

fun nySkjermingHendelse(
    journalpostId: String = Random().nextInt().toString(),
    skjerming: Skjerming = skjermingFalse(ident = journalpostId),
    aktivitetslogg: Aktivitetslogg = Aktivitetslogg(forelder = null),
): SkjermingMottattHendelse {
    return SkjermingMottattHendelse(
        journalpostId = journalpostId,
        skjerming = skjerming,
        aktivitetslogg = aktivitetslogg,
    )
}

fun nyTiltakHendelse(
    journalpostId: String = Random().nextInt().toString(),
    tiltaksaktivitet: List<Tiltaksaktivitet>? = listOf(tiltaksaktivitet()),
    feil: ArenaTiltakMottattHendelse.Feilmelding? = null,
    aktivitetslogg: Aktivitetslogg = Aktivitetslogg(forelder = null),
): ArenaTiltakMottattHendelse {
    return ArenaTiltakMottattHendelse(
        journalpostId = journalpostId,
        tiltaksaktivitet = tiltaksaktivitet,
        feil = feil,
        aktivitetslogg = aktivitetslogg,
    )
}

fun nyYtelseHendelse(
    journalpostId: String = Random().nextInt().toString(),
    ytelseSak: List<YtelseSak> = listOf(ytelseSak()),
    aktivitetslogg: Aktivitetslogg = Aktivitetslogg(forelder = null),
): YtelserMottattHendelse {
    return YtelserMottattHendelse(
        journalpostId = journalpostId,
        ytelseSak = ytelseSak,
        aktivitetslogg = aktivitetslogg,
    )
}
