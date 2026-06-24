package com.gabriel.assistentefinanceiro.controller;

import com.gabriel.assistentefinanceiro.dto.WhatsAppMensagemRequest;
import com.gabriel.assistentefinanceiro.dto.WhatsAppMensagemResponse;
import com.gabriel.assistentefinanceiro.service.WhatsappService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WhatsappWebhookController {

    private final WhatsappService whatsappService;

    @PostMapping("/webhook/whatsapp")
    public WhatsAppMensagemResponse receber(@Valid @RequestBody WhatsAppMensagemRequest request) {
        return whatsappService.processar(request);
    }

    @PostMapping(
            value = "/webhook/twilio/whatsapp",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE
    )
    public ResponseEntity<String> receberTwilio(
            @RequestParam("From") String from,
            @RequestParam("Body") String body
    ) {
        String telefone = from.replaceFirst("^whatsapp:", "");
        WhatsAppMensagemResponse response = whatsappService.processar(new WhatsAppMensagemRequest(telefone, body));
        String twiml = "<Response><Message>" + escaparXml(response.resposta()) + "</Message></Response>";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(twiml);
    }

    private String escaparXml(String texto) {
        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
