import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Technic_1 {
    public static void main(String[] argv) throws Exception {

        // info
        System.out.println("TECHNIC 1 KNEE HIP");

        String EXCHANGE_NAME = "request_exchange";
        String QUEUE_NAME = "knee";
        String QUEUE_NAME2 = "hip";
        String QUEUE_NAME3 = "Technic_1";
        String ADMIN_QUEUE = "admin";

        // connection & channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // exchange
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

        // queue & bind
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        channel.queueDeclare(QUEUE_NAME2, true, false, false, null);
        channel.queueDeclare(QUEUE_NAME3, true, false, false, null);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, QUEUE_NAME);
        channel.queueBind(QUEUE_NAME2, EXCHANGE_NAME, QUEUE_NAME2);
        System.out.println("joined queue: " + QUEUE_NAME + ", and: " + QUEUE_NAME2);

        channel.basicQos(1);

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, StandardCharsets.UTF_8);
                System.out.println("Received: " + message);

                String[] words = message.split("\\s+");
                String first_word = words.length > 0 ? words[0] : "";

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                String answer = words[2] + " " + words[1] + " done";
                channel.basicPublish("", first_word, null, answer.getBytes(StandardCharsets.UTF_8));
                channel.basicPublish("", ADMIN_QUEUE, null, answer.getBytes(StandardCharsets.UTF_8));
                System.out.println("Sent: " + answer);
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };

        Thread queue_handler = new Thread(() -> {
            try {
                channel.basicConsume(QUEUE_NAME, false, consumer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        queue_handler.start();

        Thread queue_handler2 = new Thread(() -> {
            try {
                channel.basicConsume(QUEUE_NAME2, false, consumer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        queue_handler2.start();

        Thread info_handler = new Thread(() -> {
            Consumer consumer2 = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, StandardCharsets.UTF_8);
                    System.out.println("Received: " + message);
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            };
            try {
                channel.basicConsume(QUEUE_NAME3, false, consumer2);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        info_handler.start();

        queue_handler.join();
        queue_handler2.join();
        info_handler.join();
    }
}
