import subprocess

def call_grpc_service(command):
    try:
        result = subprocess.run(command, capture_output=True, text=True)
        if result.returncode == 0:
            print(result.stdout)
        else:
            print(result.stderr)
    except FileNotFoundError:
        print("Error: grpcurl is not installed or not in the PATH.")

if __name__ == "__main__":
    server = "127.0.0.5:50051" 
    call_grpc_service(["grpcurl", "-plaintext", server, "list"])
    call_grpc_service(["grpcurl", "-plaintext", server, "list", "helloworld.Calculator"])
    call_grpc_service(["grpcurl", "-plaintext", server, "describe", "helloworld.Calculator.Add"])
    call_grpc_service(["grpcurl", "-plaintext", server, "describe", "helloworld.ArithmeticOpArguments"])
    call_grpc_service(["grpcurl", "-plaintext", server, "describe", "helloworld.MultipleOpArguments"])
    call_grpc_service(["grpcurl", "-plaintext", "-format", "text", "-d", 'name: "pythonClient"', 
                        server, "helloworld.Greeter.SayHello"])
    call_grpc_service(["grpcurl", "-plaintext", "-format", "text", "-d", 'arg1: 3, arg2: 6', 
                        server, "helloworld.Calculator.Add"])
    call_grpc_service(["grpcurl", "-plaintext", "-format", "text", "-d", 'arg1: 3, arg2: 6', 
                        server, "helloworld.Calculator.Subtract"])
    call_grpc_service(["grpcurl", "-plaintext", "-format", "json", "-d", 
                       '{"args": [{"arg1": 10, "arg2": 5}, {"arg1": 20, "arg2": 30}, {"arg1": 7, "arg2": 3}]}', 
                        server, "helloworld.Calculator.MultipleAdd"])
    call_grpc_service(["grpcurl", "-plaintext", "-format", "json", "-d", 
                       '{"args": []}', 
                        server, "helloworld.Calculator.MultipleAdd"])