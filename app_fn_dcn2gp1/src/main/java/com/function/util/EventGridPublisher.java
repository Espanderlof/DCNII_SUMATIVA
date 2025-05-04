package com.function.util;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase utilitaria para publicar eventos en Azure Event Grid.
 */
public class EventGridPublisher {
    private static final Logger logger = LoggerFactory.getLogger(EventGridPublisher.class);
    
    private static final String EVENT_GRID_ENDPOINT = "https://usuarios-roles-event.eastus2-1.eventgrid.azure.net/api/events";
    private static final String EVENT_GRID_KEY = "2bcFnyqZYfYUVTk5qrvT7AnAxejCPjKuJ42OD8xePbQrlTbGcxU2JQQJ99BDACHYHv6XJ3w3AAABAZEGTwCq";
    
    private static EventGridPublisherClient<EventGridEvent> client;
    
    // Inicializar el cliente
    static {
        try {
            client = new EventGridPublisherClientBuilder()
                .endpoint(EVENT_GRID_ENDPOINT)
                .credential(new AzureKeyCredential(EVENT_GRID_KEY))
                .buildEventGridEventPublisherClient();
            
            logger.info("Cliente de Event Grid inicializado correctamente");
        } catch (Exception e) {
            logger.error("Error al inicializar el cliente de Event Grid", e);
        }
    }
    
    /**
     * Publica un evento en Azure Event Grid.
     * 
     * @param source Origen del evento (ej. "/usuarios/created")
     * @param eventType Tipo de evento (ej. "user_created")
     * @param data Datos del evento
     * @return true si se publicó correctamente, false en caso contrario
     */
    public static boolean publishEvent(String source, String eventType, Object data) {
        try {
            if (client == null) {
                logger.error("Cliente de Event Grid no inicializado");
                return false;
            }
            
            // Crear evento
            EventGridEvent event = new EventGridEvent(
                source,                  // Source
                eventType,               // Event Type 
                BinaryData.fromObject(data), // Datos del evento
                "1.0"                    // Version
            );
            
            // Publicar evento
            client.sendEvent(event);
            
            logger.info("Evento publicado correctamente: " + eventType);
            return true;
            
        } catch (Exception e) {
            logger.error("Error al publicar evento en Event Grid", e);
            return false;
        }
    }
}