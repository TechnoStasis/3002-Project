import socket

HOST = '0.0.0.0' 
PORT = 1234 #random port

with socket.socket(socket.AF_INET,socket.SOCK_STREAM) as server_socket:
    server_socket.bind((HOST, PORT))   #Bind takes in a tuple
    server_socket.listen()
    hostname = socket.gethostname()
    ip_address = socket.gethostbyname(hostname)
    print(f'Server is lisetning on {HOST} : {PORT}')
    print(f'The IP address of {hostname} os {ip_address}')

    while True:
        conn, addr = server_socket.accept()
        print(f'New Client connected" {addr}')
        
        syn = bytes([0x01])  #send synchronisation message
        conn.send(syn)
        print('SYN sent')

        syn_ack = conn.recv(1024)
        if syn_ack == bytes([0x02]):  #Acknowledge recieved
            print('SYN-ACK recieved')

            ack = bytes([0x03])  #SErver send acknowled
            conn.send(ack)
            print('ACK sent')

            data = conn.recv(1024)
            message = data.decode('utf-8')
            print("Recieved message from the Client:", message)

            test_message = 'Hello it reached!!'  #testing sending data
            message_bytes = test_message.encode('utf-8')
            conn.send(message_bytes)

            conn.close()
        else:
            print("Failed to recieve acknlowedgements or send")