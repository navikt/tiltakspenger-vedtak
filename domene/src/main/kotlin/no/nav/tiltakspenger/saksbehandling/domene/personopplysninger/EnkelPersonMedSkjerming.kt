package no.nav.tiltakspenger.saksbehandling.domene.personopplysninger

import no.nav.tiltakspenger.libs.common.Fnr

data class EnkelPersonMedSkjerming(
    val fnr: Fnr,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val fortrolig: Boolean,
    val strengtFortrolig: Boolean,
    val strengtFortroligUtland: Boolean,
    val skjermet: Boolean,
)

fun EnkelPerson.toEnkelPersonMedSkjerming(skjermet: Boolean): EnkelPersonMedSkjerming {
    return EnkelPersonMedSkjerming(
        fnr = fnr,
        fornavn = fornavn,
        mellomnavn = mellomnavn,
        etternavn = etternavn,
        fortrolig = fortrolig,
        strengtFortrolig = strengtFortrolig,
        strengtFortroligUtland = strengtFortroligUtland,
        skjermet = skjermet,

    )
}
