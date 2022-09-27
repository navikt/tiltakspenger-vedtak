package no.nav.tiltakspenger.vedtak.repository.personopplysninger

import kotliquery.TransactionalSession
import no.nav.tiltakspenger.vedtak.Personopplysninger

internal class PersonopplysningerDAO {
    fun lagre(personopplysninger: Personopplysninger, txSession: TransactionalSession): Nothing = TODO("Not yet implemented")
    fun hent(id: String, txSession: TransactionalSession): Personopplysninger = TODO("Not yet implemented")
}
