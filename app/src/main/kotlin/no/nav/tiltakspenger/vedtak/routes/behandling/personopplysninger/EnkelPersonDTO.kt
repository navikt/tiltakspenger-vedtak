package no.nav.tiltakspenger.vedtak.routes.behandling.personopplysninger

import no.nav.tiltakspenger.libs.personklient.pdl.dto.PdlPerson
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.EnkelPerson

data class EnkelPersonDTO(
    val fnr: String,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val fortrolig: Boolean,
    val strengtFortrolig: Boolean,
    val strengtFortroligUtland: Boolean,
    val skjermet: Boolean?,
)

internal data class PdlResponse(
    val hentPerson: PdlPerson,
)

fun EnkelPerson.toDTO(skjerming: Boolean?): EnkelPersonDTO = EnkelPersonDTO(
    fnr = fnr.verdi,
    fornavn = fornavn,
    mellomnavn = mellomnavn,
    etternavn = etternavn,
    fortrolig = fortrolig,
    strengtFortrolig = strengtFortrolig,
    strengtFortroligUtland = strengtFortroligUtland,
    skjermet = skjerming,
)
