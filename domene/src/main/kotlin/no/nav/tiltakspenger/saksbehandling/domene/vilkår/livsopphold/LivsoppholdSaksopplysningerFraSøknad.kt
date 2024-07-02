package no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold

data class LivsoppholdSaksopplysningerFraSøknad(
    val alderspensjonSøknadSaksopplysning: AlderspensjonSaksopplysning.Søknad,
    val gjenlevendeSøknadSaksopplysning: GjenlevendepensjonSaksopplysning.Søknad,
    val sykepengerSøknadSaksopplysning: SykepengerSaksopplysning.Søknad,
    val jobbsjansenSøknadSaksopplysning: JobbsjansenSaksopplysning.Søknad,
    val pensjonsinntektSøknadSaksopplysning: PensjonsinntektSaksopplysning.Søknad,
    val supplerendestønadAlderSøknadSaksopplysning: SupplerendeStønadAlderSaksopplysning.Søknad,
    val supplerendestønadFlyktningSøknadSaksopplysning: SupplerendeStønadFlyktningSaksopplysning.Søknad,
    val supplerendestønadFlyktningSøknadSaksopplysning1: SupplerendeStønadFlyktningSaksopplysning.Søknad,
)
