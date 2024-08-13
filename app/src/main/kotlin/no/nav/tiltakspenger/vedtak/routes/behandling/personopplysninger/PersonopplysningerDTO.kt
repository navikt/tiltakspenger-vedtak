package no.nav.tiltakspenger.vedtak.routes.behandling.personopplysninger

import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerSøker

/**
 * Har ansvar for å serialisere Personopplysninger til json. Kontrakt mot frontend.
 */
internal data class PersonopplysningerDTO(
    val fnr: String,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val fortrolig: Boolean,
    val strengtFortrolig: Boolean,
    val strengtFortroligUtland: Boolean,
    val skjermet: Boolean?,
)

internal fun PersonopplysningerSøker.toDTO(): PersonopplysningerDTO =
    PersonopplysningerDTO(
        fnr = fnr.verdi,
        fornavn = fornavn,
        mellomnavn = mellomnavn,
        etternavn = etternavn,
        fortrolig = fortrolig,
        strengtFortrolig = strengtFortrolig,
        strengtFortroligUtland = strengtFortroligUtland,
        skjermet = skjermet,
    )
