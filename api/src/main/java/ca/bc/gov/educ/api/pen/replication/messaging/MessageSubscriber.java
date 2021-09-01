package ca.bc.gov.educ.api.pen.replication.messaging;

import ca.bc.gov.educ.api.pen.replication.constants.SagaTopicsEnum;
import ca.bc.gov.educ.api.pen.replication.helpers.LogHelper;
import ca.bc.gov.educ.api.pen.replication.orchestrator.base.EventHandler;
import ca.bc.gov.educ.api.pen.replication.struct.Event;
import ca.bc.gov.educ.api.pen.replication.util.JsonUtil;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

/**
 * The type Message subscriber.
 */
@Component
@Slf4j
public class MessageSubscriber {

  /**
   * The Handlers.
   */
  @Getter(PRIVATE)
  private final Map<String, EventHandler> handlerMap = new HashMap<>();
  private final Connection connection;

  /**
   * Instantiates a new Message subscriber.
   *
   * @param con           the con
   * @param eventHandlers the event handlers
   */
  @Autowired
  public MessageSubscriber(final Connection con, final List<EventHandler> eventHandlers) {
    this.connection = con;
    eventHandlers.forEach(handler -> {
      this.handlerMap.put(handler.getTopicToSubscribe().getCode(), handler);
      this.subscribe(handler.getTopicToSubscribe().getCode(), handler);
    });
  }

  /**
   * Subscribe.
   *
   * @param topic        the topic
   * @param eventHandler the event handler
   */
  public void subscribe(final String topic, final EventHandler eventHandler) {
    this.handlerMap.computeIfAbsent(topic, k -> eventHandler);
    final String queue = topic.replace("_", "-");
    final var dispatcher = this.connection.createDispatcher(this.onMessage(eventHandler));
    dispatcher.subscribe(topic, queue);
  }

  /**
   * On message handler.
   *
   * @param eventHandler the event handler
   * @return the message handler
   */
  public MessageHandler onMessage(final EventHandler eventHandler) {
    return (Message message) -> {
      if (message != null) {
        try {
          final var eventString = new String(message.getData());
          LogHelper.logMessagingEventDetails(eventString);
          val event = JsonUtil.getJsonObjectFromString(Event.class, eventString);
          if (SagaTopicsEnum.PEN_REPLICATION_STUDENT_CREATE_SAGA_TOPIC.getCode().equals(message.getSubject())
            || SagaTopicsEnum.PEN_REPLICATION_STUDENT_UPDATE_SAGA_TOPIC.getCode().equals(message.getSubject())) {
            eventHandler.handleEvent(event);
          } else {
            eventHandler.handleTwinTransEvent(event);
          }

        } catch (final Exception e) {
          log.error("Exception ", e);
        }
      }
    };
  }
}
