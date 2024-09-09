package no.nav.tiltakspenger.vedtak.context

import no.nav.tiltakspenger.meldekort.ports.DokumentGateway
import no.nav.tiltakspenger.vedtak.Configuration
import no.nav.tiltakspenger.vedtak.auth.AzureTokenProvider
import no.nav.tiltakspenger.vedtak.clients.dokument.DokumentHttpClient

@Suppress("unused")
internal open class DokumentContext(
    val dokumentGateway: DokumentGateway,
    val tokenProviderDokument: AzureTokenProvider,
) {
    companion object {
        fun create(): DokumentContext {
            val tokenProviderDokument = AzureTokenProvider(config = Configuration.oauthConfigDokument())

            return DokumentContext(
                dokumentGateway = DokumentHttpClient(
                    baseUrl = Configuration.dokumentClientConfig().baseUrl,
                    getToken = tokenProviderDokument::getToken,
                ),
                tokenProviderDokument = tokenProviderDokument,
            )
        }
    }
}
