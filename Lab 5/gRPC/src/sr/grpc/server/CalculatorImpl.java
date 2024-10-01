package sr.grpc.server;

import io.grpc.Status;
import sr.grpc.gen.ArithmeticOpResult;
import sr.grpc.gen.CalculatorGrpc.CalculatorImplBase;
import sr.grpc.gen.ErrorMultiply;

import java.util.List;

public class CalculatorImpl extends CalculatorImplBase 
{
	@Override
	public void add(sr.grpc.gen.ArithmeticOpArguments request,
			io.grpc.stub.StreamObserver<sr.grpc.gen.ArithmeticOpResult> responseObserver) 
	{
		System.out.println("addRequest (" + request.getArg1() + ", " + request.getArg2() +")");
		int val = request.getArg1() + request.getArg2();
		ArithmeticOpResult result = ArithmeticOpResult.newBuilder().setRes(val).build();
		if(request.getArg1() > 100 && request.getArg2() > 100) try { Thread.sleep(5000); } catch(java.lang.InterruptedException ex) { }
		responseObserver.onNext(result);
		responseObserver.onCompleted();
	}

	@Override
	public void subtract(sr.grpc.gen.ArithmeticOpArguments request,
			io.grpc.stub.StreamObserver<sr.grpc.gen.ArithmeticOpResult> responseObserver) 
	{
		System.out.println("subtractRequest (" + request.getArg1() + ", " + request.getArg2() +")");
		int val = request.getArg1() - request.getArg2();
		ArithmeticOpResult result = ArithmeticOpResult.newBuilder().setRes(val).build();
		responseObserver.onNext(result);
		responseObserver.onCompleted();
	}

	@Override
	public void multiply(sr.grpc.gen.MultiplyOpArguments request,
						 io.grpc.stub.StreamObserver<sr.grpc.gen.ArithmeticOpResult> responseObserver)
	{
		List<Integer> list = request.getArgsList();
		if (list.isEmpty()) {
			// Construct and return an error message
			ErrorMultiply error = ErrorMultiply.newBuilder()
					.setMessage("Empty list provided for multiplication")
					.setCode(1)
					.build();
			responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Empty list provided for multiplication").asRuntimeException());
			return;
		}
		System.out.println("multiplyRequest ( " + list +" )");
		int val = 1;
		for (Integer integer: list){
			val = val * integer;
		}
		ArithmeticOpResult result = ArithmeticOpResult.newBuilder().setRes(val).build();
		responseObserver.onNext(result);
		responseObserver.onCompleted();
	}
}
