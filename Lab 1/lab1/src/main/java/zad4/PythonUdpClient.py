import socket

serverIP = "127.0.0.1"
serverPort = 9008
msg = "ping python"

if __name__ == '__main__':
    print('PYTHON UDP CLIENT')
    client = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    client.sendto(bytes(msg, 'utf8'), (serverIP, serverPort))

    received, _ = client.recvfrom(1024)
    print(received.decode('utf8'))