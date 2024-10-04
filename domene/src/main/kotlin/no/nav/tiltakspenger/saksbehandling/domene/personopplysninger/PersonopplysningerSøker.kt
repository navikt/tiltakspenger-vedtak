package no.nav.tiltakspenger.saksbehandling.domene.personopplysninger

import no.nav.tiltakspenger.libs.common.Fnr
import java.time.LocalDate

data class PersonopplysningerSøker(
    val fnr: Fnr,
    val fødselsdato: LocalDate,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val fortrolig: Boolean,
    val strengtFortrolig: Boolean,
    val strengtFortroligUtland: Boolean,
    // TODO pre-mvp jah: Trenger vi kommune + bydel, eller kan vi slette?
    val kommune: String?,
    val bydel: String?,
)
