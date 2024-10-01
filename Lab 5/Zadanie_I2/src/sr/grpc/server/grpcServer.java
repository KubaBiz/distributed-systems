package sr.grpc.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.stub.StreamObserver;


public class grpcServer 
{
	private static final Logger logger = Logger.getLogger(grpcServer.class.getName());

	private String address = "127.0.0.5";
	private int port = 50051;
	private Server server;

	private SocketAddress socket;



	private void start() throws IOException 
	{
//		try { socket = new InetSocketAddress(InetAddress.getByName(address), port);	} catch(UnknownHostException e) {};

		//You will want to employ flow-control so that the queue doesn't blow up your memory. You can cast StreamObserver to CallStreamObserver to get flow-control API
//		server = //ServerBuilder.forPort(50051).executor((Executors.newFixedThreadPool(16)))
//				NettyServerBuilder.forAddress(socket).executor(Executors.newFixedThreadPool(16))
//						.keepAliveTime(120, TimeUnit.SECONDS)
//				.addService(new CalculatorImpl())
//				//.addService(new CalculatorImpl())
//				.addService(new AdvancedCalculatorImpl())
//				.addService(new StreamTesterImpl()).addService(ProtoReflectionService.newInstance())
//				.build()
//				.start();

//		server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
//				.addService(new GreeterImpl())
//				.addService(ProtoReflectionService.newInstance()) // add reflection service
//				.build();

		server = ServerBuilder.forPort(port)
				.addService(new GreeterImpl())
				.addService(ProtoReflectionService.newInstance())
			.build()
			.start();

		server = server.start();


		logger.info("Server started, listening on " + port);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// Use stderr here since the logger may have been reset by its JVM shutdown hook.
				System.err.println("Shutting down gRPC server...");
				try {
					grpcServer.this.stop();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				System.err.println("Server shut down.");
			}
		});
	}

	private void stop() throws InterruptedException{
		if (server != null) {
			server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
		}
	}

	/**
	 * Await termination on the main thread since the grpc library uses daemon threads.
	 */
	private void blockUntilShutdown() throws InterruptedException {
		if (server != null) {
			server.awaitTermination();
		}
	}

	/**
	 * Main launches the server from the command line.
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		final grpcServer server = new grpcServer();
		server.start();
		server.blockUntilShutdown();
	}

	static class GreeterImpl extends GreeterGrpc.GreeterImplBase {

		@Override
		public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
			HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + req.getName()).build();
			responseObserver.onNext(reply);
			responseObserver.onCompleted();
		}
	}


}
