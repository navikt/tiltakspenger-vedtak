@file:Suppress("unused")

package no.nav.tiltakspenger.vedtak.clients.joark

/**
 * @param eksternReferanseId Brukes som dedup-nøkkel.
 */
internal data class JoarkRequest(
    val tittel: String,
    val journalpostType: JournalPostType,
    val tema: String = "IND",
    val kanal: String?,
    val journalfoerendeEnhet: String = "9999",
    val avsenderMottaker: AvsenderMottaker,
    val bruker: Bruker,
    val sak: Sak?,
    val dokumenter: List<JournalpostDokument>,
    val eksternReferanseId: String,
) {
    enum class JournalPostType {
        INNGAAENDE,
        UTGAAENDE,
        NOTAT,
    }

    data class AvsenderMottaker(
        val id: String,
        val idType: String = "FNR",
    )

    sealed class Sak {
        data class Fagsak(
            val fagsakId: String,
            val fagsaksystem: String = "TILTAKSPENGER",
            val sakstype: String = "FAGSAK",
        ) : Sak()
    }

    data class Bruker(
        val id: String,
        val idType: String = "FNR",
    )

    data class JournalpostDokument(
        val tittel: String,
        val brevkode: String,
        val dokumentvarianter: List<DokumentVariant>,
    ) {
        sealed class DokumentVariant {
            abstract val filtype: String
            abstract val fysiskDokument: String
            abstract val variantformat: String
            abstract val filnavn: String

            data class ArkivPDF(
                override val fysiskDokument: String,
                val tittel: String,
            ) : DokumentVariant() {
                override val filtype: String = "PDFA"
                override val variantformat: String = "ARKIV"
                override val filnavn: String = "$tittel.pdf"
            }

            data class VedleggPDF(
                override val fysiskDokument: String,
                override val filnavn: String,
            ) : DokumentVariant() {
                override val filtype: String = "PDFA"
                override val variantformat: String = "ARKIV"
            }

            data class OriginalJson(
                override val fysiskDokument: String,
                val tittel: String,
            ) : DokumentVariant() {
                override val filtype: String = "JSON"
                override val variantformat: String = "ORIGINAL"
                override val filnavn: String = "$tittel.json"
            }
        }
    }
}
