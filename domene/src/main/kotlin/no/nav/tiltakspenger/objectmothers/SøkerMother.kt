@file:Suppress("LongParameterList")

package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.domene.januar
import no.nav.tiltakspenger.domene.januarDateTime
import no.nav.tiltakspenger.vedtak.Personopplysninger
import no.nav.tiltakspenger.vedtak.Skjerming
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet.DeltakelsesPeriode
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet.DeltakerStatus
import no.nav.tiltakspenger.vedtak.YtelseSak
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

fun søkerRegistrert(
    ident: String = Random().nextInt().toString(),
): Søker {
    return Søker(
        ident = ident,
    )
}

fun søkerMedSøknad(
    ident: String = Random().nextInt().toString(),
    søknad: Søknad = nySøknadMedArenaTiltak(ident = ident),
): Søker {
    val søker = søkerRegistrert(ident)
    val hendelse = nySøknadMottattHendelse(
        ident = ident,
        søknad = søknad,
    )
    søker.håndter(hendelse)
    return søker
}

fun søkerMedPersonopplysninger(
    ident: String = Random().nextInt().toString(),
    søknad: Søknad = nySøknadMedArenaTiltak(ident = ident),
    personopplysninger: List<Personopplysninger> = listOf(
        personopplysningKjedeligFyr(
            ident = ident,
            strengtFortroligUtland = false
        )
    ),
): Søker {
    val søker = søkerMedSøknad(
        ident = ident,
        søknad = søknad,
    )
    søker.håndter(
        nyPersonopplysningHendelse(
            ident = ident,
            personopplysninger = personopplysninger,
        )
    )
    return søker
}

fun søkerMedSkjerming(
    ident: String = Random().nextInt().toString(),
    søknad: Søknad = nySøknadMedArenaTiltak(ident = ident),
    personopplysninger: List<Personopplysninger> = listOf(
        personopplysningKjedeligFyr(
            ident = ident,
            strengtFortroligUtland = false
        )
    ),
    skjerming: Skjerming = skjermingFalse(ident = ident),
): Søker {
    val søker = søkerMedPersonopplysninger(
        ident = ident,
        søknad = søknad,
        personopplysninger = personopplysninger,
    )
    søker.håndter(
        nySkjermingHendelse(
            ident = ident,
            skjerming = skjerming,
        )
    )
    return søker
}

fun søkerMedTiltak(
    ident: String = Random().nextInt().toString(),
    søknad: Søknad = nySøknadMedArenaTiltak(ident = ident),
    personopplysninger: List<Personopplysninger> = listOf(
        personopplysningKjedeligFyr(
            ident = ident,
            strengtFortroligUtland = false
        )
    ),
    skjerming: Skjerming = skjermingFalse(ident = ident),
    tiltaksaktivitet: List<Tiltaksaktivitet> = listOf(tiltaksaktivitet()),
): Søker {
    val søker = søkerMedSkjerming(
        ident = ident,
        søknad = søknad,
        personopplysninger = personopplysninger,
        skjerming = skjerming,
    )
    søker.håndter(
        nyTiltakHendelse(
            ident = ident,
            tiltaksaktivitet = tiltaksaktivitet,
        )
    )
    return søker
}

fun søkerMedYtelse(
    ident: String = Random().nextInt().toString(),
    søknad: Søknad = nySøknadMedArenaTiltak(ident = ident),
    personopplysninger: List<Personopplysninger> = listOf(
        personopplysningKjedeligFyr(
            ident = ident,
            strengtFortroligUtland = false
        )
    ),
    skjerming: Skjerming = skjermingFalse(ident = ident),
    tiltaksaktivitet: List<Tiltaksaktivitet> = listOf(tiltaksaktivitet()),
    ytelseSak: List<YtelseSak> = listOf(ytelseSak()),
): Søker {
    val søker = søkerMedTiltak(
        ident = ident,
        søknad = søknad,
        personopplysninger = personopplysninger,
        skjerming = skjerming,
        tiltaksaktivitet = tiltaksaktivitet,
    )
    søker.håndter(
        nyYtelseHendelse(
            ident = ident,
            ytelseSak = ytelseSak,
        )
    )
    return søker
}

fun personopplysningKjedeligFyr(
    ident: String = Random().nextInt().toString(),
    fødselsdato: LocalDate = 1.januar(2001),
    fornavn: String = "Fornavn",
    mellomnavn: String? = null,
    etternavn: String = "Etternavn",
    fortrolig: Boolean = false,
    strengtFortrolig: Boolean = false,
    strengtFortroligUtland: Boolean = false,
    kommune: String? = null,
    bydel: String? = null,
    skjermet: Boolean? = null,
    tidsstempelHosOss: LocalDateTime = 1.januarDateTime(2022),
): Personopplysninger.Søker = Personopplysninger.Søker(
    ident = ident,
    fødselsdato = fødselsdato,
    fornavn = fornavn,
    mellomnavn = mellomnavn,
    etternavn = etternavn,
    fortrolig = fortrolig,
    strengtFortrolig = strengtFortrolig,
    strengtFortroligUtland = strengtFortroligUtland,
    skjermet = skjermet,
    kommune = kommune,
    bydel = bydel,
    tidsstempelHosOss = tidsstempelHosOss,
)

fun personopplysningMaxFyr(
    ident: String = Random().nextInt().toString(),
    fødselsdato: LocalDate = 1.januar(2001),
    fornavn: String = "Kjell",
    mellomnavn: String? = "T.",
    etternavn: String = "Ring",
    fortrolig: Boolean = false,
    strengtFortrolig: Boolean = true,
    strengtFortroligUtland: Boolean = false,
    kommune: String? = "Oslo",
    bydel: String? = "3440",
    skjermet: Boolean? = true,
    tidsstempelHosOss: LocalDateTime = 1.januarDateTime(2022),
): Personopplysninger.Søker = Personopplysninger.Søker(
    ident = ident,
    fødselsdato = fødselsdato,
    fornavn = fornavn,
    mellomnavn = mellomnavn,
    etternavn = etternavn,
    fortrolig = fortrolig,
    strengtFortrolig = strengtFortrolig,
    strengtFortroligUtland = strengtFortroligUtland,
    skjermet = skjermet,
    kommune = kommune,
    bydel = bydel,
    tidsstempelHosOss = tidsstempelHosOss,
)

fun barn(
    ident: String = Random().nextInt().toString(),
    fødselsdato: LocalDate = 1.januar(2001),
    fornavn: String = "Fornavn",
    mellomnavn: String? = null,
    etternavn: String = "Etternavn",
    fortrolig: Boolean = false,
    strengtFortrolig: Boolean = false,
    strengtFortroligUtland: Boolean = false,
    oppholdsland: String? = null,
    tidsstempelHosOss: LocalDateTime = 1.januarDateTime(2022),
): Personopplysninger.BarnMedIdent {
    return Personopplysninger.BarnMedIdent(
        ident = ident,
        fødselsdato = fødselsdato,
        fornavn = fornavn,
        mellomnavn = mellomnavn,
        etternavn = etternavn,
        fortrolig = fortrolig,
        strengtFortrolig = strengtFortrolig,
        strengtFortroligUtland = strengtFortroligUtland,
        oppholdsland = oppholdsland,
        tidsstempelHosOss = tidsstempelHosOss,
    )
}

fun skjermingFalse(
    ident: String = Random().nextInt().toString(),
): Skjerming {
    return Skjerming(
        ident = ident,
        skjerming = false,
        innhentet = 1.januarDateTime(2022),
    )
}

fun skjermingTrue(
    ident: String = Random().nextInt().toString(),
): Skjerming {
    return Skjerming(
        ident = ident,
        skjerming = true,
        innhentet = 1.januarDateTime(2022),
    )
}

fun tiltaksaktivitet(
    tiltak: Tiltaksaktivitet.Tiltak = Tiltaksaktivitet.Tiltak.JOBBK,
    aktivitetId: String = "aktivitetId",
    tiltakLokaltNavn: String? = "LokaltNavn",
    arrangør: String? = "arrangør",
    bedriftsnummer: String? = "bedriftsnummer",
    deltakelsePeriode: DeltakelsesPeriode = DeltakelsesPeriode(fom = 1.januar(2022), tom = 31.januar(2022)),
    deltakelseProsent: Float? = 100F,
    deltakerStatus: DeltakerStatus = DeltakerStatus.AKTUELL,
    statusSistEndret: LocalDate? = 1.januar(2022),
    begrunnelseInnsøking: String? = "begrunnelse",
    antallDagerPerUke: Float? = 1F,
    tidsstempelHosOss: LocalDateTime = 1.januarDateTime(2022),
): Tiltaksaktivitet {
    return Tiltaksaktivitet(
        tiltak = tiltak,
        aktivitetId = aktivitetId,
        tiltakLokaltNavn = tiltakLokaltNavn,
        arrangør = arrangør,
        bedriftsnummer = bedriftsnummer,
        deltakelsePeriode = deltakelsePeriode,
        deltakelseProsent = deltakelseProsent,
        deltakerStatus = deltakerStatus,
        statusSistEndret = statusSistEndret,
        begrunnelseInnsøking = begrunnelseInnsøking,
        antallDagerPerUke = antallDagerPerUke,
        tidsstempelHosOss = tidsstempelHosOss,
    )
}
