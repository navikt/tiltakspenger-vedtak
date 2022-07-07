package no.nav.tiltakspenger.vedtak.routes

import com.papsign.ktor.openapigen.OpenAPIGen
import com.papsign.ktor.openapigen.schema.namer.DefaultSchemaNamer
import com.papsign.ktor.openapigen.schema.namer.SchemaNamer
import io.ktor.server.application.Application
import io.ktor.server.application.install
import kotlin.reflect.KType

fun Application.openAPI() {
    install(OpenAPIGen) {
        info {
            version = "0.0.1"
            title = "Vedtak API"
            description = "API for vedtak"
        }
        server("/") {
            description = "Test server"
        }
        replaceModule(
            DefaultSchemaNamer,
            object : SchemaNamer {
                val regex = Regex("[A-Za-z0-9_.]+")
                override fun get(type: KType): String {
                    return type.toString()
                        .replace(regex) { it.value.split(".").last() }
                        .replace(Regex(">|<|, "), "_")
                        .replace("ø", "o")
                        .replace("å", "a")
                        .replace("DTO", "")
                }
            }
        )
    }
}