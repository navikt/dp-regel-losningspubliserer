package no.nav.dagpenger.regel.losningspubliserer

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

private val logger = KotlinLogging.logger { }
private val sikkerLogg = KotlinLogging.logger("tjenestekall")

class RegelLøsningAdapter(rapidsConnection: RapidsConnection) : River.PacketListener {
    companion object {
        val rapidFilter: River.() -> Unit = {
            validate { it.requireKey("minsteinntektResultat") }
            // TODO filtrere vekk resultater som ikke har hatt @behov VurderMinsteInntekt    validate { it.requireKey("?") }
            validate { it.rejectValue("@prosessertAv", "dp-regel-losningspubliserer") }
        }
    }

    init {
        River(rapidsConnection).apply(rapidFilter).register(this)
    }

    override fun onError(
        problems: MessageProblems,
        context: MessageContext,
    ) {
        super.onError(problems, context)
    }

    override fun onSevere(
        error: MessageProblems.MessageException,
        context: MessageContext,
    ) {
        super.onSevere(error, context)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
    ) {
        try {
            val behovId = packet["behovId"].asText()
            withLoggingContext("behovId" to behovId) {
                logger.info("Mottok løsning for vurdering av minsteinntekt. BehovId: $behovId")
                sikkerLogg.info("Mottok løsning for vurdering av minsteinntekt: ${packet.toJson()}")
                // TODO("Mangler detaljer")

                packet["@prosessertAv"] = "dp-regel-losningspubliserer"

                // TODO ("Kommenter inn") context.publish(packet.toJson())
            }
        } catch (e: Exception) {
            TODO("Not yet implemented")
        }
    }
}
