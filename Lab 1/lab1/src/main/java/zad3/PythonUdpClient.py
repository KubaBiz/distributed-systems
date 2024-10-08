import socket

serverIP = "127.0.0.1"
serverPort = 9008
msg_bytes = (300).to_bytes(4, byteorder='little')
print(msg_bytes)

if __name__ == '__main__':
    print('PYTHON UDP CLIENT')
    client = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    client.sendto(msg_bytes, (serverIP, serverPort))

    received, _ = client.recvfrom(1024)
    print(int.from_bytes(received, byteorder='little'))