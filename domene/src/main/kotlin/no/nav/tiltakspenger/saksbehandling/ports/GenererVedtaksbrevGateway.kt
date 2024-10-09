package no.nav.tiltakspenger.saksbehandling.ports

import arrow.core.Either
import no.nav.tiltakspenger.felles.journalføring.PdfOgJson
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Navn
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak

interface GenererVedtaksbrevGateway {
    suspend fun genererVedtaksbrev(
        vedtak: Rammevedtak,
        hentNavn: suspend (Fnr) -> Navn,
    ): Either<KunneIkkeGenererePdf, PdfOgJson>
}

object KunneIkkeGenererePdf
