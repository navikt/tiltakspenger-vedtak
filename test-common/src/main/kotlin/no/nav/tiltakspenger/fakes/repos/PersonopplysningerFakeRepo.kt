package no.nav.tiltakspenger.fakes.repos

import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.saksbehandling.ports.PersonopplysningerRepo

class PersonopplysningerFakeRepo(
    private val data: Map<SakId, SakPersonopplysninger>,
) : PersonopplysningerRepo {
    constructor(vararg data: Pair<SakId, SakPersonopplysninger>) : this(data.toMap())

    override fun hent(sakId: SakId): SakPersonopplysninger {
        return data[sakId]!!
    }
}
