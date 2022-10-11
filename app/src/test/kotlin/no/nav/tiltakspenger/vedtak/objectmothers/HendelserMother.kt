package no.nav.tiltakspenger.vedtak.objectmothers

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
    ident: String = Random().nextInt().toString(),
    søknad: Søknad = nySøknadMedArenaTiltak(),
    aktivitetslogg: Aktivitetslogg = Aktivitetslogg(forelder = null),
): SøknadMottattHendelse {
    return SøknadMottattHendelse(
        aktivitetslogg = aktivitetslogg,
        ident = ident,
        søknad = søknad,
    )
}

fun nyPersonopplysningHendelse(
    ident: String = Random().nextInt().toString(),
    personopplysninger: List<Personopplysninger> = listOf(personopplysningKjedeligFyr()),
    aktivitetslogg: Aktivitetslogg = Aktivitetslogg(forelder = null),
): PersonopplysningerMottattHendelse {
    return PersonopplysningerMottattHendelse(
        ident = ident,
        personopplysninger = personopplysninger,
        aktivitetslogg = aktivitetslogg,
    )
}

fun nySkjermingHendelse(
    ident: String = Random().nextInt().toString(),
    skjerming: Skjerming = skjermingFalse(ident = ident),
    aktivitetslogg: Aktivitetslogg = Aktivitetslogg(forelder = null),
): SkjermingMottattHendelse {
    return SkjermingMottattHendelse(
        ident = ident,
        skjerming = skjerming,
        aktivitetslogg = aktivitetslogg,
    )
}

fun nyTiltakHendelse(
    ident: String = Random().nextInt().toString(),
    tiltaksaktivitet: List<Tiltaksaktivitet> = listOf(tiltaksaktivitet()),
    aktivitetslogg: Aktivitetslogg = Aktivitetslogg(forelder = null),
): ArenaTiltakMottattHendelse {
    return ArenaTiltakMottattHendelse(
        ident = ident,
        tiltaksaktivitet = tiltaksaktivitet,
        aktivitetslogg = aktivitetslogg,
    )
}

fun nyYtelseHendelse(
    ident: String = Random().nextInt().toString(),
    ytelseSak: List<YtelseSak> = listOf(ytelseSak()),
    aktivitetslogg: Aktivitetslogg = Aktivitetslogg(forelder = null),
): YtelserMottattHendelse {
    return YtelserMottattHendelse(
        ident = ident,
        ytelseSak = ytelseSak,
        aktivitetslogg = aktivitetslogg,
    )
}
