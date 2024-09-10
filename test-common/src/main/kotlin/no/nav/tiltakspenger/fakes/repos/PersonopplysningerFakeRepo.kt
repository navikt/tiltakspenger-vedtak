package no.nav.tiltakspenger.fakes.repos

import arrow.atomic.Atomic
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.saksbehandling.ports.PersonopplysningerRepo

class PersonopplysningerFakeRepo : PersonopplysningerRepo {
    private val data = Atomic((mutableMapOf<SakId, SakPersonopplysninger>()))

    override fun hent(sakId: SakId): SakPersonopplysninger = data.get()[sakId]!!

    fun lagre(
        sakId: SakId,
        sakPersonopplysninger: SakPersonopplysninger,
    ) {
        data.get()[sakId] = sakPersonopplysninger
    }
}
