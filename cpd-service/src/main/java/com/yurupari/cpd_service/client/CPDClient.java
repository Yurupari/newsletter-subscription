package com.yurupari.cpd_service.client;

import com.yurupari.cpd_service.model.dto.IdentifyPayload;
import com.yurupari.cpd_service.model.dto.TrackPayload;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(accept = {MediaType.APPLICATION_JSON_VALUE})
public interface CPDClient {

    @PostExchange("/identify")
    void identify(@RequestBody IdentifyPayload payload);

    @PostExchange("/track")
    void track(@RequestBody TrackPayload payload);
}
