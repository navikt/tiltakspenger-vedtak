package no.nav.tiltakspenger.vedtak.context

import no.nav.tiltakspenger.distribusjon.ports.DokdistGateway
import no.nav.tiltakspenger.meldekort.ports.GenererMeldekortPdfGateway
import no.nav.tiltakspenger.meldekort.ports.JournalførMeldekortGateway
import no.nav.tiltakspenger.saksbehandling.ports.GenererVedtaksbrevGateway
import no.nav.tiltakspenger.saksbehandling.ports.JournalførVedtaksbrevGateway
import no.nav.tiltakspenger.vedtak.Configuration
import no.nav.tiltakspenger.vedtak.auth.EntraIdSystemtokenClient
import no.nav.tiltakspenger.vedtak.clients.dokdist.DokdistHttpClient
import no.nav.tiltakspenger.vedtak.clients.joark.JoarkHttpClient
import no.nav.tiltakspenger.vedtak.clients.pdfgen.PdfgenHttpClient

open class DokumentContext(
    private val entraIdSystemtokenClient: EntraIdSystemtokenClient,
) {
    private val joarkClient by lazy {
        JoarkHttpClient(
            baseUrl = Configuration.joarkUrl,
            getToken = { entraIdSystemtokenClient.getSystemtoken(Configuration.joarkScope) },
        )
    }
    open val dokdistGateway: DokdistGateway by lazy {
        DokdistHttpClient(
            baseUrl = Configuration.dokdistUrl,
            getToken = { entraIdSystemtokenClient.getSystemtoken(Configuration.dokdistScope) },
        )
    }
    open val journalførMeldekortGateway: JournalførMeldekortGateway by lazy { joarkClient }
    open val journalførVedtaksbrevGateway: JournalførVedtaksbrevGateway by lazy { joarkClient }
    private val pdfgen by lazy {
        PdfgenHttpClient(Configuration.pdfgenUrl)
    }
    open val genererMeldekortPdfGateway: GenererMeldekortPdfGateway by lazy { pdfgen }
    open val genererVedtaksbrevGateway: GenererVedtaksbrevGateway by lazy { pdfgen }
}
