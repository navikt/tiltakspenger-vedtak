package no.nav.tiltakspenger.meldekort.ports

import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.meldekort.domene.UtfyltMeldekort

interface MeldekortRepo {
    fun lagre(meldekort: UtfyltMeldekort)

    fun hentForMeldekortId(meldekortId: MeldekortId, sessionContext: SessionContext? = null): UtfyltMeldekort?
}
