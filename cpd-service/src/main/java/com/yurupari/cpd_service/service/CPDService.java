package com.yurupari.cpd_service.service;

import com.yurupari.common_data.kafka.event.CPDEvent;

public interface CPDService {

    void sendCPDNotification(CPDEvent cpdEvent);
}
