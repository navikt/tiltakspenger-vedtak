package no.nav.tiltakspenger.vedtak.context

import no.nav.tiltakspenger.meldekort.ports.DokumentGateway
import no.nav.tiltakspenger.vedtak.Configuration
import no.nav.tiltakspenger.vedtak.auth.AzureTokenProvider
import no.nav.tiltakspenger.vedtak.clients.dokument.DokumentHttpClient

@Suppress("unused")
open class DokumentContext {
    private val tokenProviderDokument by lazy { AzureTokenProvider(config = Configuration.oauthConfigDokument()) }
    open val dokumentGateway: DokumentGateway by lazy {
        DokumentHttpClient(
            baseUrl = Configuration.dokumentClientConfig().baseUrl,
            getToken = tokenProviderDokument::getToken,
        )
    }
}
