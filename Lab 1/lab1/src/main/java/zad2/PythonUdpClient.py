# -*- coding: utf-8 -*-

import socket;

serverIP = "127.0.0.1"
serverPort = 9008
msg = "żółta gęś"

if __name__ == '__main__':
    print('PYTHON UDP CLIENT')
    client = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    client.sendto(bytes(msg, 'utf-8'), (serverIP, serverPort))

    received, _ = client.recvfrom(1024)
    print(received)
    print(received.decode('utf-8'))





