package no.nav.dagpenger.regel.losningspubliserer

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RegelLøsningAdapterTest {
    private val testrapid = TestRapid()

    init {
        RegelLøsningAdapter(testrapid)
    }

    @BeforeEach
    fun reset() {
        testrapid.reset()
    }

    @Test
    fun `Skal motta løsning på vurdering av minste inntekt og videresende løsningen på riktig format`() {
        testrapid.sendTestMessage(
            """
            {
                "behovId": "finBehovId",
                "beregningsDato": "2019-02-27",
                "minsteinntektResultat": { }
            }
            """.trimIndent(),
        )
        testrapid.inspektør.size shouldBe 1
        testrapid.inspektør.message(0).let { message ->
            message["behovId"].asText() shouldBe "finBehovId"
            message["@løsning"].asText() shouldNotBe null
            message["@event_name"].asText() shouldBe "behov"
            message["@behov"] shouldNotBe null
            message["@prosessertAv"].asText() shouldBe "dp-regel-losningspubliserer"
        }
    }

    @Test
    fun `Skal ikke behandle prosesserte meldinger`() {
        //language=JSON
        testrapid.sendTestMessage(
            """
            {
                "behovId": "ekstraFinBehovId",
                "beregningsDato": "2019-02-27",
                "minsteinntektResultat": { },
                "@event_name": "behov",
                "@behov": "VurderingAvMinsteInntekt",
                "@løsning": {
                   "minsteinntektResultat": { }
                },
                "@prosessertAv": "dp-regel-losningspubliserer"
            }
            """.trimIndent(),
        )
        testrapid.inspektør.size shouldBe 0
    }

    @Test
    fun `Skal ikke behandle meldinger som har andre resultater i tillegg til minsteinntektResultat`() {
        //language=JSON
        testrapid.sendTestMessage(
            """
            {
                "behovId": "ekstraFinBehovId",
                "beregningsDato": "2019-02-27",
                "minsteinntektResultat": { },
                "periodeResultat": { }
            }
            """.trimIndent(),
        )
        testrapid.inspektør.size shouldBe 0
    }
}
