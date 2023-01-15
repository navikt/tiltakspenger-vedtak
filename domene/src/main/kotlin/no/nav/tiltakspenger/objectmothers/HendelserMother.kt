package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.vedtak.*
import no.nav.tiltakspenger.vedtak.meldinger.*
import java.util.Random

fun nySøknadMottattHendelse(
    journalpostId: String = Random().nextInt().toString(),
    ident: String = Random().nextInt().toString(),
    søknad: Søknad = nySøknadMedArenaTiltak(ident = ident),
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
    ident: String = personopplysninger.filterIsInstance<Personopplysninger.Søker>().firstOrNull()?.ident
        ?: Random().nextInt().toString(),
): PersonopplysningerMottattHendelse {
    return PersonopplysningerMottattHendelse(
        aktivitetslogg = aktivitetslogg,
        journalpostId = journalpostId,
        ident = ident,
        personopplysninger = personopplysninger,
    )
}

fun nySkjermingHendelse(
    journalpostId: String = Random().nextInt().toString(),
    ident: String = Random().nextInt().toString(),
    skjerming: Skjerming = skjermingFalse(ident = journalpostId),
    aktivitetslogg: Aktivitetslogg = Aktivitetslogg(forelder = null),
): SkjermingMottattHendelse {
    return SkjermingMottattHendelse(
        journalpostId = journalpostId,
        ident = ident,
        skjerming = skjerming,
        aktivitetslogg = aktivitetslogg,
    )
}

fun nyTiltakHendelse(
    journalpostId: String = Random().nextInt().toString(),
    tiltaksaktivitet: List<Tiltaksaktivitet> = listOf(tiltaksaktivitet()),
    aktivitetslogg: Aktivitetslogg = Aktivitetslogg(forelder = null),
): ArenaTiltakMottattHendelse {
    return ArenaTiltakMottattHendelse(
        journalpostId = journalpostId,
        tiltaksaktivitet = tiltaksaktivitet,
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

fun nyFeilHendelse(
    journalpostId: String = Random().nextInt().toString(),
    aktivitetslogg: Aktivitetslogg = Aktivitetslogg(forelder = null),
    ident: String = Random().nextInt().toString(),
    feil: Feil = Feil.PersonIkkeFunnet,
) = FeilMottattHendelse(
    aktivitetslogg = aktivitetslogg,
    journalpostId = journalpostId,
    ident = ident,
    feil = feil,
)
