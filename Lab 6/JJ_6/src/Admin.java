import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Admin {
    public static void main(String[] argv) throws Exception {

        // info
        System.out.println("ADMIN");

        String EXCHANGE_NAME = "all_exchange";
        String ADMIN_QUEUE = "admin";
        String[] queue_names = {"Doctor_1", "Doctor_2", "Technic_1", "Technic_2"};

        // connection & channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // queue
        channel.queueDeclare(ADMIN_QUEUE, true, false, false, null);

        // exchange
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);

        // queue & bind
        for (String queue: queue_names){
            channel.queueDeclare(queue, true, false, false, null);
            channel.queueBind(queue, EXCHANGE_NAME, "");
        }

        channel.basicQos(1);

        Thread queue_handler = new Thread(() -> {
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, StandardCharsets.UTF_8);
                    System.out.println("Received: " + message);
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            };
            try {
                channel.basicConsume(ADMIN_QUEUE, false, consumer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        queue_handler.start();


        while (true) {
            // read msg
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter message: ");
            String message = br.readLine();

            // break condition
            if ("exit".equals(message)) {
                queue_handler.interrupt();
                break;
            }

            if ("".equals(message)){
                System.out.println("Empty message");
                continue;
            }

            String final_message = "INFO " + message;
            // publish
            channel.basicPublish(EXCHANGE_NAME, "", null, final_message.getBytes(StandardCharsets.UTF_8));
            System.out.println("Sent: " + final_message);
        }
    }
}
